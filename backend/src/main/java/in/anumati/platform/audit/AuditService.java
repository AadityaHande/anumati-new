package in.anumati.platform.audit;

import in.anumati.platform.common.web.RequestIdFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {
    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String actor, String action, String entityType, UUID entityId, Map<String, Object> metadata) {
        Map<String, Object> enriched = new LinkedHashMap<>(metadata == null ? Map.of() : metadata);
        enriched.putIfAbsent("requestId", RequestIdFilter.current());
        repository.save(new AuditEvent(actor, action, entityType, entityId, enriched));
    }
}
