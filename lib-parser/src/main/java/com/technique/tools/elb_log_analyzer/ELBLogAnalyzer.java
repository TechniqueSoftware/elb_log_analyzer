package com.technique.tools.elb_log_analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  Summarizes an ELB access log.
 */
public class ELBLogAnalyzer {

    private static Logger logger = LoggerFactory.getLogger(ELBLogAnalyzer.class);

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    /**
     *  Holds relevant data from a single log row.
     */
    protected static class RawData {

        public Date timestamp;
        public String rawUrl;
        public String url;
        public double totalTime;
        public double backendTime;
        public int size;

        public RawData(String logline)
        throws Exception {
            // note: there are some quoted strings that this will incorrectly
            //       break, but I don't are about keeping them whole
            String[] parts = logline.split(" ");

            timestamp = dateFormat.parse(parts[0].replaceFirst("\\.[0-9]*Z", "Z"));
            rawUrl = parts[12];

            String trimmedUrl = rawUrl;
            int queryOffset = trimmedUrl.indexOf('?');
            if (queryOffset >= 0) {
                trimmedUrl = trimmedUrl.substring(0, queryOffset);
            }
            int jsessionOffset = trimmedUrl.indexOf(";jsessionid=");
            if (jsessionOffset >= 0) {
                trimmedUrl = trimmedUrl.substring(0, jsessionOffset);
            }
            url = trimmedUrl;

            totalTime = Double.parseDouble(parts[4])
                      + Double.parseDouble(parts[5])
                      + Double.parseDouble(parts[6]);
            backendTime = Double.parseDouble(parts[5]);
            size = Integer.parseInt(parts[10]);
        }
    }


    /**
     *  Holds aggregated statistics for a single URL.
     */
    protected static class UrlAggregator {

        public String url;
        public int numCalls;
        public int numSuccesses;
        public int numFails;
        public double totalTime;
        public double minTime = Double.MAX_VALUE;
        public double maxTime = 0;
        public double sumOfSquaredTotalTime;

        public UrlAggregator(RawData rawData) {
            this.url = rawData.url;
            aggregate(rawData);
        }

        public void aggregate(RawData rawData) {
            if (! rawData.url.equals(this.url)) {
                throw new IllegalArgumentException("attempted to aggregate different URL");
            }

            this.numCalls++;
            if (rawData.totalTime >= 0) {
                numSuccesses++;
                this.totalTime += rawData.totalTime;
                this.minTime = Math.min(this.minTime, rawData.totalTime);
                this.maxTime = Math.max(this.maxTime, rawData.totalTime);
                sumOfSquaredTotalTime += rawData.totalTime * rawData.totalTime;
            }
            else {
                this.numFails++;
            }
        }


        public double averageTime() {
            return totalTime / numSuccesses;
        }


        public double stdevTime() {
            // note: this is population standard deviation
            return Math.sqrt(sumOfSquaredTotalTime / numSuccesses - averageTime() * averageTime());
        }


        public static String tabFormatHeader() {
            return "URL\t" + "TotalCalls\t" + "FailedCalls\t" 
                 + "TotalTime\t" + "MinTime\t" + "MaxTime\t" + "AvgTime\t" + "StDevTime";
        }
        
        public String toTabFormat() {
            return url + "\t" + numCalls + "\t" + numFails + "\t"  + totalTime
                       + "\t" + minTime + "\t" + maxTime
                       + "\t" + averageTime() + "\t" + stdevTime();
        }

        @Override
        public String toString() {
            return String.format("%-64s numCalls %8d numFails %8d totalTime %9.3f minTime %6.3f maxTime %6.3f avgTime %6.3f stdDevTime %6.3f",
                                 url, numCalls, numFails, totalTime, minTime, maxTime, averageTime(), stdevTime());
        }
    }


    /**
     *  Aggregates statistics for all URLs.
     */
    protected static class Aggregator {

        public TreeMap<String,UrlAggregator> aggregations = new TreeMap<>();

        public void aggregate(RawData rawData) {
            UrlAggregator urlAggr = aggregations.get(rawData.url);
            if (urlAggr == null) {
                aggregations.put(rawData.url, new UrlAggregator(rawData));
            }
            else {
                urlAggr.aggregate(rawData);
            }
        }
    }


    public static void main(String[] argv)
    throws Exception
    {
        Aggregator aggregator = new Aggregator();
        for (String filename : argv) {
            process(new File(filename), aggregator);
        }

        writeOutput(aggregator);
    }


    private static void process(File fileOrDirectory, Aggregator aggregator)
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


    private static void processFile(File file, Aggregator aggregator)
    throws Exception {
        logger.debug("processing {}", file.getName());
        int linesRead = 0;
        int parseErrors = 0;
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
             BufferedReader in = new BufferedReader(isr)) {

            String inputLine = null;
            while ((inputLine = in.readLine()) != null) {
                try {
                    linesRead++;
                    aggregator.aggregate(new RawData(inputLine));
                }
                catch (Exception ex) {
                    parseErrors++;
                }
            }
        }
        logger.debug("finished {}: {} lines read, {} parse errors", file.getName(), linesRead, parseErrors);
    }


    private static void writeOutput(Aggregator aggregator)
    throws Exception {
        for (UrlAggregator aggr: aggregator.aggregations.values()) {
            System.out.println(aggr);
        }
    }

}
