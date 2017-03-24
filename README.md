# elb_log_analyzer

A tool to parse [ELB access logs](http://docs.aws.amazon.com/elasticloadbalancing/latest/classic/access-log-collection.html)
and accumulate various statistics.

----

To use, you must first enable access logs on the ELB (note that Elastic Beanstalk disables logs
by default, which mans that you need to re-enable logs whenever you deploy a new beanstalk.

Once you have logs, download them to your local machine. The logs are written using a standard
path that identifies the load balancer and region. I recommend downloading logs on a monthly
or daily basis, unless you have multiple gigabytes of space:

    aws s3 cp --recursive s3://clubos-elb-logs/web/AWSLogs/286482568436/elasticloadbalancing/us-east-1/2017/03 .

Check out and build the project, then point it at one or more directories. It's probably most useful to aggregate
a single day's traffic, as shown here. _Be sure to redirect the output!_

    java -jar target/elb_log_analyzer-1.0.0.jar ~/Transfer/elb-logs/03/22 > /tmp/logstats.txt

The output file aggregates statistics by URL, stripping off any query string. Each row contains a separate URL,
with one or more spaces delimiting fields:

    http://app.club-os.com:80/  numCalls 107  numFails 0  totalTime 0.269   minTime 0.001   maxTime 0.037   avgTime 0.003   stdDevTime 0.005

The numeric fields are output as name followed by value. All times reported in seconds.

* `numCalls` - number of times that the URL was called, both successes and failures.
* `numFails` - number of calls that failed (did not return data to the client).
* `totalTime` - aggregated time performing this call (only successes).
* `minTime` - minimum time for a single call.
* `maxTime` - maximum time for a single call.
* `avgTime` - average time for a single call (total / number of successes).
* `stdDevTime` - standard deviation of call time (lower numbers better).

The raw output isn't terribly readable; I recommend loading into a spreadsheet for analysis.
