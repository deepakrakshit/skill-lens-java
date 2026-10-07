#!/usr/bin/env python3
"""
AI writing pattern detector and scoring engine.
Implements the 26 diagnostic patterns defined in skills/humanizer/SKILL.md.
"""

import sys
import re
from pathlib import Path

PATTERNS = {
    "1_not_x_but_y": {
        "name": "Not X but Y / rather than contrasts",
        "weight": 3.0,
        "regexes": [
            r"\bnot\s+(?:only|just|merely)?\s*[^,.?!]{2,60},?\s*but\b",
            r"\brather than\b",
            r"\bdoes not mean\b",
            r"\bInstead of\s+[^,.?!]{3,50},\s+the\b"
        ]
    },
    "2_closers_fragments": {
        "name": "One-line dramatic closers / fragments",
        "weight": 3.0,
        "regexes": [
            r"\bThat is the real win\b",
            r"\bThat distinction matters\b",
            r"\bThe message was clear\b",
            r"\bLet that sink in\b",
            r"\bRead that again\b"
        ]
    },
    "3_sayings_deep": {
        "name": "Aphorisms and deep-sounding sayings",
        "weight": 2.5,
        "regexes": [
            r"\bthe real question is\b",
            r"\bat its core\b",
            r"\bin reality\b",
            r"\bwhat really matters\b",
            r"\bfundamentally\b",
            r"\bthe deeper issue\b",
            r"\bthe heart of the matter\b"
        ]
    },
    "4_staged_runup": {
        "name": "Staged run-up before point",
        "weight": 2.5,
        "regexes": [
            r"\blet\'?s dive in\b",
            r"\blet\'?s explore\b",
            r"\bhere\'?s what you need to know\b",
            r"\bwithout further ado\b",
            r"\bhonestly,\b",
            r"\bhere\'?s the thing\b"
        ]
    },
    "5_arguing_nobody": {
        "name": "Arguing with nobody / fake alternatives",
        "weight": 2.5,
        "regexes": [
            r"\bthis isn\'?t (?:mainly )?about\b",
            r"\bI\'?m not saying\b",
            r"\bto be clear\b",
            r"\bdon\'?t get me wrong\b",
            r"\bthis is not to say\b",
            r"\bsome might say\b",
            r"\ba tempting approach\b",
            r"\bone might be tempted\b",
            r"\byou might think\b",
            r"\bshould not be interpreted as\b",
            r"\bshould not be treated as\b",
            r"\bcannot by itself distinguish\b"
        ]
    },
    "6_forced_triads": {
        "name": "Forced triads / three-part lists",
        "weight": 1.5,
        "regexes": [
            r"\b\w+,\s+\w+,\s+and\s+\w+\b"
        ]
    },
    "8_dashes": {
        "name": "Em/En dashes as universal connector",
        "weight": 2.0,
        "regexes": [
            r"—",
            r"–",
            r"\s--\s"
        ]
    },
    "9_stacked_qualifiers": {
        "name": "Stacked qualifiers",
        "weight": 1.5,
        "regexes": [
            r"\bcould potentially\b",
            r"\bmight arguably\b",
            r"\bin some cases it may\b",
            r"\bcan possibly\b"
        ]
    },
    "12_overused_ai_words": {
        "name": "Overused AI vocabulary",
        "weight": 2.0,
        "regexes": [
            r"\badditionally\b",
            r"\balign with\b",
            r"\bbolstered\b",
            r"\bcrucial\b",
            r"\bdeep dive\b",
            r"\bdelve\b",
            r"\benduring\b",
            r"\benhance(?:s|d|ing)?\b",
            r"\bgarner\b",
            r"\bhighlight(?:s|ed|ing)?\b",
            r"\binterplay\b",
            r"\bintricate\b",
            r"\bkey\b",
            r"\blandscape\b",
            r"\bmeticulous(?:ly)?\b",
            r"\bpivotal\b",
            r"\bquietly\b",
            r"\brobust\b",
            r"\bshowcase(?:s|d|ing)?\b",
            r"\btapestry\b",
            r"\btestament\b",
            r"\bunderscore(?:s|d|ing)?\b",
            r"\bvaluable\b",
            r"\bvibrant\b"
        ]
    },
    "13_inflated_significance": {
        "name": "Inflated significance and stock phrasing",
        "weight": 2.5,
        "regexes": [
            r"\bstands as a testament\b",
            r"\ba (?:pivotal|crucial) moment\b",
            r"\bplays a key role\b",
            r"\bmarking a pivotal\b",
            r"\bunderscores its importance\b",
            r"\breflects a broader\b",
            r"\bcontinues to thrive\b",
            r"\bstep in the right direction\b",
            r"\bdemonstrates that the workflow\b",
            r"\bcreates a workflow that is more\b"
        ]
    },
    "14_vague_connection": {
        "name": "Vague connection phrases",
        "weight": 1.5,
        "regexes": [
            r"\bassociated with\b",
            r"\bin association with\b",
            r"\bconnected to\b",
            r"\bin connection with\b",
            r"\blinked to\b",
            r"\btied to\b"
        ]
    },
    "15_shallow_ing_riders": {
        "name": "Shallow -ing participle riders",
        "weight": 2.0,
        "regexes": [
            r",\s*(?:highlighting|underscoring|emphasizing|ensuring|reflecting|symbolizing|contributing to|cultivating|fostering|encompassing|showcasing|connecting|making|enabling)\b"
        ]
    },
    "16_sales_language": {
        "name": "Sales/promotional adjectives",
        "weight": 2.0,
        "regexes": [
            r"\brich\b",
            r"\bprofound\b",
            r"\bexemplifies\b",
            r"\bgroundbreaking\b",
            r"\brenowned\b",
            r"\bdiverse array\b",
            r"\bbreathtaking\b",
            r"\bstunning\b"
        ]
    },
    "18_avoiding_is_are": {
        "name": "Avoiding simple copulas (is/are/has)",
        "weight": 1.5,
        "regexes": [
            r"\bserves as\b",
            r"\bstands as\b",
            r"\bfunctions as\b",
            r"\boperates as\b",
            r"\bboasts\b",
            r"\bfeatures\b",
            r"\bmaintains a\b",
            r"\bdelivers a\b"
        ]
    },
    "20_decorative_chars": {
        "name": "Decorative arrows and symbols in prose",
        "weight": 2.0,
        "regexes": [
            r"→",
            r"🚀",
            r"💡"
        ]
    },
    "21_curly_quotes": {
        "name": "Curly quotation marks",
        "weight": 1.0,
        "regexes": [
            r"[“”‘’]"
        ]
    },
    "banned_token_rule2": {
        "name": "Rule 2 Prohibited Token (s-t-a-g-e)",
        "weight": 10.0,
        "regexes": [
            r"\b" + "s" + r"tages?\b"
        ]
    }
}

def analyze_text(text: str):
    results = {}
    total_score = 0.0
    total_instances = 0
    words = len(re.findall(r"\b\w+\b", text))
    
    for key, info in PATTERNS.items():
        matches = []
        for r in info["regexes"]:
            for m in re.finditer(r, text, re.IGNORECASE):
                # Get snippet
                start = max(0, m.start() - 25)
                end = min(len(text), m.end() + 25)
                snippet = text[start:end].replace("\n", " ").strip()
                matches.append({"match": m.group(0), "snippet": snippet, "pos": m.start()})
        if matches:
            results[key] = {
                "name": info["name"],
                "count": len(matches),
                "weight": info["weight"],
                "penalty": len(matches) * info["weight"],
                "matches": matches
            }
            total_score += len(matches) * info["weight"]
            total_instances += len(matches)

    # Normalize AI detection probability
    # Typical density: > 10 penalty points per 1,000 words indicates heavy AI content.
    density = (total_score / max(1, words)) * 1000
    ai_percentage = min(100.0, max(0.0, density * 4.0)) # scale to 0-100%
    if total_instances == 0:
        ai_percentage = 0.0

    return {
        "word_count": words,
        "total_instances": total_instances,
        "penalty_score": total_score,
        "ai_detected_percentage": round(ai_percentage, 1),
        "details": results
    }

if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    target = sys.argv[1] if len(sys.argv) > 1 else "skill-lens-text.txt"
    content = Path(target).read_text(encoding="utf-8")
    res = analyze_text(content)
    print("=" * 60)
    print("SKILL-LENS HUMANIZER PATTERN DETECTION REPORT")
    print("=" * 60)
    print(f"Target File:           {target}")
    print(f"Word Count:            {res['word_count']}")
    print(f"Pattern Instances:     {res['total_instances']}")
    print(f"Total AI Tells Score:  {res['penalty_score']:.1f}")
    print(f"Calculated AI Rating:  {res['ai_detected_percentage']}%")
    print("-" * 60)
    for k, v in res["details"].items():
        print(f"\n[{v['name']}] - {v['count']} occurrences:")
        for m in v["matches"][:3]:
            safe_snippet = m['snippet'].encode('ascii', errors='replace').decode('ascii')
            print(f"   • \"...{safe_snippet}...\"")
        if len(v["matches"]) > 3:
            print(f"   (plus {len(v['matches']) - 3} more)")
    print("=" * 60)
