package com.campaign;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
    name = "campaign-csv-analyzer",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Analyzes campaign CSV data and produces top-10 ranked reports."
)
public class App implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, required = true,
            description = "Path to the input CSV file")
    private File inputFile;

    @Option(names = {"-o", "--output"}, required = true,
            description = "Path to the output directory")
    private File outputDir;

    @Spec
    private CommandSpec spec;

    @Override
    public Integer call() {
        PrintWriter out = spec.commandLine().getOut();
        PrintWriter err = spec.commandLine().getErr();

        // Validate input file exists
        if (!inputFile.exists()) {
            err.println("Error: Input file does not exist: " + inputFile.getPath());
            return 1;
        }
        if (!inputFile.isFile()) {
            err.println("Error: Input path is not a file: " + inputFile.getPath());
            return 1;
        }

        // Create output directory if it does not exist
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created) {
                err.println("Error: Could not create output directory: " + outputDir.getPath());
                return 1;
            }
            out.println("Created output directory: " + outputDir.getPath());
        } else if (!outputDir.isDirectory()) {
            err.println("Error: Output path is not a directory: " + outputDir.getPath());
            return 1;
        }

        out.println("Input: " + inputFile.getPath());
        out.println("Output: " + outputDir.getPath());

        try {
            Map<String, CampaignMetrics> results = CsvProcessor.process(inputFile);
            out.println("Processed " + results.size() + " campaigns.");

            ReportWriter.writeTopCtr(results, outputDir);
            out.println("Written: " + new File(outputDir, "top10_ctr.csv").getPath());

            ReportWriter.writeTopCpa(results, outputDir);
            out.println("Written: " + new File(outputDir, "top10_cpa.csv").getPath());
        } catch (IOException | IllegalArgumentException e) {
            err.println("Error processing CSV: " + e.getMessage());
            return 1;
        }

        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
