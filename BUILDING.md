# Build LevelZ+ 2.1.3.1 from source

**Platform:** Minecraft Java **1.20.1**, Fabric, Java **17**.

The source snapshot is the verified stable 2.1.3.1 release, preserving the multiplayer XP, optional JobsAddon and other integrations.

## Prerequisites
- **JDK 17**
- **Gradle 8.10**
- Network access for first-time Gradle dependency resolution

The build targets **LevelZ 1.4.13**, Fabric Loader **0.16.14**, Fabric API **0.92.2+1.20.1** and other optional mod APIs through Modrinth Maven, CurseMaven and the repositories listed in `build.gradle`. Third-party JARs are not committed in the public source repository.

## Commands

```sh
python3 tools/verify_public_source.py
gradle --no-daemon clean build --stacktrace
```

On success, the production JAR is found at:

`build/libs/levelz-plus-2.1.3.1+mc1.20.1.jar`

A separate `-sources.jar` may also be generated. Install **only the production JAR** on both client and dedicated server together with its required dependencies.

## Verification notes
- Fabric metadata version: `2.1.3.1+mc1.20.1`
- Mod ID: `levelz_plus`
- Minecraft: `1.20.1`
- LevelZ: `1.4.13`
- Required Fabric Loader: `0.16.14+`
- Six Mixin configurations exist, including optional JobsAddon and Felled adapters
- Every compiled class must target Java 17 (major version 61)

Gradle compilation and CI checks do not simulate every optional mod setup. The 2.1.3.1a predecessor was tested in-game by the pack maintainer and approved before the stable metadata update.

**Source rights:** `LICENSE` grants private inspection and local verification builds for original LevelZ+ material. It does not grant general redistribution rights. Third-party licences and legal rights remain effective.
