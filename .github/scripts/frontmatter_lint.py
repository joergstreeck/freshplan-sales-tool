#!/usr/bin/env python3
import os, sys, re

# PyYAML ist optional – wir fallen bei Importproblemen auf einen einfachen Parser zurück
try:
    import yaml  # type: ignore
except Exception:
    yaml = None

ROOT = "docs/planung/features-neu"
MODULES_IN_SCOPE = ("02_neukundengewinnung", "03_kundenmanagement")
REQUIRED = ["module", "doc_type", "status", "owner", "updated"]
ALLOWED_DOC_TYPE = {
    "analyse","konzept","contract","guideline","deltalog","adr",
    "stub","technical_concept","sprint_map"
}

def iter_md():
    for module in MODULES_IN_SCOPE:
        root = os.path.join(ROOT, module)
        if not os.path.isdir(root):
            continue
        for dirpath, _, files in os.walk(root):
            # Legacy-Bereich bewusst ausklammern
            if "/legacy-planning/" in dirpath.replace("\\", "/"):
                continue
            for fn in files:
                if fn.endswith(".md"):
                    yield os.path.join(dirpath, fn)

def parse_frontmatter(path: str):
    try:
        with open(path, "r", encoding="utf-8") as f:
            text = f.read()
    except UnicodeDecodeError:
        print(f"DEBUG: Unicode decode error in {path}, skipping")
        return None, ""

    if not text.lstrip().startswith("---"):
        return None, text

    parts = text.split("---", 2)
    if len(parts) < 3:
        return None, text

    fm_text = parts[1]
    body = parts[2]

    # Robuster YAML- oder Fallback-Parser
    def fallback_parse(lines: str):
        data = {}
        for line in lines.splitlines():
            line = line.strip()
            if not line or line.startswith("#") or ":" not in line:
                continue
            k, v = line.split(":", 1)
            data[k.strip()] = v.strip().strip('"\'')

        return data

    if yaml is not None:
        try:
            data = yaml.safe_load(fm_text) or {}
        except Exception as e:
            print(f"DEBUG: YAML parsing error in {path}: {e} – using fallback parser")
            data = fallback_parse(fm_text)
    else:
        data = fallback_parse(fm_text)

    return data, body

def main():
    errors = []
    processed = 0
    skipped = 0

    for p in iter_md():
        fm, body = parse_frontmatter(p)
        if fm is None:
            errors.append(f"{p}: fehlende Front-Matter (--- ... ---)")
            processed += 1
            continue

        processed += 1

        # Pflichtfelder (domain ist optional)
        for k in REQUIRED:
            if k not in fm:
                errors.append(f"{p}: Front-Matter Pflichtfeld fehlt: {k}")

        # doc_type validieren, wenn vorhanden
        dt = fm.get("doc_type")
        if dt and dt not in ALLOWED_DOC_TYPE:
            errors.append(f"{p}: unbekannter doc_type: {dt}")

        # Stub-Regeln: moved_to muss existieren
        if fm.get("doc_type") == "stub" or fm.get("status") == "moved":
            if not fm.get("moved_to"):
                errors.append(f"{p}: stub/moved braucht moved_to")

    print(f"DEBUG: Processed {processed} files in modules {MODULES_IN_SCOPE}, skipped {skipped} legacy files")

    if errors:
        print("\n".join(errors))
        sys.exit(1)

if __name__ == "__main__":
    main()