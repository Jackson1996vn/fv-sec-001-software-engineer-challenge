# PROMPTS.md

All prompts used during development of this project with Claude Code (AI coding assistant). Pasted as-is, raw and unedited.

---

## Prompt 1: Initial Project Setup & Planning

```
I want to build a console application (CLI) with Java to process the large csv. I have a csv file round 1GB and the schema like Column    Type    Description
campaign_id    string    Campaign ID
date    string    Date in YYYY-MM-DD format
impressions    integer    Number of impressions
clicks    integer    Number of clicks
spend    float    Advertising cost (USD)
conversions    integer    Number of conversions
. Your task are create a console application (CLI) using java 21 version to processes the CSV file and produces aggregated analytics. 1. Aggregate data by campagn_id: For each campaign_id, compute:

total_impressions
total_clicks
total_spend
total_conversions
CTR = total_clicks / total_impressions
CPA = total_spend / total_conversions
If conversions = 0, ignore or return null for CPA
. 2 Generate two result lists: A. Top 10 campaigns with teh highest CTR output as CSV format call name top10_ctr.csv. B. Top  10 campaigns with the loweast CPA output as CSV format. exclude campaigns with zero conversions that's name is top10_cpa.csv. 3 Technical Requirements: a optimize with large input file (memory and good performance), the program should be runable via cli for example: java --jar --input input.csv --output results/. Give me the plan first
```

This prompt was used with `/gsd-do` command which routed to `/gsd-new-project` for full project initialization.

---

## Prompt 2: Interactive Setup Questions (answered during project init)

During the GSD workflow, I answered several interactive questions:

- **Build Tool:** Gradle
- **CSV Parsing:** "choose the thing you think this is the best way" (Claude chose Apache Commons CSV)
- **CLI Parsing:** Picocli (Recommended)
- **Mode:** YOLO (auto-approve)
- **Granularity:** Coarse (3-5 phases)
- **Git Tracking:** No (keep .planning/ local)
- **Research:** No (well-defined CLI task)
- **Plan Check:** Yes
- **Verifier:** Yes
- **AI Models:** Quality (Opus)

---

## Prompt 3: Plan Phase 1

```
/gsd-plan-phase 1
```

---

## Prompt 4: Execute Phase 1

```
/gsd-execute-phase 1
```

---

## Prompt 5: Plan Phase 2

```
/gsd-plan-phase 2
```

---

## Prompt 6: Execute Phase 2

```
/gsd-execute-phase 2
```

---

## Prompt 7: Plan Phase 3

```
/gsd-plan-phase 3
```

---

## Prompt 8: Execute Phase 3

```
/gsd-execute-phase 3
```

---

## Summary of Approach

1. **Single detailed prompt** described the full problem: input schema, aggregation logic, output format, performance requirements, and CLI interface
2. **GSD workflow** (Get Shit Done) was used to structure the work into phases automatically
3. **Interactive decisions** were made for tooling choices (Gradle, Picocli, Commons CSV)
4. **3 phases** were planned and executed sequentially:
   - Phase 1: Project setup, Gradle build, CLI skeleton with Picocli
   - Phase 2: CSV streaming parser, CampaignMetrics accumulator, aggregation logic
   - Phase 3: ReportWriter with ranked CSV output, 1GB performance validation
5. **Each phase** followed: plan -> verify plan -> execute -> code review -> verify results
6. **Slash commands** (`/gsd-plan-phase`, `/gsd-execute-phase`) handled the heavy lifting after the initial problem description
