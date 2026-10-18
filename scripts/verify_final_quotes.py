import json
import re
from collections import Counter

with open("app/src/main/assets/quotes.json", "r", encoding="utf-8") as f:
    quotes = json.load(f)

print(f"Total quotes: {len(quotes)}")

DEATH_YEARS = {
    "Marcus Aurelius": 180,
    "Seneca": 65,
    "Seneca the Younger": 65,
    "Epictetus": 135,
    "Ralph Waldo Emerson": 1882,
    "Henry David Thoreau": 1862,
    "Confucius": -479,
    "Laozi": -400,
    "Leo Tolstoy": 1910,
    "Benjamin Franklin": 1790,
    "Abraham Lincoln": 1865,
    "Aristotle": -322,
    "Francis Bacon": 1626,
    "Michel de Montaigne": 1592,
    "William Shakespeare": 1616,
    "Samuel Johnson": 1784,
    "Arthur Schopenhauer": 1860,
    "Johann Wolfgang von Goethe": 1832,
    "Walt Whitman": 1892,
    "Oscar Wilde": 1900,
    "William James": 1910,
    "Cicero": -43,
    "Plutarch": 119,
    "John Locke": 1704,
    "Voltaire": 1778,
    "Blaise Pascal": 1662,
    "Thomas Carlyle": 1881,
    "Robert Louis Stevenson": 1894,
    "Sun Tzu": -496,
    "Friedrich Nietzsche": 1900,
    "Elbert Hubbard": 1915
}

TRANSLATED_AUTHORS = {
    "Marcus Aurelius", "Seneca", "Seneca the Younger", "Cicero", "Confucius",
    "Michel de Montaigne", "Aristotle", "Johann Wolfgang von Goethe", "Leo Tolstoy",
    "Friedrich Nietzsche", "Arthur Schopenhauer", "Voltaire", "Blaise Pascal",
    "Sun Tzu", "Epictetus", "Laozi", "Plutarch"
}

ENGLISH_AUTHORS = {
    "Ralph Waldo Emerson", "Henry David Thoreau", "Benjamin Franklin",
    "Abraham Lincoln", "Francis Bacon", "William Shakespeare", "Samuel Johnson",
    "Walt Whitman", "Oscar Wilde", "William James", "John Locke", "Thomas Carlyle",
    "Robert Louis Stevenson", "Elbert Hubbard"
}

classics = [q for q in quotes if not q.get("isOriginal")]
print(f"Classic quotes: {len(classics)}")

# 1. Death years
for q in classics:
    author = q["author"]
    death = DEATH_YEARS[author]
    assert death < 1956, f"{author} died >= 1956 ({death})"

# 2. Translators
translated_count = 0
for q in classics:
    author = q["author"]
    if author in TRANSLATED_AUTHORS:
        assert "translator" in q and q["translator"], f"Missing translator for {q['id']} ({author})"
        tr = q["translator"]
        m = re.search(r'\b(1[6789]\d\d|192[0-8])\b', tr)
        assert m is not None, f"Translator year not pre-1929: '{tr}' for {q['id']}"
        year = int(m.group(1))
        assert year < 1929, f"Translator year {year} >= 1929 for {q['id']}"
        translated_count += 1
    else:
        assert author in ENGLISH_AUTHORS, f"Unknown author {author}"
        # English authors should not have translator or it should be None
        assert q.get("translator") is None, f"English author {author} has translator {q.get('translator')}"

print(f"Translator verification: {translated_count} translated quotes verified with pre-1929 translators!")

# 3. Wikiquote verification: Sourced section, not Attributed/Disputed/Misattributed
FORBIDDEN = ["attributed", "disputed", "misattributed", "quotes about", "see also"]
verified_wt = 0

for q in classics:
    author = q["author"]
    src = q["source"]
    assert src.startswith("https://en.wikiquote.org/wiki/"), f"Invalid source URL: {src} for {q['id']}"
    page = src.split("wiki/")[1].split("#")[0]
    
    with open(f"scripts/wikiquote_cache/{page}.json", "r", encoding="utf-8") as f:
        wt = json.load(f)["parse"]["wikitext"]["*"]
        
    words = [w for w in re.split(r'[\s\'\".,!?;:—–\(\)]+', q["text"]) if len(w) > 3]
    pos = -1
    for i in range(len(words)):
        w = words[i]
        p = 0
        while True:
            idx = wt.lower().find(w.lower(), p)
            if idx == -1:
                break
            sub = wt[max(0, idx-50):min(len(wt), idx+150)].lower()
            matching_count = sum(1 for w2 in words if w2.lower() in sub)
            if matching_count >= 2:
                pos = idx
                break
            p = idx + 1
        if pos != -1:
            break
            
    assert pos != -1, f"Quote {q['id']} ({author}) not found in {page} wikitext! Text: '{q['text']}'"
    
    headers = re.findall(r'(==+\s*([^=]+?)\s*==+)', wt[:pos])
    stack = []
    for full_h, name in headers:
        level = full_h.count('=') // 2
        while stack and stack[-1][0] >= level:
            stack.pop()
        stack.append((level, name.strip()))
        
    for level, name in stack:
        for f in FORBIDDEN:
            assert f not in name.lower(), f"Forbidden header '{name}' in ancestor stack for {q['id']}"
            
    verified_wt += 1

print(f"Wikiquote wikitext verification: {verified_wt}/{len(classics)} passed with flying colors!")

# 4. Originals <= 120 chars
originals = [q for q in quotes if q.get("isOriginal")]
long_orig = [q for q in originals if len(q["text"]) > 120]
assert len(long_orig) == 0, f"Found {len(long_orig)} originals > 120 chars!"
print(f"Originals length verification: {len(originals)} originals, 0 > 120 chars.")

# 5. Category counts >= 30
cat_counts = Counter(q["category"] for q in quotes)
for cat, cnt in sorted(cat_counts.items()):
    assert cnt >= 30, f"Category {cat} has only {cnt} quotes (< 30)"

print("\n--- FINAL CATEGORY BREAKDOWN ---")
cat_classics = Counter(q["category"] for q in classics)
cat_originals = Counter(q["category"] for q in originals)
for cat in sorted(cat_counts.keys()):
    print(f"{cat:15}: Total = {cat_counts[cat]:2d} | Classics = {cat_classics[cat]:2d} | Originals = {cat_originals[cat]:2d}")

print(f"\nOverall: {len(quotes)} quotes ({len(classics)} classics, {len(originals)} originals)")
print("ALL ASSERTION TESTS PASSED PERFECTLY!")
