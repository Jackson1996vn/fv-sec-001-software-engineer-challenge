package com.campaign;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance test that generates a ~1GB CSV file and processes it.
 * <p>
 * This test is excluded from normal {@code ./gradlew test} runs via {@code @Tag("performance")}.
 * Run with: {@code ./gradlew performanceTest}
 * <p>
 * The Gradle performanceTest task is configured with {@code -Xmx256m} to prove that the
 * streaming HashMap approach handles large files without OutOfMemoryError.
 * <p>
 * Manual validation: {@code java -Xmx256m -jar build/libs/campaign-csv-analyzer.jar --input large.csv --output results/}
 */
@Tag("performance")
class PerformanceTest {

    @Test
    void processLargeFileWithoutOOM(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("large_input.csv").toFile();

        // Generate ~1GB CSV: 19,000,000 rows * ~55 bytes each = ~1.045 GB
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile), 1 << 16)) {
            writer.write("campaign_id,date,impressions,clicks,spend,conversions\n");
            for (int i = 0; i < 19_000_000; i++) {
                writer.write("campaign_");
                writer.write(String.format("%04d", i % 100));
                writer.write(",2024-01-01,1000,50,12.50,3\n");
            }
        }

        // Process the file through the full pipeline
        File outputDir = tempDir.resolve("output").toFile();
        outputDir.mkdirs();

        Map<String, CampaignMetrics> results = CsvProcessor.process(inputFile);

        assertEquals(100, results.size(), "Should have exactly 100 unique campaigns");

        ReportWriter.writeTopCtr(results, outputDir);
        ReportWriter.writeTopCpa(results, outputDir);

        File ctrFile = new File(outputDir, "top10_ctr.csv");
        File cpaFile = new File(outputDir, "top10_cpa.csv");

        assertTrue(ctrFile.exists(), "top10_ctr.csv should exist");
        assertTrue(cpaFile.exists(), "top10_cpa.csv should exist");

        List<String> ctrLines = Files.readAllLines(ctrFile.toPath());
        List<String> cpaLines = Files.readAllLines(cpaFile.toPath());

        assertEquals(11, ctrLines.size(), "top10_ctr.csv should have header + 10 data rows");
        assertEquals(11, cpaLines.size(), "top10_cpa.csv should have header + 10 data rows");
    }
}
