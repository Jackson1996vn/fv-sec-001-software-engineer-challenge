package com.campaign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvProcessorTest {

    private static final String CSV_HEADER = "campaign_id,date,impressions,clicks,spend,conversions";

    @Test
    void singleRowProducesOneEntry(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, CSV_HEADER + "\ncam1,2024-01-01,1000,50,12.50,3\n");

        Map<String, CampaignMetrics> result = CsvProcessor.process(csv.toFile());

        assertEquals(1, result.size());
        CampaignMetrics m = result.get("cam1");
        assertNotNull(m);
        assertEquals(1000, m.getTotalImpressions());
        assertEquals(50, m.getTotalClicks());
        assertEquals(12.50, m.getTotalSpend(), 0.001);
        assertEquals(3, m.getTotalConversions());
    }

    @Test
    void multipleRowsSameCampaignAreSummed(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, CSV_HEADER + "\n"
            + "cam1,2024-01-01,1000,50,12.50,3\n"
            + "cam1,2024-01-02,2000,100,25.00,7\n");

        Map<String, CampaignMetrics> result = CsvProcessor.process(csv.toFile());

        assertEquals(1, result.size());
        CampaignMetrics m = result.get("cam1");
        assertEquals(3000, m.getTotalImpressions());
        assertEquals(150, m.getTotalClicks());
        assertEquals(37.50, m.getTotalSpend(), 0.001);
        assertEquals(10, m.getTotalConversions());
    }

    @Test
    void differentCampaignIdsProduceSeparateEntries(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, CSV_HEADER + "\n"
            + "cam1,2024-01-01,1000,50,12.50,3\n"
            + "cam2,2024-01-01,500,25,8.00,1\n");

        Map<String, CampaignMetrics> result = CsvProcessor.process(csv.toFile());

        assertEquals(2, result.size());
        assertNotNull(result.get("cam1"));
        assertNotNull(result.get("cam2"));
        assertEquals(1000, result.get("cam1").getTotalImpressions());
        assertEquals(500, result.get("cam2").getTotalImpressions());
    }

    @Test
    void allSixColumnsAreParsedCorrectly(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, CSV_HEADER + "\ncamX,2024-06-15,5000,250,99.99,42\n");

        Map<String, CampaignMetrics> result = CsvProcessor.process(csv.toFile());

        CampaignMetrics m = result.get("camX");
        assertNotNull(m);
        assertEquals(5000, m.getTotalImpressions());
        assertEquals(250, m.getTotalClicks());
        assertEquals(99.99, m.getTotalSpend(), 0.001);
        assertEquals(42, m.getTotalConversions());
    }

    @Test
    void derivedMetricsWorkAfterProcessing(@TempDir Path tempDir) throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, CSV_HEADER + "\n"
            + "cam1,2024-01-01,2000,100,50.00,10\n"
            + "cam2,2024-01-01,1000,50,20.00,0\n");

        Map<String, CampaignMetrics> result = CsvProcessor.process(csv.toFile());

        // cam1: CTR = 100/2000 = 0.05, CPA = 50.00/10 = 5.00
        assertEquals(0.05, result.get("cam1").getCtr(), 0.0001);
        assertEquals(5.00, result.get("cam1").getCpa(), 0.001);

        // cam2: CTR = 50/1000 = 0.05, CPA = null (zero conversions)
        assertEquals(0.05, result.get("cam2").getCtr(), 0.0001);
        assertNull(result.get("cam2").getCpa());
    }
}
