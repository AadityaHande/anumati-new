package in.anumati.platform;

import in.anumati.platform.audit.AuditService;
import in.anumati.platform.dependency.*;
import in.anumati.platform.regulatory.Approval;
import in.anumati.platform.regulatory.ApprovalRepository;
import in.anumati.platform.regulatory.RegulatorySource;
import in.anumati.platform.regulatory.SourceType;
import in.anumati.platform.regulatory.SourceVerificationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalDependencyServiceTest {
    @Mock ApprovalDependencyRepository dependencyRepository;
    @Mock ApprovalRepository approvalRepository;
    @Mock AuditService auditService;

    @Test
    void rejectsSelfDependency() {
        RegulatorySource source = source();
        Approval approval = new Approval("A", "A", "Authority", "", source, true);
        when(approvalRepository.findById(approval.getId())).thenReturn(Optional.of(approval));

        ApprovalDependencyService service = new ApprovalDependencyService(dependencyRepository, approvalRepository, auditService);
        var request = new ApprovalDependencyRequest(approval.getId(), approval.getId(), DependencyType.BLOCKING, "self", true);

        assertThatThrownBy(() -> service.create(request, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot depend on itself");
        verifyNoInteractions(dependencyRepository);
    }

    @Test
    void rejectsCycleBeforePersisting() {
        RegulatorySource source = source();
        Approval a = new Approval("A", "A", "Authority", "", source, true);
        Approval b = new Approval("B", "B", "Authority", "", source, true);
        when(approvalRepository.findById(a.getId())).thenReturn(Optional.of(a));
        when(approvalRepository.findById(b.getId())).thenReturn(Optional.of(b));
        ApprovalDependency existing = new ApprovalDependency(b, a, DependencyType.BLOCKING, "existing", true);
        when(dependencyRepository.findByActiveTrue()).thenReturn(List.of(existing));

        ApprovalDependencyService service = new ApprovalDependencyService(dependencyRepository, approvalRepository, auditService);
        var request = new ApprovalDependencyRequest(a.getId(), b.getId(), DependencyType.BLOCKING, "cycle", true);

        assertThatThrownBy(() -> service.create(request, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cycle");
        verify(dependencyRepository, never()).save(any());
    }

    @Test
    void rejectsIndirectThreeNodeCycleBeforePersisting() {
        RegulatorySource source = source();
        Approval a = new Approval("A", "A", "Authority", "", source, true);
        Approval b = new Approval("B", "B", "Authority", "", source, true);
        Approval c = new Approval("C", "C", "Authority", "", source, true);
        when(approvalRepository.findById(c.getId())).thenReturn(Optional.of(c));
        when(approvalRepository.findById(a.getId())).thenReturn(Optional.of(a));
        when(dependencyRepository.findByActiveTrue()).thenReturn(List.of(
                new ApprovalDependency(a, b, DependencyType.BLOCKING, "A needs B", true),
                new ApprovalDependency(b, c, DependencyType.BLOCKING, "B needs C", true)
        ));

        ApprovalDependencyService service = new ApprovalDependencyService(dependencyRepository, approvalRepository, auditService);
        var request = new ApprovalDependencyRequest(c.getId(), a.getId(), DependencyType.BLOCKING, "C needs A", true);

        assertThatThrownBy(() -> service.create(request, "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cycle");
        verify(dependencyRepository, never()).save(any());
    }

    private RegulatorySource source() {
        return new RegulatorySource("Source", "https://example.gov.in/source", SourceType.RULE,
                SourceVerificationStatus.VERIFIED, null, LocalDate.now().minusDays(1), null,
                Instant.now(), "hash");
    }
}
