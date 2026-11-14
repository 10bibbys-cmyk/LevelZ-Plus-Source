# LevelZ+ — Expanded Progression & Compatibility

**LevelZ+** is a modular expansion and compatibility suite for **LevelZ** on Minecraft **1.20.1 Fabric**.

It extends LevelZ skills into additional gameplay systems, improves independent progression XP for multiplayer, and provides optional compatibility with popular mods. LevelZ is the only required *gameplay* mod; external integrations are inactive when their corresponding mods are missing.

Version **2.1.3.1** retains the fully optional **JobsAddon 1.1.6** compatibility to the shared XP, progression and stat features from previous releases. All JobsAddon compatibility toggles default to **off**.

---

## Expanded LevelZ Skill Integration

LevelZ+ connects LevelZ skills to additional attributes and interactions. Bonuses are configurable through `config/levelz_plus.properties`; setting a per-level bonus to `0` effectively disables it.

**Stamina / Strength**
- Maximum stamina and regeneration through Stamina Attributes.
- Reduced melee stamina costs through Better Combat Extension, based on Strength.
- Increased food effectiveness from Stamina.

**Health**
- Faster passive regeneration through Passive Regen, based on the LevelZ Health skill.

**Agility**
- Greater Combat Roll distance and faster recharge.
- Additional roll charges at configurable skill thresholds.

**Luck**
- Virtual Fortune applied to eligible block loot.
- Improved **vanilla Minecraft XP** gains.
- Critical-hit chance via Zenith Attributes.

**Archery**
- Improved draw speed through Zenith Attributes.

**Trade**
- More Numismatic Overhaul coins from supported Bountiful bounty rewards.

**Alchemy**
- Increased maximum mana and mana regeneration through RPG Mana.

**Important:** Luck's XP bonus affects *ordinary Minecraft experience*, not LevelZ+'s directly granted independent progression XP. The two systems remain separate.

---

## Cooperative LevelZ Progression XP

LevelZ+ can award LevelZ independent progression XP from combat without changing vanilla experience earned for enchanting or other mechanics.

When the optional custom combat XP system is enabled:
- Every player who successfully damages a mob can receive **their own full configured reward**. XP is not split by damage dealt, and the killing blow is not required.
- Contributions persist if the damaged mob is unloaded and later reloaded.
- To receive XP, a contributor must remain **alive and continuously connected** from their first successful hit until the mob dies.
- Dying or disconnecting forfeits that mob's reward, even if the player reconnects or damages it again. No offline reward queue exists.
- Player-owned projectiles, TNT and tamed entities can contribute to attribution.
- `doMobLoot` and LevelZ's spawner-mob XP setting are respected.
- Ender Dragon multipart damage can contribute to optional shared Dragon XP.

### Mob XP calculation

The configurable default formula is:

```text
base mob XP × mob multiplier + mob maximum health × health fraction
```

Defaults:

```properties
levelz_xp.combat.mob_xp_multiplier=1.0
levelz_xp.combat.max_health_fraction=0.10
levelz_xp.combat.reward_percent_per_contributor=100
```

LevelZ's optional level-based multiplier may also apply. Each eligible player receives their own calculated reward, with at least 1 progression XP when the final calculated reward is positive.

### Avoiding duplicated progression XP

**Custom combat XP is disabled by default.** To use shared LevelZ+ mob XP, use:

In LevelZ's `config/levelz.json5`:

```json5
useIndependentExp: true,
mobXPMultiplier: 0.0,
```

In `config/levelz_plus.properties`:

```properties
levelz_xp.combat.enabled=true
```

For shared **Ender Dragon** XP, also set LevelZ's native `dragonXPMultiplier: 0.0`, and configure `levelz_xp.combat.dragon_xp_multiplier` in LevelZ+ (default `0.5`). If native Dragon XP is active, LevelZ+ skips its own shared Dragon award.

These settings are not changed automatically. LevelZ+ logs configuration warnings for conflicting or missing XP routes. Existing values are preserved when upgrading.

---

## Farming & Harvesting XP

An optional **Actually Harvest** integration can grant independent LevelZ progression XP from successful supported right-click crop harvesting. Vanilla Minecraft XP remains separate.

The feature and its XP scaling are configurable. JobsAddon Farmer XP, described below, is a separate opt-in compatibility feature.

---

## Optional JobsAddon 1.1.6 Integration

New in **2.1.3**, LevelZ+ can integrate with JobsAddon while leaving native JobsAddon behaviour unchanged on a fresh install.

Individual opt-in features include:
- **Lumberjack:** XP for successfully mined Felled / Zarith falling-tree log cells.
- **Farmer:** XP for Actually Harvest crops, eligible mature modded crops, and Farmer's Delight Cooking Pot or Skillet results.
- **Fisher:** XP for successfully caught Tide fish.
- **Smither:** XP fallback for supported additional modded equipment.
- **Builder:** XP fallback when eligible placed blocks are missed by native JobsAddon.
- **Brewer:** XP after successful Runic Attunement inscription.
- **Profession control:** Optionally enrol all eight professions, prevent quitting, and show a compact alternative Jobs screen header.
- **Jobs XP correction:** An optional fix for XP carry-over when a job increases rank.

**All JobsAddon flags default to `false`.** JobsAddon itself is not required.

To opt in, enable `jobsaddon.enabled=true` together with the individual desired `jobsaddon.*` toggles. For example:

```properties
jobsaddon.enabled=true
jobsaddon.actually_harvest_bridge_v2=true
jobsaddon.felled_lumberjack_bridge=true
```

Felled axe protection is a separate opt-in setting, `felled.protect_fallen_logs=false` by default. It does not apply on fresh installations.

The integrations use JobsAddon's own configured XP rates wherever supported. They do not automatically change your job-level curves, reward settings, or modpack datapacks.

---

## Other Optional Compatibility Modules

**Tom's Simple Storage**  
Applies LevelZ crafting requirements inside the Crafting Terminal so restricted recipes cannot bypass progression.

**Better Combat**  
Enforces LevelZ item-use restrictions on Better Combat attack requests, including off-hand attacks.

**Spell Engine**  
Checks the active casting weapon against LevelZ item restrictions and blocks restricted spell synchronization and release.

**Small Ships — Dockyard**  
Checks the selected ship's LevelZ crafting requirement before construction begins, including its wood variant. Ship restrictions are read from normal LevelZ rules rather than hard-coded in LevelZ+. Optional item-ID mappings support addon vessels.

**Additional stat and reward systems**  
Stamina Attributes, RPG Mana, Better Combat Extension, Combat Roll, Passive Regen, Diet, Zenith Attributes, Bountiful, Numismatic Overhaul, XP Storage, and Actually Harvest have targeted optional integrations.

Each optional module uses its configured toggle and installed-mod checks. Compatibility is built for specific Minecraft 1.20.1-era versions; arbitrary later upstream versions are not guaranteed.

---

## Configuration

LevelZ+ generates its annotated configuration at:

`config/levelz_plus.properties`

Settings include stat multipliers, optional modules, independent XP rewards, Dockyard rules, and JobsAddon bridges. The configuration is non-destructively migrated: existing values remain intact and newly supported keys are appended with defaults.

Configuration edits require a restart.

The config also supports detailed debug logging when diagnosing optional compatibility issues. Disable diagnostics during normal gameplay.

---

## Multiplayer

LevelZ+ runs in singleplayer and on dedicated servers. Its gameplay restrictions are enforced server-side, including when optional mods implement their actions through custom networking.

The custom combat XP system rewards each eligible contributor separately, making progression cooperative instead of last-hit-dependent. For multiplayer, install the mod on **both client and server** to retain the client-side compatibility and tooltip features.

---

## Requirements

**Required**
- **Minecraft:** 1.20.1
- **Mod loader:** Fabric Loader **0.16.14 or newer**
- **Fabric API:** Required
- **LevelZ:** **1.4.13** (verified target; no broader-version guarantee)
- **Java:** 17 or newer
- **Environment:** Client and server

**Optional integration targets** (examples of verified or compiled-against Minecraft 1.20.1 versions; not hard dependencies):

| Mod | Integration |
|---|---|
| Tom's Simple Storage 1.7.1 | Crafting Terminal restrictions |
| Better Combat 1.8.6+1.20.1 | Server-side item-use enforcement |
| Spell Engine 0.15.12+1.20.1 | Casting restrictions |
| Small Ships 2.0.1 | Dockyard construction checks |
| Zenith Attributes 1.0.3 | Critical chance / draw speed |
| Stamina Attributes 1.6.0 | Stamina and regeneration |
| RPG Mana 1.1.0 | Maximum mana and mana regeneration |
| Better Combat Extension 1.4.0 | Melee stamina costs |
| Combat Roll 1.3.3+1.20.1 | Agility bonuses |
| Passive Regen 1.3.3+1.20.1-fabric | Health regeneration |
| Diet 3.0.2+1.20.1 | Diet gain |
| Bountiful 6.0.4+1.20.1 | Coin rewards |
| Numismatic Overhaul 0.2.18+1.20 | Currency support |
| Actually Harvest 1.1.4.0 | Harvesting XP |
| JobsAddon 1.1.6 | Optional profession compatibility |
| Farmer's Delight, Tide, Runic Attunement, Felled and Zarith | Additional opt-in JobsAddon bridges |
| XP Storage | Protection against XP withdrawal bonus recycling |

Optional mods are not required for LevelZ+ to load. Follow their own dependency and version requirements when installing them.

---

## Installation

1. Install Fabric Loader and Fabric API for Minecraft 1.20.1.
2. Install LevelZ 1.4.13 and any optional integrations you want.
3. Install the LevelZ+ JAR in your `mods` folder on the client and server.
4. Remove older LevelZ+ versions before starting the game.
5. Let `config/levelz_plus.properties` generate or update with missing keys, then configure your desired modules.
6. Restart after changing configuration.

**Version 2.1.3.1** includes the contributor-sharing rules introduced in 2.1.2 and the optional JobsAddon compatibility features. This maintenance update optimises periodic attribute synchronisation by leaving already-correct modifiers unchanged, without altering XP rewards, progression balance or optional integrations.

**One mod. More meaningful progression. Fully configurable.**

## Source Code and Licensing

The **LevelZ+ 2.1.3.1** source is publicly available for private inspection and verification on [GitHub](https://github.com/10bibbys-cmyk/LevelZ-Plus-Source).

The independently owned original code is **source-available, All Rights Reserved**, not conventionally open-source licensed. Private, non-commercial local compilation and inspection are permitted under the [source notice](https://github.com/10bibbys-cmyk/LevelZ-Plus-Source/blob/main/LICENSE). Redistribution, derivative publication and incorporation into other distributed projects require permission, except where applicable law or third-party licence terms grant rights. The upstream LevelZ project and all optional integrations retain their own copyright and licensing terms.

**AI-Assisted Development Disclosure:** This mod was written with AI assistance. The implementation has been reviewed, tested and refined by its developer for functionality, compatibility and performance.
