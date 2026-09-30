package in.anumati.platform.business.twin;

import in.anumati.platform.business.BusinessProfileService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BusinessRegulatoryTwinService {
    private final BusinessProfileService profiles;

    public BusinessRegulatoryTwinService(BusinessProfileService profiles) { this.profiles = profiles; }

    public BusinessRegulatoryTwinResponse current(UUID businessProfileId, String actor) {
        var profile = profiles.getForActor(businessProfileId, actor);
        return new BusinessRegulatoryTwinResponse(profile.getId(), profile.getVersionNumber(), profile.getUpdatedAt(), in.anumati.platform.business.BusinessProfileResponse.from(profile));
    }
}
