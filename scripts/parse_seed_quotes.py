import re
import json

with open('app/src/main/java/com/myplanetservices/stillwords/data/local/SeedQuotes.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Pattern to capture QuoteEntity(...) blocks
pattern = re.compile(r'QuoteEntity\s*\((.*?)\n\s*\)', re.DOTALL)
matches = pattern.findall(content)
print(f"Total quotes found in SeedQuotes.kt: {len(matches)}")

quotes = []
categories = {}

for m in matches:
    id_m = re.search(r'id\s*=\s*"([^"]+)"', m)
    text_m = re.search(r'text\s*=\s*"([^"]+)"', m)
    author_m = re.search(r'author\s*=\s*"([^"]+)"', m)
    cat_m = re.search(r'category\s*=\s*"([^"]+)"', m)
    source_m = re.search(r'source\s*=\s*"([^"]+)"', m)
    tags_m = re.search(r'tags\s*=\s*"([^"]+)"', m)
    moods_m = re.search(r'moods\s*=\s*"([^"]+)"', m)
    situations_m = re.search(r'situations\s*=\s*"([^"]+)"', m)
    expl_m = re.search(r'explanation\s*=\s*"([^"]+)"', m)
    feat_m = re.search(r'isFeatured\s*=\s*(true|false)', m)
    style_m = re.search(r'defaultStyle\s*=\s*"([^"]+)"', m)
    
    qid = id_m.group(1) if id_m else ""
    text = text_m.group(1) if text_m else ""
    author = author_m.group(1) if author_m else ""
    cat = cat_m.group(1) if cat_m else ""
    source = source_m.group(1) if source_m else ""
    tags = tags_m.group(1) if tags_m else ""
    moods = moods_m.group(1) if moods_m else ""
    situations = situations_m.group(1) if situations_m else ""
    expl = expl_m.group(1) if expl_m else ""
    feat = feat_m.group(1) == "true" if feat_m else False
    style = style_m.group(1) if style_m else "NOIR_STILLWORDS"
    
    quotes.append({
        "id": qid,
        "text": text,
        "author": author,
        "category": cat,
        "source": source,
        "tags": tags,
        "moods": moods,
        "situations": situations,
        "explanation": expl,
        "isFeatured": feat,
        "defaultStyle": style
    })
    categories[cat] = categories.get(cat, 0) + 1

print("\n--- Quote Count Per Category BEFORE Adding Anything ---")
for cat in sorted(categories.keys()):
    print(f"{cat}: {categories[cat]}")

with open('scripts/extracted_initial_quotes.json', 'w', encoding='utf-8') as out:
    json.dump(quotes, out, indent=2)

print(f"\nExtracted {len(quotes)} quotes to scripts/extracted_initial_quotes.json")
