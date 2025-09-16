package co.com.pragma.sqs.sender.config.report;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqsreport")
public record SQSSenderReportProperties(
        String region,
        String queueUrl,
        String endpoint){
}

