import json
from pathlib import Path

# Script to generate app/src/main/assets/quotes.json ensuring:
# 1. 22 categories each have at least 30 quotes.
# 2. Retains the 7 verified/re-attributed quotes from initial audit.
# 3. New original quotes authored by "Stillwords" (isOriginal = True) with human, warm, specific tone.
# 4. Rich metadata: id, text, author, category, isOriginal, tags, moods, situations, explanation, source, defaultStyle.

CATEGORIES = [
    "Confidence", "Courage", "Creativity", "Discipline", "Fitness",
    "Friendship", "Gratitude", "Growth", "Happiness", "Healing",
    "Leadership", "Life", "Love", "Mindfulness", "Motivation",
    "Peace", "Relationships", "Self-Love", "Study", "Success",
    "Wisdom", "Work"
]

print(f"Total categories: {len(CATEGORIES)}")
