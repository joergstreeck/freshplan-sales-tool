import os, sys, yaml

ROOT = "docs"
REQUIRED = ["module","domain","doc_type","status","owner","updated"]
ALLOWED_DOMAIN = {"frontend","backend","shared"}
ALLOWED_DOC_TYPE = {"analyse","konzept","contract","guideline","deltalog","adr","stub","technical_concept","sprint_map"}

def iter_md():
    for dirpath,_,filenames in os.walk(ROOT):
        for fn in filenames:
            if fn.endswith(".md"):
                yield os.path.join(dirpath, fn)

def parse_frontmatter(path):
    try:
        with open(path, "r", encoding="utf-8") as f:
            text = f.read()
    except UnicodeDecodeError:
        print(f"Unicode decode error in {path}, skipping")
        return None, ""
    if not text.startswith("---"):
        return None, text
    parts = text.split("---", 2)
    if len(parts) < 3:
        return None, text
    fm = parts[1]
    body = parts[2]
    try:
        data = yaml.safe_load(fm) or {}
    except yaml.YAMLError as e:
        print(f"YAML parsing error in {path}: {e}")
        return None, text
    return data, body

errors = []
processed_count = 0
skipped_count = 0

for p in iter_md():
    # ONLY check Module 02/03 files - skip everything else
    module_match = None
    if "/02_neukundengewinnung/" in p:
        module_match = "02"
    elif "/03_kundenmanagement/" in p:
        module_match = "03"

    if not module_match:
        # Skip ALL files that are not in Module 02/03
        skipped_count += 1
        continue

    # Skip legacy-planning in Module 02/03 - they have different structure
    if "/legacy-planning/" in p:
        skipped_count += 1
        continue

    fm, body = parse_frontmatter(p)

    processed_count += 1

    if fm is None:
        errors.append(f"{p}: fehlende Front-Matter (--- ... ---)")
        continue

    # Pflichtfelder prüfen (only for Module 02/03)
    for k in REQUIRED:
        if k not in fm:
            errors.append(f"{p}: Front-Matter Pflichtfeld fehlt: {k}")

    # domänen & Typen validieren
    if "domain" in fm and fm["domain"] not in ALLOWED_DOMAIN:
        errors.append(f"{p}: domain ungültig: {fm['domain']}")
    if "doc_type" in fm and fm["doc_type"] not in ALLOWED_DOC_TYPE:
        errors.append(f"{p}: doc_type ungültig: {fm['doc_type']}")

    # Stubs brauchen phase & moved_to
    if fm.get("doc_type") == "stub":
        if fm.get("phase") != "archived":
            errors.append(f"{p}: stub braucht phase: 'archived'")
        if not fm.get("moved_to"):
            errors.append(f"{p}: stub braucht moved_to")

print(f"DEBUG: Processed {processed_count} files, skipped {skipped_count} legacy files")
if errors:
    print("\n".join(errors))
    sys.exit(1)
print("Front-Matter OK")