package in.anumati.platform.business;

import in.anumati.platform.audit.AuditService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;

@Service
public class BusinessProfileService {
    private final BusinessProfileRepository repository;
    private final AuditService auditService;
    private final String adminUsername;
    private final BusinessProfileVersionRepository versionRepository;

    public BusinessProfileService(BusinessProfileRepository repository, AuditService auditService,
                                  @Value("${anumati.security.admin-username:admin}") String adminUsername,
                                  BusinessProfileVersionRepository versionRepository) {
        this.repository = repository;
        this.auditService = auditService;
        this.adminUsername = adminUsername;
        this.versionRepository = versionRepository;
    }

    @Transactional
    public BusinessProfileResponse create(BusinessProfileRequest request, String actor) {
        BusinessProfile profile = new BusinessProfile(
                request.businessName(), request.sector(), request.activity(), request.district(),
                request.midcUnit(), request.investmentInr(), request.panNumber(), request.gstin(), request.employees(),
                request.regulatoryAttributes(), request.powerUsageKw(), request.businessStage(), actor);
        repository.save(profile);
        versionRepository.save(snapshot(profile, "CREATED", actor));
        auditService.record(actor, "BUSINESS_PROFILE_CREATED", "BusinessProfile", profile.getId(),
                Map.of("version", profile.getVersionNumber()));
        return BusinessProfileResponse.from(profile);
    }


    @Transactional
    public BusinessProfileResponse update(UUID id, BusinessProfileRequest request, String actor) {
        BusinessProfile profile = getForActor(id, actor);
        long before = profile.getVersionNumber();
        profile.update(request.businessName(), request.sector(), request.activity(), request.district(),
                request.midcUnit(), request.investmentInr(), request.panNumber(), request.gstin(),
                request.employees(), request.regulatoryAttributes(), request.powerUsageKw(), request.businessStage());
        repository.save(profile);
        versionRepository.save(snapshot(profile, "UPDATED", actor));
        auditService.record(actor, "BUSINESS_PROFILE_UPDATED", "BusinessProfile", id,
                Map.of("previousVersion", before, "newVersion", profile.getVersionNumber()));
        return BusinessProfileResponse.from(profile);
    }


    private BusinessProfileVersion snapshot(BusinessProfile p, String changeType, String actor) {
        Map<String,Object> data = new LinkedHashMap<>();
        data.put("businessName", p.getBusinessName());
        data.put("sector", p.getSector());
        data.put("activity", p.getActivity());
        data.put("district", p.getDistrict());
        data.put("midcUnit", p.getMidcUnit());
        data.put("investmentInr", p.getInvestmentInr());
        data.put("panNumber", p.getPanNumber());
        data.put("gstin", p.getGstin());
        data.put("employees", p.getEmployees());
        data.put("regulatoryAttributes", p.getRegulatoryAttributes());
        data.put("powerUsageKw", p.getPowerUsageKw());
        data.put("businessStage", p.getBusinessStage().name());
        return new BusinessProfileVersion(p.getId(), p.getVersionNumber(), data, changeType, actor);
    }

    @Transactional(readOnly = true)
    public java.util.List<BusinessProfileResponse> listForActor(String actor) {
        return repository.findByOwnerActorOrderByUpdatedAtDesc(actor).stream()
                .map(BusinessProfileResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessProfile get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Business profile not found: " + id));
    }

    @Transactional(readOnly = true)
    public BusinessProfile getForActor(UUID id, String actor) {
        BusinessProfile profile = get(id);
        if (!profile.getOwnerActor().equals(actor) && !adminUsername.equals(actor)) {
            throw new org.springframework.security.access.AccessDeniedException("Not permitted for this business profile");
        }
        return profile;
    }

    @Transactional(readOnly = true)
    public BusinessProfileResponse getResponse(UUID id, String actor) {
        return BusinessProfileResponse.from(getForActor(id, actor));
    }
}
