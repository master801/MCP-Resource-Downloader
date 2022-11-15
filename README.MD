# MCP-Resource-Downloader
An alternative to downloading resource files for older versions of Minecraft for MCP (Minecraft-Coder-Pack).<br/>
Downloads directly from Minecraft servers. No middleman, no sketchy website.

## CMD

### Usage:
Normal: `gradlew -p CMD run --args="{insert arguments here}"`<br/>
Print out all Minecraft versions: `gradlew -p CMD run`<br/>

### Arguments:
- `--mcp` [`MCP directory`] - The directory of where MCP is (**REQUIRED**)
- `--mcVersion` [`Minecraft version`] - The version of Minecraft to download resources (**REQUIRED**)
- `--jars` - If jar files should be downloaded (**NOT** required)
- `--client` - If the client jar should be downloaded (**REQUIRED** if `--jars` used)
- `--server` - If the server jar should be downloaded (**REQUIRED** if `--jars` used)
- `--libraries` - If library jars should be downloaded (**NOT** required)
- `--natives` - If natives should be downloaded and extracted (**NOT** required)
- `--linux` - If Linux natives should be downloaded and extracted (**NOT** required even if `--natives` used)
- `--windows` - If Windows natives should be downloaded and extracted (**NOT** required even if `--natives` used)
- `--osx` - If OSX natives should be downloaded and extracted (**NOT** required even if `--natives` used)
- `--resources` - If resources (sound, music) should be downloaded (**NOT** required)
- `--overwrite` - If downloaded files should be overwritten should one already exist (**NOT** required)

### Example:
`gradlew -p CMD run --args="--mcp mcp62 --mcVersion 1.2.5 --jars --client --libraries --natives --resources --overwrite"`<br/>
`gradlew -p CMD run --args="--mcp mcp811 --mcVersion 1.6.4 --jars --client --libraries --natives --resources --overwrite"`<br/>

### Troubleshooting:
- I keep getting error `FileNotFoundException` `MCP directory "XXXXXX" does not exist!`!
  - Make sure when inputting the `--mcp` argument you're using either a relative location to the `CMD` folder
  (if MCP folder is in root directory -where `build.gradle` is-, use `../{MCP directory}`),
  or an exact location of where the MCP folder is (`C:\Users\User\Downloads\{MCP directory}`).

## GUI

### Usage:
`gradlew -p GUI run`

## Supported versions:
- [X] 1.6.4
- [X] 1.2.5
- [X] 1.2.3

## Untested versions:
- 1.5.2
- 1.4.7
- 1.3.2


## Known Issues:
- Server jar can't decompile
  - This is due to Mojang hotfixing server jars years later, due to an exploit / bug fix or Java 8 issue. Not easily fixable unless MCP files are repaired.
- 1.6+ versions aren't supported
  - Yes, it's being worked on it. We know.
- Broken sounds and music in 1.6
  - Being worked on. Not entirely sure how the assets index system works.
- Mac OS (OSX) users may not have natives extracted
  - Open a new issue. I don't have a Mac, so I don't know what Java expects.

Found an issue? Open a new GitHub issue.

## Workarounds:
- 1.6.4
  - MCP can't decompile because it tries copying launcher files
    - Manually run the decompile script with
    `runtime\bin\python\python_mcp runtime\decompile.py --nocopy --client`
    (in MCP directory) after running MCP-RD in client mode.
- Mac OS (OSX) users may not have natives extracted
  - Manually extract jars files

## Built with:
[JetBrains IntelliJ Community Edition 2022.3 Beta](https://www.jetbrains.com/idea/)<br/>
Java 18.0.1.1<br/>
[Gradle 7.5.1](https://gradle.org/)<br/>
[Moshi 1.14.0](https://github.com/square/moshi)<br/>
[Lombok 1.18.24](https://projectlombok.org/)<br/>
[Lombok Gradle Plugin 6.5.1](https://plugins.gradle.org/plugin/io.freefair.lombok)<br/>
JavaFX 18<br/>
[JavaFX Plugin 0.0.13](https://plugins.gradle.org/plugin/org.openjfx.javafxplugin)<br/>

## License
Do not steal or claim this code as yours.<br/>
No derivative versions without releasing the source code.

---
Copyright Master801 2022