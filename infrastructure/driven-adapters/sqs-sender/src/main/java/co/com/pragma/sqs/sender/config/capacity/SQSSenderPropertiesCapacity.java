package co.com.pragma.sqs.sender.config.capacity;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqscapacity")
public record SQSSenderPropertiesCapacity (
        String region,
        String queueUrl,
        String endpoint){
}

