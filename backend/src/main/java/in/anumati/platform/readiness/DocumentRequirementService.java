package in.anumati.platform.readiness;

import in.anumati.platform.regulatory.Approval;
import in.anumati.platform.regulatory.ApprovalRepository;
import in.anumati.platform.regulatory.RegulatorySource;
import in.anumati.platform.regulatory.RegulatorySourceRepository;
import in.anumati.platform.regulatory.SourceVerificationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class DocumentRequirementService {
    private final ApprovalRepository approvalRepository;
    private final RegulatorySourceRepository sourceRepository;
    private final DocumentRequirementRepository requirementRepository;

    public DocumentRequirementService(ApprovalRepository approvalRepository,
                                      RegulatorySourceRepository sourceRepository,
                                      DocumentRequirementRepository requirementRepository) {
        this.approvalRepository = approvalRepository;
        this.sourceRepository = sourceRepository;
        this.requirementRepository = requirementRepository;
    }

    @Transactional
    public DocumentRequirement create(DocumentRequirementRequest request, String actor) {
        Approval approval = approvalRepository.findById(request.approvalId())
                .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + request.approvalId()));
        RegulatorySource source = sourceRepository.findById(request.sourceId())
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + request.sourceId()));
        if (source.getVerificationStatus() != SourceVerificationStatus.VERIFIED) {
            throw new IllegalArgumentException("A document requirement cannot use an unverified source.");
        }
        DocumentRequirement requirement = new DocumentRequirement(
                approval, request.category(), request.documentName(), request.description(),
                request.mandatory(), request.active(), source);
        requirementRepository.save(requirement);
        return requirement;
    }
}
