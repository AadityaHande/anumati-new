package in.anumati.platform.regulatory;

import in.anumati.platform.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
public class RegulatoryAdminService {
    private final RegulatorySourceRepository sourceRepository;
    private final ApprovalRepository approvalRepository;
    private final RegulatoryRuleRepository ruleRepository;
    private final AuditService auditService;

    public RegulatoryAdminService(RegulatorySourceRepository sourceRepository,
                                  ApprovalRepository approvalRepository,
                                  RegulatoryRuleRepository ruleRepository,
                                  AuditService auditService) {
        this.sourceRepository = sourceRepository;
        this.approvalRepository = approvalRepository;
        this.ruleRepository = ruleRepository;
        this.auditService = auditService;
    }

    @Transactional
    public RegulatorySource createSource(SourceRequest request, String actor) {
        if (!request.url().startsWith("https://")) {
            throw new IllegalArgumentException("Regulatory sources must use HTTPS.");
        }
        if (request.verificationStatus() == SourceVerificationStatus.VERIFIED && (request.contentHash() == null || request.contentHash().isBlank())) {
            throw new IllegalArgumentException("A verified source must have a content hash for traceability.");
        }
        if (request.effectiveFrom() != null && request.expiresOn() != null && request.expiresOn().isBefore(request.effectiveFrom())) {
            throw new IllegalArgumentException("Source expiry date must be on or after the effective date.");
        }
        Instant verifiedAt = request.verificationStatus() == SourceVerificationStatus.VERIFIED ? Instant.now() : null;
        RegulatorySource source = new RegulatorySource(request.title(), request.url(), request.sourceType(),
                request.verificationStatus(), request.publishedOn(), request.effectiveFrom(), request.expiresOn(), verifiedAt,
                request.contentHash());
        sourceRepository.save(source);
        auditService.record(actor, "REGULATORY_SOURCE_CREATED", "RegulatorySource", source.getId(),
                Map.of("verificationStatus", source.getVerificationStatus().name()));
        return source;
    }

    @Transactional
    public Approval createApproval(ApprovalRequest request, String actor) {
        RegulatorySource source = sourceRepository.findById(request.sourceId())
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + request.sourceId()));
        if (source.getVerificationStatus() != SourceVerificationStatus.VERIFIED) {
            throw new IllegalArgumentException("An approval cannot be published against an unverified source.");
        }
        Approval approval = new Approval(request.code(), request.name(), request.authority(), request.purpose(), source, request.active());
        approvalRepository.save(approval);
        auditService.record(actor, "APPROVAL_CREATED", "Approval", approval.getId(), Map.of("code", approval.getCode()));
        return approval;
    }

    @Transactional
    public RegulatoryRule createRule(RuleRequest request, String actor) {
        Approval approval = approvalRepository.findById(request.approvalId())
                .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + request.approvalId()));
        RegulatorySource source = sourceRepository.findById(request.sourceId())
                .orElseThrow(() -> new IllegalArgumentException("Source not found: " + request.sourceId()));
        if (source.getVerificationStatus() != SourceVerificationStatus.VERIFIED) {
            throw new IllegalArgumentException("A rule cannot be published against an unverified source.");
        }
        if (request.effectiveFrom() != null && request.expiresOn() != null && request.expiresOn().isBefore(request.effectiveFrom())) {
            throw new IllegalArgumentException("Rule expiry date must be on or after the effective date.");
        }
        if (request.conditions().isEmpty()) {
            throw new IllegalArgumentException("A regulatory rule must contain at least one condition.");
        }
        RegulatoryRule rule = new RegulatoryRule(request.code(), request.name(), approval, source,
                request.outcome(), request.priority(), request.versionNumber(), request.active(), request.effectiveFrom(), request.expiresOn());
        request.conditions().forEach(c -> rule.addCondition(new RuleCondition(c.field(), c.operator(), c.valueType(), c.value(), c.sequenceNumber())));
        ruleRepository.save(rule);
        auditService.record(actor, "REGULATORY_RULE_CREATED", "RegulatoryRule", rule.getId(),
                Map.of("code", rule.getCode(), "version", rule.getVersionNumber()));
        return rule;
    }

    @Transactional(readOnly = true)
    public java.util.List<RegulatorySourceResponse> listSources() {
        return sourceRepository.findAll().stream().map(RegulatorySourceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<SourceChangeResponse> sourceChanges() {
        java.util.Map<String, java.util.List<RegulatorySource>> grouped = sourceRepository.findAll().stream()
                .collect(java.util.stream.Collectors.groupingBy(RegulatorySource::getTitle, java.util.LinkedHashMap::new, java.util.stream.Collectors.toList()));
        java.util.List<SourceChangeResponse> result = new java.util.ArrayList<>();
        for (var entry : grouped.entrySet()) {
            var versions = entry.getValue().stream()
                    .sorted(java.util.Comparator.comparing(RegulatorySource::getPublishedOn, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())))
                    .map(s -> new SourceChangeResponse.Version(s.getId(), s.getTitle(), s.getUrl(), s.getVerificationStatus().name(), s.getPublishedOn(), s.getEffectiveFrom(), s.getExpiresOn(), s.getContentHash()))
                    .toList();
            long distinctHashes = versions.stream().map(SourceChangeResponse.Version::contentHash).filter(java.util.Objects::nonNull).distinct().count();
            if (versions.size() > 1) {
                result.add(new SourceChangeResponse(entry.getKey(), distinctHashes > 1, versions));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public java.util.List<Approval> listApprovals() {
        return approvalRepository.findAll();
    }
}
