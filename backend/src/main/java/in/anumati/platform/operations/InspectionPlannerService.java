package in.anumati.platform.operations;

import in.anumati.platform.application.ApplicationRecord;
import in.anumati.platform.application.Inspection;
import in.anumati.platform.application.InspectionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InspectionPlannerService {
    private final InspectionRepository inspections;
    private final int coordinationWindowDays;

    public InspectionPlannerService(InspectionRepository inspections,
                                    @Value("${anumati.inspections.coordination-window-days:3}") int coordinationWindowDays) {
        this.inspections = inspections;
        this.coordinationWindowDays = Math.max(1, coordinationWindowDays);
    }

    @Transactional(readOnly = true)
    public InspectionPlannerResponse plan() {
        List<Inspection> all = inspections.findAllByOrderByScheduledAtAsc();
        LocalDateTime now = LocalDateTime.now();
        List<InspectionPlannerResponse.InspectionItem> items = all.stream()
                .filter(i -> i.getScheduledAt().isAfter(now.minusMinutes(1)))
                .map(this::toItem)
                .toList();

        Map<UUID, List<Inspection>> byBusiness = all.stream()
                .filter(i -> i.getScheduledAt().isAfter(now.minusMinutes(1)))
                .collect(Collectors.groupingBy(i -> i.getApplication().getBusinessProfile().getId()));

        List<InspectionPlannerResponse.CoordinationOpportunity> opportunities = new ArrayList<>();
        for (Map.Entry<UUID, List<Inspection>> entry : byBusiness.entrySet()) {
            List<Inspection> list = entry.getValue();
            if (list.size() < 2) continue;
            list.sort(Comparator.comparing(Inspection::getScheduledAt));
            for (int i = 0; i < list.size(); i++) {
                LocalDate start = list.get(i).getScheduledAt().toLocalDate();
                LocalDate end = start.plusDays(coordinationWindowDays);
                List<Inspection> window = list.stream()
                        .filter(x -> !x.getScheduledAt().toLocalDate().isBefore(start)
                                && !x.getScheduledAt().toLocalDate().isAfter(end))
                        .toList();
                Set<String> authorities = window.stream()
                        .map(x -> x.getApplication().getAuthoritySnapshot())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                if (window.size() >= 2 && authorities.size() >= 2) {
                    var profile = window.getFirst().getApplication().getBusinessProfile();
                    opportunities.add(new InspectionPlannerResponse.CoordinationOpportunity(
                            entry.getKey(), profile.getBusinessName(), profile.getDistrict(), start, end,
                            window.size(), List.copyOf(authorities)));
                    break;
                }
            }
        }

        return new InspectionPlannerResponse(items, opportunities);
    }

    private InspectionPlannerResponse.InspectionItem toItem(Inspection inspection) {
        ApplicationRecord app = inspection.getApplication();
        return new InspectionPlannerResponse.InspectionItem(
                inspection.getId(),
                app.getId(),
                app.getExternalReference() == null ? app.getId().toString() : app.getExternalReference(),
                app.getBusinessProfile().getBusinessName(),
                app.getBusinessProfile().getDistrict(),
                app.getAuthoritySnapshot(),
                inspection.getScheduledAt(),
                inspection.getAssignedOfficer(),
                inspection.getOutcome().name());
    }
}
