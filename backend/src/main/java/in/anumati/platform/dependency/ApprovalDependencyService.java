package in.anumati.platform.dependency;

import in.anumati.platform.audit.AuditService;
import in.anumati.platform.regulatory.Approval;
import in.anumati.platform.regulatory.ApprovalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovalDependencyService {
    private final ApprovalDependencyRepository repository;
    private final ApprovalRepository approvalRepository;
    private final AuditService auditService;

    public ApprovalDependencyService(ApprovalDependencyRepository repository,
                                     ApprovalRepository approvalRepository,
                                     AuditService auditService) {
        this.repository = repository;
        this.approvalRepository = approvalRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ApprovalDependency create(ApprovalDependencyRequest request, String actor) {
        Approval approval = getApproval(request.approvalId());
        Approval dependsOn = getApproval(request.dependsOnApprovalId());
        if (approval.getId().equals(dependsOn.getId())) {
            throw new IllegalArgumentException("An approval cannot depend on itself");
        }
        if (request.active() && wouldCreateCycle(approval.getId(), dependsOn.getId())) {
            throw new IllegalArgumentException("Dependency would create a cycle in the approval graph");
        }
        ApprovalDependency dependency = repository.save(new ApprovalDependency(
                approval, dependsOn, request.dependencyType(), request.reason(), request.active()));
        auditService.record(actor, "APPROVAL_DEPENDENCY_CREATED", "ApprovalDependency", dependency.getId(),
                Map.of("approvalId", approval.getId(), "dependsOnApprovalId", dependsOn.getId(),
                        "type", request.dependencyType().name()));
        return dependency;
    }

    @Transactional(readOnly = true)
    public List<ApprovalDependencyResponse> list() {
        return repository.findByActiveTrue().stream().map(ApprovalDependencyResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ApprovalDependencyResponse> forApproval(UUID approvalId) {
        return repository.findByApproval_IdAndActiveTrue(approvalId).stream().map(ApprovalDependencyResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Graph buildGraph() {
        List<ApprovalDependency> dependencies = repository.findByActiveTrue();
        Map<UUID, Node> nodes = new LinkedHashMap<>();
        dependencies.forEach(d -> {
            nodes.computeIfAbsent(d.getApproval().getId(), id -> Node.from(d.getApproval()));
            nodes.computeIfAbsent(d.getDependsOnApproval().getId(), id -> Node.from(d.getDependsOnApproval()));
        });
        List<Edge> edges = dependencies.stream()
                .map(d -> new Edge(d.getApproval().getId(), d.getDependsOnApproval().getId(), d.getDependencyType(), d.getReason()))
                .toList();
        return new Graph(List.copyOf(nodes.values()), edges);
    }

    private Approval getApproval(UUID id) {
        return approvalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval not found: " + id));
    }

    private boolean wouldCreateCycle(UUID approvalId, UUID dependencyTargetId) {
        Map<UUID, Set<UUID>> outgoing = repository.findByActiveTrue().stream()
                .collect(Collectors.groupingBy(
                        d -> d.getApproval().getId(),
                        Collectors.mapping(d -> d.getDependsOnApproval().getId(), Collectors.toSet())));
        outgoing.computeIfAbsent(approvalId, ignored -> new HashSet<>()).add(dependencyTargetId);

        Set<UUID> visited = new HashSet<>();
        Deque<UUID> stack = new ArrayDeque<>();
        stack.push(approvalId);
        while (!stack.isEmpty()) {
            UUID current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }
            if (dependencyTargetId.equals(current) && !approvalId.equals(current)) {
                // Still need to explore whether the target can return to the source.
            }
            for (UUID next : outgoing.getOrDefault(current, Set.of())) {
                if (approvalId.equals(next)) {
                    return true;
                }
                stack.push(next);
            }
        }
        return false;
    }

    public record Graph(List<Node> nodes, List<Edge> edges) {}

    public record Node(UUID approvalId, String code, String name, String authority) {
        static Node from(Approval approval) {
            return new Node(approval.getId(), approval.getCode(), approval.getName(), approval.getAuthority());
        }
    }

    public record Edge(UUID approvalId, UUID dependsOnApprovalId, DependencyType dependencyType, String reason) {}
}
