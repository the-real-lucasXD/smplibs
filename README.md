**Warning: This mod is currently in open beta. Please expect bugs and report any to [issues](https://github.com/the-real-lucasXD/smplibs/issues). Thanks for helping me improve my mod!**

<img src="src/main/resources/assets/smplibs/icon.png" alt="SMPLibs" width="150">

## Lucas's SMP Libraries
Lucas's SMP Libraries is a FabricMC utility and library mod with features useful for small or private Minecraft SMP servers, plus API helpers to help me (and other developers) develop custom SMP mods.

### Features
- Combat system
  - Customizable settings, bans and limits
  - Automatically enters and resets on player attack
  - `/combat` admin commands to manage combat
  - Kills the player when they disconnect while in combat
- _(Coming soon)_ Teams

### API Utilities
- Automatic config and player data storage system
- Event listeners
  - Player death listeners by all sources and by another player



## Usage
<details>
  <summary>Using the mod</summary>
  
  #### Prerequisites:
  - Working Minecraft Java `26.3+` server
  - Fabric loader for the corresponding Minecraft version
  #### Instructions:
  1) Get the desired version of the mod from [Releases](https://github.com/the-real-lucasXD/smplibs/releases).
  2) Download the `.jar` file from the release and put it into the `mods` folder in your server root directory.
</details>


<details>
  <summary>Depending on the mod</summary>

  Locate your project's `build.gradle` file and add the following code:
  1. At `repositories`:
  ```groovy
  repositories {
      exclusiveContent {
          forRepository {
              maven {
                  name = "Modrinth"
                  url = "https://api.modrinth.com/maven"
              }
          }
          // forRepositories(fg.repository) // Uncomment if using ForgeGradle
          filter {
              includeGroup "maven.modrinth"
          }
      }
  }
  ```
  
  2. At `dependencies`:
  ```groovy
  dependencies {
      implementation "maven.modrinth:smplibs:<version>"
      // implementation "maven.modrinth:smplibs:<version>:javadoc" // Uncomment for Javadoc, ONLY if using v1.1.0 or later
  }
  ```
</details>




## Acknowledgements
This project uses the [MIT license](LICENSE).
