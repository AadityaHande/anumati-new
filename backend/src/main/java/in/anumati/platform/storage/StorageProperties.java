package in.anumati.platform.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "anumati.storage")
public record StorageProperties(
        String mode,
        String bucket,
        String region,
        String endpoint,
        String accessKey,
        String secretKey,
        String localRoot,
        String localBaseUrl,
        int presignMinutes,
        long maxFileBytes
) {}
