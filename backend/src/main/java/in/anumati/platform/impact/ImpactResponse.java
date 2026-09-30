package in.anumati.platform.impact;

import java.util.List;

public record ImpactResponse(List<Change> added,List<Change> removed,List<Change> changed,int totalChanged){
 public record Change(String approvalCode,String approvalName,String authority,String beforeStatus,String afterStatus,String beforeRuleCode,String afterRuleCode,String beforeSourceUrl,String afterSourceUrl,String beforeReason,String afterReason){}
}
