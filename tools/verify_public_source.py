#!/usr/bin/env python3
"""Validate the public LevelZ+ 2.1.3.1 source snapshot."""
from pathlib import Path
import json
import re
import sys

root = Path(__file__).resolve().parents[1]
errors = []

def require(ok, message):
    if not ok:
        errors.append(message)

def get_json(path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as ex:
        errors.append(f"Cannot parse {path.relative_to(root)}: {ex}")
        return {}

meta = get_json(root / "src/main/resources/fabric.mod.json")
require(meta.get("id") == "levelz_plus", "Incorrect mod ID")
require(meta.get("version") == "2.1.3.1+mc1.20.1", "Incorrect stable version")
require(meta.get("depends", {}).get("levelz") == "1.4.13", "Wrong LevelZ target")
require(meta.get("depends", {}).get("fabricloader") == ">=0.16.14", "Wrong Fabric Loader requirement")
require(meta.get("depends", {}).get("minecraft") == "~1.20.1", "Wrong Minecraft version")
require(meta.get("contact", {}).get("sources") == "https://github.com/10bibbys-cmyk/LevelZ-Plus-Source", "Source URL is not public")

gradle = (root / "build.gradle").read_text(encoding="utf-8")
m = re.search(r"(?m)^\s*version\s*=\s*['\"]([^'\"]+)['\"]", gradle)
require(m is not None and m.group(1) == meta.get("version"), "Gradle and metadata version mismatch")
require("fileTree(dir:'libs'" not in gradle, "Forbidden local JAR compile fallback")

mixins = meta.get("mixins", [])
require(isinstance(mixins, list) and len(mixins) == 6, "Expected six Mixin configurations")
for config_name in mixins:
    config_file = root / "src/main/resources" / config_name
    conf = get_json(config_file)
    package = conf.get("package", "")
    for category in ("mixins", "client", "server"):
        entries = conf.get(category, [])
        require(isinstance(entries, list), f"{config_name}: {category} must be a list")
        for name in entries if isinstance(entries, list) else []:
            source = root / "src/main/java" / Path(*(package + "." + name).split("."))
            source = source.with_suffix(".java")
            require(source.is_file(), f"Missing Mixin class source {source.relative_to(root)}")
for entrypoint_group in ("main", "client"):
    for name in meta.get("entrypoints", {}).get(entrypoint_group, []):
        code = root / "src/main/java" / Path(*name.split("."))
        require(code.with_suffix(".java").is_file(), f"Missing {entrypoint_group} entrypoint: {name}")
for file in (root / "src/main/resources").rglob("*.json"):
    get_json(file)
for name in ("LICENSE", "NOTICE.md", "README.md", "BUILDING.md", "CHANGELOG.md"):
    require((root / name).is_file(), f"Missing public document: {name}")
for forbidden in ("AUDIT.md", "SOURCE_RECOVERY.md", "RECOVERY_STATUS.md",
                  "RELEASE_CHECKLIST.md", "DEVELOPMENT.md", "baseline/Baseline.md"):
    require(not (root / forbidden).exists(), f"Private development record committed: {forbidden}")
for path in root.rglob("*.jar"):
    require("build" in path.parts or ".gradle" in path.parts, f"Bundled binary not allowed: {path}")
for path in root.rglob("*.class"):
    require("build" in path.parts or ".gradle" in path.parts, f"Bundled class not allowed: {path}")
if errors:
    for message in errors:
        print("ERROR:", message)
    sys.exit(1)
print("LevelZ+ 2.1.3.1 public source validation: PASS")
