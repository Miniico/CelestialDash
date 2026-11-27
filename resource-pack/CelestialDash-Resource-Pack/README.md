# CelestialDash Resource Pack

This optional pack gives Celestial Tears and Celestial Amulets custom 32x32 pixel-art textures. It changes only the two configured `CustomModelData` values, so normal Ghast Tears and Nautilus Shells remain vanilla.

## Supported clients

- Minecraft 1.20 through 1.21.3: legacy item-model overrides.
- Minecraft 1.21.4 through 26.3 (resource-pack format 97.1): modern item definitions in the `v1214` overlay.

`pack.mcmeta` retains `pack_format`, `supported_formats`, and overlay `formats` for older clients. Modern clients use `min_format` and `max_format`, including the explicit `[97, 1]` upper bound. These fields follow the [official metadata schema](https://www.minecraft.net/en-us/article/minecraft-snapshot-25w31a); the [26.3 release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3) specify format 97.1.

Custom models were observed in clients 1.20 and 26.3 during the [crafting validation](../../docs/crafting-validation-2026-11-24.md). The 26.3 test used a separate modern-only ZIP; this unified source preserves those textures and item definitions within its modern overlay. The regenerated unified ZIP has been checked structurally and reproducibly; that is not a fresh client download or visual test of this exact archive. Intermediate client versions have not each been tested.

## Build

From the repository root, using Python 3.9 or newer:

```sh
python scripts/package_resource_pack.py target/CelestialDash-Resource-Pack.zip
```

The unpacked directory is the authoritative source. The script validates metadata, both model formats, vanilla fallbacks, texture references and PNG integrity before writing the ZIP. It normalizes text line endings, sorts entries, and fixes timestamps and file permissions. Entries use ZIP storage without compression so output is independent of the installed zlib version. It prints the generated SHA-1 for the server configuration and replaces an existing output only after successful validation. `pack.mcmeta`, `pack.png`, `assets/`, and `v1214/` are at the archive root.

## Install

1. Build the ZIP with the command above, or download the `CelestialDash-Resource-Pack.zip` asset from the official CelestialDash release.
2. Give that ZIP to players, or host it through your server resource-pack configuration.
3. In `plugins/CelestialDash/config.yml`, use the pack's reserved values:

   ```yml
   tear-custom-model-data: 22001

   celestial-amulet:
     custom-model-data: 22002
   ```

4. Run `/celestialdash reload`, then issue new Tears with `/celestialdash give` and craft or reissue Amulets.

## Notes

- The resource pack only changes appearance; it cannot create plugin items or read their Persistent Data Container markers.
- Tears created with another `tear-custom-model-data` value remain valid after changing the value. Their appearance is not changed retroactively.
- Existing Amulets stay functional but keep the vanilla shell appearance until they are recreated with `22002`.
- `22001` and `22002` are reserved by this pack. If another pack replaces these vanilla item-model files, combine its model rules with this pack instead of changing pack order blindly.
