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
        assertTrue(output.contains("cam1"), "Should list cam1");
        assertTrue(output.contains("cam2"), "Should list cam2");
    }

    @Test
    void cpaShowsNAForZeroConversions(@TempDir Path tempDir) throws IOException {
        File inputFile = tempDir.resolve("test.csv").toFile();
        Files.writeString(inputFile.toPath(),
            "campaign_id,date,impressions,clicks,spend,conversions\n"
            + "cam1,2024-01-01,1000,50,10.00,0\n");
        File outputDir = tempDir.resolve("output").toFile();
        outputDir.mkdirs();

        StringWriter outSw = new StringWriter();
        CommandLine cmd = new CommandLine(new App());
        cmd.setOut(new PrintWriter(outSw));
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());

        assertEquals(0, exitCode);
        String output = outSw.toString();
        assertTrue(output.contains("CPA=N/A"), "CPA should be N/A for zero conversions");
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
        assertTrue(output.contains("impressions=3000"), "Impressions should be summed: 1000+2000=3000");
        assertTrue(output.contains("clicks=150"), "Clicks should be summed: 50+100=150");
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
}
