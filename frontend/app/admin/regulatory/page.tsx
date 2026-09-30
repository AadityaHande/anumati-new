"use client";

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { platformClient, ApprovalCatalog, DocumentRequirement, RegulatorySource, SourceType, SourceVerificationStatus, SourceChange, ConditionField, ConditionOperator, ValueType } from "../../../lib/api";
import { ArrowUpRight, Database, GitCompare, Shield, Plus } from "../../../components/icons";

const sourceTypes: SourceType[] = ["ACT", "RULE", "NOTIFICATION", "CIRCULAR", "OFFICIAL_PORTAL", "GUIDELINE", "OTHER"];
const conditionFields: ConditionField[] = ["SECTOR", "ACTIVITY", "DISTRICT", "MIDC_UNIT", "INVESTMENT_INR", "EMPLOYEES", "POWER_USAGE_KW", "BUSINESS_STAGE", "REGULATORY_ATTRIBUTE"];
const operators: ConditionOperator[] = ["EQ", "NEQ", "GT", "GTE", "LT", "LTE", "IN", "NOT_IN"];
const valueTypes: ValueType[] = ["STRING", "NUMBER", "BOOLEAN"];

export default function RegulatoryAdminPage() {
  const [sources, setSources] = useState<RegulatorySource[]>([]);
  const [approvals, setApprovals] = useState<ApprovalCatalog[]>([]);
  const [requirements, setRequirements] = useState<DocumentRequirement[]>([]);
  const [changes, setChanges] = useState<SourceChange[]>([]);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const [sourceTitle, setSourceTitle] = useState("");
  const [sourceUrl, setSourceUrl] = useState("");
  const [sourceType, setSourceType] = useState<SourceType>("OFFICIAL_PORTAL");
  const [sourceStatus, setSourceStatus] = useState<SourceVerificationStatus>("VERIFIED");
  const [sourceHash, setSourceHash] = useState("");

  const [approvalCode, setApprovalCode] = useState("");
  const [approvalName, setApprovalName] = useState("");
  const [approvalAuthority, setApprovalAuthority] = useState("");
  const [approvalPurpose, setApprovalPurpose] = useState("");
  const [approvalSourceId, setApprovalSourceId] = useState("");

  const [ruleCode, setRuleCode] = useState("");
  const [ruleName, setRuleName] = useState("");
  const [ruleApprovalId, setRuleApprovalId] = useState("");
  const [ruleSourceId, setRuleSourceId] = useState("");
  const [ruleOutcome, setRuleOutcome] = useState("APPLICABLE");
  const [ruleField, setRuleField] = useState<ConditionField>("SECTOR");
  const [ruleOperator, setRuleOperator] = useState<ConditionOperator>("EQ");
  const [ruleValueType, setRuleValueType] = useState<ValueType>("STRING");
  const [ruleValue, setRuleValue] = useState("");

  const [docApprovalId, setDocApprovalId] = useState("");
  const [docCategory, setDocCategory] = useState("");
  const [docName, setDocName] = useState("");
  const [docSourceId, setDocSourceId] = useState("");
  const [docMandatory, setDocMandatory] = useState(true);

  const [slaApprovalId, setSlaApprovalId] = useState("");
  const [slaSourceId, setSlaSourceId] = useState("");
  const [slaTargetHours, setSlaTargetHours] = useState(72);
  const [slaWarningHours, setSlaWarningHours] = useState(24);
  const [slaScopeKey, setSlaScopeKey] = useState("");
  const [slaScopeValue, setSlaScopeValue] = useState("");
  const [slaMaxInvestment, setSlaMaxInvestment] = useState("");

  const [incentiveCode, setIncentiveCode] = useState("");
  const [incentiveName, setIncentiveName] = useState("");
  const [incentiveAuthority, setIncentiveAuthority] = useState("");
  const [incentiveBenefit, setIncentiveBenefit] = useState("");
  const [incentiveUrl, setIncentiveUrl] = useState("");
  const [incentiveSourceId, setIncentiveSourceId] = useState("");
  const [incentiveField, setIncentiveField] = useState<ConditionField>("SECTOR");
  const [incentiveOperator, setIncentiveOperator] = useState<ConditionOperator>("EQ");
  const [incentiveValueType, setIncentiveValueType] = useState<ValueType>("STRING");
  const [incentiveValue, setIncentiveValue] = useState("");

  const verifiedSources = useMemo(() => sources.filter((source) => source.verificationStatus === "VERIFIED"), [sources]);

  const load = async () => {
    setError("");
    const [s, a, r, c] = await Promise.all([
      platformClient.regulatorySources(),
      platformClient.approvalsCatalog(),
      platformClient.documentRequirementsCatalog(),
      platformClient.sourceChanges(),
    ]);
    setSources(s); setApprovals(a); setRequirements(r); setChanges(c);
    const verified = s.filter((source) => source.verificationStatus === "VERIFIED");
    if (!approvalSourceId && verified.length) setApprovalSourceId(verified[0].id);
    if (!ruleApprovalId && a.length) setRuleApprovalId(a[0].id);
    if (!ruleSourceId && verified.length) setRuleSourceId(verified[0].id);
    if (!docApprovalId && a.length) setDocApprovalId(a[0].id);
    if (!docSourceId && verified.length) setDocSourceId(verified[0].id);
    if (!slaApprovalId && a.length) setSlaApprovalId(a[0].id);
    if (!slaSourceId && verified.length) setSlaSourceId(verified[0].id);
    if (!incentiveSourceId && verified.length) setIncentiveSourceId(verified[0].id);
  };

  useEffect(() => { load().catch((err) => setError(err instanceof Error ? err.message : "Could not load the regulatory catalogue.")); }, []);

  const run = async (fn: () => Promise<unknown>, message: string) => {
    setError(""); setNotice("");
    try { await fn(); await load(); setNotice(message); } catch (err) { setError(err instanceof Error ? err.message : "The operation could not be completed."); }
  };

  const createSource = async (event: FormEvent) => {
    event.preventDefault();
    await run(() => platformClient.createSource({ title: sourceTitle, url: sourceUrl, sourceType, verificationStatus: sourceStatus, contentHash: sourceHash || null }), "Source registered.");
    setSourceTitle(""); setSourceUrl(""); setSourceHash("");
  };

  const createApproval = async (event: FormEvent) => {
    event.preventDefault();
    await run(() => platformClient.createApproval({ code: approvalCode, name: approvalName, authority: approvalAuthority, purpose: approvalPurpose, sourceId: approvalSourceId, active: true }), "Approval registered.");
    setApprovalCode(""); setApprovalName(""); setApprovalAuthority(""); setApprovalPurpose("");
  };

  const createRule = async (event: FormEvent) => {
    event.preventDefault();
    await run(() => platformClient.createRule({ code: ruleCode, name: ruleName, approvalId: ruleApprovalId, sourceId: ruleSourceId, outcome: ruleOutcome, priority: 100, versionNumber: 1, active: true, conditions: [{ field: ruleField, operator: ruleOperator, valueType: ruleValueType, value: ruleValue, sequenceNumber: 1 }] }), "Rule published.");
    setRuleCode(""); setRuleName(""); setRuleValue("");
  };

  const createRequirement = async (event: FormEvent) => {
    event.preventDefault();
    await run(() => platformClient.createDocumentRequirement({ approvalId: docApprovalId, category: docCategory, documentName: docName, description: null, mandatory: docMandatory, active: true, sourceId: docSourceId }), "Document requirement registered.");
    setDocCategory(""); setDocName("");
  };

  const createSla = async (event: FormEvent) => {
    event.preventDefault();
    await run(() => platformClient.createSlaConfig({ approvalId: slaApprovalId, targetHours: slaTargetHours, warningHours: slaWarningHours, sourceId: slaSourceId, active: true, scopeAttributes: slaScopeKey && slaScopeValue ? { [slaScopeKey]: slaScopeValue } : {}, maxInvestmentInr: slaMaxInvestment ? Number(slaMaxInvestment) : null }), "SLA configuration saved.");
  };

  const createIncentive = async (event: FormEvent) => {
    event.preventDefault();
    await run(() => platformClient.createIncentiveScheme({
      code: incentiveCode, name: incentiveName, authority: incentiveAuthority, benefitSummary: incentiveBenefit,
      applicationUrl: incentiveUrl || null, sourceId: incentiveSourceId, active: true,
      conditions: [{ field: incentiveField, operator: incentiveOperator, valueType: incentiveValueType, value: incentiveValue, sequenceNumber: 1 }]
    }), "Support scheme registered.");
    setIncentiveCode(""); setIncentiveName(""); setIncentiveAuthority(""); setIncentiveBenefit(""); setIncentiveUrl(""); setIncentiveValue("");
  };

  return <div className="workspace-page">
    <header className="workspace-header"><div><span className="eyebrow">Administration</span><h1>Regulatory catalogue.</h1><p>Maintain the verified source layer that governs requirements, evidence, rules, document readiness, and service timelines.</p></div><Link href="/app" className="button button-secondary">Applicant workspace</Link></header>
    {error ? <div className="form-alert">{error}</div> : null}
    {notice ? <div className="form-success">{notice}</div> : null}

    <section className="metric-grid">
      <article className="metric-card"><span>Sources</span><strong>{sources.length}</strong></article>
      <article className="metric-card"><span>Verified</span><strong>{verifiedSources.length}</strong></article>
      <article className="metric-card"><span>Approvals</span><strong>{approvals.length}</strong></article>
      <article className="metric-card"><span>Requirements</span><strong>{requirements.length}</strong></article>
    </section>

    <section className="dashboard-grid">
      <form className="surface-card form-stack" onSubmit={createSource}><div className="section-bar"><div><span className="card-kicker">Source registry</span><h2>Add authoritative source</h2></div><Plus size={18}/></div><input className="input" placeholder="Title" value={sourceTitle} onChange={e=>setSourceTitle(e.target.value)} required/><input className="input" placeholder="HTTPS URL" value={sourceUrl} onChange={e=>setSourceUrl(e.target.value)} required/><div className="form-grid"><select className="input" value={sourceType} onChange={e=>setSourceType(e.target.value as SourceType)}>{sourceTypes.map(v=><option key={v}>{v}</option>)}</select><select className="input" value={sourceStatus} onChange={e=>setSourceStatus(e.target.value as SourceVerificationStatus)}><option>VERIFIED</option><option>UNVERIFIED</option><option>EXPIRED</option><option>RETIRED</option></select></div><input className="input" placeholder="SHA-256 content hash (64 hex)" value={sourceHash} onChange={e=>setSourceHash(e.target.value)} required={sourceStatus === "VERIFIED"}/><button className="button button-primary" type="submit">Register source</button></form>

      <form className="surface-card form-stack" onSubmit={createApproval}><div className="section-bar"><div><span className="card-kicker">Approval catalogue</span><h2>Add approval</h2></div></div><input className="input" placeholder="Approval code" value={approvalCode} onChange={e=>setApprovalCode(e.target.value)} required/><input className="input" placeholder="Approval name" value={approvalName} onChange={e=>setApprovalName(e.target.value)} required/><input className="input" placeholder="Authority" value={approvalAuthority} onChange={e=>setApprovalAuthority(e.target.value)} required/><textarea className="input" placeholder="Purpose" value={approvalPurpose} onChange={e=>setApprovalPurpose(e.target.value)} rows={3}/><select className="input" value={approvalSourceId} onChange={e=>setApprovalSourceId(e.target.value)} required>{verifiedSources.map(s=><option key={s.id} value={s.id}>{s.title}</option>)}</select><button className="button button-primary" type="submit" disabled={!verifiedSources.length}>Register approval</button></form>
    </section>

    <section className="dashboard-grid">
      <form className="surface-card form-stack" onSubmit={createRule}><div className="section-bar"><div><span className="card-kicker">Deterministic rules</span><h2>Add applicability rule</h2></div></div><div className="form-grid"><input className="input" placeholder="Rule code" value={ruleCode} onChange={e=>setRuleCode(e.target.value)} required/><input className="input" placeholder="Rule name" value={ruleName} onChange={e=>setRuleName(e.target.value)} required/></div><div className="form-grid"><select className="input" value={ruleApprovalId} onChange={e=>setRuleApprovalId(e.target.value)} required>{approvals.map(a=><option key={a.id} value={a.id}>{a.code} · {a.name}</option>)}</select><select className="input" value={ruleSourceId} onChange={e=>setRuleSourceId(e.target.value)} required>{verifiedSources.map(s=><option key={s.id} value={s.id}>{s.title}</option>)}</select></div><div className="form-grid"><select className="input" value={ruleOutcome} onChange={e=>setRuleOutcome(e.target.value)}><option>APPLICABLE</option><option>CONDITIONAL</option><option>NOT_APPLICABLE</option></select><select className="input" value={ruleField} onChange={e=>setRuleField(e.target.value as ConditionField)}>{conditionFields.map(v=><option key={v}>{v}</option>)}</select></div><div className="form-grid"><select className="input" value={ruleOperator} onChange={e=>setRuleOperator(e.target.value as ConditionOperator)}>{operators.map(v=><option key={v}>{v}</option>)}</select><select className="input" value={ruleValueType} onChange={e=>setRuleValueType(e.target.value as ValueType)}>{valueTypes.map(v=><option key={v}>{v}</option>)}</select></div><input className="input" placeholder="Condition value" value={ruleValue} onChange={e=>setRuleValue(e.target.value)} required/><button className="button button-primary" type="submit" disabled={!approvals.length || !verifiedSources.length}>Publish rule</button></form>

      <form className="surface-card form-stack" onSubmit={createRequirement}><div className="section-bar"><div><span className="card-kicker">Evidence requirements</span><h2>Add document requirement</h2></div></div><select className="input" value={docApprovalId} onChange={e=>setDocApprovalId(e.target.value)} required>{approvals.map(a=><option key={a.id} value={a.id}>{a.code} · {a.name}</option>)}</select><input className="input" placeholder="Document category" value={docCategory} onChange={e=>setDocCategory(e.target.value)} required/><input className="input" placeholder="Document name" value={docName} onChange={e=>setDocName(e.target.value)} required/><select className="input" value={docSourceId} onChange={e=>setDocSourceId(e.target.value)} required>{verifiedSources.map(s=><option key={s.id} value={s.id}>{s.title}</option>)}</select><label className="checkbox-row"><input type="checkbox" checked={docMandatory} onChange={e=>setDocMandatory(e.target.checked)}/> Mandatory requirement</label><button className="button button-primary" type="submit" disabled={!approvals.length || !verifiedSources.length}>Register requirement</button></form>
    </section>

    <section className="surface-card form-stack"><div className="section-bar"><div><span className="card-kicker">Service timelines</span><h2>Configure approval SLA</h2></div><span className="status-pill status-neutral">Source-backed</span></div><form className="form-grid" onSubmit={createSla}><select className="input" value={slaApprovalId} onChange={e=>setSlaApprovalId(e.target.value)} required>{approvals.map(a=><option key={a.id} value={a.id}>{a.code} · {a.name}</option>)}</select><select className="input" value={slaSourceId} onChange={e=>setSlaSourceId(e.target.value)} required>{verifiedSources.map(s=><option key={s.id} value={s.id}>{s.title}</option>)}</select><input className="input" type="number" min={1} value={slaTargetHours} onChange={e=>setSlaTargetHours(Number(e.target.value))} placeholder="Target hours"/><input className="input" type="number" min={0} value={slaWarningHours} onChange={e=>setSlaWarningHours(Number(e.target.value))} placeholder="Warning hours"/><input className="input" value={slaScopeKey} onChange={e=>setSlaScopeKey(e.target.value)} placeholder="Scope key (optional)"/><input className="input" value={slaScopeValue} onChange={e=>setSlaScopeValue(e.target.value)} placeholder="Scope value (optional)"/><input className="input" type="number" min={0} value={slaMaxInvestment} onChange={e=>setSlaMaxInvestment(e.target.value)} placeholder="Max investment INR (optional)"/><button className="button button-primary" type="submit" disabled={!approvals.length || !verifiedSources.length}>Save SLA</button></form></section>
    <section className="surface-card form-stack"><div className="section-bar"><div><span className="card-kicker">Government support</span><h2>Register incentive scheme</h2></div><span className="status-pill status-neutral">Rule-driven</span></div><form onSubmit={createIncentive} className="form-stack"><div className="form-grid"><input className="input" placeholder="Scheme code" value={incentiveCode} onChange={e=>setIncentiveCode(e.target.value)} required/><input className="input" placeholder="Scheme name" value={incentiveName} onChange={e=>setIncentiveName(e.target.value)} required/></div><div className="form-grid"><input className="input" placeholder="Authority" value={incentiveAuthority} onChange={e=>setIncentiveAuthority(e.target.value)} required/><input className="input" placeholder="Benefit summary" value={incentiveBenefit} onChange={e=>setIncentiveBenefit(e.target.value)} required/></div><input className="input" placeholder="Official application URL (optional)" value={incentiveUrl} onChange={e=>setIncentiveUrl(e.target.value)}/><select className="input" value={incentiveSourceId} onChange={e=>setIncentiveSourceId(e.target.value)} required>{verifiedSources.map(s=><option key={s.id} value={s.id}>{s.title}</option>)}</select><div className="form-grid"><select className="input" value={incentiveField} onChange={e=>setIncentiveField(e.target.value as ConditionField)}>{conditionFields.map(v=><option key={v}>{v}</option>)}</select><select className="input" value={incentiveOperator} onChange={e=>setIncentiveOperator(e.target.value as ConditionOperator)}>{operators.map(v=><option key={v}>{v}</option>)}</select></div><div className="form-grid"><select className="input" value={incentiveValueType} onChange={e=>setIncentiveValueType(e.target.value as ValueType)}>{valueTypes.map(v=><option key={v}>{v}</option>)}</select><input className="input" placeholder="Eligibility value" value={incentiveValue} onChange={e=>setIncentiveValue(e.target.value)} required/></div><button className="button button-primary" type="submit" disabled={!verifiedSources.length}>Register scheme</button></form></section>

    <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Source registry</span><h2>Authoritative material</h2></div></div><div className="analysis-list">{sources.length===0?<div className="empty-panel"><Database size={20}/><h3>No sources configured.</h3><p>Add and verify government material before rules can influence decisions.</p></div>:sources.map((source)=><article className="analysis-row" key={source.id}><div><div className="analysis-title-line"><h3>{source.title}</h3><span className={`status-pill ${source.verificationStatus === "VERIFIED" ? "status-positive" : "status-warning"}`}>{source.verificationStatus}</span></div><p>{source.sourceType}</p><div className="analysis-meta"><span>Effective {source.effectiveFrom || "Not set"}</span><span>{source.contentHash ? `Hash ${source.contentHash.slice(0,12)}…` : "No content hash"}</span></div></div><a className="source-link" href={source.url} target="_blank" rel="noreferrer">Open source <ArrowUpRight size={14}/></a></article>)}</div></section>

    <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Source change intelligence</span><h2>Version families</h2></div></div><div className="analysis-list">{changes.length===0?<div className="empty-panel"><GitCompare size={20}/><h3>No comparable source versions.</h3><p>Version families appear when multiple published records share a title.</p></div>:changes.map(change=><article className="analysis-row" key={change.sourceFamily}><div><div className="analysis-title-line"><h3>{change.sourceFamily}</h3><span className={`status-pill ${change.contentChanged ? "status-warning" : "status-neutral"}`}>{change.contentChanged ? "Content hash changed" : "Metadata versions"}</span></div><div className="analysis-meta"><span>{change.versions.length} versions recorded</span></div></div><Shield size={17}/></article>)}</div></section>

    <section className="surface-card"><div className="section-bar"><div><span className="card-kicker">Operating principle</span><h2>Verified sources govern outcomes.</h2></div></div><p className="muted-copy">Anumati does not allow unverified regulatory material to drive applicability, evidence requirements, service timelines, or incentives. The catalogue is the source-controlled boundary between policy material and operational decisions.</p></section>
  </div>;
}
