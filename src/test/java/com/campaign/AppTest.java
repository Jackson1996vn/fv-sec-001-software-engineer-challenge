package com.campaign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void helpShowsInputAndOutputOptions() {
        StringWriter sw = new StringWriter();
        CommandLine cmd = new CommandLine(new App());
        cmd.setOut(new PrintWriter(sw));
        int exitCode = cmd.execute("--help");
        assertEquals(0, exitCode);
        String output = sw.toString();
        assertTrue(output.contains("--input"), "Help should mention --input");
        assertTrue(output.contains("--output"), "Help should mention --output");
    }

    @Test
    void missingArgumentsExitsWithError() {
        StringWriter errSw = new StringWriter();
        CommandLine cmd = new CommandLine(new App());
        cmd.setErr(new PrintWriter(errSw));
        int exitCode = cmd.execute();
        assertNotEquals(0, exitCode, "Should exit with non-zero when no args provided");
        String errOutput = errSw.toString();
        assertTrue(errOutput.contains("--input") || errOutput.contains("--output"),
                "Error should mention missing options");
    }

    @Test
    void nonexistentInputFileExitsWithError(@TempDir Path tempDir) {
        StringWriter errSw = new StringWriter();
        CommandLine cmd = new CommandLine(new App());
        cmd.setErr(new PrintWriter(errSw));
        File nonexistent = tempDir.resolve("nonexistent.csv").toFile();
        int exitCode = cmd.execute("--input", nonexistent.getPath(),
                "--output", tempDir.toString());
        assertEquals(1, exitCode, "Should exit 1 for nonexistent input file");
        String errOutput = errSw.toString();
        assertTrue(errOutput.contains("does not exist"),
                "Error should say file does not exist");
    }

    @Test
    void validInputAndExistingOutputSucceeds(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(inputFile.toPath(),
            "campaign_id,date,impressions,clicks,spend,conversions\n"
            + "cam1,2024-01-01,1000,50,12.50,3\n");
        File outputDir = tempDir.resolve("output").toFile();
        outputDir.mkdirs();

        CommandLine cmd = new CommandLine(new App());
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());
        assertEquals(0, exitCode, "Should exit 0 with valid input and existing output dir");

        assertTrue(new File(outputDir, "top10_ctr.csv").exists(),
                "top10_ctr.csv should be created");
        assertTrue(new File(outputDir, "top10_cpa.csv").exists(),
                "top10_cpa.csv should be created");
    }

    @Test
    void outputDirectoryCreatedIfMissing(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(inputFile.toPath(),
            "campaign_id,date,impressions,clicks,spend,conversions\n"
            + "cam1,2024-01-01,1000,50,12.50,3\n");
        File outputDir = tempDir.resolve("new-output-dir").toFile();
        assertFalse(outputDir.exists(), "Output dir should not exist before test");

        CommandLine cmd = new CommandLine(new App());
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());
        assertEquals(0, exitCode, "Should exit 0 when output dir is auto-created");
        assertTrue(outputDir.exists(), "Output dir should be created automatically");
        assertTrue(outputDir.isDirectory(), "Output path should be a directory");
    }

    @Test
    void processesValidCsvAndReportsCampaigns(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(inputFile.toPath(),
            "campaign_id,date,impressions,clicks,spend,conversions\n"
            + "cam1,2024-01-01,1000,50,12.50,3\n"
            + "cam2,2024-01-01,2000,100,25.00,0\n");
        File outputDir = tempDir.resolve("output").toFile();
        outputDir.mkdirs();

        StringWriter outSw = new StringWriter();
        CommandLine cmd = new CommandLine(new App());
        cmd.setOut(new PrintWriter(outSw));
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());

        assertEquals(0, exitCode);
        String output = outSw.toString();
        assertTrue(output.contains("Processed 2 campaigns"), "Should report 2 campaigns");
        assertTrue(output.contains("Written:"), "Should print Written: for output files");
        assertTrue(output.contains("top10_ctr.csv"), "Should mention top10_ctr.csv");
    }

    @Test
    void zeroConversionCampaignExcludedFromCpaFile(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(inputFile.toPath(),
            "campaign_id,date,impressions,clicks,spend,conversions\n"
            + "cam1,2024-01-01,1000,50,10.00,0\n"
            + "cam2,2024-01-01,2000,100,20.00,5\n");
        File outputDir = tempDir.resolve("output").toFile();
        outputDir.mkdirs();

        CommandLine cmd = new CommandLine(new App());
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());

        assertEquals(0, exitCode);
        String cpaContent = Files.readString(new File(outputDir, "top10_cpa.csv").toPath());
        assertFalse(cpaContent.contains("cam1"),
                "Zero-conversion campaign cam1 should not appear in top10_cpa.csv");
        assertTrue(cpaContent.contains("cam2"),
                "Campaign cam2 with conversions should appear in top10_cpa.csv");
    }

    @Test
    void aggregatesMultipleRowsSameCampaign(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(inputFile.toPath(),
            "campaign_id,date,impressions,clicks,spend,conversions\n"
            + "cam1,2024-01-01,1000,50,12.50,3\n"
            + "cam1,2024-01-02,2000,100,25.00,7\n");
        File outputDir = tempDir.resolve("output").toFile();
        outputDir.mkdirs();

        StringWriter outSw = new StringWriter();
        CommandLine cmd = new CommandLine(new App());
        cmd.setOut(new PrintWriter(outSw));
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());

        assertEquals(0, exitCode);
        String output = outSw.toString();
        assertTrue(output.contains("Processed 1 campaigns"), "Two rows same campaign = 1 campaign");

        String ctrContent = Files.readString(new File(outputDir, "top10_ctr.csv").toPath());
        assertTrue(ctrContent.contains("3000"), "Impressions should be summed: 1000+2000=3000");
        assertTrue(ctrContent.contains("150"), "Clicks should be summed: 50+100=150");
    }

    @Test
    void malformedCsvExitsWithError(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("bad.csv").toFile();
        Files.writeString(inputFile.toPath(), "not,a,valid,csv,header\nfoo\n");
        File outputDir = tempDir.resolve("output").toFile();
        outputDir.mkdirs();

        StringWriter errSw = new StringWriter();
        CommandLine cmd = new CommandLine(new App());
        cmd.setErr(new PrintWriter(errSw));
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());

        assertEquals(1, exitCode, "Should exit 1 for malformed CSV");
        String errOutput = errSw.toString();
        assertTrue(errOutput.contains("Error processing CSV"),
            "Should print error message for malformed CSV");
    }

    @Test
    void outputFilesContainCorrectHeaders(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(inputFile.toPath(),
            "campaign_id,date,impressions,clicks,spend,conversions\n"
            + "cam1,2024-01-01,1000,50,12.50,3\n"
            + "cam2,2024-01-01,2000,200,20.00,10\n"
            + "cam3,2024-01-01,3000,60,30.00,5\n");
        File outputDir = tempDir.resolve("output").toFile();
        outputDir.mkdirs();

        CommandLine cmd = new CommandLine(new App());
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());
        assertEquals(0, exitCode);

        List<String> ctrLines = Files.readAllLines(new File(outputDir, "top10_ctr.csv").toPath());
        List<String> cpaLines = Files.readAllLines(new File(outputDir, "top10_cpa.csv").toPath());

        assertTrue(ctrLines.get(0).startsWith("campaign_id,total_impressions,total_clicks,total_spend,total_conversions,ctr,cpa"),
                "CTR file should have correct header");
        assertTrue(cpaLines.get(0).startsWith("campaign_id,total_impressions,total_clicks,total_spend,total_conversions,ctr,cpa"),
                "CPA file should have correct header");
    }

    @Test
    void outputFilesWrittenToSpecifiedDirectory(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(inputFile.toPath(),
            "campaign_id,date,impressions,clicks,spend,conversions\n"
            + "cam1,2024-01-01,1000,50,12.50,3\n");
        File nestedDir = tempDir.resolve("nested").resolve("deep").resolve("output").toFile();
        nestedDir.mkdirs();

        CommandLine cmd = new CommandLine(new App());
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", nestedDir.getPath());
        assertEquals(0, exitCode);

        assertTrue(new File(nestedDir, "top10_ctr.csv").exists(),
                "top10_ctr.csv should exist in nested directory");
        assertTrue(new File(nestedDir, "top10_cpa.csv").exists(),
                "top10_cpa.csv should exist in nested directory");
    }

    @Test
    void top10CtrRankedCorrectly(@TempDir Path tempDir) throws IOException {
        StringBuilder csv = new StringBuilder("campaign_id,date,impressions,clicks,spend,conversions\n");
        // Create 12 campaigns with distinct CTR values (clicks/impressions)
        for (int i = 1; i <= 12; i++) {
            // impressions=1000, clicks=i*10 => CTR = i*10/1000 = i*0.01
            csv.append(String.format("campaign_%02d,2024-01-01,1000,%d,10.00,1%n", i, i * 10));
        }
        File inputFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(inputFile.toPath(), csv.toString());
        File outputDir = tempDir.resolve("output").toFile();
        outputDir.mkdirs();

        CommandLine cmd = new CommandLine(new App());
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());
        assertEquals(0, exitCode);

        List<String> ctrLines = Files.readAllLines(new File(outputDir, "top10_ctr.csv").toPath());
        // Header + 10 data rows = 11 lines
        assertEquals(11, ctrLines.size(), "Should have header + 10 data rows");

        // First data row should be campaign_12 (highest CTR: 120/1000 = 0.12)
        assertTrue(ctrLines.get(1).startsWith("campaign_12"),
                "First ranked campaign should be campaign_12 with highest CTR");

        // campaign_01 and campaign_02 should not appear (ranked 11th and 12th)
        String allContent = String.join("\n", ctrLines.subList(1, ctrLines.size()));
        assertFalse(allContent.contains("campaign_01"),
                "campaign_01 should not be in top 10");
        assertFalse(allContent.contains("campaign_02"),
                "campaign_02 should not be in top 10");
    }

    @Test
    void top10CpaExcludesZeroConversions(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(inputFile.toPath(),
            "campaign_id,date,impressions,clicks,spend,conversions\n"
            + "cam_a,2024-01-01,1000,50,10.00,5\n"
            + "cam_b,2024-01-01,1000,50,20.00,0\n"   // zero conversions
            + "cam_c,2024-01-01,1000,50,15.00,3\n"
            + "cam_d,2024-01-01,1000,50,25.00,0\n"   // zero conversions
            + "cam_e,2024-01-01,1000,50,30.00,10\n");
        File outputDir = tempDir.resolve("output").toFile();
        outputDir.mkdirs();

        CommandLine cmd = new CommandLine(new App());
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());
        assertEquals(0, exitCode);

        List<String> cpaLines = Files.readAllLines(new File(outputDir, "top10_cpa.csv").toPath());
        // 3 campaigns with conversions + 1 header = 4 lines total
        assertEquals(4, cpaLines.size(), "Should have header + 3 data rows (excluding zero-conversion campaigns)");

        String cpaContent = String.join("\n", cpaLines);
        assertFalse(cpaContent.contains("cam_b"), "Zero-conversion cam_b should be excluded");
        assertFalse(cpaContent.contains("cam_d"), "Zero-conversion cam_d should be excluded");
    }
}
