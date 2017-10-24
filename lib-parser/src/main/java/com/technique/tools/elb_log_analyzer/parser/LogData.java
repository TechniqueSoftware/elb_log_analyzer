package com.technique.tools.elb_log_analyzer.parser;

import java.time.Instant;

/**
 *  Holds the raw data for a single log row as a mutable struct.
 *  <p>
 *  Note: instances are created using a {@link LogDataParser}, which
 *  is designed for use in a Java8 stream.
 */
public class LogData {
    public Instant  timestamp;
    public String   elbId;
    public String   clientIp;
    public String   clientPort;
    public String   targetIp;
    public String   targetPort;
    public double   request_processing_time;
    public double   target_processing_time;
    public double   response_processing_time;
    public String   elb_status_code;
    public String   target_status_code;
    public int      received_bytes;
    public int      sent_bytes;
    public String   httpMethod;
    public String   fullUrl;
    public String   urlHost;
    public String   urlPath;
    public String   userAgent;
    public String   ssl_cipher;
    public String   ssl_protocol;
    public String   target_group_arn;
    public String   trace_id;
    public String   domain_name;
    public String   chosen_cert_arn;
}
