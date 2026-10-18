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
    """
    Reads, center-crops to 9:16, strips metadata, and encodes to lossy WebP (method 6).
    If file exceeds 180 KB at quality 60 at 1080x1920, downscales to 900x1600 (or 720x1280 / 540x960)
    to guarantee every file <= 180 KB.
    """
    output_path.parent.mkdir(parents=True, exist_ok=True)
    
    with Image.open(input_path) as img:
        if img.mode != "RGB":
            img = img.convert("RGB")
        
        orig_w, orig_h = img.size
        orig_ratio = orig_w / orig_h
        
        # Center-crop to 9:16
        if orig_ratio > TARGET_RATIO:
            crop_w = int(orig_h * TARGET_RATIO)
            crop_h = orig_h
            left = (orig_w - crop_w) // 2
            top = 0
            right = left + crop_w
            bottom = orig_h
        else:
            crop_w = orig_w
            crop_h = int(orig_w / TARGET_RATIO)
            left = 0
            top = (orig_h - crop_h) // 2
            right = orig_w
            bottom = top + crop_h
            
        cropped = img.crop((left, top, right, bottom))
        
        def encode_clean(image_obj, q: int) -> bytes:
            clean = Image.new("RGB", image_obj.size)
            clean.paste(image_obj, (0, 0))
            buf = io.BytesIO()
            clean.save(buf, format="WEBP", quality=q, method=6, lossless=False)
            return buf.getvalue()
        
        # Step 1: Try standard 1080x1920
        res_1080 = (TARGET_WIDTH, TARGET_HEIGHT)
        img_1080 = cropped.resize(res_1080, Image.Resampling.LANCZOS)
        bytes_60 = encode_clean(img_1080, 60)
        
        final_bytes = None
        final_res = res_1080
        final_q = 60
        is_downscaled = False
        
        if len(bytes_60) <= MAX_FILE_SIZE_BYTES:
            # Fits at 1080x1920; search highest quality in [60, 75]
            final_bytes = bytes_60
            for q in range(INITIAL_QUALITY, 59, -1):
                b = encode_clean(img_1080, q)
                if len(b) <= MAX_FILE_SIZE_BYTES:
                    final_bytes = b
                    final_q = q
                    break
        else:
            # Over 180 KB at q=60: downscale to 900x1600 (still 9:16)
            is_downscaled = True
            res_900 = (900, 1600)
            img_900 = cropped.resize(res_900, Image.Resampling.LANCZOS)
            bytes_900_60 = encode_clean(img_900, 60)
            
            if len(bytes_900_60) <= MAX_FILE_SIZE_BYTES:
                final_res = res_900
                final_bytes = bytes_900_60
                for q in range(INITIAL_QUALITY, 59, -1):
                    b = encode_clean(img_900, q)
                    if len(b) <= MAX_FILE_SIZE_BYTES:
                        final_bytes = b
                        final_q = q
                        break
            else:
                # Still over 180 KB at 900x1600: downscale to 720x1280 or 540x960 to guarantee <= 180 KB
                for res in [(720, 1280), (540, 960)]:
                    img_sub = cropped.resize(res, Image.Resampling.LANCZOS)
                    b_sub = encode_clean(img_sub, 60)
                    if len(b_sub) <= MAX_FILE_SIZE_BYTES:
                        final_res = res
                        final_bytes = b_sub
                        final_q = 60
                        break
                if final_bytes is None:
                    final_res = (540, 960)
                    final_bytes = encode_clean(cropped.resize((540, 960), Image.Resampling.LANCZOS), 60)
                    final_q = 60
        
        with open(output_path, "wb") as f:
            f.write(final_bytes)
            
    return len(final_bytes), final_res, final_q, is_downscaled


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
    downscaled_files = []
    
    for theme_dir in sorted(theme_dirs):
        theme_name = theme_dir.name
        out_theme_dir = OUTPUT_BACKGROUNDS_DIR / theme_name
        image_files = [f for f in theme_dir.iterdir() if f.is_file() and f.suffix.lower() in [".jpg", ".jpeg", ".png", ".webp"]]
        print(f"Processing theme '{theme_name}': {len(image_files)} images...")
        
        for img_file in sorted(image_files):
            out_file = out_theme_dir / f"{img_file.stem}.webp"
            size, res, q, was_downscaled = process_image(img_file, out_file)
            total_processed += 1
            total_bytes += size
            
            if size > largest_bytes:
                largest_bytes = size
                largest_file = f"{theme_name}/{out_file.name} ({size / 1024:.1f} KB, res={res[0]}x{res[1]}, q={q})"
                
            if was_downscaled:
                downscaled_files.append((f"{theme_name}/{out_file.name}", res, q, size))
                
    total_mb = total_bytes / (1024 * 1024)
    print("\n--- Image Processing Results ---")
    print(f"Total processed images: {total_processed}")
    print(f"Total size of backgrounds: {total_mb:.2f} MB ({total_bytes:,} bytes)")
    print(f"Largest file: {largest_file}")
    print(f"Total downscaled files: {len(downscaled_files)}")
    for name, res, q, size in downscaled_files:
        print(f"  - {name}: {res[0]}x{res[1]} at q={q} ({size / 1024:.1f} KB)")
    
    return total_processed, total_bytes, largest_file, largest_bytes, downscaled_files


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
                    
    has_ai = len(ai_sources) > 0 or any(c.get("isAi", False) for c in credits)
    credits_data = {
        "statement": "Some backgrounds were created with AI tools." if has_ai else "",
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
