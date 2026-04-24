package com.campaign;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ReportWriter {

    private static final String[] HEADERS = {
        "campaign_id", "total_impressions", "total_clicks",
        "total_spend", "total_conversions", "ctr", "cpa"
    };

    public static void writeTopCtr(Map<String, CampaignMetrics> metrics, File outputDir) throws IOException {
        List<Map.Entry<String, CampaignMetrics>> sorted = metrics.entrySet().stream()
                .sorted(Comparator.comparingDouble((Map.Entry<String, CampaignMetrics> e) -> e.getValue().getCtr()).reversed())
                .limit(10)
                .toList();

        writeReport(sorted, new File(outputDir, "top10_ctr.csv"));
    }

    public static void writeTopCpa(Map<String, CampaignMetrics> metrics, File outputDir) throws IOException {
        List<Map.Entry<String, CampaignMetrics>> sorted = metrics.entrySet().stream()
                .filter(e -> e.getValue().getCpa() != null)
                .sorted(Comparator.comparingDouble((Map.Entry<String, CampaignMetrics> e) -> e.getValue().getCpa()))
                .limit(10)
                .toList();

        writeReport(sorted, new File(outputDir, "top10_cpa.csv"));
    }

    private static void writeReport(List<Map.Entry<String, CampaignMetrics>> entries, File outputFile) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile);
             CSVPrinter printer = CSVFormat.DEFAULT.builder()
                     .setHeader(HEADERS)
                     .build()
                     .print(writer)) {

            for (Map.Entry<String, CampaignMetrics> entry : entries) {
                String id = entry.getKey();
                CampaignMetrics m = entry.getValue();
                printer.printRecord(
                        id,
                        m.getTotalImpressions(),
                        m.getTotalClicks(),
                        String.format("%.2f", m.getTotalSpend()),
                        m.getTotalConversions(),
                        String.format("%.4f", m.getCtr()),
                        m.getCpa() != null ? String.format("%.2f", m.getCpa()) : "N/A"
                );
            }
        }
    }
}
