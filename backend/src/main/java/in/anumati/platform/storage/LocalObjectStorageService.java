package in.anumati.platform.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Deterministic local object storage used by the hackathon/demo profile.
 * It keeps the same ObjectStorageService contract as S3 so the workflow is real
 * locally without requiring a cloud account.
 */
@Service
@Profile("local")
public class LocalObjectStorageService implements ObjectStorageService {
    private final StorageProperties properties;
    private final Path root;

    public LocalObjectStorageService(StorageProperties properties) {
        this.properties = properties;
        this.root = Path.of(properties.localRoot()).toAbsolutePath().normalize();
    }

    @Override
    public PresignedUpload createUpload(String objectKey, String contentType, Duration expiresIn) {
        String encoded = URLEncoder.encode(objectKey, StandardCharsets.UTF_8);
        String url = properties.localBaseUrl().replaceAll("/$", "") + "/api/v1/documents/local-upload?key=" + encoded;
        return new PresignedUpload(url, Instant.now().plus(expiresIn));
    }

    @Override
    public void put(String objectKey, InputStream content, long contentLength, String contentType) {
        try {
            Path target = resolve(objectKey);
            Files.createDirectories(target.getParent());
            long copied = Files.copy(content, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            if (copied != contentLength) {
                Files.deleteIfExists(target);
                throw new IllegalArgumentException("Uploaded file size does not match the declared size");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not store the uploaded document locally", ex);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        try { return Files.newInputStream(resolve(objectKey)); }
        catch (IOException ex) { throw new IllegalStateException("Stored document could not be opened", ex); }
    }

    @Override
    public StoredObject head(String objectKey) {
        try {
            Path file = resolve(objectKey);
            long size = Files.size(file);
            String contentType = Files.probeContentType(file);
            String etag = Long.toHexString(Files.getLastModifiedTime(file).toMillis()) + "-" + size;
            return new StoredObject(objectKey, size, contentType, etag);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Uploaded document was not found in local storage", ex);
        }
    }

    private Path resolve(String objectKey) {
        Path target = root.resolve(objectKey).normalize();
        if (!target.startsWith(root)) throw new IllegalArgumentException("Invalid storage key");
        return target;
    }
}
