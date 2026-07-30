# CelestialDash Resource Pack

This optional pack gives Celestial Tears and Celestial Amulets custom 32x32 pixel-art textures. It changes only the two configured `CustomModelData` values, so normal Ghast Tears and Nautilus Shells remain vanilla.

## Supported clients

- Minecraft 1.20 through 1.21.3: legacy item-model overrides.
- Minecraft 1.21.4 through resource-pack format 88: modern item definitions in the `v1214` overlay.

## Install

1. Zip the **contents** of this directory, so `pack.mcmeta`, `pack.png`, `assets/`, and `v1214/` are at the root of the ZIP.
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
- Tears created with another `tear-custom-model-data` value are not valid after changing the value. Reissue them after switching to `22001`.
- Existing Amulets stay functional but keep the vanilla shell appearance until they are recreated with `22002`.
- `22001` and `22002` are reserved by this pack. If another pack replaces these vanilla item-model files, combine its model rules with this pack instead of changing pack order blindly.
