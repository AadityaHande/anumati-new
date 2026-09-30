"use client";

import { FormEvent, useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { apiClient, Application, ApplicationTimeline, Sla } from "../../../lib/api";
import { platformClient } from "../../../lib/api";
import { ArrowLeft, ArrowRight, Check, FileText } from "../../../components/icons";

function statusClass(status?: string) { return status === "APPROVED" || status === "INSPECTION_COMPLETE" || status === "RESPONDED" ? "status-positive" : status === "QUERY_RAISED" || status === "NEEDS_REVIEW" ? "status-warning" : "status-neutral"; }

export default function ApplicationPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [application, setApplication] = useState<Application | null>(null);
  const [timeline, setTimeline] = useState<ApplicationTimeline | null>(null);
  const [responseText, setResponseText] = useState("");
  const [activeQuery, setActiveQuery] = useState<string | null>(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [sla, setSla] = useState<Sla | null>(null);

  async function load() {
    const [app, flow, currentSla] = await Promise.all([apiClient.getApplication(id), apiClient.timeline(id), platformClient.sla(id)]);
    setApplication(app); setTimeline(flow); setSla(currentSla);
  }
  useEffect(() => { load().catch((err) => setError(err instanceof Error ? err.message : "Could not load the application.")); }, [id]);

  async function respond(event: FormEvent) {
    event.preventDefault();
    if (!activeQuery || !responseText.trim()) return;
    setBusy(true); setError("");
    try { await apiClient.respondToQuery(id, activeQuery, responseText.trim()); setResponseText(""); setActiveQuery(null); await load(); } catch (err) { setError(err instanceof Error ? err.message : "Could not send the response."); } finally { setBusy(false); }
  }

  if (!application || !timeline) return <div className="loading-state">Loading application...</div>;
  return <div className="profile-page">
    <header className="workspace-header"><div><span className="eyebrow">Application · {application.approvalCode}</span><h1>{application.approvalName}</h1><p>{application.externalReference || application.id} · {timeline.currentStatus.replaceAll("_", " ")}</p></div><Link className="button button-secondary" href={`/app/profiles/${application.businessProfileId}/applications`}><ArrowLeft size={16}/> Back to applications</Link></header>
    {error ? <div className="form-alert">{error}</div> : null}
    <section className="profile-grid"><article className="profile-card"><span className="card-kicker">Service timeline</span><h2>{sla?.status.replaceAll("_", " ") || "Not configured"}</h2><p>{sla?.dueAt ? `Current service due date: ${new Date(sla.dueAt).toLocaleString("en-IN")}.` : "No verified service timeline is configured for this approval."}</p></article>
      <article className="profile-card profile-card-wide"><span className="card-kicker">Status</span><h2><span className={`status-pill ${statusClass(application.status)}`}>{application.status.replaceAll("_", " ")}</span></h2><div className="timeline-list">{timeline.statusHistory.map((event) => <div className="timeline-item" key={event.id}><div className="timeline-dot"/><div><strong>{event.toStatus.replaceAll("_", " ")}</strong><p>{event.reason || "Status updated"}</p><small>{new Date(event.createdAt).toLocaleString()} · {event.actor}</small></div></div>)}</div></article>
      <article className="profile-card"><div className="icon-box"><FileText size={20}/></div><span className="card-kicker">Record</span><h2>Traceability</h2><p>Profile version {application.profileVersion}. Analysis run {application.analysisRunId.slice(0, 12)}.</p><div className="product-note"><Check size={17}/><span>The application remains attached to the analysis snapshot used at submission.</span></div><Link className="text-link" href={`/app/applications/${id}/evidence-graph`}>Open evidence graph <ArrowRight size={15}/></Link></article>
    </section>

    <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Department communication</span><h2>Queries</h2></div></div><div className="analysis-list">
      {timeline.queries.length === 0 ? <div className="empty-panel">No queries have been raised.</div> : timeline.queries.map((query) => <article className="analysis-row" key={query.id}><div><div className="analysis-title-line"><h3>{query.subject}</h3><span className={`status-pill ${statusClass(query.status)}`}>{query.status}</span></div><p>{query.queryText}</p><div className="query-thread">{query.responses.map((answer) => <div className="thread-message" key={answer.id}><strong>{answer.responder}</strong><p>{answer.responseText}</p><small>{new Date(answer.createdAt).toLocaleString()}</small></div>)}</div></div>{query.status === "OPEN" ? <button className="button button-secondary button-small" onClick={() => setActiveQuery(query.id)} disabled={activeQuery === query.id}>Respond</button> : null}</article>)}
    </div></section>
    {activeQuery ? <section className="form-card inline-form"><form onSubmit={respond}><div className="form-section"><div><h2>Respond to query</h2><p>Your response becomes part of the application record.</p></div><label className="field"><span>Response</span><textarea rows={6} value={responseText} onChange={(e) => setResponseText(e.target.value)} required /></label><div className="form-footer"><button type="button" className="button button-secondary" onClick={() => setActiveQuery(null)}>Cancel</button><button type="submit" className="button button-primary" disabled={busy}>{busy ? "Sending..." : "Send response"}</button></div></div></form></section> : null}

    <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Inspections</span><h2>Inspection record</h2></div></div><div className="analysis-list">{timeline.inspections.length === 0 ? <div className="empty-panel">No inspection has been scheduled.</div> : timeline.inspections.map((inspection) => <div className="analysis-row" key={inspection.id}><div><div className="analysis-title-line"><h3>{inspection.assignedOfficer}</h3><span className="status-pill status-neutral">{inspection.outcome || "SCHEDULED"}</span></div><p>{new Date(inspection.scheduledAt).toLocaleString()}</p><div className="analysis-meta"><span>{inspection.outcomeNotes || "No outcome recorded"}</span></div></div></div>)}</div></section>
    {timeline.decision ? <section className="profile-card decision-card"><span className="card-kicker">Decision</span><h2>{timeline.decision.outcome}</h2><p>{timeline.decision.decisionNotes || "No decision note was provided."}</p>{timeline.decision.citedRuleCode ? <div className="analysis-meta"><span>Rule {timeline.decision.citedRuleCode}</span></div> : null}{timeline.decision.validUntil ? <div className="analysis-meta"><span>Valid until {new Date(timeline.decision.validUntil).toLocaleDateString("en-IN")}</span></div> : null}</section> : null}
  </div>;
}
