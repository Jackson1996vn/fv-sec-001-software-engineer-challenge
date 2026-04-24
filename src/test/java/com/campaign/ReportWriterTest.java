package com.campaign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReportWriterTest {

    @TempDir
    File outputDir;

    private static CampaignMetrics createMetrics(long impressions, long clicks, double spend, long conversions) {
        CampaignMetrics m = new CampaignMetrics();
        m.addRow(impressions, clicks, spend, conversions);
        return m;
    }

    @Test
    void writeTopCtr_ranksByCtrDescending() throws IOException {
        Map<String, CampaignMetrics> map = new LinkedHashMap<>();
        // Create 15 campaigns with distinct CTR values
        // CTR = clicks / impressions
        for (int i = 1; i <= 15; i++) {
            map.put("camp_" + i, createMetrics(1000, i * 10, 100.0, 5));
        }
        // camp_15 has highest CTR (150/1000=0.15), camp_1 has lowest (10/1000=0.01)

        ReportWriter.writeTopCtr(map, outputDir);

        File outputFile = new File(outputDir, "top10_ctr.csv");
        assertTrue(outputFile.exists(), "top10_ctr.csv should exist");

        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(11, lines.size(), "Should have 1 header + 10 data rows");

        // Verify header
        assertEquals("campaign_id,total_impressions,total_clicks,total_spend,total_conversions,ctr,cpa",
                lines.get(0));

        // First data row should be camp_15 (highest CTR)
        assertTrue(lines.get(1).startsWith("camp_15,"), "First row should be camp_15 (highest CTR)");

        // Last data row should be camp_6 (10th highest CTR)
        assertTrue(lines.get(10).startsWith("camp_6,"), "Last row should be camp_6 (10th highest CTR)");
    }

    @Test
    void writeTopCpa_ranksByCpaAscending_excludesZeroConversions() throws IOException {
        Map<String, CampaignMetrics> map = new LinkedHashMap<>();
        // 12 campaigns with conversions (CPA = spend / conversions)
        for (int i = 1; i <= 12; i++) {
            map.put("camp_" + i, createMetrics(1000, 100, i * 10.0, 5));
        }
        // camp_1: CPA=10/5=2.0, camp_2: CPA=20/5=4.0, ... camp_12: CPA=120/5=24.0

        // 3 campaigns with zero conversions
        map.put("zero_a", createMetrics(1000, 100, 50.0, 0));
        map.put("zero_b", createMetrics(1000, 100, 60.0, 0));
        map.put("zero_c", createMetrics(1000, 100, 70.0, 0));

        ReportWriter.writeTopCpa(map, outputDir);

        File outputFile = new File(outputDir, "top10_cpa.csv");
        assertTrue(outputFile.exists(), "top10_cpa.csv should exist");

        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(11, lines.size(), "Should have 1 header + 10 data rows");

        // No zero-conversion campaigns should appear
        String allContent = String.join("\n", lines);
        assertFalse(allContent.contains("zero_a"), "zero_a should be excluded");
        assertFalse(allContent.contains("zero_b"), "zero_b should be excluded");
        assertFalse(allContent.contains("zero_c"), "zero_c should be excluded");

        // First data row should be camp_1 (lowest CPA = 2.0)
        assertTrue(lines.get(1).startsWith("camp_1,"), "First row should be camp_1 (lowest CPA)");
    }

    @Test
    void writeTopCtr_fewerThan10() throws IOException {
        Map<String, CampaignMetrics> map = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            map.put("camp_" + i, createMetrics(1000, i * 10, 100.0, 5));
        }

        ReportWriter.writeTopCtr(map, outputDir);

        File outputFile = new File(outputDir, "top10_ctr.csv");
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(6, lines.size(), "Should have 1 header + 5 data rows");
    }

    @Test
    void writeTopCpa_fewerThan10Eligible() throws IOException {
        Map<String, CampaignMetrics> map = new LinkedHashMap<>();
        // 5 campaigns with conversions
        for (int i = 1; i <= 5; i++) {
            map.put("camp_" + i, createMetrics(1000, 100, i * 10.0, 5));
        }
        // 3 campaigns with zero conversions
        map.put("zero_a", createMetrics(1000, 100, 50.0, 0));
        map.put("zero_b", createMetrics(1000, 100, 60.0, 0));
        map.put("zero_c", createMetrics(1000, 100, 70.0, 0));

        ReportWriter.writeTopCpa(map, outputDir);

        File outputFile = new File(outputDir, "top10_cpa.csv");
        List<String> lines = Files.readAllLines(outputFile.toPath());
        assertEquals(6, lines.size(), "Should have 1 header + 5 eligible data rows");
    }
}
