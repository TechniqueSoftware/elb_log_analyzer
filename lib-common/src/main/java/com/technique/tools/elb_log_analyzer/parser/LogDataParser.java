package com.technique.tools.elb_log_analyzer.parser;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import net.sf.kdgcommons.lang.StringUtil;


/**
 *  Parses a row from the access log to produce a {@link LogData} object.
 *  Returns <code>null</code> if unable to parse, and optionally calls an
 *  error reporting function.
 *  <p>
 *  Instances are not threadsafe: maintaining per-thread state would require
 *  creating a context object for each call to {@link #parse}. If you want to
 *  use a parallel stream, wrap the parser instance in a <code>ThreadLocal</code>
 */
public class LogDataParser {

    // the following are initialized on each call to parse()
    private String  logLine;
    private int     curPos;
    private int     lastPos;
    private LogData result;


    public LogData parse(String logEntry)
    {
        this.logLine = logEntry;
        this.curPos = 0;
        this.lastPos = 0;
        this.result = new LogData();

        try
        {
            result.timestamp                = parseTimestamp();
            result.elbId                    = parseString();
            result.clientIp                 = parseTargetHost();
            result.clientPort               = parseTargetIp();
            result.targetIp                 = parseTargetHost();
            result.targetPort               = parseTargetIp();
            result.request_processing_time  = parseDouble();
            result.target_processing_time   = parseDouble();
            result.response_processing_time = parseDouble();
            result.elb_status_code          = parseString();
            result.target_status_code       = parseString();
            result.received_bytes           = parseInt();
            result.sent_bytes               = parseInt();
                                              parseConnectString();
            result.userAgent                = parseQuoteDelimitedString();
            result.ssl_cipher               = parseString();
            result.ssl_protocol             = parseString();

            return result;
        }
        catch (IllegalStateException ex)
        {
            // TODO - call error function
            return null;
        }
    }


    /**
     *  Returns the next token, ending at either the passed delimiter
     *  or the end of the string.
     */
    private String nextToken(char delimiter)
    {
        curPos = logLine.indexOf(delimiter, lastPos) + 1;
        if (curPos == 0)
        {
            curPos = logLine.length();
            return logLine.substring(lastPos);
        }
        else
        {
            return logLine.substring(lastPos, curPos - 1);
        }
    }


    /**
     *  Parses a normal space-delimited string field.
     */
    private String parseString()
    {
        String token = nextToken(' ');
        lastPos = curPos;
        return token;
    }


    /**
     *  Parses a quote-delimited string that is followed by either a space or end-of-input.
     */
    private String parseQuoteDelimitedString()
    {
        // assumption: there are no embedded quotes or other escapes
        if (logLine.charAt(curPos++) != '"')
            throw new IllegalStateException("expected double-quote to start field");

        int endPos = logLine.indexOf('"', curPos);
        if (endPos < 0)
            throw new IllegalStateException("expected double-quote to end field");
        String token = logLine.substring(curPos, endPos);
        curPos = endPos + 2;
        lastPos = curPos;
        return token;
    }


    /**
     *  Parses an ISO-8601 timestamp.
     */
    private Instant parseTimestamp()
    {
        String token = nextToken(' ');
        try
        {
            Instant value = Instant.parse(token);
            lastPos = curPos;
            return value;
        }
        catch (DateTimeParseException ex)
        {
            throw new IllegalStateException("expected timestamp, was: " + token);
        }
    }


    /**
     *  Parses an integer.
     */
    private int parseInt()
    {
        String token = nextToken(' ');
        try
        {
            int value = Integer.parseInt(token);
            lastPos = curPos;
            return value;
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalStateException("expected int, was \"" + token + "\"");
        }
    }


    /**
     *  Parses a floating-point number.
     */
    private double parseDouble()
    {
        String token = nextToken(' ');
        try
        {
            double value = Double.parseDouble(token);
            lastPos = curPos;
            return value;
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalStateException("expected double, was \"" + token + "\"");
        }
    }


    /**
     *  Parses the IP address portion of a host reference. Recognizes that "-"
     *  signals an invalid request, and leaves it for {#link parseHostPort}
     *  (and returns null).
     */
    private String parseTargetHost()
    {
        String token = nextToken(':');
        if (token.startsWith("-"))
        {
            curPos = lastPos;
            return null;
        }
        else
        {
            lastPos = curPos;
            return token;
        }
    }


    /**
     *  Parses the port portion of a host reference. Recognizes that "-" signals
     *  an invalid request and returns null.
     */
    private String parseTargetIp()
    {
        String token = nextToken(' ');
        lastPos = curPos;
        return (token.startsWith("-"))
             ? null
             : token;
    }


    /**
     *  Extracts the connection string from the current log data and
     *  breaks it into its component parts.
     */
    private void parseConnectString()
    {
        int savedPos = lastPos;
        String[] connectStringParts = parseQuoteDelimitedString().split(" ");
        if (connectStringParts.length != 3)
        {
            lastPos = savedPos;
            throw new IllegalStateException("connect string does not have expected components");
        }

        result.httpMethod = connectStringParts[0];
        result.fullUrl = connectStringParts[1];

        String urlAfterScheme = StringUtil.extractRight(result.fullUrl, "://");
        result.urlHost = StringUtil.extractLeft(
                            StringUtil.extractLeft(urlAfterScheme,"/"),
                            ":");

        String urlAfterHost = "/" + StringUtil.extractRight(urlAfterScheme,"/");
        result.urlPath = StringUtil.extractLeft(urlAfterHost, "?");
    }
}
