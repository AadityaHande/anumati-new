package in.anumati.platform.business.twin;

import in.anumati.platform.business.BusinessProfileResponse;

import java.time.Instant;
import java.util.UUID;

public record BusinessRegulatoryTwinResponse(UUID businessProfileId, long regulatoryStateVersion, Instant stateCapturedAt, BusinessProfileResponse currentProfile) {}
