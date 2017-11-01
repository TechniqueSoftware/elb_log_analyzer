package com.technique.tools.elb_log_analyzer.summary;

import com.technique.tools.elb_log_analyzer.parser.LogData;

/**
 *  Accumulates processing-time statistics from log entries. Caller is responsible for
 *  associating instances with a relevant key.
 */
public class Accumulator {

    public int numCalls;
    public int numSuccesses;
    public int numFails;
    public double totalTime;
    public double minTime = Double.MAX_VALUE;
    public double maxTime = 0;
    public double sumOfSquaredTotalTime;


    public void add(LogData data) {
        this.numCalls++;
        double requestTime = data.request_processing_time + data.target_processing_time + data.response_processing_time;
        if (requestTime >= 0) {
            numSuccesses++;
            this.totalTime += requestTime;
            this.minTime = Math.min(this.minTime, requestTime);
            this.maxTime = Math.max(this.maxTime, requestTime);
            sumOfSquaredTotalTime += requestTime * requestTime;
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
}