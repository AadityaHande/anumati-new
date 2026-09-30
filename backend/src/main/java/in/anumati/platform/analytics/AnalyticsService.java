package in.anumati.platform.analytics;

import in.anumati.platform.application.ApplicationRecord;
import in.anumati.platform.application.ApplicationRepository;
import in.anumati.platform.application.ApplicationStatus;
import in.anumati.platform.application.ApplicationQueryRepository;
import in.anumati.platform.application.QueryStatus;
import in.anumati.platform.grievance.GrievanceRepository;
import in.anumati.platform.grievance.GrievanceStatus;
import in.anumati.platform.compliance.ComplianceRepository;
import in.anumati.platform.compliance.ComplianceObligation;
import in.anumati.platform.compliance.ComplianceStatus;
import in.anumati.platform.sla.SlaService;
import in.anumati.platform.sla.SlaStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private final ApplicationRepository apps;
    private final ApplicationQueryRepository queries;
    private final GrievanceRepository grievances;
    private final ComplianceRepository compliance;
    private final SlaService sla;

    public AnalyticsService(ApplicationRepository apps, ApplicationQueryRepository queries, GrievanceRepository grievances, ComplianceRepository compliance, SlaService sla) {
        this.apps = apps;
        this.queries = queries;
        this.grievances = grievances;
        this.compliance = compliance;
        this.sla = sla;
    }

    @Transactional(readOnly = true)
    public DepartmentDashboardResponse summary() {
        List<ApplicationRecord> all = apps.findAll();
        long pending = all.stream().filter(a -> a.getStatus() == ApplicationStatus.UNDER_SCRUTINY).count();
        long awaiting = all.stream().flatMap(a -> queries.findByApplication_IdOrderByRaisedAtAsc(a.getId()).stream()).filter(q -> q.getStatus() == QueryStatus.OPEN).count();
        long risk = all.stream().map(sla::status).filter(x -> x.status() == SlaStatus.AT_RISK).count();
        long overdue = all.stream().map(sla::status).filter(x -> x.status() == SlaStatus.OVERDUE).count();
        long insp = all.stream().filter(a -> a.getStatus() == ApplicationStatus.INSPECTION_SCHEDULED).count();
        Map<String, Long> byStatus = all.stream().collect(Collectors.groupingBy(a -> a.getStatus().name(), TreeMap::new, Collectors.counting()));
        Map<String, Long> byDept = all.stream().collect(Collectors.groupingBy(ApplicationRecord::getAuthoritySnapshot, TreeMap::new, Collectors.counting()));
        long complianceDue = compliance.findAllByOrderByDueDateAsc().stream().peek(ComplianceObligation::refresh).filter(c -> c.getStatus() == ComplianceStatus.DUE || c.getStatus() == ComplianceStatus.OVERDUE).count();
        return new DepartmentDashboardResponse(all.size(), pending, awaiting, risk, overdue, insp,
                all.stream().filter(a -> a.getStatus() == ApplicationStatus.APPROVED).count(),
                all.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count(),
                grievances.countByStatus(GrievanceStatus.OPEN), complianceDue, byStatus, byDept);
    }
}
