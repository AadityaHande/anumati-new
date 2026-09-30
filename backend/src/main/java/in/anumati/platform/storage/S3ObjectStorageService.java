package in.anumati.platform.storage;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.Instant;

@Service
@Profile("!local")
public class S3ObjectStorageService implements ObjectStorageService {
    private final S3Client s3;
    private final S3Presigner presigner;
    private final StorageProperties properties;

    public S3ObjectStorageService(S3Client s3, S3Presigner presigner, StorageProperties properties) {
        this.s3 = s3;
        this.presigner = presigner;
        this.properties = properties;
    }

    @Override
    public PresignedUpload createUpload(String objectKey, String contentType, Duration expiresIn) {
        var request = software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .build();
        var presigned = presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(expiresIn)
                .putObjectRequest(request)
                .build());
        return new PresignedUpload(presigned.url().toString(), Instant.now().plus(expiresIn));
    }

    @Override
    public java.io.InputStream download(String objectKey) {
        return s3.getObject(GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build());
    }

    @Override
    public void put(String objectKey, java.io.InputStream content, long contentLength, String contentType) {
        s3.putObject(PutObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(objectKey)
                        .contentType(contentType)
                        .contentLength(contentLength)
                        .build(),
                RequestBody.fromInputStream(content, contentLength));
    }

    @Override
    public StoredObject head(String objectKey) {
        var response = s3.headObject(HeadObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build());
        return new StoredObject(objectKey, response.contentLength(), response.contentType(), response.eTag());
    }
}
