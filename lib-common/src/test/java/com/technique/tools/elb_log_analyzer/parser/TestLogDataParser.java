package com.technique.tools.elb_log_analyzer.parser;

import java.time.Instant;

import org.junit.Test;
import static org.junit.Assert.*;


public class TestLogDataParser {

    @Test
    public void testSuccessfulRequest() throws Exception  {

        String testLogEntry = "2017-10-20T09:55:56.485559Z awseb-e-q-AWSEBLoa-1Q8VQHOXIQB0C "
                            + "50.78.24.42:57667 10.187.48.21:80 0.000215 0.211262 0.000186 200 200 331 46819 "
                            + "\"POST https://edge.club-os.com:443/action/EventPopup/open?query=example HTTP/1.1\" "
                            + "\"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36\" "
                            + "ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2";



        LogDataParser parser = new LogDataParser();
        LogData data = parser.parse(testLogEntry);

        assertEquals("timestamp",               Instant.ofEpochSecond(1508493356, 485559000),   data.timestamp);
        assertEquals("elbId",                   "awseb-e-q-AWSEBLoa-1Q8VQHOXIQB0C",             data.elbId);
        assertEquals("clientIp",                "50.78.24.42",                                  data.clientIp);
        assertEquals("clientPort",              "57667",                                        data.clientPort);
        assertEquals("targetIp",                "10.187.48.21",                                 data.targetIp);
        assertEquals("targetPort",              "80",                                           data.targetPort);
        assertEquals("request_processing_time", 0.000215,                                       data.request_processing_time,   0.0);
        assertEquals("target_processing_time",  0.211262,                                       data.target_processing_time,    0.0);
        assertEquals("response_processing_time", 0.000186,                                      data.response_processing_time,  0.0);
        assertEquals("elb_status_code",         "200",                                          data.elb_status_code);
        assertEquals("target_status_code",      "200",                                          data.target_status_code);
        assertEquals("received_bytes",          331,                                            data.received_bytes);
        assertEquals("sent_bytes",              46819,                                          data.sent_bytes);
        assertEquals("ssl_cipher",              "ECDHE-RSA-AES128-GCM-SHA256",                  data.ssl_cipher);
        assertEquals("ssl_protocol",            "TLSv1.2",                                      data.ssl_protocol);

        assertEquals("httpMethod",              "POST",                                         data.httpMethod);
        assertEquals("urlHost",                 "edge.club-os.com",                             data.urlHost);
        assertEquals("urlPath",                 "/action/EventPopup/open",                      data.urlPath);

        assertEquals("fullUrl",                 "https://edge.club-os.com:443/action/EventPopup/open?query=example",
                                                data.fullUrl);
        assertEquals("userAgent",               "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36",
                                                data.userAgent);
    }


    @Test
    public void testInvalidRequest() throws Exception  {

        String testLogEntry = "2017-10-20T00:01:01.884700Z awseb-e-q-AWSEBLoa-1Q8VQHOXIQB0C "
                            + "73.220.190.42:54451 - -1 -1 -1 504 0 1381 0 "
                            + "\"POST https://app.club-os.com:443/action/FollowUp/save HTTP/1.1\" "
                            + "\"Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36\" "
                            + "ECDHE-RSA-AES128-GCM-SHA256 TLSv1.2";

        LogDataParser parser = new LogDataParser();
        LogData data = parser.parse(testLogEntry);

        assertEquals("timestamp",               Instant.ofEpochSecond(1508457661, 884700000),   data.timestamp);
        assertEquals("elbId",                   "awseb-e-q-AWSEBLoa-1Q8VQHOXIQB0C",             data.elbId);
        assertEquals("clientIp",                "73.220.190.42",                                data.clientIp);
        assertEquals("clientPort",              "54451",                                        data.clientPort);
        assertEquals("targetIp",                null,                                           data.targetIp);
        assertEquals("targetPort",              null,                                           data.targetPort);
        assertEquals("request_processing_time", -1,                                             data.request_processing_time,   0.0);
        assertEquals("target_processing_time",  -1,                                             data.target_processing_time,    0.0);
        assertEquals("response_processing_time", -1,                                            data.response_processing_time,  0.0);
        assertEquals("elb_status_code",         "504",                                          data.elb_status_code);
        assertEquals("target_status_code",      "0",                                            data.target_status_code);
        assertEquals("received_bytes",          1381,                                           data.received_bytes);
        assertEquals("sent_bytes",              0,                                              data.sent_bytes);
        assertEquals("ssl_cipher",              "ECDHE-RSA-AES128-GCM-SHA256",                  data.ssl_cipher);
        assertEquals("ssl_protocol",            "TLSv1.2",                                      data.ssl_protocol);

        assertEquals("httpMethod",              "POST",                                         data.httpMethod);
        assertEquals("urlHost",                 "app.club-os.com",                              data.urlHost);
        assertEquals("urlPath",                 "/action/FollowUp/save",                        data.urlPath);

        assertEquals("fullUrl",                 "https://app.club-os.com:443/action/FollowUp/save",
                                                data.fullUrl);
        assertEquals("userAgent",               "Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36",
                                                data.userAgent);
    }
}
