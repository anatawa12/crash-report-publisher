Crash Report Publisher
===

Publishes the crash report of minecraft to some chat tools.

### Supported versions

Minecraft since 1.6.4 until 1.12.2 with MinecraftForge.

<details>
<summary>Why since 1.6.4 until 1.12.2</summary>

the versions which

- Launched with [launch wrapper](https://github.com/Mojang/LegacyLauncher)
- FML has ITweaker loader with `TweakClass` Manifest
- FML has ITweaker sorting configuration with `TweakOrder` Manifest

</details>

### How to use

1. Download the latest jar from [releases]
1. Add into `mods` directory
1. Create `crash-report-publisher.properties` in `config` directory like shown below
   ```properties
   service-kind=discord
   hook-url=<webhook url>
   ```

   You can get webhook url from `integrations` in channel settings or server settings.
1. Now you can get and see the crash report on Discord.

[releases]: https://github.com/anatawa12/crash-report-publisher/releases/latest
