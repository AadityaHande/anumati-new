package in.anumati.platform.sla; import java.time.*; import java.util.*;
public record SlaResponse(UUID applicationId,SlaStatus status,Instant submittedAt,Instant dueAt,long remainingSeconds,Integer targetHours,Integer warningHours){ }
