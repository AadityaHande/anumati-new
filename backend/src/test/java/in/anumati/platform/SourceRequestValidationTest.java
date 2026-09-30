package in.anumati.platform;

import in.anumati.platform.regulatory.SourceRequest;
import in.anumati.platform.regulatory.SourceType;
import in.anumati.platform.regulatory.SourceVerificationStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SourceRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsExactSha256ContentHash() {
        SourceRequest request = new SourceRequest(
                "Official source",
                "https://example.gov.in/source",
                SourceType.OFFICIAL_PORTAL,
                SourceVerificationStatus.VERIFIED,
                null,
                null,
                null,
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsHashWithTrailingCharacters() {
        SourceRequest request = new SourceRequest(
                "Official source",
                "https://example.gov.in/source",
                SourceType.OFFICIAL_PORTAL,
                SourceVerificationStatus.VERIFIED,
                null,
                null,
                null,
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdefx");

        assertThat(validator.validate(request))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("contentHash");
    }
}
