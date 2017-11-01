package com.technique.tools.elb_log_analyzer.summary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.technique.tools.elb_log_analyzer.parser.LogData;
import com.technique.tools.elb_log_analyzer.parser.LogDataParser;


public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] argv) throws Exception {
        TreeMap<String,Accumulator> aggregator = new TreeMap<>();
        for (String filename : argv) {
            process(new File(filename), aggregator);
        }

        writeOutput(aggregator);
    }


    private static void process(File fileOrDirectory, TreeMap<String,Accumulator> aggregator)
    throws Exception {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                process(child, aggregator);
            }
        }
        else {
            processFile(fileOrDirectory, aggregator);
        }
    }


    private static void processFile(File file, TreeMap<String,Accumulator> aggregator) throws Exception {
        logger.debug("processing {}", file.getName());
        int linesRead = 0;
        int parseErrors = 0;
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
             BufferedReader in = new BufferedReader(isr)) {

            LogDataParser parser = new LogDataParser();

            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                try {
                    linesRead++;
                    LogData data = parser.parse(inputLine);
                    aggregator.computeIfAbsent(data.urlPath, k -> new Accumulator());
                    aggregator.get(data.urlPath).add(data);
                } catch (Exception ex) {
                    parseErrors++;
                }
            }
        }
        logger.debug("finished {}: {} lines read, {} parse errors", file.getName(), linesRead, parseErrors);
    }


    private static void writeOutput(TreeMap<String,Accumulator> aggregator)
    throws Exception {
        System.out.println(String.format("%-64s %12s %12s %12s %12s %12s %12s %12s",
                                         "path", "numCalls", "numFails", "totalTime", "minTime", "maxTime", "avgTime", "stdDev"));

        
        for (Map.Entry<String,Accumulator> entry: aggregator.entrySet()) {
            Accumulator acc = entry.getValue();
            System.out.println(String.format("%-64s %12d %12d %12.3f %12.3f %12.3f %12.3f %12.3f",
                                             entry.getKey(), 
                                             acc.numCalls, acc.numFails, 
                                             acc.totalTime, acc.minTime, acc.maxTime, acc.averageTime(), acc.stdevTime()));
        }
    }
}
