package in.anumati.platform.regulatory;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegulatoryRuleRepository extends JpaRepository<RegulatoryRule, UUID> {
    @EntityGraph(attributePaths = {"approval", "approval.source", "source", "conditions"})
    List<RegulatoryRule> findByActiveTrue();
}
