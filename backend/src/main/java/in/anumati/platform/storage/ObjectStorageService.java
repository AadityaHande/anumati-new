package in.anumati.platform.storage;

import java.time.Duration;
import java.time.Instant;
import java.io.InputStream;

public interface ObjectStorageService {
    PresignedUpload createUpload(String objectKey, String contentType, Duration expiresIn);
    StoredObject head(String objectKey);
    InputStream download(String objectKey);
    void put(String objectKey, InputStream content, long contentLength, String contentType);

    record PresignedUpload(String url, Instant expiresAt) {}
    record StoredObject(String key, long contentLength, String contentType, String eTag) {}
}
