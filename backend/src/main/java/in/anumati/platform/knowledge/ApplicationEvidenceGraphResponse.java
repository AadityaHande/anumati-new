package in.anumati.platform.knowledge;

import java.util.List;
import java.util.UUID;

public record ApplicationEvidenceGraphResponse(
        UUID applicationId,
        List<Node> nodes,
        List<Edge> edges) {
    public record Node(UUID id, String type, String label, String detail) {}
    public record Edge(UUID from, UUID to, String relationship) {}
}
