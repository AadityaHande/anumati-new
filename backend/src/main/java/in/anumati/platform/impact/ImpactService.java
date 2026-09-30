package in.anumati.platform.impact;

import in.anumati.platform.analysis.RuleEngineService;
import in.anumati.platform.business.BusinessProfile;
import in.anumati.platform.business.BusinessProfileService;
import in.anumati.platform.regulatory.RegulatoryRule;
import in.anumati.platform.regulatory.RegulatoryRuleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ImpactService {
 private final BusinessProfileService profiles; private final RegulatoryRuleRepository rules; private final RuleEngineService engine; private final boolean verifiedOnly;
 public ImpactService(BusinessProfileService p,RegulatoryRuleRepository r,RuleEngineService e,@Value("${anumati.analysis.require-verified-sources:true}") boolean v){profiles=p;rules=r;engine=e;verifiedOnly=v;}
 @Transactional(readOnly=true) public ImpactResponse simulate(UUID profileId,ImpactRequest req,String actor){
   BusinessProfile p=profiles.getForActor(profileId,actor);
   if(req.sector()==null && req.activity()==null && req.district()==null && req.midcUnit()==null && req.investmentInr()==null && req.employees()==null && req.powerUsageKw()==null && req.businessStage()==null) {
       throw new IllegalArgumentException("At least one proposed business change is required for impact simulation");
   }
   BusinessProfile scenario=new BusinessProfile(p.getBusinessName(), req.sector()!=null?req.sector():p.getSector(), req.activity()!=null?req.activity():p.getActivity(), req.district()!=null?req.district():p.getDistrict(), req.midcUnit()!=null?req.midcUnit():p.getMidcUnit(), req.investmentInr()!=null?req.investmentInr():p.getInvestmentInr(), p.getPanNumber(), p.getGstin(), req.employees()!=null?req.employees():p.getEmployees(), p.getRegulatoryAttributes(), req.powerUsageKw()!=null?req.powerUsageKw():p.getPowerUsageKw(), req.businessStage()!=null?req.businessStage():p.getBusinessStage(), p.getOwnerActor());
   List<RegulatoryRule> all=rules.findByActiveTrue();
   Map<String,RuleEngineService.AnalysisResult> before=engine.evaluate(p,all,verifiedOnly).stream().collect(Collectors.toMap(RuleEngineService.AnalysisResult::approvalCode,Function.identity(),(a,b)->a,LinkedHashMap::new));
   Map<String,RuleEngineService.AnalysisResult> after=engine.evaluate(scenario,all,verifiedOnly).stream().collect(Collectors.toMap(RuleEngineService.AnalysisResult::approvalCode,Function.identity(),(a,b)->a,LinkedHashMap::new));
   List<ImpactResponse.Change> added=new ArrayList<>(), removed=new ArrayList<>(), changed=new ArrayList<>();
   Set<String> codes=new LinkedHashSet<>(); codes.addAll(before.keySet()); codes.addAll(after.keySet());
   for(String code:codes){var b=before.get(code);var a=after.get(code);String bs=b==null?"NONE":b.status().name();String as=a==null?"NONE":a.status().name(); if("NONE".equals(bs)&&!"NONE".equals(as)) added.add(change(code,b,a)); else if(!"NONE".equals(bs)&&"NONE".equals(as)) removed.add(change(code,b,a)); else if(!Objects.equals(bs,as) || !Objects.equals(b==null?null:b.ruleCode(),a==null?null:a.ruleCode()) || !Objects.equals(b==null?null:b.sourceId(),a==null?null:a.sourceId())) changed.add(change(code,b,a));}
   return new ImpactResponse(added,removed,changed,added.size()+removed.size()+changed.size());
 }
 private ImpactResponse.Change change(String code,RuleEngineService.AnalysisResult b,RuleEngineService.AnalysisResult a){var x=a!=null?a:b;return new ImpactResponse.Change(code,x.approvalName(),x.authority(),b==null?"NONE":b.status().name(),a==null?"NONE":a.status().name(),b==null?null:b.ruleCode(),a==null?null:a.ruleCode(),b==null?null:b.sourceUrl(),a==null?null:a.sourceUrl(),b==null?"":b.reason(),a==null?"":a.reason());}
}
