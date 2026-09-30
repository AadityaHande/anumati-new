"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { apiClient, ApplicationSummary, BusinessProfile } from "../../../../../lib/api";
import { ArrowRight, Landmark } from "../../../../../components/icons";

export default function ApplicationsPage() {
  const { id } = useParams<{ id: string }>();
  const [profile, setProfile] = useState<BusinessProfile | null>(null);
  const [applications, setApplications] = useState<ApplicationSummary[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([apiClient.getProfile(id), apiClient.listApplications(id)]).then(([p, a]) => { setProfile(p); setApplications(a); }).catch((err) => setError(err instanceof Error ? err.message : "Could not load applications."));
  }, [id]);

  if (!profile) return <div className="loading-state">Loading applications...</div>;
  return <div className="profile-page">
    <header className="workspace-header"><div><span className="eyebrow">Lifecycle · {profile.businessName}</span><h1>Applications.</h1><p>Track submissions, department activity, inspections, decisions, and renewal states against a fixed analysis run.</p></div><Link href={`/app/profiles/${id}`} className="button button-secondary">Back to analysis <ArrowRight size={16}/></Link></header>
    {error ? <div className="form-alert">{error}</div> : null}
    <section className="analysis-list">
      {applications.length === 0 ? <div className="empty-panel"><h3>No applications yet</h3><p>Complete evidence readiness and submit an applicable approval from the analysis page.</p></div> : applications.map((app) => <Link href={`/app/applications/${app.id}`} className="analysis-row application-row" key={app.id}><div><div className="analysis-title-line"><h3>{app.approvalName}</h3><span className="status-pill status-neutral">{app.status.replaceAll("_", " ")}</span></div><p>{app.approvalCode} · Reference {app.externalReference || "Pending"}</p><div className="analysis-meta"><span>Analysis run {app.analysisRunId.slice(0, 8)}</span><span>Profile v{app.profileVersion}</span><span>Updated {new Date(app.updatedAt).toLocaleDateString()}</span></div></div><ArrowRight size={17}/></Link>)}
    </section>
    <div className="product-note"><Landmark size={18}/><span>Application decisions remain with the responsible government authority. Anumati provides the preparation and lifecycle workspace.</span></div>
  </div>;
}
