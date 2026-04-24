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
        Files.writeString(inputFile.toPath(), "header\nvalue\n");
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
        Files.writeString(inputFile.toPath(), "header\nvalue\n");
        File outputDir = tempDir.resolve("new-output-dir").toFile();
        assertFalse(outputDir.exists(), "Output dir should not exist before test");

        CommandLine cmd = new CommandLine(new App());
        int exitCode = cmd.execute("--input", inputFile.getPath(),
                "--output", outputDir.getPath());
        assertEquals(0, exitCode, "Should exit 0 when output dir is auto-created");
        assertTrue(outputDir.exists(), "Output dir should be created automatically");
        assertTrue(outputDir.isDirectory(), "Output path should be a directory");
    }
}
