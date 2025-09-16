package co.com.pragma.sqs.sender.sendernotification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqsnotificacionesporcorreo")
public record SQSSenderPropertiesNotification(
        String region,
        String queueUrl,
        String endpoint) {
}


