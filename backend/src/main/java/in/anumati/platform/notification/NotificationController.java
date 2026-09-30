package in.anumati.platform.notification;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponse> list(Authentication a) {
        return service.list(a.getName());
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public long unread(Authentication a) {
        return service.unread(a.getName());
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public void read(@PathVariable UUID id, Authentication a) {
        service.read(id, a.getName());
    }
}
