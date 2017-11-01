package com.technique.tools.elb_log_analyzer.summary;

import org.junit.Test;
import static org.junit.Assert.*;

import com.technique.tools.elb_log_analyzer.parser.LogDataParser;


public class TestAccumulator {

    private final static String SUCCESS_LOG_A1 = "2017-02-08T13:21:59.123557Z awseb-e-d-AWSEBLoa-FJNVR58R7C54 192.168.2.345:51004 10.123.160.45:80 0.000102 0.060686 0.000044 200 200 0 1234 \"GET https://www.example.com:443/action/Dashboard?haveANiceDay=true HTTP/1.1\" \"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36\" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2";
    private final static String SUCCESS_LOG_A2 = "2017-02-08T14:21:59.123557Z awseb-e-d-AWSEBLoa-FJNVR58R7C54 192.168.2.345:51004 10.123.160.45:80 0.000190 0.123456 0.000144 200 200 0 5678 \"GET https://www.example.com:443/action/Dashboard?thisIsADifferentQuery=true HTTP/1.1\" \"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36\" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2";

    private final static String FAIL_LOG_A1    = "2017-02-08T13:51:59.123557Z awseb-e-d-AWSEBLoa-FJNVR58R7C54 192.168.2.345:51004 - -1 -1 -1 504 0 0 89 \"GET https://www.example.com:443/action/Dashboard?haveANiceDay=false HTTP/1.1\" \"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36\" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2";

    private final static double SUCCESS_A1_TIME = 0.000102 + 0.060686 + 0.000044;
    private final static double SUCCESS_A2_TIME = 0.000190 + 0.123456 + 0.000144;


    @Test
    public void testBasicAggregation() throws Exception {

        LogDataParser parser = new LogDataParser();
        Accumulator aggregator = new Accumulator();

        aggregator.add(parser.parse(SUCCESS_LOG_A1));

        assertEquals("after success 1: numCalls",  1,                                       aggregator.numCalls);
        assertEquals("after success 1: numFails",  0,                                       aggregator.numFails);
        assertEquals("after success 1: totalTime", SUCCESS_A1_TIME,                         aggregator.totalTime,   .000001);
        assertEquals("after success 1: minTime",   SUCCESS_A1_TIME,                         aggregator.minTime,     .000001);
        assertEquals("after success 1: maxTime",   SUCCESS_A1_TIME,                         aggregator.maxTime,     .000001);

        aggregator.add(parser.parse(SUCCESS_LOG_A2));

        assertEquals("after success 2: numCalls",  2,                                       aggregator.numCalls);
        assertEquals("after success 2: numFails",  0,                                       aggregator.numFails);
        assertEquals("after success 2: totalTime", SUCCESS_A1_TIME + SUCCESS_A2_TIME,       aggregator.totalTime,   .000001);
        assertEquals("after success 2: minTime",   SUCCESS_A1_TIME,                         aggregator.minTime,     .000001);
        assertEquals("after success 2: maxTime",   SUCCESS_A2_TIME,                         aggregator.maxTime,     .000001);
        assertEquals("after success 2: avgTime",   (SUCCESS_A1_TIME + SUCCESS_A2_TIME) / 2, aggregator.averageTime(),.000001);
        assertEquals("after success 2: stdDev",    0.031479,                                aggregator.stdevTime(), .000001);

        aggregator.add(parser.parse(FAIL_LOG_A1));

        assertEquals("after fail 1: numCalls",     3,                                       aggregator.numCalls);
        assertEquals("after fail 1: numFails",     1,                                       aggregator.numFails);
        assertEquals("after fail 1: totalTime",    SUCCESS_A1_TIME + SUCCESS_A2_TIME,       aggregator.totalTime,   .000001);
        assertEquals("after fail 1: minTime",      SUCCESS_A1_TIME,                         aggregator.minTime,     .000001);
        assertEquals("after fail 1: maxTime",      SUCCESS_A2_TIME,                         aggregator.maxTime,     .000001);
        assertEquals("after fail 1: avgTime",      (SUCCESS_A1_TIME + SUCCESS_A2_TIME) / 2, aggregator.averageTime(),.000001);
        assertEquals("after fail 1: stdDev",       0.031479,                                aggregator.stdevTime(), .000001);
    }
}
