# LevelZ+ Changelog

## 2.1.3.1+mc1.20.1 — Stable Maintenance

- Reduced unnecessary server-side attribute modifier updates: the five-second safety refresh now skips modifiers that already have the correct amount and operation.
- Corrected public documentation to cover 2.1.3 features, multiplayer combat XP, JobsAddon integrations, Fabric Loader 0.16.14+ and the distinction between vanilla XP and independent LevelZ XP.
- Adopted the four-part Path of the Ascended version scheme and published an independently inspectable source snapshot.
- **No XP/reward balancing, level requirements, modifier UUIDs, config defaults, JobsAddon, Tide or Felled gameplay changes.** The tested 2.1.3.1a code is preserved.

## 2.1.3+mc1.20.1 — Optional JobsAddon integration

- Added independently configurable JobsAddon 1.1.6 compatibility for Felled logs, Actually Harvest, modded crops, Farmer's Delight cooking, Tide fishing, Runic Attunement brewing, Builder placements and extra modded Smither equipment.
- Optional employment of all eight professions, job locking, Jobs screen copy and rank XP-overflow correction.
- All JobsAddon integration flags and Felled protection default to off; existing configurations retain their values.
- Kept cooperative 2.1.2 combat XP and existing LevelZ stat balance unchanged.

## 2.1.2+mc1.20.1 — Shared Combat XP Update

- Every surviving, continuously connected player who successfully damages an enemy receives their **own full configured LevelZ progression XP reward** when it dies. No damage thresholds or killing-blow requirement.
- Contributions are tracked once per player, persist when mobs unload, and have no expiration or contributor-count limit.
- **Survival and connection requirement:** dying or disconnecting at any point after the first hit permanently forfeits that mob's reward. No XP is queued for offline players.
- Improved player attribution for melee, projectiles, TNT and tamed entities. Added Ender Dragon multipart damage tracking.
- Added optional shared Dragon XP with a separate multiplier. Requires disabling LevelZ's native Dragon progression XP to avoid duplicates.
- Kept configurable mob base-XP multiplier **and** bonus based on maximum mob health.
- Preserved independent LevelZ progression, the `doMobLoot` gamerule and LevelZ's spawner exclusion.
- Added diagnostics for missing independent XP settings and potential double native XP.
- Updated generated config: removed obsolete contributor timeout/cap options, documents the Dragon multiplier, defaults new installations to **opt-in shared XP** to avoid double payouts. Existing config values are never overwritten.
- Removed project-specific wording and test markers from public metadata, configuration and documentation.
- Retained the existing stat integrations and optional compatibility modules.

## 2.1.1+mc1.20.1 — Maintenance
- Resolved combat progression XP compatibility issues from 2.1.0 and refined configuration compatibility.
- Preserved the existing LevelZ+ skill bonuses and integrations.

## 2.1.0+mc1.20.1 — Independent Progression XP
- Introduced multiplayer contributor-based independent LevelZ XP, with normal Minecraft XP unaffected.
- Added configurable health-based mob XP bonuses.
- Added optional independent XP integration for Actually Harvest.
- Added startup diagnostics for progression XP configuration.

## 2.0.x — Compatibility and Configuration
- Expanded skill stat integrations and compatibility with storage, combat, spellcasting, ships, stamina, mana, food and rewards.
- Stabilised optional mixins and config migration for Fabric 1.20.1.
