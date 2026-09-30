package in.anumati.platform.business;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record BusinessProfileVersionResponse(UUID id, UUID businessProfileId, long versionNumber, Map<String,Object> snapshot, String changeType, String capturedBy, Instant capturedAt) {
    public static BusinessProfileVersionResponse from(BusinessProfileVersion v){
        return new BusinessProfileVersionResponse(v.getId(), v.getBusinessProfileId(), v.getVersionNumber(), v.getSnapshot(), v.getChangeType(), v.getCapturedBy(), v.getCapturedAt());
    }
}
