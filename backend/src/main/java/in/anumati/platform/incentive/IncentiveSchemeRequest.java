package in.anumati.platform.incentive;
import jakarta.validation.constraints.*;
import java.util.*;
public record IncentiveSchemeRequest(@NotBlank String code,@NotBlank String name,@NotBlank String authority,@NotBlank String benefitSummary,String applicationUrl,@NotNull UUID sourceId,@NotEmpty List<IncentiveConditionRequest> conditions,boolean active) {}
