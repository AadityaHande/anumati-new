package in.anumati.platform;

import in.anumati.platform.business.BusinessProfileRequest;
import in.anumati.platform.business.BusinessStage;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessProfileRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidIdentityAndNegativeOperationalValues() {
        BusinessProfileRequest request = new BusinessProfileRequest(
                "Test Unit", "Manufacturing", "Assembly", "Pune", true,
                new BigDecimal("1000000"), "INVALIDPAN", "INVALIDGSTIN", -1,
                java.util.Map.of(), new BigDecimal("-1"), BusinessStage.SETUP);

        var violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("panNumber", "gstin", "employees", "powerUsageKw");
    }

    @Test
    void acceptsOptionalBlankPanAndGstin() {
        BusinessProfileRequest request = new BusinessProfileRequest(
                "Test Unit", "Manufacturing", "Assembly", "Pune", false,
                BigDecimal.ZERO, "", "", 0, java.util.Map.of(), BigDecimal.ZERO, BusinessStage.IDEA);

        assertThat(validator.validate(request)).isEmpty();
    }
}
