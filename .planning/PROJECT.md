# Campaign CSV Analyzer

## What This Is

A Java 21 CLI application that processes large CSV files (~1GB) containing advertising campaign data and produces aggregated analytics. It reads campaign performance metrics, aggregates by campaign_id, and outputs ranked CSV reports for CTR and CPA.

## Core Value

Correctly aggregate campaign metrics from a large CSV and produce accurate top-10 rankings — memory-efficiently and fast enough to handle gigabyte-scale input.

## Requirements

### Validated

- ✓ CLI interface with --input and --output flags — Phase 1
- ✓ Packaged as executable JAR: `java -jar app.jar --input input.csv --output results/` — Phase 1
- ✓ Read and parse large CSV files with streaming (not loading entire file into memory) — Phase 2
- ✓ Aggregate metrics by campaign_id: total_impressions, total_clicks, total_spend, total_conversions — Phase 2
- ✓ Compute derived metrics: CTR = total_clicks / total_impressions, CPA = total_spend / total_conversions — Phase 2
- ✓ Handle CPA edge case: if total_conversions = 0, CPA is null/excluded — Phase 2

- ✓ Output top10_ctr.csv: top 10 campaigns ranked by highest CTR — Phase 3
- ✓ Output top10_cpa.csv: top 10 campaigns ranked by lowest CPA, excluding zero-conversion campaigns — Phase 3
- ✓ Memory-efficient processing suitable for 1GB+ files — Phase 3 (validated with 256MB heap)
- ✓ Good performance on large datasets — Phase 3 (1GB/19M rows in 18s)

### Active

(All requirements validated)

### Out of Scope

- GUI or web interface — CLI only per requirements
- Database storage — in-memory aggregation, output to CSV
- Date-based filtering or time-series analysis — aggregate across all dates
- Multi-file input — single CSV file input
- Real-time streaming — batch processing only

## Context

- Software engineering challenge (fv-sec-001)
- CSV schema: campaign_id (string), date (string YYYY-MM-DD), impressions (integer), clicks (integer), spend (float USD), conversions (integer)
- Input file expected to have many rows per campaign_id (multiple dates), requiring aggregation
- Java 21 with modern features available (records, virtual threads if beneficial)

## Constraints

- **Language**: Java 21 — required by challenge specification
- **Build**: Gradle — user preference
- **Dependencies**: Apache Commons CSV (CSV parsing), Picocli (CLI argument parsing)
- **Packaging**: Fat/uber JAR runnable via `java -jar`
- **Memory**: Must handle 1GB+ files without OutOfMemoryError on default JVM heap

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Gradle over Maven | User preference | — Pending |
| Apache Commons CSV | Robust streaming CSV parser, handles quoting/escaping edge cases | — Pending |
| Picocli for CLI | Lightweight, generates --help, validates args | — Pending |
| Stream-based processing | BufferedReader + Commons CSV iterator — never loads full file | — Pending |
| HashMap for aggregation | O(1) lookup per row, bounded by unique campaign_ids (not row count) | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-24 after Phase 3 completion — all phases complete*
