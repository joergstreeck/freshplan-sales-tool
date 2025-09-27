import os, sys, yaml

ROOT = "docs"
REQUIRED = ["module","domain","doc_type","status","owner","updated"]
ALLOWED_DOMAIN = {"frontend","backend","shared"}
ALLOWED_DOC_TYPE = {"analyse","konzept","contract","guideline","deltalog","adr","stub"}

def iter_md():
    for dirpath,_,filenames in os.walk(ROOT):
        for fn in filenames:
            if fn.endswith(".md"):
                yield os.path.join(dirpath, fn)

def parse_frontmatter(path):
    with open(path, "r", encoding="utf-8") as f:
        text = f.read()
    if not text.startswith("---"):
        return None, text
    parts = text.split("---", 2)
    if len(parts) < 3:
        return None, text
    fm = parts[1]
    body = parts[2]
    data = yaml.safe_load(fm) or {}
    return data, body

errors = []
for p in iter_md():
    fm, body = parse_frontmatter(p)
    # Trigger-Sprints dürfen separate Sprint-Header haben → Front-Matter optional
    if fm is None:
        if "/features-neu/" in p:
            errors.append(f"{p}: fehlende Front-Matter (--- ... ---)")
        continue

    # Pflichtfelder prüfen
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

if errors:
    print("\n".join(errors))
    sys.exit(1)
print("Front-Matter OK")