# Requirements: Campaign CSV Analyzer

**Defined:** 2026-04-24
**Core Value:** Correctly aggregate campaign metrics from a large CSV and produce accurate top-10 rankings -- memory-efficiently and fast enough to handle gigabyte-scale input.

## v1 Requirements

### CSV Parsing

- [x] **CSV-01**: Application reads a CSV file from a path specified via --input flag
- [x] **CSV-02**: Application streams CSV rows without loading the entire file into memory
- [x] **CSV-03**: Application correctly parses all columns: campaign_id, date, impressions, clicks, spend, conversions

### Aggregation

- [x] **AGG-01**: Application groups rows by campaign_id and computes total_impressions (sum)
- [x] **AGG-02**: Application groups rows by campaign_id and computes total_clicks (sum)
- [x] **AGG-03**: Application groups rows by campaign_id and computes total_spend (sum)
- [x] **AGG-04**: Application groups rows by campaign_id and computes total_conversions (sum)
- [x] **AGG-05**: Application computes CTR = total_clicks / total_impressions per campaign
- [x] **AGG-06**: Application computes CPA = total_spend / total_conversions per campaign
- [x] **AGG-07**: Application handles CPA edge case: returns null/excludes campaigns with zero total_conversions

### Output

- [x] **OUT-01**: Application writes top10_ctr.csv containing top 10 campaigns ranked by highest CTR
- [x] **OUT-02**: Application writes top10_cpa.csv containing top 10 campaigns ranked by lowest CPA, excluding zero-conversion campaigns
- [x] **OUT-03**: Output files are written to the directory specified via --output flag
- [x] **OUT-04**: Output CSVs include headers and all aggregated metric columns

### CLI Interface

- [x] **CLI-01**: Application accepts --input flag for input CSV file path
- [x] **CLI-02**: Application accepts --output flag for output directory path
- [x] **CLI-03**: Application creates output directory if it does not exist
- [x] **CLI-04**: Application provides meaningful error messages for missing/invalid arguments
- [x] **CLI-05**: Application is packaged as an executable fat JAR runnable via `java -jar`

### Performance

- [x] **PERF-01**: Application processes a 1GB CSV file without OutOfMemoryError on default JVM heap
- [x] **PERF-02**: Application uses streaming I/O (not loading full file into memory)

## v2 Requirements

### Extended Features

- **EXT-01**: Support for date range filtering (--from, --to flags)
- **EXT-02**: Configurable top-N count (--top flag, default 10)
- **EXT-03**: Summary statistics output (total campaigns, total rows, processing time)

## Out of Scope

| Feature | Reason |
|---------|--------|
| Date-based filtering | Not in challenge requirements, v2 candidate |
| Multi-file input | Single CSV input per challenge spec |
| Database storage | In-memory aggregation only |
| GUI/web interface | CLI-only per requirements |
| Parallel file reading | Single-threaded streaming is sufficient and simpler |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| CSV-01 | Phase 2 | Complete (02-01) |
| CSV-02 | Phase 2 | Complete (02-01) |
| CSV-03 | Phase 2 | Complete (02-01) |
| AGG-01 | Phase 2 | Complete (02-01) |
| AGG-02 | Phase 2 | Complete (02-01) |
| AGG-03 | Phase 2 | Complete (02-01) |
| AGG-04 | Phase 2 | Complete (02-01) |
| AGG-05 | Phase 2 | Complete (02-01) |
| AGG-06 | Phase 2 | Complete (02-01) |
| AGG-07 | Phase 2 | Complete (02-01) |
| OUT-01 | Phase 3 | Complete (03-01) |
| OUT-02 | Phase 3 | Complete (03-01) |
| OUT-03 | Phase 3 | Complete (03-01) |
| OUT-04 | Phase 3 | Complete (03-01) |
| CLI-01 | Phase 1 | Complete (01-02) |
| CLI-02 | Phase 1 | Complete (01-02) |
| CLI-03 | Phase 1 | Complete (01-02) |
| CLI-04 | Phase 1 | Complete (01-02) |
| CLI-05 | Phase 1 | Complete (01-01) |
| PERF-01 | Phase 3 | Complete (03-02) |
| PERF-02 | Phase 2 | Complete (02-01) |

**Coverage:**
- v1 requirements: 21 total
- Mapped to phases: 21
- Unmapped: 0

---
*Requirements defined: 2026-04-24*
*Last updated: 2026-04-24 after roadmap creation*
