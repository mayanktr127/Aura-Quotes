#!/usr/bin/env python3
"""
scripts/generate_quotes_json.py

Combines Batch 1, Batch 2, and Batch 3 to generate app/src/main/assets/quotes.json.
Validates:
1. All 22 categories have at least 30 quotes each.
2. Unique quote IDs across the entire catalog.
3. Proper JSON format with fields:
   - id
   - text
   - author
   - category
   - isOriginal
   - tags
   - moods
   - situations
   - explanation
   - source
   - isFeatured
   - defaultStyle
"""

import sys
import json
from pathlib import Path

# Add scripts directory to path for imports
BASE_DIR = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE_DIR / "scripts"))

from quotes_data.cat_batch1 import BATCH1_QUOTES
from quotes_data.cat_batch2 import BATCH2_QUOTES
from quotes_data.cat_batch3 import BATCH3_QUOTES

OUTPUT_FILE = BASE_DIR / "app" / "src" / "main" / "assets" / "quotes.json"

CATEGORIES = [
    "Confidence", "Courage", "Creativity", "Discipline", "Fitness",
    "Friendship", "Gratitude", "Growth", "Happiness", "Healing",
    "Leadership", "Life", "Love", "Mindfulness", "Motivation",
    "Peace", "Relationships", "Self-Love", "Study", "Success",
    "Wisdom", "Work"
]

def main():
    combined = {}
    combined.update(BATCH1_QUOTES)
    combined.update(BATCH2_QUOTES)
    combined.update(BATCH3_QUOTES)

    print("--- Validating 22 Categories ---")
    all_quotes = []
    seen_ids = set()

    for cat in sorted(CATEGORIES):
        quotes = combined.get(cat, [])
        count = len(quotes)
        print(f"Category '{cat}': {count} quotes")
        if count < 30:
            print(f"ERROR: Category '{cat}' has only {count} quotes! Minimum required is 30.")
            sys.exit(1)

        for q in quotes:
            qid = q["id"]
            if qid in seen_ids:
                print(f"ERROR: Duplicate quote ID '{qid}' found!")
                sys.exit(1)
            seen_ids.add(qid)

            # Ensure all domain fields are set
            quote_obj = {
                "id": qid,
                "text": q["text"].strip(),
                "author": q.get("author", "Stillwords").strip(),
                "category": cat,
                "isOriginal": bool(q.get("isOriginal", True)),
                "tags": q.get("tags", f"{cat.lower()},inspiration,wisdom"),
                "moods": q.get("moods", "Thoughtful,Calm"),
                "situations": q.get("situations", "Morning,Before Sleep"),
                "explanation": q.get("explanation", f"A reflection on {cat.lower()} from Stillwords."),
                "source": q.get("source", "Stillwords Collection"),
                "isFeatured": bool(q.get("isFeatured", False)),
                "defaultStyle": q.get("defaultStyle", "NOIR_STILLWORDS")
            }
            all_quotes.append(quote_obj)

    print(f"\nTotal validated quotes: {len(all_quotes)} across {len(CATEGORIES)} categories.")

    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(all_quotes, f, indent=2, ensure_ascii=False)

    print(f"Successfully generated: {OUTPUT_FILE} ({OUTPUT_FILE.stat().st_size:,} bytes)")

if __name__ == "__main__":
    main()
