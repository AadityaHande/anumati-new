"use client";

import { FormEvent, useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { apiClient, Application, ApplicationStatus, ApplicationTimeline } from "../../../../lib/api";
import { ArrowLeft, Check } from "../../../../components/icons";

export default function OfficerApplicationPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [application, setApplication] = useState<Application | null>(null);
  const [timeline, setTimeline] = useState<ApplicationTimeline | null>(null);
  const [querySubject, setQuerySubject] = useState("");
  const [queryText, setQueryText] = useState("");
  const [decisionNotes, setDecisionNotes] = useState("");
  const [decisionOutcome, setDecisionOutcome] = useState("APPROVED");
  const [validUntil, setValidUntil] = useState("");
  const [inspectionAt, setInspectionAt] = useState("");
  const [officer, setOfficer] = useState("");
  const [inspectionOutcome, setInspectionOutcome] = useState("PASSED");
  const [inspectionNotes, setInspectionNotes] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function load() { const [app, flow] = await Promise.all([apiClient.getApplication(id), apiClient.timeline(id)]); setApplication(app); setTimeline(flow); }
  useEffect(() => { load().catch((err) => setError(err instanceof Error ? err.message : "Could not load the application.")); }, [id]);

  async function doAction(fn: () => Promise<unknown>) { setBusy(true); setError(""); try { await fn(); await load(); } catch (err) { setError(err instanceof Error ? err.message : "The action could not be completed."); } finally { setBusy(false); } }
  async function raiseQuery(e: FormEvent) { e.preventDefault(); await doAction(() => apiClient.raiseQuery(id, querySubject, queryText)); setQuerySubject(""); setQueryText(""); }
  async function scheduleInspection(e: FormEvent) { e.preventDefault(); await doAction(() => apiClient.scheduleInspection(id, new Date(inspectionAt).toISOString().slice(0, 19), officer)); setInspectionAt(""); }
  async function completeInspection(e: FormEvent, inspectionId: string) { e.preventDefault(); await doAction(() => apiClient.completeInspection(id, inspectionId, inspectionOutcome, inspectionNotes)); }
  async function decision(e: FormEvent) { e.preventDefault(); await doAction(() => apiClient.decide(id, { outcome: decisionOutcome, decisionNotes, citedRuleCode: decisionOutcome === "REJECTED" ? application?.matchedRuleCode || undefined : undefined, citedSourceId: decisionOutcome === "REJECTED" ? application?.sourceId || undefined : undefined, validUntil: validUntil || undefined })); }
  async function move(status: ApplicationStatus) { await doAction(() => apiClient.transitionApplication(id, status, "Department workflow update")); }

  if (!application || !timeline) return <div className="loading-state">Loading department record...</div>;
  return <div className="profile-page">
    <header className="workspace-header"><div><span className="eyebrow">Officer review · {application.approvalCode}</span><h1>{application.approvalName}</h1><p>{application.externalReference || application.id} · {application.status.replaceAll("_", " ")}</p></div><button className="button button-secondary" onClick={() => router.push("/officer")}><ArrowLeft size={16}/> Back to control tower</button></header>
    {error ? <div className="form-alert">{error}</div> : null}
    <section className="profile-card"><div className="card-header"><div><span className="card-kicker">Lifecycle</span><h2>Current status</h2></div><span className="status-pill status-neutral">{application.status.replaceAll("_", " ")}</span></div><div className="workflow-actions">
      {application.status === "SUBMITTED" ? <button className="button button-secondary" disabled={busy} onClick={() => move("UNDER_SCRUTINY")}>Start scrutiny</button> : null}
      {application.status === "UNDER_SCRUTINY" ? <button className="button button-secondary" disabled={busy} onClick={() => move("RESUBMITTED")}>Mark ready for next stage</button> : null}
    </div></section>

    <section className="officer-two-col">
      <article className="form-card"><div className="form-section"><span className="card-kicker">Query</span><h2>Raise a deficiency</h2><p>Ask for a correction or missing evidence. The applicant can respond against this application.</p><form className="auth-form" onSubmit={raiseQuery}><label className="field"><span>Subject</span><input value={querySubject} onChange={(e) => setQuerySubject(e.target.value)} required /></label><label className="field"><span>Question</span><textarea rows={5} value={queryText} onChange={(e) => setQueryText(e.target.value)} required /></label><button className="button button-primary" disabled={busy} type="submit">Raise query</button></form></div></article>
      <article className="form-card"><div className="form-section"><span className="card-kicker">Inspection</span><h2>Schedule inspection</h2><p>Assign an officer and record the appointment.</p><form className="auth-form" onSubmit={scheduleInspection}><label className="field"><span>Scheduled time</span><input type="datetime-local" value={inspectionAt} onChange={(e) => setInspectionAt(e.target.value)} required /></label><label className="field"><span>Assigned officer</span><input value={officer} onChange={(e) => setOfficer(e.target.value)} required /></label><button className="button button-primary" disabled={busy} type="submit">Schedule</button></form></div></article>
    </section>

    <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Inspection history</span><h2>Inspections</h2></div></div><div className="analysis-list">{timeline.inspections.length === 0 ? <div className="empty-panel">No inspections scheduled.</div> : timeline.inspections.map((inspection) => <article className="analysis-row" key={inspection.id}><div><h3>{inspection.assignedOfficer}</h3><p>{new Date(inspection.scheduledAt).toLocaleString()}</p><div className="analysis-meta"><span>{inspection.outcome || "SCHEDULED"}</span><span>{inspection.outcomeNotes || "No outcome recorded"}</span></div></div>{!inspection.outcome ? <form className="mini-inline-form" onSubmit={(e) => completeInspection(e, inspection.id)}><select value={inspectionOutcome} onChange={(e) => setInspectionOutcome(e.target.value)}><option value="PASSED">Passed</option><option value="FAILED">Failed</option><option value="CONDITIONAL">Conditional</option></select><input value={inspectionNotes} onChange={(e) => setInspectionNotes(e.target.value)} placeholder="Outcome note"/><button className="button button-secondary button-small" disabled={busy}>Complete</button></form> : null}</article>)}</div></section>

    <section className="form-card"><div className="form-section"><span className="card-kicker">Decision</span><h2>Record decision</h2><p>Rejections require the rule and source already attached to this application snapshot.</p><form className="auth-form" onSubmit={decision}><label className="field"><span>Outcome</span><select value={decisionOutcome} onChange={(e) => setDecisionOutcome(e.target.value)}><option value="APPROVED">Approved</option><option value="REJECTED">Rejected</option><option value="CONDITIONAL">Conditional</option></select></label><label className="field"><span>Decision notes</span><textarea rows={4} value={decisionNotes} onChange={(e) => setDecisionNotes(e.target.value)} /></label><label className="field"><span>Valid until</span><input type="date" value={validUntil} onChange={(e) => setValidUntil(e.target.value)} /><small>Optional. Use only when the approval validity is established by the applicable authority/source.</small></label><button className="button button-primary" disabled={busy} type="submit">Record decision</button></form></div></section>

    <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Audit trail</span><h2>Status history</h2></div></div><div className="analysis-list">{timeline.statusHistory.map((event) => <div className="analysis-row" key={event.id}><div><div className="analysis-title-line"><h3>{event.toStatus.replaceAll("_", " ")}</h3><Check size={16}/></div><p>{event.reason || "Status updated"}</p><div className="analysis-meta"><span>{event.actor}</span><span>{new Date(event.createdAt).toLocaleString()}</span></div></div></div>)}</div></section>
  </div>;
}
