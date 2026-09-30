package in.anumati.platform.business;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Map;

public record BusinessProfileRequest(
        @NotBlank @Size(max = 250) String businessName,
        @NotBlank @Size(max = 100) String sector,
        @NotBlank @Size(max = 200) String activity,
        @NotBlank @Size(max = 100) String district,
        @NotNull Boolean midcUnit,
        @NotNull @DecimalMin("0.0") BigDecimal investmentInr,
        @jakarta.validation.constraints.Pattern(regexp = "^$|[A-Z]{5}[0-9]{4}[A-Z]$", message = "Invalid PAN format") String panNumber,
        @jakarta.validation.constraints.Pattern(regexp = "^$|[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$", message = "Invalid GSTIN format") String gstin,
        @NotNull @Min(0) Integer employees,
        @NotNull @Size(max = 32) Map<String, String> regulatoryAttributes,
        @NotNull @DecimalMin("0.0") BigDecimal powerUsageKw,
        @NotNull BusinessStage businessStage
) {}
