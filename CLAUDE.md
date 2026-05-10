# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Campaign CSV Analyzer — a Java 21 CLI that streams a large advertising-performance CSV (~1GB), aggregates per `campaign_id`, and writes two ranked CSV reports (`top10_ctr.csv`, `top10_cpa.csv`) to an output directory. Submitted as solution to the FV-SEC-001 software engineer challenge; see `README.md` for the original task brief.

## Common Commands

```bash
./gradlew build                      # compile + run unit tests + build fat JAR
./gradlew test                       # unit tests only (excludes @Tag("performance"))
./gradlew test --tests CampaignMetricsTest               # single test class
./gradlew test --tests CampaignMetricsTest.ctrIsZeroWhenImpressionsZero   # single method
./gradlew performanceTest            # 1GB synthetic CSV under -Xmx256m (5 min timeout)
./gradlew shadowJar                  # produce build/libs/campaign-csv-analyzer.jar

java -jar build/libs/campaign-csv-analyzer.jar --input ad_data.csv --output results/
java -Xmx256m -jar build/libs/campaign-csv-analyzer.jar --input ad_data.csv --output results/   # validate memory budget manually
```

The `.csv.zip` input dataset is gitignored — unzip `ad_data.csv.zip` locally before running.

## Architecture

Single-pass streaming aggregation. The whole pipeline is four classes in `src/main/java/com/campaign/`:

- **`App`** — Picocli `@Command` entry point. Validates `--input`/`--output`, creates the output dir if missing, then calls `CsvProcessor.process()` and the two `ReportWriter` writers in sequence. Uses `@Spec` injection for the `PrintWriter`s so tests can capture stdout/stderr.
- **`CsvProcessor.process(File)`** — Streams the CSV with `BufferedReader` + Apache Commons CSV's iterator (`setHeader().setSkipHeaderRecord(true)`). Never loads the full file. Accumulates into a `HashMap<String, CampaignMetrics>` keyed by `campaign_id`. Memory is bounded by **unique campaign count, not row count** — this is the core reason a 1GB / 19M-row file fits in a 256MB heap.
- **`CampaignMetrics`** — Mutable per-campaign accumulator. `addRow(...)` sums totals; `getCtr()` / `getCpa()` derive ratios on demand. `getCpa()` returns **`Double` (nullable)** — `null` signals zero conversions, distinct from a numeric zero. `ReportWriter` filters on this null for the CPA report.
- **`ReportWriter`** — Sorts the in-memory map twice with stream `Comparator`s (descending CTR, ascending CPA with null filter), takes the top 10, and writes via Commons CSV `CSVPrinter`. Spend/CTR/CPA are formatted (`%.2f`, `%.4f`, `%.2f`); zero-conversion rows print `N/A` for CPA in the CTR report.

Data flow: `CSV → CsvProcessor (stream parse) → Map<id, CampaignMetrics> (in memory) → ReportWriter (sort + top-10 → CSV)`.

## Things Worth Knowing

- **Test tagging.** `build.gradle` configures `test` to **exclude** `@Tag("performance")` and registers a separate `performanceTest` task that **includes** it with `-Xmx256m`. Don't move `PerformanceTest` out of its tag — it generates a ~1GB temp file and will blow up `./gradlew test`.
- **Numeric types.** Impressions/clicks/conversions are `long` (a 1GB CSV easily exceeds `Integer.MAX_VALUE` totals); spend is `double`. Don't silently downcast.
- **`getCpa()` returns `Double`, not `double`.** Null is meaningful (zero-conversion campaigns are excluded from the CPA report and rendered as `N/A` in the CTR report).
- **Fat JAR name is fixed.** `shadowJar` strips the version classifier — the artifact is always `build/libs/campaign-csv-analyzer.jar`. The README, performance test, and CI all assume that name.
- **`.planning/` is the GSD workflow log** (gitignored). It contains phase plans and decision history; treat it as reference, not source of truth — when the code and a plan disagree, the code wins.
- **`PROMPTS.md` is required by the challenge** and must contain raw, unedited prompts. If you make changes here through a new prompt cycle, append to it rather than rewriting.
- **`results/top10_ctr.csv` and `results/top10_cpa.csv`** are committed sample outputs from a real 1GB run (also a challenge submission requirement) — don't delete them when refactoring output paths.
