package in.anumati.platform.regulatory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApprovalRepository extends JpaRepository<Approval, UUID> {}
