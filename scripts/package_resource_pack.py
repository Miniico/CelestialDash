#!/usr/bin/env python3
"""Validate and reproducibly package the checked-in CelestialDash resource pack.

Usage: python scripts/package_resource_pack.py path/to/CelestialDash-Resource-Pack.zip
Only Python's standard library is required. Text line endings, entry order,
timestamps and permissions are normalized; ZIP_STORED avoids zlib-version drift.
The existing output is replaced atomically only after source validation succeeds.
"""

import argparse
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import struct
import sys
import tempfile
import zipfile
import zlib


SOURCE = Path(__file__).resolve().parents[1] / "resource-pack" / "CelestialDash-Resource-Pack"
OVERLAY = "v1214"
ITEMS = (
    ("ghast_tear", "celestial_tear", 22001),
    ("nautilus_shell", "celestial_amulet", 22002),
)
MIN_FORMAT = (15, 0)  # Minecraft 1.20
MODERN_FORMAT = (46, 0)  # Minecraft 1.21.4 item definitions
MAX_TESTED_FORMAT = (97, 1)  # Minecraft 26.3
RESOURCE_ID = re.compile(r"[a-z0-9_.-]+:[a-z0-9_./-]+\Z")


class InvalidPack(ValueError):
    """The source cannot produce the supported deployable pack."""


def require(condition, message):
    if not condition:
        raise InvalidPack(message)


def format_version(value, upper=False):
    if type(value) is int and value >= 0:
        return value, 0x7fffffff if upper else 0
    if isinstance(value, list) and len(value) == 1:
        return format_version(value[0], upper)
    if (isinstance(value, list) and len(value) == 2
            and all(type(part) is int and part >= 0 for part in value)):
        return tuple(value)
    raise InvalidPack(f"Invalid pack format version: {value!r}")


def legacy_range(value):
    if type(value) is int:
        return value, value
    if isinstance(value, list) and len(value) == 2:
        low, high = value
    elif isinstance(value, dict) and set(value) == {"min_inclusive", "max_inclusive"}:
        low, high = value["min_inclusive"], value["max_inclusive"]
    else:
        raise InvalidPack(f"Invalid legacy format range: {value!r}")
    require(type(low) is int and type(high) is int and 0 <= low <= high,
            f"Invalid legacy format range: {value!r}")
    return low, high


def validate_range(node, legacy_key, minimum, maximum, label):
    require(isinstance(node, dict), f"{label} must be an object")
    low, high = legacy_range(node.get(legacy_key))
    modern_low = format_version(node.get("min_format"))
    modern_high = format_version(node.get("max_format"), upper=True)
    require(modern_low <= minimum and modern_high >= maximum,
            f"{label} must cover formats {minimum} through {maximum}")
    require(low <= minimum[0] and high >= maximum[0],
            f"{label}.{legacy_key} does not cover the supported clients")
    require(low == modern_low[0] and high == modern_high[0],
            f"{label}: legacy and modern format bounds disagree")


def json_object(data, name):
    def unique_keys(pairs):
        result = {}
        for key, value in pairs:
            require(key not in result, f"{name}: duplicate JSON key {key!r}")
            result[key] = value
        return result

    def invalid_constant(value):
        raise InvalidPack(f"{name}: invalid JSON constant {value}")

    try:
        result = json.loads(data.decode("utf-8"), object_pairs_hook=unique_keys,
                            parse_constant=invalid_constant)
    except (UnicodeError, json.JSONDecodeError) as error:
        raise InvalidPack(f"{name}: invalid UTF-8 JSON: {error}") from error
    require(isinstance(result, dict), f"{name}: expected a JSON object")
    return result


def validate_png(data, name, expected_size=None):
    """Check chunk boundaries/CRC and the actual pixels in our RGBA PNG assets."""
    require(data.startswith(b"\x89PNG\r\n\x1a\n"), f"{name}: invalid PNG signature")
    offset, chunks, compressed = 8, [], bytearray()
    width = height = 0
    while offset < len(data):
        require(offset + 12 <= len(data), f"{name}: truncated PNG chunk")
        size = struct.unpack_from(">I", data, offset)[0]
        kind = data[offset + 4:offset + 8]
        end = offset + 8 + size
        require(end + 4 <= len(data), f"{name}: truncated PNG payload")
        payload = data[offset + 8:end]
        crc = struct.unpack_from(">I", data, end)[0]
        require(zlib.crc32(kind + payload) & 0xffffffff == crc,
                f"{name}: invalid {kind!r} PNG checksum")
        chunks.append(kind)
        if kind == b"IHDR":
            require(len(chunks) == 1 and size == 13, f"{name}: invalid PNG header")
            width, height, depth, color, comp, filt, interlace = struct.unpack(">IIBBBBB", payload)
            require(width > 0 and height > 0 and width <= 4096 and height <= 4096,
                    f"{name}: invalid PNG dimensions")
            require((depth, color, comp, filt, interlace) == (8, 6, 0, 0, 0),
                    f"{name}: expected a noninterlaced 8-bit RGBA PNG")
        elif kind == b"IDAT":
            compressed.extend(payload)
        elif kind == b"IEND":
            require(size == 0 and end + 4 == len(data), f"{name}: invalid PNG end")
        offset = end + 4
    require(chunks and chunks[0] == b"IHDR" and chunks[-1] == b"IEND"
            and chunks.count(b"IHDR") == 1 and chunks.count(b"IEND") == 1 and compressed,
            f"{name}: incomplete PNG")
    if expected_size:
        require((width, height) == expected_size, f"{name}: expected dimensions {expected_size}")
    try:
        decoder = zlib.decompressobj()
        expected_bytes = height * (1 + width * 4)
        pixels = decoder.decompress(compressed, expected_bytes + 1)
        require(decoder.eof and not decoder.unused_data and not decoder.unconsumed_tail
                and len(pixels) == expected_bytes, f"{name}: invalid PNG image data")
        require(all(pixels[row * (1 + width * 4)] <= 4 for row in range(height)),
                f"{name}: invalid PNG row filter")
    except zlib.error as error:
        raise InvalidPack(f"{name}: invalid PNG image data: {error}") from error


def required(data, name):
    require(name in data, f"Missing required pack file: {name}")
    return data[name]


def validate_models(documents, data):
    for vanilla, custom, cmd in ITEMS:
        base_id = f"celestialdash:item/{vanilla}_base"
        custom_id = f"celestialdash:item/{custom}"
        for model_name, texture in ((custom, custom_id), (f"{vanilla}_base", f"minecraft:item/{vanilla}")):
            name = f"assets/celestialdash/models/item/{model_name}.json"
            model = required(documents, name)
            require(model.get("parent") == "minecraft:item/generated"
                    and isinstance(model.get("textures"), dict)
                    and model.get("textures", {}).get("layer0") == texture,
                    f"{name}: incorrect generated model or texture")
        required(data, f"assets/celestialdash/textures/item/{custom}.png")
        name = f"assets/minecraft/models/item/{vanilla}.json"
        model = required(documents, name)
        require(model.get("parent") == "minecraft:item/generated"
                and isinstance(model.get("textures"), dict)
                and model.get("textures", {}).get("layer0") == f"minecraft:item/{vanilla}"
                and model.get("overrides") == [
                    {"predicate": {"custom_model_data": cmd}, "model": custom_id},
                    {"predicate": {"custom_model_data": cmd + 1}, "model": base_id}],
                f"{name}: legacy CustomModelData rules or vanilla fallback are broken")
        name = f"{OVERLAY}/assets/minecraft/items/{vanilla}.json"
        model = required(documents, name).get("model")
        fallback = {"type": "minecraft:model", "model": base_id}
        require(isinstance(model, dict)
                and model.get("type") == "minecraft:range_dispatch"
                and model.get("property") == "minecraft:custom_model_data"
                and model.get("index", 0) == 0
                and model.get("entries") == [
                    {"threshold": cmd, "model": {"type": "minecraft:model", "model": custom_id}},
                    {"threshold": cmd + 1, "model": fallback}]
                and model.get("fallback") == fallback,
                f"{name}: modern CustomModelData rules or vanilla fallback are broken")

    def check_references(value, name):
        if isinstance(value, dict):
            for key, child in value.items():
                if key in ("model", "parent") and isinstance(child, str):
                    check_resource(child, "models", ".json", name)
                elif key == "textures" and isinstance(child, dict):
                    for texture in child.values():
                        if isinstance(texture, str) and not texture.startswith("#"):
                            check_resource(texture, "textures", ".png", name)
                else:
                    check_references(child, name)
        elif isinstance(value, list):
            for child in value:
                check_references(child, name)

    def check_resource(resource, folder, extension, name):
        require(RESOURCE_ID.fullmatch(resource) and ".." not in resource.split("/")
                and ":/" not in resource, f"{name}: invalid resource location {resource!r}")
        namespace, location = resource.split(":", 1)
        if namespace != "minecraft":
            required(data, f"assets/{namespace}/{folder}/{location}{extension}")

    for name, document in documents.items():
        if name != "pack.mcmeta":
            check_references(document, name)


def collect_source(source):
    require(source.is_dir() and not source.is_symlink(), f"Missing or linked pack source: {source}")
    data, documents = {}, {}
    for path in sorted(source.rglob("*")):
        require(not path.is_symlink(), f"Symlinks are not allowed in the pack: {path}")
        require(path.resolve().is_relative_to(source.resolve()), f"Pack path escapes source directory: {path}")
        if path.is_dir():
            continue
        name = path.relative_to(source).as_posix()
        require(path.is_file() and path.resolve().is_relative_to(source.resolve()),
                f"Invalid pack source path: {path}")
        allowed = name in ("pack.mcmeta", "pack.png", "README.md") or (
            (name.startswith("assets/") or name.startswith(f"{OVERLAY}/assets/"))
            and path.suffix in (".json", ".png"))
        require(allowed and "\\" not in name and not any(part.startswith(".") for part in PurePosixPath(name).parts),
                f"Unexpected file in pack source: {name}")
        contents = path.read_bytes()
        require(contents, f"Empty pack file: {name}")
        if path.suffix in (".json", ".mcmeta", ".md"):
            try:
                contents = contents.decode("utf-8").replace("\r\n", "\n").replace("\r", "\n").encode("utf-8")
            except UnicodeError as error:
                raise InvalidPack(f"{name}: invalid UTF-8 text") from error
        data[name] = contents
        if path.suffix in (".json", ".mcmeta"):
            documents[name] = json_object(contents, name)
        elif path.suffix == ".png":
            validate_png(contents, name, (32, 32) if name.startswith("assets/celestialdash/textures/item/") else None)
    metadata = required(documents, "pack.mcmeta")
    required(data, "pack.png")
    pack = metadata.get("pack")
    validate_range(pack, "supported_formats", MIN_FORMAT, MAX_TESTED_FORMAT, "pack")
    require(type(pack.get("pack_format")) is int and pack["pack_format"] == MIN_FORMAT[0],
            "pack.pack_format must remain 15 for Minecraft 1.20")
    require(isinstance(pack.get("description"), (str, dict, list)) and pack["description"],
            "pack.description is required")
    require(isinstance(metadata.get("overlays"), dict), "Missing overlays object")
    overlays = metadata["overlays"].get("entries")
    require(isinstance(overlays, list) and len(overlays) == 1,
            f"Expected one {OVERLAY} overlay declaration")
    overlay = overlays[0]
    require(isinstance(overlay, dict) and overlay.get("directory") == OVERLAY,
            f"Modern item definitions must use the {OVERLAY} overlay")
    validate_range(overlay, "formats", MODERN_FORMAT, MAX_TESTED_FORMAT, "overlay")
    require(format_version(overlay["min_format"]) == MODERN_FORMAT,
            "Modern item definitions must start at format 46")
    validate_models(documents, data)
    return data


def package(output, source=SOURCE):
    require(not source.is_symlink(), f"Linked pack source is not allowed: {source}")
    source = source.resolve()
    output = output.absolute()
    require(output.suffix.lower() == ".zip", "Output path must end in .zip")
    require(not output.is_symlink(), "Output must not be a symlink")
    require(not output.resolve().is_relative_to(source), "Output must be outside the pack source directory")
    data = collect_source(source)
    output.parent.mkdir(parents=True, exist_ok=True)
    descriptor, temporary = tempfile.mkstemp(prefix=f".{output.name}.", suffix=".tmp", dir=output.parent)
    try:
        with os.fdopen(descriptor, "w+b") as stream:
            with zipfile.ZipFile(stream, "w", compression=zipfile.ZIP_STORED) as archive:
                for name, contents in sorted(data.items()):
                    entry = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
                    entry.create_system = 3
                    entry.external_attr = 0o100644 << 16
                    entry.compress_type = zipfile.ZIP_STORED
                    archive.writestr(entry, contents)
        with zipfile.ZipFile(temporary) as archive:
            require(archive.namelist() == sorted(data) and archive.testzip() is None,
                    "Generated ZIP failed its integrity check")
        os.replace(temporary, output)
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)
    return len(data), hashlib.sha1(output.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output", type=Path, help="destination ZIP (outside the unpacked source)")
    args = parser.parse_args()
    try:
        count, sha1 = package(args.output)
    except (InvalidPack, OSError, zipfile.BadZipFile) as error:
        print(f"Resource pack packaging failed: {error}", file=sys.stderr)
        return 1
    print(f"Packaged {count} files: {args.output}\nSHA-1: {sha1}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
