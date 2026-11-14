# LevelZ+ 2.1.3.1 — Stable Maintenance Release

**Minecraft 1.20.1 · Fabric · Java 17**

## Changes

- **Performance maintenance:** LevelZ+ no longer removes and recreates attribute modifiers that already have the correct value and operation. The five-second periodic safety refresh is retained.
- Updated the public description to cover the 2.1.3 JobsAddon adapters, cooperative progression XP, verified dependencies, and the distinction between vanilla and LevelZ experience.
- Aligned the version number with the new four-part Path of the Ascended release format.
- Published independently inspectable source with All Rights Reserved notices and upstream attribution.

## Unchanged

- Skill stat bonuses, modifier UUIDs, attribute operation types, XP formulas, shared contributor and Dragon XP rules, and config defaults.
- JobsAddon, Felled, Tide, harvesting, crafting, combat restrictions and all optional integrations.
- Existing worlds and `config/levelz_plus.properties` are compatible; no migration or reset is required.

## Requirements

Minecraft **1.20.1**, Fabric Loader **0.16.14+**, LevelZ **1.4.13**, Java **17+**; use Fabric API and additional optional dependencies for their corresponding integrations. Install on both client and server.

The 2.1.3.1a candidate passed Java 17 CI; the modpack maintainer verified it in-game without crashes or observed regressions. Stable 2.1.3.1 removes the alpha designation without additional gameplay changes.

**Public source:** https://github.com/10bibbys-cmyk/LevelZ-Plus-Source
