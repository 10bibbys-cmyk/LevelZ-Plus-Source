# Optional JobsAddon Integration (Fabric 1.20.1)

LevelZ+ **2.1.3** supports JobsAddon **1.1.6** as an optional dependency.
A fresh installation does **not** change JobsAddon mechanics: every JobsAddon-related
feature and Felled log protection is **off by default**. Existing config keys
are preserved on upgrade, so explicitly enabled beta/test options stay enabled
until you change them.

## Configure

Start once to generate `config/levelz_plus.properties`. Restart after editing.
To use any JobsAddon integration set `jobsaddon.enabled=true` on both sides.
Then enable only the features you want. The server governs rewards; matching
client settings govern the Jobs screen.

| Property | Default | Effect |
|---|---|---|
| `jobsaddon.enabled` | false | Master JobsAddon integration switch |
| `jobsaddon.auto_employ_all` | false | Auto-enrol all eight JobsAddon jobs at login, set job count to eight and disable cooldown |
| `jobsaddon.lock_all_jobs` | false | Keep all eight jobs enrolled and block quitting |
| `jobsaddon.custom_jobs_screen_header` | false | When jobs are locked, show an 'all professions active' hint instead of employment/cooldown text |
| `jobsaddon.fix_xp_overflow` | false | Correct JobsAddon XP-to-next-rank carry-over |
| `jobsaddon.builder_placement_bridge` | false | Award eligible Builder XP when native placement hook misses |
| `jobsaddon.actually_harvest_bridge_v2` | false | Give Farmer XP on successful Actually Harvest right-click crop harvests |
| `jobsaddon.modded_crop_farmer_bridge` | false | Fallback Farmer XP for mature modded crops broken normally |
| `jobsaddon.farmers_delight_cooking_bridge` | false | Cooking Pot and Skillet Farmer XP |
| `jobsaddon.tide_fisher_bridge` | false | Fisher XP for Tide fishing catches |
| `jobsaddon.expanded_gear_smither_bridge` | false | Smither XP for supported non-vanilla equipment crafting |
| `jobsaddon.runic_attunement_brewer_bridge` | false | Brewer XP for successful Runic Attunement inscriptions, including Creative testing |
| `jobsaddon.felled_lumberjack_bridge` | false | Lumberjack XP on manual mining of Felled's falling-tree log cells |
| `felled.protect_fallen_logs` | false | Independent Felled log-cell axe protection; explicitly opt in |
| `jobsaddon.debug_actually_harvest` | false | Diagnostic harvest logs |
| `jobsaddon.debug_farmers_delight_cooking` | false | Diagnostic Cooking Pot logs |
| `jobsaddon.debug_builder` | false | Diagnostic placement logs |
| `jobsaddon.debug_runic_attunement` | false | Diagnostic attunement logs |

The `jobsaddon.*` switches do **not** change vanilla JobsAddon XP rates,
job-level costs, job rewards or datapack overrides. Configure those in the
JobsAddon config and datapacks. All eight professions are **only** forced
active when opting in to the employment/lock features.

## Mod availability

- JobsAddon is not a required dependency. With it absent or
  `jobsaddon.enabled=false`, JobsAddon-specific classes, callbacks and mixins
  are inactive.
- Each compatibility bridge also checks for its required third-party mod.
- Felled/Zarith cell hooks are inactive unless Felled protection is enabled,
  or the Felled+JobsAddon Lumberjack bridge is active.
- The replacement Jobs screen header only applies if the JobsAddon integration
  and both `jobsaddon.lock_all_jobs` and `jobsaddon.custom_jobs_screen_header`
  are enabled. Otherwise JobsAddon's standard header remains unchanged.
- Custom profession tooltip text belongs in a resource pack under
  `assets/jobsaddon/lang/en_us.json` (and other languages as desired).
  Language keys are not changed globally by the LevelZ+ JAR.

## Modpack config merging

If you already have `config/levelz_plus.properties`, **do not replace the whole
file** to enable JobsAddon. Merge just the relevant properties; replacing it
would reset independent progression and other LevelZ+ settings.

For Path of the Ascended, use the separately distributed example opt-in
properties fragment together with the JobsAddon config and KubeJS data.
That profile is an example, not LevelZ+'s public default.

## Logs and diagnostics

You can switch individual `jobsaddon.debug_*` properties to `true` when
diagnosing XP awards; leave them `false` in release builds. The client and
server must restart for changes. Always remove older LevelZ+ JARs before
installing a new one.
