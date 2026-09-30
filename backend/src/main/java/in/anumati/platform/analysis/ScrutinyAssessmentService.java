package in.anumati.platform.analysis;

import in.anumati.platform.business.BusinessProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrutinyAssessmentService {
    private final BigDecimal enhancedInvestmentThreshold;
    private final int enhancedEmployeeThreshold;

    public ScrutinyAssessmentService(
            @Value("${anumati.scrutiny.enhanced-investment-inr:1000000000}") BigDecimal enhancedInvestmentThreshold,
            @Value("${anumati.scrutiny.enhanced-employee-threshold:250}") int enhancedEmployeeThreshold) {
        this.enhancedInvestmentThreshold = enhancedInvestmentThreshold;
        this.enhancedEmployeeThreshold = enhancedEmployeeThreshold;
    }

    public Assessment assess(BusinessProfile profile) {
        List<String> reasons = new ArrayList<>();
        if (profile.getBusinessStage().name().equals("SETUP") || profile.getBusinessStage().name().equals("EXPANSION")) {
            reasons.add("Project is in setup or expansion stage.");
        }
        if (profile.getInvestmentInr().compareTo(enhancedInvestmentThreshold) >= 0) {
            reasons.add("Project investment crosses the configured enhanced-scrutiny threshold.");
        }
        if (profile.getEmployees() >= enhancedEmployeeThreshold) {
            reasons.add("Employee count crosses the configured enhanced-scrutiny threshold.");
        }
        if ("true".equalsIgnoreCase(profile.getRegulatoryAttributes().get("hazardousProcess"))) {
            reasons.add("Business profile declares a hazardous process characteristic.");
        }
        if ("true".equalsIgnoreCase(profile.getRegulatoryAttributes().get("environmentalConsentRequired"))) {
            reasons.add("Business profile declares environmental consent relevance.");
        }
        boolean high = "true".equalsIgnoreCase(profile.getRegulatoryAttributes().get("hazardousProcess"))
                && ("true".equalsIgnoreCase(profile.getRegulatoryAttributes().get("environmentalConsentRequired"))
                || profile.getInvestmentInr().compareTo(enhancedInvestmentThreshold.multiply(new BigDecimal("2"))) >= 0);
        String tier = high ? "HIGH" : reasons.size() >= 2 ? "ENHANCED" : "STANDARD";
        return new Assessment(tier, List.copyOf(reasons));
    }

    public record Assessment(String tier, List<String> reasons) {}
}
