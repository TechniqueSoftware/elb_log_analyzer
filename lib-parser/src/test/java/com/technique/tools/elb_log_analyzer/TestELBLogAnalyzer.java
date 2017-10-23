package com.technique.tools.elb_log_analyzer;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestELBLogAnalyzer {

    private final static String SUCCESS_LOG_A1 = "2017-02-08T13:21:59.123557Z awseb-e-d-AWSEBLoa-FJNVR58R7C54 192.168.2.345:51004 10.123.160.45:80 0.000102 0.060686 0.000044 200 200 0 1234 \"GET https://www.example.com:443/action/Dashboard?haveANiceDay=true HTTP/1.1\" \"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36\" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2";
    private final static String SUCCESS_LOG_A2 = "2017-02-08T14:21:59.123557Z awseb-e-d-AWSEBLoa-FJNVR58R7C54 192.168.2.345:51004 10.123.160.45:80 0.000190 0.123456 0.000144 200 200 0 5678 \"GET https://www.example.com:443/action/Dashboard?thisIsADifferentQuery=true HTTP/1.1\" \"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36\" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2";

    private final static String SUCCESS_LOG_B1 = "2017-02-08T13:24:59.123557Z awseb-e-d-AWSEBLoa-FJNVR58R7C54 192.168.2.345:51004 10.123.160.45:80 0.000202 0.070686 0.000944 200 200 0 3210 \"GET https://www.example.com:443/action/SomewhereElse?haveANiceDay=true HTTP/1.1\" \"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36\" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2";

    private final static String FAIL_LOG_A1    = "2017-02-08T13:51:59.123557Z awseb-e-d-AWSEBLoa-FJNVR58R7C54 192.168.2.345:51004 - -1 -1 -1 504 0 0 89 \"GET https://www.example.com:443/action/Dashboard?haveANiceDay=false HTTP/1.1\" \"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36\" ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2";

    private final static double SUCCESS_A1_TIME = 0.000102 + 0.060686 + 0.000044;
    private final static double SUCCESS_A2_TIME = 0.000190 + 0.123456 + 0.000144;
    private final static double SUCCESS_B1_TIME = 0.000202 + 0.070686 + 0.000944 ;

    private final static String URL_A = "https://www.example.com:443/action/Dashboard";
    private final static String URL_B = "https://www.example.com:443/action/SomewhereElse";


    @Test
    public void testConstructRawData() throws Exception {
        ELBLogAnalyzer.RawData successData = new ELBLogAnalyzer.RawData(SUCCESS_LOG_A1);
        assertEquals("timestamp",   1486560119000L,
                                    successData.timestamp.getTime());
        assertEquals("rawUrl",      URL_A + "?haveANiceDay=true",
                                    successData.rawUrl);
        assertEquals("url",         URL_A,
                                    successData.url);
        assertEquals("totalTime",   0.000102 + 0.060686 + 0.000044,
                                    successData.totalTime, 0.000001);
        assertEquals("backendTime", 0.060686,
                                    successData.backendTime, 0.000001);
        assertEquals("size",        1234,
                                    successData.size);

        ELBLogAnalyzer.RawData failData = new ELBLogAnalyzer.RawData(FAIL_LOG_A1);
        assertEquals("timestamp",   1486561919000L,
                                    failData.timestamp.getTime());
        assertEquals("rawUrl",      URL_A + "?haveANiceDay=false",
                                    failData.rawUrl);
        assertEquals("url",         URL_A,
                                    failData.url);
        assertEquals("totalTime",   -3,
                                    failData.totalTime, 0.000001);
        assertEquals("backendTime", -1,
                                    failData.backendTime, 0.000001);
        assertEquals("size",        89,
                                    failData.size);
    }


    @Test
    public void UrlAggregator() throws Exception {

        ELBLogAnalyzer.UrlAggregator aggregator = new ELBLogAnalyzer.UrlAggregator(
                                                        new ELBLogAnalyzer.RawData(SUCCESS_LOG_A1));
        assertEquals("success_1: url",       URL_A,
                                             aggregator.url);
        assertEquals("success_1: numCalls",  1,
                                             aggregator.numCalls);
        assertEquals("success_1: numFails",  0,
                                             aggregator.numFails);
        assertEquals("success_1: totalTime", SUCCESS_A1_TIME,
                                             aggregator.totalTime,
                                             .000001);
        assertEquals("success_1: minTime",   SUCCESS_A1_TIME,
                                             aggregator.minTime,
                                             .000001);
        assertEquals("success_1: maxTime",   SUCCESS_A1_TIME,
                                             aggregator.maxTime,
                                             .000001);

        aggregator.aggregate(new ELBLogAnalyzer.RawData(SUCCESS_LOG_A2));
        assertEquals("success_2: numCalls",  2,
                                             aggregator.numCalls);
        assertEquals("success_2: numFails",  0,
                                             aggregator.numFails);
        assertEquals("success_2: totalTime", SUCCESS_A1_TIME + SUCCESS_A2_TIME,
                                             aggregator.totalTime,
                                             .000001);
        assertEquals("success_2: minTime",   SUCCESS_A1_TIME,
                                             aggregator.minTime,
                                             .000001);
        assertEquals("success_2: maxTime",   SUCCESS_A2_TIME,
                                             aggregator.maxTime,
                                             .000001);
        assertEquals("success_2: avgTime",   (SUCCESS_A1_TIME + SUCCESS_A2_TIME) / 2,
                                             aggregator.averageTime(),
                                             .000001);
        assertEquals("success_2: stdDev",    0.031479,
                                             aggregator.stdevTime(),
                                             .000001);

        aggregator.aggregate(new ELBLogAnalyzer.RawData(FAIL_LOG_A1));
        assertEquals("fail_1: numCalls",     3,
                                             aggregator.numCalls);
        assertEquals("fail_1: numFails",     1,
                                             aggregator.numFails);
        assertEquals("fail_1: totalTime",    SUCCESS_A1_TIME + SUCCESS_A2_TIME,
                                             aggregator.totalTime,
                                             .000001);
        assertEquals("fail_1: minTime",      SUCCESS_A1_TIME,
                                             aggregator.minTime,
                                             .000001);
        assertEquals("fail_1: maxTime",      SUCCESS_A2_TIME,
                                             aggregator.maxTime,
                                             .000001);
    }


    @Test
    public void testAggregator() throws Exception
    {
        ELBLogAnalyzer.Aggregator aggregator = new ELBLogAnalyzer.Aggregator();
        aggregator.aggregate(new ELBLogAnalyzer.RawData(SUCCESS_LOG_A1));
        aggregator.aggregate(new ELBLogAnalyzer.RawData(SUCCESS_LOG_B1));
        aggregator.aggregate(new ELBLogAnalyzer.RawData(FAIL_LOG_A1));
        aggregator.aggregate(new ELBLogAnalyzer.RawData(SUCCESS_LOG_A2));

        assertEquals("number of aggregations", 2, aggregator.aggregations.size());
        ELBLogAnalyzer.UrlAggregator aggregatorA = aggregator.aggregations.get(URL_A);
        ELBLogAnalyzer.UrlAggregator aggregatorB = aggregator.aggregations.get(URL_B);

        assertEquals("URL bozo check A", URL_A, aggregatorA.url);
        assertEquals("URL bozo check B", URL_B, aggregatorB.url);

        assertEquals("numCalls A", 3, aggregatorA.numCalls);
        assertEquals("numCalls B", 1, aggregatorB.numCalls);

        assertEquals("totalTime A", SUCCESS_A1_TIME + SUCCESS_A2_TIME, aggregatorA.totalTime, 0.000001);
        assertEquals("totalTime B", SUCCESS_B1_TIME,                   aggregatorB.totalTime, 0.000001);
    }

}
