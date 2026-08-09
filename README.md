# Ender QOL Mod

Quality of life features and integrations.

## Features

- **AE2 & Building Gadgets 2 Pattern Sync (`com.ender.qol.bg2pattern`)**:
  - Automatically converts Building Gadgets 2 Copy-Paste schematic material selections into AE2 Wireless Pattern Encoding Terminal inputs.
  - Adds a **"Copy to Pattern"** radial menu action button to the Building Gadgets 2 GUI.

## Building from Source

### Prerequisites
- JDK 17 (Eclipse Adoptium / Temurin)
- Gradle 8.5 (bundled via `gradlew`)

### Build Steps
```bash
# Clone the repository
git clone https://github.com/ender-gs/ender-start-qol
cd ender-start-qol

# Build the mod JAR
./gradlew build
```

The compiled mod JAR will be placed in `build/libs/enderqol-1.0.0.jar`.

## License
[MIT](LICENSE)
