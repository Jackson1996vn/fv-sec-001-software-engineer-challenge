## Project

Campaign CSV Analyzer — Java 21 CLI application that processes large CSV files with campaign advertising data and produces aggregated analytics.

## Build & Run

```bash
./gradlew build
java -jar build/libs/campaign-csv-analyzer.jar --input input.csv --output results/
```

## Tech Stack

- Java 21
- Gradle (build)
- Apache Commons CSV (CSV parsing)
- Picocli (CLI argument parsing)
