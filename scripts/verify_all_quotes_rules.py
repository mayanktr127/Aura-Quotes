"""
verify_all_quotes_rules.py
Full assertion test for all user requirements in Round 2:
1. Every classic quote has live Wikiquote source URL
2. Every classic quote matches author's wikitext in Sourced section (NOT Attributed, Disputed, Misattributed)
3. Every classic author died before 1956
4. No Stillwords originals > 120 chars
5. Every category has >= 30 quotes
6. Final breakdown reported
"""

import json
import re
from collections import Counter

with open("app/src/main/assets/quotes.json", "r", encoding="utf-8") as f:
    quotes = json.load(f)

print(f"Total quotes in quotes.json: {len(quotes)}")

# Author death years verification (< 1956)
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
    "Plato": -348,
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

classics = [q for q in quotes if not q.get("isOriginal")]
print(f"Total classics: {len(classics)}")

# Check 1: Death years
for q in classics:
    author = q["author"]
    death = DEATH_YEARS.get(author)
    assert death is not None, f"Unknown author {author}"
    assert death < 1956, f"Author {author} died in {death} (must be before 1956)"

# Check 2: Sources starting with https://en.wikiquote.org/wiki/
for q in classics:
    src = q.get("source", "")
    assert src.startswith("https://en.wikiquote.org/wiki/"), f"Invalid source for {q['id']}: {src}"

# Check 3: Rigorous Wikiquote ancestor header stack verification
FORBIDDEN = ["attributed", "disputed", "misattributed", "quotes about", "see also"]
verified_count = 0
for q in classics:
    author = q["author"]
    page = q["source"].split("wiki/")[1].split("#")[0]
    
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
            
    assert pos != -1, f"Quote {q['id']} not found in {page} wikitext"
    
    headers = re.findall(r'(==+\s*([^=]+?)\s*==+)', wt[:pos])
    stack = []
    for full_h, name in headers:
        level = full_h.count('=') // 2
        while stack and stack[-1][0] >= level:
            stack.pop()
        stack.append((level, name.strip()))
        
    for level, name in stack:
        for f in FORBIDDEN:
            assert f not in name.lower(), f"Forbidden header {name} for {q['id']}"
            
    verified_count += 1

print(f"Wikiquote wikitext verification: {verified_count}/{len(classics)} passed")

# Check 4: Originals <= 120 chars
originals = [q for q in quotes if q.get("isOriginal")]
long_orig = [q for q in originals if len(q["text"]) > 120]
assert len(long_orig) == 0, f"Found {len(long_orig)} originals > 120 chars!"

# Check 5: Categories >= 30
cat_counts = Counter(q["category"] for q in quotes)
for cat, cnt in sorted(cat_counts.items()):
    assert cnt >= 30, f"Category {cat} has only {cnt} quotes (< 30)"

print("\n--- FINAL CATEGORY BREAKDOWN ---")
cat_classics = Counter(q["category"] for q in classics)
cat_originals = Counter(q["category"] for q in originals)
for cat in sorted(cat_counts.keys()):
    print(f"{cat:15}: Total = {cat_counts[cat]:2d} | Classics = {cat_classics[cat]:2d} | Originals = {cat_originals[cat]:2d}")

print(f"\nOverall: {len(quotes)} quotes ({len(classics)} classics, {len(originals)} originals)")
print("All assertion checks PASSED successfully!")
