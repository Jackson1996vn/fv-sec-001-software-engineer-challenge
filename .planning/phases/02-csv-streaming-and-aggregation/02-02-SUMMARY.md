---
phase: 02-csv-streaming-and-aggregation
plan: 02
subsystem: cli-integration
tags: [cli, integration, csv-processing, picocli, error-handling]

requires:
  - phase: 02-csv-streaming-and-aggregation
    plan: 01
    provides: "CsvProcessor and CampaignMetrics classes"
provides:
  - "CLI wired to CsvProcessor for end-to-end CSV processing"
  - "Per-campaign metric summary output with CTR and CPA"
  - "Error handling for malformed CSV input with exit code 1"
  - "9 integration/unit tests for App CLI (5 existing updated + 4 new)"
affects: [03-ranking-output-and-integration]

tech-stack:
  added: []
  patterns: [cli-processor-wiring, multi-exception-catch, nullable-cpa-display]

key-files:
  created: []
  modified:
    - src/main/java/com/campaign/App.java
    - src/test/java/com/campaign/AppTest.java

key-decisions:
  - "Catch both IOException and IllegalArgumentException for robust CSV error handling"
  - "Updated existing tests to use valid campaign CSV data instead of dummy headers"
  - "CPA displayed as N/A string for zero-conversion campaigns"

requirements-completed: [CSV-01, CSV-02, CSV-03, AGG-01, AGG-02, AGG-03, AGG-04, AGG-05, AGG-06, AGG-07, PERF-02]

duration: 1min
completed: 2026-04-24
---

# Phase 2 Plan 2: CLI Integration with CsvProcessor Summary

**Wired CsvProcessor into App.call() replacing placeholder, with per-campaign metric output and multi-exception error handling for malformed CSV**

## Performance

- **Duration:** 1 min
- **Started:** 2026-04-24T01:42:20Z
- **Completed:** 2026-04-24T01:44:10Z
- **Tasks:** 1
- **Files modified:** 2

## Accomplishments
- App.java now calls CsvProcessor.process(inputFile) instead of printing placeholder
- Prints "Processed N campaigns." followed by per-campaign detail lines
- Each campaign line shows: impressions, clicks, spend, conversions, CTR, CPA
- CPA displays as "N/A" for campaigns with zero conversions (nullable Double handling)
- IOException and IllegalArgumentException caught with "Error processing CSV:" message and exit code 1
- 4 new integration tests added covering: valid CSV processing, CPA N/A display, multi-row aggregation, malformed CSV error
- 2 existing tests updated to use valid campaign CSV content (instead of dummy "header\nvalue\n")
- All 20 tests pass (11 from 02-01 + 9 AppTest)
- JAR smoke test confirms end-to-end processing with correct aggregation and CPA=N/A

## Task Commits

1. **Task 1: Wire CsvProcessor into App.call() and add integration tests** - `ccdab36` (feat)

## Files Created/Modified
- `src/main/java/com/campaign/App.java` - Replaced placeholder with CsvProcessor.process() call, added imports, added error handling
- `src/test/java/com/campaign/AppTest.java` - Updated 2 existing tests with valid CSV data, added 4 new integration tests

## Decisions Made
- Catch both IOException and IllegalArgumentException -- CsvProcessor throws IOException for I/O errors, but IllegalArgumentException for missing CSV columns (e.g., record.get("campaign_id") on a CSV without that header)
- Updated existing test CSV content from "header\nvalue\n" to valid campaign CSV -- required because App now passes CSV through CsvProcessor which expects proper headers
- CPA rendered as "N/A" string (not "null" or empty) for zero-conversion campaigns, matching plan specification

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Catch IllegalArgumentException for malformed CSV**
- **Found during:** Task 1 verification
- **Issue:** CsvProcessor.process() throws IllegalArgumentException (not IOException) when CSV header is missing expected columns like "campaign_id". The malformedCsvExitsWithError test failed because only IOException was caught.
- **Fix:** Changed catch clause from `IOException` to `IOException | IllegalArgumentException`
- **Files modified:** src/main/java/com/campaign/App.java
- **Commit:** ccdab36

**2. [Rule 3 - Blocking] Updated existing test CSV data**
- **Found during:** Task 1 implementation
- **Issue:** Two existing tests (validInputAndExistingOutputSucceeds, outputDirectoryCreatedIfMissing) used dummy CSV content "header\nvalue\n" which would fail with CsvProcessor since it expects campaign_id column
- **Fix:** Updated test CSV content to use valid campaign CSV with proper headers and data
- **Files modified:** src/test/java/com/campaign/AppTest.java
- **Commit:** ccdab36

## Issues Encountered
None beyond the auto-fixed deviations above.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- App.java now processes CSV end-to-end and prints campaign metrics
- Phase 3 (ranking and output) will add top-10 sorting and CSV file output to the output directory
- The HashMap<String, CampaignMetrics> returned by CsvProcessor.process() is ready for ranking

## Self-Check: PASSED

- All 2 modified files verified present on disk
- Commit hash ccdab36 verified in git log
- All 20 tests pass
- Placeholder "Processing not yet implemented" confirmed removed (grep returns 0)
- CsvProcessor.process wired in (grep returns 1 occurrence)
- JAR smoke test produces correct output with aggregation and CPA=N/A

---
*Phase: 02-csv-streaming-and-aggregation*
*Completed: 2026-04-24*
