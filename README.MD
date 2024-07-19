# MCP-Resource-Downloader
An alternative to downloading resource files for older versions of Minecraft for MCP (Minecraft-Coder-Pack).<br/>
Downloads directly from Minecraft servers. No middleman, no sketchy website.

## CMD

### Usage:
Normal: `gradlew -p CMD run --args="{insert arguments here}"`<br/>
Print out all Minecraft versions: `gradlew -p CMD run`<br/>

### Arguments:
- `--mcp` [`MCP directory`]
  - The directory of where MCP is (**REQUIRED**)
- `--mcVersion` [`Minecraft version`]
  - The version of Minecraft to download resources (**REQUIRED**)
- `--jars`
  - If jar files should be downloaded (**NOT** required)
- `--client`
  - If the client jar should be downloaded (**REQUIRED** if `--jars` used)
- `--server`
  - If the server jar should be downloaded (**REQUIRED** if `--jars` used)
- `--libraries`
  - If library jars should be downloaded (**NOT** required)
- `--natives`
  - If natives should be downloaded and extracted (**NOT** required)
- `--linux`
  - If Linux natives should be downloaded and extracted (**NOT** required even if `--natives` used)
- `--windows`
  - If Windows natives should be downloaded and extracted (**NOT** required even if `--natives` used)
- `--w32`
  - If Windows 32-bit natives should be downloaded and extracted (**NOT** required even if `--windows` used)
- `--w64`
  - If Windows 64-bit natives should be downloaded and extracted (**NOT** required even if `--windows` used)
- `--osx`
  - If OSX natives should be downloaded and extracted (**NOT** required even if `--natives` used)
- `--resources`
  - If resources (sound, music) should be downloaded (**NOT** required)
- `--forge`
  - Does extra things for Forge, specifically. `1.3.2`, `1.4.7` and `1.5.2` are the only compatible versions at this time.
- `--overwrite`
  - If downloaded files should be overwritten should one already exist (**NOT** required)

### Examples:
`gradlew -p CMD run --args="--mcp mcp62 --mcVersion 1.2.5 --jars --client --libraries --natives --resources --overwrite"`<br/>

`gradlew -p CMD run --args="--mcp forge-1.4.7-6.6.2.534-src/forge/mcp --mcVersion 1.4.7 --jars --client --server --libraries --natives --linux --windows --osx --resources --forge --overwrite"`

`gradlew -p CMD run --args="--mcp mcp811 --mcVersion 1.6.4 --jars --client --libraries --natives --resources --overwrite"`<br/>

### Troubleshooting:
- I keep getting error `FileNotFoundException` `MCP directory "XXXXXX" does not exist!`!
  - Make sure when inputting the `--mcp` argument you're using either a relative location to the `CMD` folder
  (if MCP folder is in root directory -where `build.gradle` is-, use `../<MCP directory>`),
  or an exact location of where the MCP folder is (ex: `C:\Users\User\Downloads\<MCP directory>`).

## GUI

Easier to use, however more limited than CMD.

### Usage:
`gradlew -p GUI run`

## Supported versions:
- [X] 1.19.2
- [X] 1.12
- [X] 1.8
- [X] 1.7.10
- [X] 1.6.4
- [X] 1.5.2 (Forge)
- [X] 1.4.7 (Forge)
- [X] 1.3.2 (Forge)
- [X] 1.2.5 (Forge)
- [X] 1.2.3 (Forge)

## Known Issues:
- Server jar can't decompile
  - This is due to Mojang hotfixing server jars years later. This is an MCP problem.
- Certain versions aren't working
  - Create a new issue with the specific version (both Minecraft and MCP) you're having troubles with.
- Mac OS (OSX) users may not have natives extracted
  - Create a new issue. I don't have a Mac, so this cannot be tested.

Found an issue? Create a new GitHub issue.

## Workarounds:
- 1.8+
  - Starting the client throws error `Can't find the resource index file: assets/indexes/1.8.json`
    - Another MCP problem. In directory `<MCP Dir>/jars/assets/indexes`, rename file `<version>.json` (ex: `1.12`) to `1.8.json`
- 1.6+
  - MCP can't decompile because it tries copying launcher files
    - Manually run the decompile script with
    `runtime/bin/python/python_mcp runtime/decompile.py --nocopy --client` (Windows: `runtime\bin\python\python_mcp.exe runtime\decompile.py --nocopy --client`)
    (in MCP directory) after running MCP-RD in client mode.
- Mac OS (OSX) users may not have natives extracted
  - Manually extract jars files

## Dependencies:
[Moshi 1.15.1](https://github.com/square/moshi)<br/>
[Lombok 1.18.34](https://projectlombok.org/)<br/>
JavaFX 17 (controls, fxml)<br/>

## Built with:
[JetBrains IntelliJ Community Edition 2024.1.4](https://www.jetbrains.com/idea/)<br/>
[Eclipse Temurin JDK 17.0.11.9](https://adoptium.net/temurin/releases/?version=17)<br/>
[Gradle 8.9](https://gradle.org/)<br/>
[Lombok Gradle Plugin 8.6](https://plugins.gradle.org/plugin/io.freefair.lombok)<br/>
[JavaFX Plugin 0.1.0](https://plugins.gradle.org/plugin/org.openjfx.javafxplugin)<br/>

## License
Do not steal or claim this code as yours.<br/>
No derivative versions without releasing the source code.

---
Copyright Master801 2022-2024
