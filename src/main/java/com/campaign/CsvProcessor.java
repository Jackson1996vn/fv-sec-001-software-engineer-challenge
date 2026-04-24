package com.campaign;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CsvProcessor {

    public static Map<String, CampaignMetrics> process(File inputFile) throws IOException {
        Map<String, CampaignMetrics> metrics = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setSkipHeaderRecord(true)
                 .setTrim(true)
                 .build()
                 .parse(reader)) {

            for (CSVRecord record : parser) {
                String campaignId = record.get("campaign_id");
                long impressions = Long.parseLong(record.get("impressions"));
                long clicks = Long.parseLong(record.get("clicks"));
                double spend = Double.parseDouble(record.get("spend"));
                long conversions = Long.parseLong(record.get("conversions"));

                metrics.computeIfAbsent(campaignId, k -> new CampaignMetrics())
                       .addRow(impressions, clicks, spend, conversions);
            }
        }

        return metrics;
    }
}
