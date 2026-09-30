package in.anumati.platform.business;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record BusinessProfileResponse(
        UUID id, String businessName, String sector, String activity, String district,
        boolean midcUnit, BigDecimal investmentInr, String panNumber, String gstin, int employees, Map<String, String> regulatoryAttributes, BigDecimal powerUsageKw,
        BusinessStage businessStage, long versionNumber, Instant createdAt, Instant updatedAt) {

    public static BusinessProfileResponse from(BusinessProfile p) {
        return new BusinessProfileResponse(p.getId(), p.getBusinessName(), p.getSector(), p.getActivity(),
                p.getDistrict(), p.getMidcUnit(), p.getInvestmentInr(), p.getPanNumber(), p.getGstin(), p.getEmployees(), p.getRegulatoryAttributes(), p.getPowerUsageKw(),
                p.getBusinessStage(), p.getVersionNumber(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
