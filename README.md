# LevelZ+

**LevelZ+ 2.1.3.1 (stable)** expands [LevelZ](https://modrinth.com/mod/levelz) with configurable skill bonuses, optional mod integrations, and cooperative LevelZ progression XP for Minecraft **1.20.1 / Fabric**.

Fabric Loader **0.16.14+**, LevelZ **1.4.13** and **Java 17** are required. Other supported integrations activate only when their target mods are installed.

## Features

- **Shared combat LevelZ XP:** When enabled, each player who successfully damages an enemy receives the complete configured progression XP reward if they stay alive and connected until the enemy dies. Damage dealt and the killing blow do not divide rewards.
- **Health-aware mob rewards:** Configurable mob XP multiplier and bonus based on the defeated enemy's **maximum health**. Positive calculated rewards have a one-point minimum.
- **Persistent contributor tracking:** Combat contributions survive mob chunk unloading. A player who dies or disconnects after their first hit forfeits that mob's reward, even after respawning or reconnecting. No offline payout queue.
- **Optional dragon reward sharing:** Uses its own configurable dragon multiplier. See the native LevelZ setting below to prevent duplicate progression XP.
- **LevelZ attribute improvements:** Configurable Endurance, Agility, Luck, Archery, Trade and Alchemy bonuses.
- **Mod compatibility:** Optional integrations cover Tom's Simple Storage, Better Combat, Spell Engine, Small Ships, Actually Harvest, Combat Roll, stamina and mana systems, Diet, Passive Regen, Bountiful and other supported mods.
- **Optional JobsAddon integration (new in 2.1.3):** Opt-in XP compatibility with Felled, Actually Harvest, Farmer's Delight, Tide, Runic Attunement, Builder placement and modded equipment; optional full profession employment, lockable jobs and a compact all-active Jobs screen header. All JobsAddon integration flags default to **false**. [Full settings and examples](docs/JOBSADDON_INTEGRATION.md).

Normal Minecraft experience and LevelZ independent progression experience are separate. **Luck's XP bonus affects normal Minecraft XP, not directly awarded independent LevelZ XP.**

## Installation

1. Install Fabric Loader for Minecraft 1.20.1, Java 17, and LevelZ 1.4.13.
2. Place the `levelz-plus-2.1.3.1+mc1.20.1.jar` in your `mods/` folder. Remove older LevelZ+ JARs.
3. Launch the game once. The mod creates `config/levelz_plus.properties` if it does not exist. Existing properties files are preserved and receive only missing configuration keys.
4. Restart the game or server after editing settings.

## Optional JobsAddon 1.1.6 integration (off by default)

JobsAddon itself is **not required**. LevelZ+ 2.1.3 and later add individual optional
bridges for jobs XP from modded harvesting, cooking, crafting, building, fishing,
Runic Attunement and Felled. All integrations are disabled on fresh installs.

To use one, set `jobsaddon.enabled=true` and enable its individual
`jobsaddon.*_bridge` toggle in `config/levelz_plus.properties`.
To force all eight jobs always active, opt into `jobsaddon.auto_employ_all`
and `jobsaddon.lock_all_jobs`. To replace the now-irrelevant Jobs screen
employment labels with compact hover help, additionally enable
`jobsaddon.custom_jobs_screen_header` on clients.

Existing true/false config values are preserved on upgrade. Configuring
JobsAddon XP rates and progression curves is separate from enabling the
LevelZ+ compatibility bridges. See [JobsAddon configuration](docs/JOBSADDON_INTEGRATION.md).

## Shared combat XP configuration (opt-in)

**Fresh installations leave shared combat XP disabled** to avoid doubling LevelZ's native mob XP. To enable it:

In `config/levelz.json5`, set:

```json5
useIndependentExp: true,
mobXPMultiplier: 0.0,
```

In `config/levelz_plus.properties`, set:

```properties
levelz_xp.combat.enabled=true
levelz_xp.combat.reward_percent_per_contributor=100
levelz_xp.combat.mob_xp_multiplier=1.0
levelz_xp.combat.max_health_fraction=0.10
```

The default bonus formula for ordinary mobs is `base XP × configured multiplier + maximum health × configured fraction`. The result is scaled by LevelZ's optional level-based XP modifier and the contributor percentage, then converted into integer LevelZ XP. A positive calculated reward grants at least 1 XP.

You can use a different mob multiplier to match your preferred progression balance.

To **also** enable shared Ender Dragon XP, set `dragonXPMultiplier: 0.0` in LevelZ and set `levelz_xp.combat.dragon_xp_multiplier` to the desired multiplier in LevelZ+ (default `0.5`). If native dragon progression XP is nonzero, the shared dragon path is skipped to prevent duplicates.

The survival requirement begins on the player's first successful damaging hit. There is no contribution timeout or player cap. Players must remain alive and continuously connected until the enemy dies; disconnecting or dying permanently disqualifies them for that enemy. The vanilla gamerule `doMobLoot` and LevelZ's spawner XP restriction are respected.

## Configuration and compatibility

The full, annotated `levelz_plus.properties` is generated automatically. You do not need to install a modpack-specific LevelZ configuration. A sample and additional guidance are included in the release package.

If you upgrade from 2.1.1 or 2.1.2, keep your existing config. Previously configured values are preserved. The obsolete `levelz_xp.combat.contribution_timeout_seconds` and `levelz_xp.combat.max_contributors` lines are ignored in 2.1.2 and can be removed manually.

**Notes:** This mod does not replace LevelZ or manage other mods' native XP without the settings above. Mods with unusual damage-source attribution may need additional compatibility patches.

## Support

Source code and issue tracker: https://github.com/10bibbys-cmyk/LevelZ-Plus-SourcePlus-Source

Mod ID: `levelz_plus` · Licence: All Rights Reserved

## Version 2.1.3.1

This maintenance release reduces unnecessary server-side attribute updates. Every five seconds, the normal safety synchronisation checks each known modifier's UUID, amount and operation. Unchanged modifiers remain in place; changed or missing modifiers are updated and obsolete modifiers removed. Skill scaling and XP calculations are unchanged. The opt-in JobsAddon, Felled and Tide integrations work as before.

**Public source:** https://github.com/10bibbys-cmyk/LevelZ-Plus-SourcePlus-Source. Original addon material is source-available, All Rights Reserved, subject to the explicit private-inspection permissions in LICENSE. Third-party rights remain with the respective mod authors.
