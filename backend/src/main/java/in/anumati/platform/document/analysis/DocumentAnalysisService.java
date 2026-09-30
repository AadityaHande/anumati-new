package in.anumati.platform.document.analysis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.anumati.platform.audit.AuditService;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.document.Document;
import in.anumati.platform.document.DocumentRepository;
import in.anumati.platform.business.BusinessProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DocumentAnalysisService {
    private final DocumentRepository documentRepository;
    private final DocumentExtractionRepository extractionRepository;
    private final DocumentConsistencyCheckRepository consistencyRepository;
    private final DocumentTextExtractionService textExtractionService;
    private final DocumentFieldExtractor fieldExtractor;
    private final BusinessProfileService profileService;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public DocumentAnalysisService(DocumentRepository documentRepository,
                                   DocumentExtractionRepository extractionRepository,
                                   DocumentConsistencyCheckRepository consistencyRepository,
                                   DocumentTextExtractionService textExtractionService,
                                   DocumentFieldExtractor fieldExtractor,
                                   BusinessProfileService profileService,
                                   ObjectMapper objectMapper,
                                   AuditService auditService) {
        this.documentRepository = documentRepository;
        this.extractionRepository = extractionRepository;
        this.consistencyRepository = consistencyRepository;
        this.textExtractionService = textExtractionService;
        this.fieldExtractor = fieldExtractor;
        this.profileService = profileService;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @Transactional
    public DocumentAnalysisResponse analyze(UUID documentId, String actor) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        if (document.getStatus() == in.anumati.platform.document.DocumentStatus.CREATED) {
            throw new IllegalArgumentException("Document must be uploaded before analysis");
        }
        BusinessProfile profile = profileService.getForActor(document.getBusinessProfile().getId(), actor);
        String text;
        Map<String, String> fields;
        try {
            text = textExtractionService.extract(document);
            fields = fieldExtractor.extract(text);
        } catch (RuntimeException ex) {
            document.markNeedsReview();
            DocumentExtraction extraction = extractionRepository.findByDocument_Id(documentId).orElseGet(() ->
                    new DocumentExtraction(document, "LOCAL_TESSERACT", DocumentAnalysisStatus.FAILED,
                            "", "{}", ex.getMessage()));
            extraction.update("LOCAL_TESSERACT", DocumentAnalysisStatus.FAILED, "", "{}", ex.getMessage());
            extractionRepository.save(extraction);
            auditService.record(actor, "DOCUMENT_ANALYSIS_FAILED", "Document", documentId,
                    Map.of("businessProfileId", profile.getId()));
            throw ex;
        }

        try {
            String fieldsJson = objectMapper.writeValueAsString(fields);
            DocumentExtraction extraction = extractionRepository.findByDocument_Id(documentId).orElseGet(() ->
                    new DocumentExtraction(document, "LOCAL_TESSERACT", DocumentAnalysisStatus.COMPLETED,
                            text, fieldsJson, null));
            extraction.update("LOCAL_TESSERACT", DocumentAnalysisStatus.COMPLETED, text, fieldsJson, null);
            extractionRepository.save(extraction);

            consistencyRepository.findByDocument_IdOrderByFieldNameAsc(documentId)
                    .forEach(consistencyRepository::delete);

            List<DocumentAnalysisResponse.Consistency> results = new ArrayList<>();
            compareWhenPresent(results, document, "BUSINESS_NAME", profile.getBusinessName(), fields.get("BUSINESS_NAME"));
            compareWhenPresent(results, document, "DISTRICT", profile.getDistrict(), fields.get("DISTRICT"));
            compareWhenPresent(results, document, "PAN", profile.getPanNumber(), fields.get("PAN"));
            compareWhenPresent(results, document, "GSTIN", profile.getGstin(), fields.get("GSTIN"));
            requireCategoryFieldIfDeclared(results, document, profile, fields);

            boolean mismatch = results.stream().anyMatch(c -> c.status() == ConsistencyStatus.MISMATCH);
            boolean notFound = results.stream().anyMatch(c -> c.status() == ConsistencyStatus.NOT_FOUND);
            boolean noComparableEvidence = results.isEmpty();
            if (mismatch || notFound || noComparableEvidence) document.markNeedsReview(); else document.markReady(profile.getVersionNumber());

            auditService.record(actor, "DOCUMENT_ANALYZED", "Document", documentId,
                    Map.of("businessProfileId", profile.getId(), "consistencyStatus", document.getStatus().name()));

            return new DocumentAnalysisResponse(documentId, DocumentAnalysisStatus.COMPLETED, "LOCAL_TESSERACT",
                    fields, results, document.getStatus().name(), extraction.getAnalyzedAt(), null);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not save document analysis", ex);
        }
    }

    @Transactional(readOnly = true)
    public DocumentAnalysisResponse get(UUID documentId, String actor) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
        profileService.getForActor(document.getBusinessProfile().getId(), actor);
        DocumentExtraction extraction = extractionRepository.findByDocument_Id(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document has not been analyzed"));
        try {
            Map<String, String> fields = objectMapper.readValue(extraction.getExtractedFieldsJson(), new TypeReference<>() {});
            List<DocumentAnalysisResponse.Consistency> consistency = consistencyRepository.findByDocument_IdOrderByFieldNameAsc(documentId).stream()
                    .map(c -> new DocumentAnalysisResponse.Consistency(c.getFieldName(), c.getProfileValue(), c.getDocumentValue(), c.getStatus(), c.getReason()))
                    .toList();
            return new DocumentAnalysisResponse(documentId, extraction.getStatus(), extraction.getEngine(), fields,
                    consistency, document.getStatus().name(), extraction.getAnalyzedAt(), extraction.getErrorMessage());
        } catch (Exception e) {
            throw new IllegalStateException("Could not read document analysis", e);
        }
    }

    private void compareWhenPresent(List<DocumentAnalysisResponse.Consistency> results, Document document, String fieldName, String profileValue, String documentValue) {
        if (documentValue != null && !documentValue.isBlank()) check(results, document, fieldName, profileValue, documentValue);
    }

    private void requireCategoryFieldIfDeclared(List<DocumentAnalysisResponse.Consistency> results, Document document, BusinessProfile profile, Map<String,String> fields) {
        String category = document.getNormalizedCategory();
        String required = null;
        if (category.contains("PAN")) required = "PAN";
        else if (category.contains("GST")) required = "GSTIN";
        else if (category.contains("ADDRESS")) required = "DISTRICT";
        if (required != null && fields.get(required) == null) {
            String profileValue = switch (required) { case "PAN" -> profile.getPanNumber(); case "GSTIN" -> profile.getGstin(); default -> profile.getDistrict(); };
            results.add(new DocumentAnalysisResponse.Consistency(required, profileValue, null, ConsistencyStatus.NOT_FOUND, "The category indicates this field should be present, but it was not confidently extracted."));
            consistencyRepository.save(new DocumentConsistencyCheck(document, required, profileValue, null, ConsistencyStatus.NOT_FOUND, "The category indicates this field should be present, but it was not confidently extracted."));
        }
    }

    private void check(List<DocumentAnalysisResponse.Consistency> results, Document document,
                       String fieldName, String profileValue, String documentValue) {
        ConsistencyStatus status;
        String reason;
        if (documentValue == null || documentValue.isBlank()) {
            status = ConsistencyStatus.NOT_FOUND;
            reason = "The field was not confidently extracted from the document.";
        } else if (normalize(profileValue).equals(normalize(documentValue))) {
            status = ConsistencyStatus.MATCH;
            reason = "Extracted value matches the business profile.";
        } else {
            status = ConsistencyStatus.MISMATCH;
            reason = "Extracted value differs from the business profile. Review required.";
        }
        DocumentConsistencyCheck saved = consistencyRepository.save(
                new DocumentConsistencyCheck(document, fieldName, profileValue, documentValue, status, reason));
        results.add(new DocumentAnalysisResponse.Consistency(saved.getFieldName(), saved.getProfileValue(), saved.getDocumentValue(), saved.getStatus(), saved.getReason()));
    }



    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
