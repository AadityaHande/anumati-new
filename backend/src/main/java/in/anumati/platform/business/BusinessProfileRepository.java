package in.anumati.platform.business;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, UUID> {
    java.util.List<BusinessProfile> findByOwnerActorOrderByUpdatedAtDesc(String ownerActor);
}
