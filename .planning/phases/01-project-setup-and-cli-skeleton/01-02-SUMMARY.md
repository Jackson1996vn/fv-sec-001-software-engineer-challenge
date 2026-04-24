---
phase: 01-project-setup-and-cli-skeleton
plan: 02
subsystem: cli
tags: [picocli, cli, java21, junit-jupiter, tdd]

# Dependency graph
requires:
  - phase: 01-project-setup-and-cli-skeleton/01
    provides: "Gradle build with Picocli dependency and minimal App entry point"
provides:
  - "Picocli CLI with --input and --output argument parsing"
  - "Input file existence validation with clear error messages"
  - "Output directory auto-creation via mkdirs()"
  - "5 JUnit tests covering all CLI argument scenarios"
affects: [02-csv-parsing-and-aggregation, 03-output-generation-and-polish]

# Tech tracking
tech-stack:
  added: []
  patterns: [picocli-command-pattern, picocli-spec-injection, tdd-red-green]

key-files:
  created: [src/test/java/com/campaign/AppTest.java]
  modified: [src/main/java/com/campaign/App.java]

key-decisions:
  - "Used @Spec injection for testable output/error writers instead of System.out/System.err"

patterns-established:
  - "Picocli @Command with Callable<Integer> pattern for CLI commands"
  - "Use spec.commandLine().getOut()/getErr() for testable console output"
  - "TDD with CommandLine programmatic execution for testing CLI behavior"

requirements-completed: [CLI-01, CLI-02, CLI-03, CLI-04]

# Metrics
duration: 2min
completed: 2026-04-24
---

# Phase 1 Plan 2: CLI Argument Parsing Summary

**Picocli CLI with --input/--output validation, auto-directory creation, and 5 JUnit tests via TDD**

## Performance

- **Duration:** 2 min
- **Started:** 2026-04-24T00:59:04Z
- **Completed:** 2026-04-24T01:01:00Z
- **Tasks:** 1 (TDD: RED + GREEN commits)
- **Files modified:** 2

## Accomplishments
- Full Picocli CLI with --help, --version, --input, and --output flags
- Input file existence validation with clear "does not exist" error message
- Output directory auto-creation when missing
- 5 comprehensive JUnit tests covering all argument validation scenarios
- Fat JAR verified end-to-end with all CLI scenarios

## Task Commits

Each task was committed atomically:

1. **Task 1 RED: Failing tests for CLI argument validation** - `b17fb31` (test)
2. **Task 1 GREEN: Picocli CLI implementation** - `c314251` (feat)

## Files Created/Modified
- `src/main/java/com/campaign/App.java` - Full Picocli @Command with --input/--output options, validation, and directory creation
- `src/test/java/com/campaign/AppTest.java` - 5 JUnit tests: help output, missing args, nonexistent input, valid args, output dir creation

## Decisions Made
- Used `@Spec` injection to access CommandLine's out/err PrintWriters instead of System.out/System.err, enabling tests to capture output via `cmd.setOut()`/`cmd.setErr()`

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Used @Spec PrintWriters instead of System.err**
- **Found during:** Task 1 GREEN phase
- **Issue:** Plan used `System.err.println()` for error messages, but tests capture output via `cmd.setErr(new PrintWriter(sw))` which only intercepts the CommandLine's err writer, not JVM System.err
- **Fix:** Added `@Spec CommandSpec spec` field and used `spec.commandLine().getErr()` / `spec.commandLine().getOut()` for all output
- **Files modified:** src/main/java/com/campaign/App.java
- **Verification:** All 5 tests pass including nonexistentInputFileExitsWithError
- **Committed in:** c314251

---

**Total deviations:** 1 auto-fixed (1 bug)
**Impact on plan:** Essential for test correctness. No scope creep.

## TDD Gate Compliance

- RED gate: `b17fb31` - test(01-02) commit with 5 failing tests
- GREEN gate: `c314251` - feat(01-02) commit with passing implementation
- REFACTOR gate: skipped (code already clean)

## Issues Encountered
None.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- CLI skeleton complete with argument parsing and validation
- Ready for Phase 2: CSV parsing and aggregation
- Picocli @Command pattern established for extending with processing logic

---
*Phase: 01-project-setup-and-cli-skeleton*
*Completed: 2026-04-24*
