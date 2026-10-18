#!/usr/bin/env python3
"""
scripts/process_images.py

Batch processes background images for Stillwords:
1. Center-crops each image to 9:16 and resizes to 1080x1920.
2. Converts to WebP format at ~75 quality, dynamically tuning if needed to stay under 180 KB.
3. Strips all EXIF/ICC metadata.
4. Preserves file stem names with .webp extension under app/src/main/assets/backgrounds/<theme>/.
5. Converts design-assets/credits.csv and design-assets/ai-sources.csv to app/src/main/assets/credits.json.

How to run:
    python scripts/process_images.py
"""

import os
import sys
import csv
import json
import io
from pathlib import Path
from PIL import Image

TARGET_WIDTH = 1080
TARGET_HEIGHT = 1920
TARGET_RATIO = TARGET_WIDTH / TARGET_HEIGHT  # 9/16 = 0.5625
MAX_FILE_SIZE_BYTES = 180 * 1024  # 180 KB
INITIAL_QUALITY = 75

BASE_DIR = Path(__file__).resolve().parent.parent
INPUT_BACKGROUNDS_DIR = BASE_DIR / "design-assets" / "backgrounds"
OUTPUT_BACKGROUNDS_DIR = BASE_DIR / "app" / "src" / "main" / "assets" / "backgrounds"
CREDITS_CSV = BASE_DIR / "design-assets" / "credits.csv"
AI_SOURCES_CSV = BASE_DIR / "design-assets" / "ai-sources.csv"
OUTPUT_CREDITS_JSON = BASE_DIR / "app" / "src" / "main" / "assets" / "credits.json"


def process_image(input_path: Path, output_path: Path):
    """Reads, center-crops to 9:16, resizes to 1080x1920, strips metadata, and saves as WebP <= 180KB."""
    output_path.parent.mkdir(parents=True, exist_ok=True)
    
    with Image.open(input_path) as img:
        # Convert image to RGB (stripping alpha if any or converting CMYK/P)
        if img.mode != "RGB":
            img = img.convert("RGB")
        
        orig_w, orig_h = img.size
        orig_ratio = orig_w / orig_h
        
        # Center-crop calculation for 9:16
        if orig_ratio > TARGET_RATIO:
            # Image is wider than 9:16: crop left and right
            crop_w = int(orig_h * TARGET_RATIO)
            crop_h = orig_h
            left = (orig_w - crop_w) // 2
            top = 0
            right = left + crop_w
            bottom = orig_h
        else:
            # Image is taller than 9:16: crop top and bottom
            crop_w = orig_w
            crop_h = int(orig_w / TARGET_RATIO)
            left = 0
            top = (orig_h - crop_h) // 2
            right = orig_w
            bottom = top + crop_h
            
        cropped = img.crop((left, top, right, bottom))
        
        # Resize to 1080x1920 using high-quality Lanczos filter
        resized = cropped.resize((TARGET_WIDTH, TARGET_HEIGHT), Image.Resampling.LANCZOS)
        
        # Create a fresh clean image object to guarantee no metadata/exif is retained
        clean_img = Image.new("RGB", (TARGET_WIDTH, TARGET_HEIGHT))
        clean_img.paste(resized, (0, 0))
        
        # Quality search to ensure file size <= 180 KB
        quality = INITIAL_QUALITY
        min_quality = 40
        best_bytes = None
        
        while quality >= min_quality:
            buffer = io.BytesIO()
            clean_img.save(buffer, format="WEBP", quality=quality, method=6)
            size = buffer.tell()
            
            if size <= MAX_FILE_SIZE_BYTES or quality == min_quality:
                best_bytes = buffer.getvalue()
                break
            
            quality -= 5  # Step down quality
            
        with open(output_path, "wb") as f:
            f.write(best_bytes)
            
    return len(best_bytes)


def process_all_images():
    print(f"Scanning themes in: {INPUT_BACKGROUNDS_DIR}")
    if not INPUT_BACKGROUNDS_DIR.exists():
        print(f"Error: {INPUT_BACKGROUNDS_DIR} does not exist!")
        sys.exit(1)
        
    theme_dirs = [d for d in INPUT_BACKGROUNDS_DIR.iterdir() if d.is_dir()]
    print(f"Found {len(theme_dirs)} themes: {[d.name for d in theme_dirs]}")
    
    total_processed = 0
    total_bytes = 0
    largest_file = None
    largest_bytes = 0
    
    for theme_dir in sorted(theme_dirs):
        theme_name = theme_dir.name
        out_theme_dir = OUTPUT_BACKGROUNDS_DIR / theme_name
        image_files = [f for f in theme_dir.iterdir() if f.is_file() and f.suffix.lower() in [".jpg", ".jpeg", ".png", ".webp"]]
        print(f"Processing theme '{theme_name}': {len(image_files)} images...")
        
        for img_file in sorted(image_files):
            out_file = out_theme_dir / f"{img_file.stem}.webp"
            size = process_image(img_file, out_file)
            total_processed += 1
            total_bytes += size
            
            if size > largest_bytes:
                largest_bytes = size
                largest_file = f"{theme_name}/{out_file.name}"
                
            if size > MAX_FILE_SIZE_BYTES:
                print(f"  WARNING: {out_file.name} is {size / 1024:.1f} KB (> 180 KB)")
            else:
                pass
                
    total_mb = total_bytes / (1024 * 1024)
    print("\n--- Image Processing Results ---")
    print(f"Total processed images: {total_processed}")
    print(f"Total size of backgrounds: {total_mb:.2f} MB ({total_bytes:,} bytes)")
    print(f"Largest file: {largest_file} ({largest_bytes / 1024:.1f} KB, {largest_bytes:,} bytes)")
    
    return total_processed, total_bytes, largest_file, largest_bytes


def build_credits_json():
    print(f"\nConverting credits from {CREDITS_CSV} and {AI_SOURCES_CSV} to {OUTPUT_CREDITS_JSON}...")
    credits = []
    if CREDITS_CSV.exists():
        with open(CREDITS_CSV, mode="r", encoding="utf-8-sig") as f:
            reader = csv.DictReader(f)
            for row in reader:
                orig_file = row.get("file", "").strip()
                photographer = row.get("photographer", "").strip()
                source_url = row.get("source_url", "").strip()
                
                # Derive webp file name
                stem = Path(orig_file).stem
                webp_file = f"{stem}.webp"
                
                # Determine theme from file name prefix, e.g. "forest-botanical-01.jpg" -> "forest-botanical"
                parts = stem.rsplit("-", 1)
                theme = parts[0] if len(parts) == 2 else ""
                
                credits.append({
                    "file": webp_file,
                    "originalFile": orig_file,
                    "theme": theme,
                    "photographer": photographer,
                    "sourceUrl": source_url,
                    "isAi": False
                })
                
    ai_sources = []
    if AI_SOURCES_CSV.exists():
        with open(AI_SOURCES_CSV, mode="r", encoding="utf-8-sig") as f:
            reader = csv.DictReader(f)
            for row in reader:
                file_name = row.get("file", "").strip()
                tool = row.get("tool", "").strip()
                date = row.get("date", "").strip()
                if file_name:
                    stem = Path(file_name).stem
                    ai_sources.append({
                        "file": f"{stem}.webp",
                        "originalFile": file_name,
                        "tool": tool,
                        "date": date
                    })
                    
    credits_data = {
        "statement": "Some backgrounds were created with AI tools.",
        "credits": credits,
        "aiSources": ai_sources
    }
    
    OUTPUT_CREDITS_JSON.parent.mkdir(parents=True, exist_ok=True)
    with open(OUTPUT_CREDITS_JSON, "w", encoding="utf-8") as f:
        json.dump(credits_data, f, indent=2, ensure_ascii=False)
        
    print(f"Generated {OUTPUT_CREDITS_JSON} with {len(credits)} photo credits and {len(ai_sources)} AI sources.")


if __name__ == "__main__":
    process_all_images()
    build_credits_json()
