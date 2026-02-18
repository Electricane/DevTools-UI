# DevTools

A multi-tool developer utility built with JavaFX and Groovy, designed for quick data manipulation, encoding, and visualization.

| Default Light Mode | Default Dark Mode |
| :---: | :---: |
| ![Light Mode](Screenshot%202026-02-18%20174603.png) | ![Dark Mode](Screenshot%202026-02-18%20174626.png) |

## Features

### Text & Data Tools
- **JSON Tree Editor**: A dual-pane visual editor for JSON.
  - **Visual Node Editor**: Double-click any node (except root) to edit the key and value simultaneously. Press `Enter` to save changes.
  - **Raw JSON Area**: Sync changes between the visual tree and the raw JSON text.
  - **Number Integration**: Right-click values that look like numbers (Long/BigDecimal) to open them in the Number Utils tool.
- **Base64 Converter**: Encode and decode strings to/from Base64.
- **AES Encryption**: Simple encryption and decryption tool.
- **SHA-256 Hasher**: Quickly generate hashes for text.
- **Regex Tester**: Real-time regular expression testing.
- **JWT Decoder**: Decode JSON Web Tokens to inspect headers and payloads.
- **Generators**: UUID and random data generation.
- **SQL Formatter**: Pretty-print and format SQL queries.
- **Cron Parser**: Human-readable explanations for Cron expressions.

### Number Tools
- **NumberUtils**: A specialized tool for converting between formats (Hex, Dec, Bin) and performing quick numeric transformations.

### System Monitoring
- **Live Metrics**: Real-time CPU usage and RAM monitoring in the status bar.
- **Dark Mode**: Toggle between light and dark themes (settings are persisted in `config.properties`).

## Getting Started

### Prerequisites
- JDK 23 or higher
- Gradle

### Running the Application
```bash
./gradlew run
```
