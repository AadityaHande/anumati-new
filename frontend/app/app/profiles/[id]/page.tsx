"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { apiClient, AnalysisRun, BusinessProfile, Readiness } from "../../../../lib/api";
import { ArrowRight, Check, FileText, GitBranch, Landmark, Scale, Calendar, MessageSquare, Bell, Shield } from "../../../../components/icons";

function statusClass(status: string) {
  if (status === "APPLICABLE" || status === "READY") return "status-positive";
  if (status === "CONDITIONAL" || status === "ACTION_NEEDED") return "status-warning";
  return "status-neutral";
}

export default function ProfilePage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const [profile, setProfile] = useState<BusinessProfile | null>(null);
  const [analysis, setAnalysis] = useState<AnalysisRun | null>(null);
  const [readiness, setReadiness] = useState<Readiness | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    apiClient.getProfile(params.id).then(setProfile).catch((err) => setError(err instanceof Error ? err.message : "Could not load profile."));
    apiClient.latestAnalysis(params.id).then(async (run) => {
      setAnalysis(run);
      if (typeof window !== "undefined") sessionStorage.setItem(`anumati_analysis_${params.id}`, run.id);
      return apiClient.readiness(params.id, run.id);
    }).then(setReadiness).catch(() => undefined);
  }, [params.id]);

  async function analyse() {
    setBusy(true);
    setError("");
    try {
      const run = await apiClient.analyse(params.id);
      setAnalysis(run);
      if (typeof window !== "undefined") sessionStorage.setItem(`anumati_analysis_${params.id}`, run.id);
      const ready = await apiClient.readiness(params.id, run.id);
      setReadiness(ready);
    } catch (err) {
      setError(err instanceof Error ? err.message : "The analysis could not be completed.");
    } finally {
      setBusy(false);
    }
  }

  async function submitApplication(approvalId: string) {
    if (!analysis) return;
    setBusy(true); setError("");
    try {
      const application = await apiClient.createApplication({ businessProfileId: params.id, analysisRunId: analysis.id, approvalId });
      router.push(`/app/applications/${application.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "The application could not be submitted.");
    } finally { setBusy(false); }
  }

  if (error && !profile) return <div className="empty-state"><h1>We could not load this profile.</h1><p>{error}</p><Link className="button button-secondary" href="/app">Back to workspace</Link></div>;
  if (!profile) return <div className="loading-state">Loading profile...</div>;

  return (
    <div className="profile-page">
      <header className="workspace-header"><div><span className="eyebrow">Business regulatory twin · Version {profile.versionNumber}</span><h1>{profile.businessName}</h1><p>{profile.activity} · {profile.district} · {profile.sector}</p></div><div className="analysis-actions"><Link href={`/app/profiles/${params.id}/twin`} className="button button-secondary button-small">Business twin</Link><Link href={`/app/profiles/${params.id}/history`} className="button button-secondary button-small">History</Link><Link href={`/app/profiles/${params.id}/edit`} className="button button-secondary button-small">Edit profile</Link><span className="status-pill status-neutral">Profile saved</span></div></header>
      {error ? <div className="form-alert" role="alert">{error}</div> : null}
      <section className="profile-grid">
        <article className="profile-card profile-card-wide"><div className="card-header"><div><span className="card-kicker">Operating context</span><h2>Business facts</h2></div></div><div className="detail-grid">
          <div><span>Sector</span><strong>{profile.sector}</strong></div><div><span>Activity</span><strong>{profile.activity}</strong></div><div><span>District</span><strong>{profile.district}</strong></div><div><span>MIDC</span><strong>{profile.midcUnit ? "Yes" : "No"}</strong></div><div><span>Investment</span><strong>₹{profile.investmentInr.toLocaleString("en-IN")}</strong></div><div><span>Employees</span><strong>{profile.employees}</strong></div><div><span>Power usage</span><strong>{profile.powerUsageKw} kW</strong></div><div><span>Stage</span><strong>{profile.businessStage.toLowerCase()}</strong></div>
        </div></article>
        <article className="profile-card"><div className="icon-box"><Landmark size={21}/></div><span className="card-kicker">Regulatory analysis</span><h2>{analysis ? "Analysis complete" : "Analyse this profile"}</h2><p>{analysis ? `${analysis.results.length} approval catalogue entries evaluated against profile version ${analysis.profileVersion}.` : "Run the verified rule set against this saved profile to see applicable and conditional requirements."}</p><button className="button button-primary" onClick={analyse} disabled={busy}>{busy ? "Analysing..." : analysis ? "Run again" : "Run analysis"} <ArrowRight size={17}/></button></article>
      </section>
      {analysis ? <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Analysis run</span><h2>What may apply</h2></div><div className="analysis-actions"><Link className="button button-secondary button-small" href={`/app/profiles/${params.id}/preflight`}>Pre-Flight</Link><Link className="button button-secondary button-small" href={`/app/profiles/${params.id}/evidence`}>Evidence Passport</Link><Link className="button button-secondary button-small" href={`/app/profiles/${params.id}/impact`}>Regulatory Impact</Link><span className="status-pill status-neutral">Profile v{analysis.profileVersion}</span>{analysis ? <span className={`status-pill ${analysis.scrutinyTier === "HIGH" ? "status-danger" : analysis.scrutinyTier === "ENHANCED" ? "status-warning" : "status-positive"}`}>Administrative screening: {analysis.scrutinyTier}</span> : null}</div></div><div className="product-note"><Shield size={16}/><span><strong>{analysis.scrutinyTier} scrutiny</strong>{analysis.scrutinyReasons.length ? ` · ${analysis.scrutinyReasons.join(" ")}` : " · No enhanced scrutiny indicators recorded."}</span></div><div className="analysis-list">
        {analysis.results.length === 0 ? <div className="empty-panel">No catalogue rules are currently available for this profile.</div> : analysis.results.map((result) => <article className="analysis-row" key={result.approvalId}><div><div className="analysis-title-line"><h3>{result.approvalName}</h3><span className={`status-pill ${statusClass(result.status)}`}>{result.status.replaceAll("_", " ")}</span></div><p>{result.authority}</p><div className="analysis-reason"><strong>Why</strong><span>{result.reason}</span></div>{result.ruleCode ? <div className="analysis-meta"><span>Rule {result.ruleCode}</span><span>{result.sourceTitle || "Source on file"}</span></div> : <div className="analysis-meta"><span>No matching active rule</span></div>}</div><div className="analysis-actions"><Link className="source-link" href={`/app/knowledge/approvals/${result.approvalId}`}>Trace <ArrowRight size={14}/></Link>{result.sourceUrl ? <a className="source-link" href={result.sourceUrl} target="_blank" rel="noreferrer">Source <ArrowRight size={14}/></a> : null}{result.status !== "NOT_APPLICABLE" && readiness?.approvals.find((item) => item.approvalId === result.approvalId)?.status === "READY" ? <button className="button button-secondary button-small" onClick={() => submitApplication(result.approvalId)} disabled={busy}>Submit application</button> : null}</div></article>)}
      </div></section> : null}
      {readiness ? <section className="readiness-section"><div className="section-bar"><div><span className="card-kicker">Preparation</span><h2>Evidence readiness</h2></div><div className="readiness-score"><strong>{readiness.overallScore}%</strong><span>{readiness.satisfiedRequirements} of {readiness.knownRequirements} known requirements satisfied</span></div></div><div className="readiness-grid">{readiness.approvals.length === 0 ? <div className="empty-panel">No document requirements are configured for the current applicable approvals.</div> : readiness.approvals.map((approval) => <article className="readiness-card" key={approval.approvalId}><div className="analysis-title-line"><h3>{approval.approvalName}</h3><span className={`status-pill ${statusClass(approval.status)}`}>{approval.status.replaceAll("_", " ")}</span></div><p>{approval.mandatorySatisfied} of {approval.mandatoryKnown} mandatory requirements ready.</p><div className="readiness-bar"><span style={{ width: `${approval.score}%` }} /></div></article>)}</div></section> : null}
      {!analysis ? <div className="product-note"><Check size={18}/><span>Analysis is versioned. Any future profile edit creates a new version rather than changing the meaning of this run.</span></div> : null}
      <section className="profile-grid secondary-grid">
        <Link href={`/app/profiles/${params.id}/documents`} className="profile-card profile-card-link"><div className="icon-box"><FileText size={21}/></div><span className="card-kicker">Evidence</span><h2>Documents</h2><p>Upload, analyse, review, and promote matched fields into verified evidence.</p><span className="text-link">Open documents <ArrowRight size={16}/></span></Link>
        <Link href={`/app/profiles/${params.id}/applications`} className="profile-card profile-card-link"><div className="icon-box"><GitBranch size={21}/></div><span className="card-kicker">Lifecycle</span><h2>Applications</h2><p>Submit ready approvals and follow queries, inspections, decisions, and service status.</p><span className="text-link">Open applications <ArrowRight size={16}/></span></Link>
        <Link href={`/app/profiles/${params.id}/incentives`} className="profile-card profile-card-link"><div className="icon-box"><Scale size={21}/></div><span className="card-kicker">Government support</span><h2>Support schemes</h2><p>See source-backed schemes whose configured eligibility rules match this profile.</p><span className="text-link">Review schemes <ArrowRight size={16}/></span></Link>
        <Link href={`/app/profiles/${params.id}/renewals`} className="profile-card profile-card-link"><div className="icon-box"><Calendar size={21}/></div><span className="card-kicker">Continuity</span><h2>Renewals</h2><p>Track approval validity and upcoming renewal actions.</p><span className="text-link">Open renewals <ArrowRight size={16}/></span></Link>
        <Link href={`/app/profiles/${params.id}/grievances`} className="profile-card profile-card-link"><div className="icon-box"><MessageSquare size={21}/></div><span className="card-kicker">Escalation</span><h2>Grievances</h2><p>Record issues against the relevant business or application and follow the response.</p><span className="text-link">Open grievances <ArrowRight size={16}/></span></Link>
        <Link href="/app/notifications" className="profile-card profile-card-link"><div className="icon-box"><Bell size={21}/></div><span className="card-kicker">Action centre</span><h2>Notifications</h2><p>Keep application queries, decisions, and other recorded actions in one place.</p><span className="text-link">Open notifications <ArrowRight size={16}/></span></Link>
      </section>
    </div>
  );
}
