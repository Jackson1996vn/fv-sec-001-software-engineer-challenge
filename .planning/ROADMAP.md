# Roadmap: Campaign CSV Analyzer

## Overview

This project delivers a Java 21 CLI application that streams large CSV files, aggregates campaign metrics, and outputs ranked top-10 reports. Three phases move from project scaffolding through core data processing to final output and performance validation.

## Phases

- [x] **Phase 1: Project Setup and CLI Skeleton** - Gradle project with Picocli CLI that accepts arguments and packages as executable fat JAR
- [ ] **Phase 2: CSV Streaming and Aggregation** - Stream-parse CSV input and aggregate all metrics by campaign_id with derived CTR/CPA
- [ ] **Phase 3: Ranked Output and Performance Validation** - Write top-10 CSV reports and validate memory-efficient processing on large files

## Phase Details

### Phase 1: Project Setup and CLI Skeleton
**Goal**: User can run the application JAR with --input and --output flags and receive clear feedback on argument validation
**Depends on**: Nothing (first phase)
**Requirements**: CLI-01, CLI-02, CLI-03, CLI-04, CLI-05
**Success Criteria** (what must be TRUE):
  1. User can run `java -jar app.jar --help` and see usage instructions with --input and --output flags
  2. User can run the JAR with --input and --output flags and the application accepts them without error
  3. User receives a clear error message when required arguments are missing or the input file does not exist
  4. The output directory is created automatically if it does not exist
**Plans:** 2 plans

Plans:
- [x] 01-01-PLAN.md — Initialize Gradle project with dependencies, shadow plugin, and fat JAR packaging
- [x] 01-02-PLAN.md — Implement Picocli CLI with --input/--output parsing, validation, and tests

### Phase 2: CSV Streaming and Aggregation
**Goal**: Application reads a CSV file row-by-row and produces correct per-campaign aggregations including derived metrics
**Depends on**: Phase 1
**Requirements**: CSV-01, CSV-02, CSV-03, AGG-01, AGG-02, AGG-03, AGG-04, AGG-05, AGG-06, AGG-07, PERF-02
**Success Criteria** (what must be TRUE):
  1. User can point the application at a multi-row CSV and it reads all rows without loading the entire file into memory
  2. Application correctly sums impressions, clicks, spend, and conversions per campaign_id across multiple date rows
  3. Application computes CTR (clicks/impressions) and CPA (spend/conversions) correctly for each campaign
  4. Campaigns with zero total conversions have CPA excluded (null), not divided-by-zero
**Plans**: TBD

Plans:
- [ ] 02-01: TBD

### Phase 3: Ranked Output and Performance Validation
**Goal**: Application produces correctly ranked top-10 CSV output files and handles gigabyte-scale input without memory issues
**Depends on**: Phase 2
**Requirements**: OUT-01, OUT-02, OUT-03, OUT-04, PERF-01
**Success Criteria** (what must be TRUE):
  1. Running the application produces top10_ctr.csv with the 10 highest-CTR campaigns in descending order, with headers and all metric columns
  2. Running the application produces top10_cpa.csv with the 10 lowest-CPA campaigns in ascending order, excluding zero-conversion campaigns, with headers and all metric columns
  3. Both output files appear in the directory specified by --output
  4. Application processes a 1GB CSV file without OutOfMemoryError on default JVM heap settings
**Plans**: TBD

Plans:
- [ ] 03-01: TBD

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Project Setup and CLI Skeleton | 2/2 | Complete | 2026-04-24 |
| 2. CSV Streaming and Aggregation | 0/1 | Not started | - |
| 3. Ranked Output and Performance Validation | 0/1 | Not started | - |
