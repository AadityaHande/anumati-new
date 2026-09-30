package in.anumati.platform.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component
@Profile("!local")
public class StorageBucketInitializer {
    private static final Logger log = LoggerFactory.getLogger(StorageBucketInitializer.class);
    private final S3Client s3;
    private final StorageProperties properties;
    private final boolean autoCreate;

    public StorageBucketInitializer(S3Client s3, StorageProperties properties,
                                    @org.springframework.beans.factory.annotation.Value("${anumati.storage.auto-create-bucket:false}") boolean autoCreate) {
        this.s3 = s3;
        this.properties = properties;
        this.autoCreate = autoCreate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureBucket() {
        if (!autoCreate) return;
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(properties.bucket()).build());
        } catch (Exception missing) {
            try {
                s3.createBucket(CreateBucketRequest.builder().bucket(properties.bucket()).build());
                log.info("Created configured object storage bucket {}", properties.bucket());
            } catch (BucketAlreadyOwnedByYouException | BucketAlreadyExistsException ignored) {
                // Another startup instance created it first.
            }
        }
    }
}
