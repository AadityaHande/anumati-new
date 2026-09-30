package in.anumati.platform.document;

import in.anumati.platform.audit.AuditService;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessProfileService;
import in.anumati.platform.storage.ObjectStorageService;
import in.anumati.platform.storage.StorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf", "image/png", "image/jpeg", "text/plain",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private final BusinessProfileService profileService;
    private final DocumentRepository repository;
    private final ObjectStorageService storage;
    private final StorageProperties storageProperties;
    private final AuditService auditService;
    private final String adminUsername;

    public DocumentService(BusinessProfileService profileService, DocumentRepository repository,
                           ObjectStorageService storage, StorageProperties storageProperties,
                           AuditService auditService,
                           @Value("${anumati.security.admin-username:admin}") String adminUsername) {
        this.profileService = profileService;
        this.repository = repository;
        this.storage = storage;
        this.storageProperties = storageProperties;
        this.auditService = auditService;
        this.adminUsername = adminUsername;
    }

    @Transactional
    public PresignResponse createPresignedUpload(DocumentRequest request, String actor) {
        if (!ALLOWED_TYPES.contains(request.contentType())) {
            throw new IllegalArgumentException("Unsupported document type: " + request.contentType());
        }
        if (request.sizeBytes() > storageProperties.maxFileBytes()) {
            throw new IllegalArgumentException("Document exceeds maximum allowed size");
        }
        BusinessProfile profile = profileService.getForActor(request.businessProfileId(), actor);
        String safeName = request.originalFilename().replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = "business/" + profile.getId() + "/documents/" + UUID.randomUUID() + "-" + safeName;
        Document document = repository.save(new Document(profile, request.category(), request.originalFilename(),
                request.contentType(), request.sizeBytes(), key));
        var upload = storage.createUpload(key, request.contentType(), Duration.ofMinutes(storageProperties.presignMinutes()));
        auditService.record(actor, "DOCUMENT_UPLOAD_URL_CREATED", "Document", document.getId(),
                java.util.Map.of("businessProfileId", profile.getId(), "objectKey", key));
        return new PresignResponse(document.getId(), key, upload.url(), upload.expiresAt(), document.getStatus());
    }

    @Transactional
    public void uploadLocal(String objectKey, java.io.InputStream content, long contentLength, String contentType, String actor) {
        Document document = repository.findByObjectKey(objectKey)
                .orElseThrow(() -> new IllegalArgumentException("Document upload target not found"));
        ensureOwnerOrAdmin(document.getBusinessProfile(), actor);
        if (contentLength < 0 || contentLength != document.getDeclaredSizeBytes()) {
            throw new IllegalArgumentException("Uploaded file size does not match the declared size");
        }
        if (contentType != null && !contentType.isBlank() && !document.getContentType().equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Uploaded content type does not match the declared document type");
        }
        storage.put(objectKey, content, contentLength, document.getContentType());
    }

    @Transactional
    public DocumentResponse complete(UUID documentId, String actor) {
        Document document = repository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        ensureOwnerOrAdmin(document.getBusinessProfile(), actor);
        var stored = storage.head(document.getObjectKey());
        if (stored.contentLength() != document.getDeclaredSizeBytes()) {
            throw new IllegalArgumentException("Uploaded file size does not match the declared size");
        }
        if (stored.contentType() != null && !stored.contentType().isBlank() && !document.getContentType().equalsIgnoreCase(stored.contentType())) {
            throw new IllegalArgumentException("Uploaded content type does not match the declared document type");
        }
        document.markUploaded(stored.contentLength(), stored.eTag());
        auditService.record(actor, "DOCUMENT_UPLOADED", "Document", document.getId(),
                java.util.Map.of("businessProfileId", document.getBusinessProfile().getId()));
        return DocumentResponse.from(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(UUID businessProfileId, String actor) {
        profileService.getForActor(businessProfileId, actor);
        return repository.findByBusinessProfile_IdOrderByCreatedAtDesc(businessProfileId).stream().map(DocumentResponse::from).toList();
    }

    private void ensureOwnerOrAdmin(BusinessProfile profile, String actor) {
        if (!profile.getOwnerActor().equals(actor) && !adminUsername.equals(actor)) {
            throw new org.springframework.security.access.AccessDeniedException("Not permitted for this business profile");
        }
    }
}
