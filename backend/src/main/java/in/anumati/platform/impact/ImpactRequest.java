package in.anumati.platform.impact;

import in.anumati.platform.business.BusinessStage;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record ImpactRequest(String sector,String activity,String district,Boolean midcUnit,@DecimalMin("0.0") BigDecimal investmentInr,Integer employees,@DecimalMin("0.0") BigDecimal powerUsageKw,BusinessStage businessStage) {}
