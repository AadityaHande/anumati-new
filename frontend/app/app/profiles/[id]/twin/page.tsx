"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import { apiClient, platformClient, BusinessRegulatoryTwin, BusinessProfile, EvidencePassport, Renewal, Compliance, IncentiveMatch, ApplicationSummary } from "../../../../../lib/api";
import { ArrowLeft, ArrowRight, Database, FileText, Layers, Shield, Calendar, Scale, ClipboardCheck } from "../../../../../components/icons";

export default function BusinessTwinPage() {
  const { id } = useParams<{ id: string }>();
  const [twin, setTwin] = useState<BusinessRegulatoryTwin | null>(null);
  const [evidence, setEvidence] = useState<EvidencePassport | null>(null);
  const [renewals, setRenewals] = useState<Renewal[]>([]);
  const [compliance, setCompliance] = useState<Compliance[]>([]);
  const [incentives, setIncentives] = useState<IncentiveMatch[]>([]);
  const [applications, setApplications] = useState<ApplicationSummary[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([
      apiClient.regulatoryTwin(id),
      platformClient.evidencePassport(id),
      platformClient.renewals(id),
      platformClient.compliance(id),
      platformClient.incentives(id),
      apiClient.listApplications(id),
    ]).then(([t, e, r, c, i, a]) => {
      setTwin(t); setEvidence(e); setRenewals(r); setCompliance(c); setIncentives(i); setApplications(a);
    }).catch((err) => setError(err instanceof Error ? err.message : "Could not load the regulatory twin."));
  }, [id]);

  if (error) return <div className="empty-state"><h1>Business twin unavailable.</h1><p>{error}</p><Link className="button button-secondary" href={`/app/profiles/${id}`}>Back to profile</Link></div>;
  if (!twin) return <div className="loading-state">Loading business twin...</div>;
  const p: BusinessProfile = twin.currentProfile;
  const activeApplications = applications.filter((item) => !["APPROVED", "REJECTED", "RENEWAL_DUE"].includes(item.status));
  const dueRenewals = renewals.filter((item) => item.status === "DUE" || item.status === "EXPIRED");
  const dueCompliance = compliance.filter((item) => item.status === "DUE" || item.status === "OVERDUE");

  return <div className="workspace-page">
    <header className="workspace-header">
      <div><span className="eyebrow">Business regulatory twin</span><h1>{p.businessName}</h1><p>The structured state Anumati uses to evaluate approvals, evidence, compliance, incentives, and regulatory changes.</p></div>
      <Link href={`/app/profiles/${id}`} className="button button-secondary"><ArrowLeft size={16}/> Back to profile</Link>
    </header>

    <section className="metric-grid">
      <article className="metric-card"><span>State version</span><strong>v{twin.regulatoryStateVersion}</strong></article>
      <article className="metric-card"><span>Verified evidence</span><strong>{evidence?.verifiedFields ?? 0}</strong></article>
      <article className="metric-card"><span>Active applications</span><strong>{activeApplications.length}</strong></article>
      <article className={`metric-card ${dueCompliance.length ? "metric-warning" : ""}`}><span>Compliance needing action</span><strong>{dueCompliance.length}</strong></article>
      <article className={`metric-card ${dueRenewals.length ? "metric-warning" : ""}`}><span>Renewals needing action</span><strong>{dueRenewals.length}</strong></article>
      <article className="metric-card"><span>Potential support schemes</span><strong>{incentives.length}</strong></article>
    </section>

    <section className="profile-grid secondary-grid">
      <article className="profile-card profile-card-wide"><div className="icon-box"><Layers size={20}/></div><span className="card-kicker">Operating state</span><h2>Current business facts</h2><div className="detail-grid"><div><span>Sector</span><strong>{p.sector}</strong></div><div><span>Activity</span><strong>{p.activity}</strong></div><div><span>District</span><strong>{p.district}</strong></div><div><span>MIDC</span><strong>{p.midcUnit ? "Yes" : "No"}</strong></div><div><span>Investment</span><strong>₹{p.investmentInr.toLocaleString("en-IN")}</strong></div><div><span>Employees</span><strong>{p.employees}</strong></div><div><span>Power usage</span><strong>{p.powerUsageKw} kW</strong></div><div><span>Stage</span><strong>{p.businessStage}</strong></div></div></article>
      <article className="profile-card"><div className="icon-box"><Database size={20}/></div><span className="card-kicker">State lineage</span><h2>Versioned</h2><p>Captured {new Date(twin.stateCapturedAt).toLocaleString("en-IN")}.</p><Link className="text-link" href={`/app/profiles/${id}/history`}>View profile history <ArrowRight size={15}/></Link></article>
      <article className="profile-card"><div className="icon-box"><Shield size={20}/></div><span className="card-kicker">Evidence boundary</span><h2>{evidence?.verifiedFields ?? 0} verified facts</h2><p>Evidence is tied to this profile version and can be reused where the requirement accepts the same fact.</p><Link className="text-link" href={`/app/profiles/${id}/evidence`}>Open Evidence Passport <ArrowRight size={15}/></Link></article>
      <article className="profile-card"><div className="icon-box"><ClipboardCheck size={20}/></div><span className="card-kicker">Execution</span><h2>{activeApplications.length} active application{activeApplications.length === 1 ? "" : "s"}</h2><p>Applications retain the exact analysis run and business version used at submission.</p><Link className="text-link" href={`/app/profiles/${id}/applications`}>Open applications <ArrowRight size={15}/></Link></article>
      <article className="profile-card"><div className="icon-box"><Calendar size={20}/></div><span className="card-kicker">Continuity</span><h2>{dueRenewals.length + dueCompliance.length} attention item{dueRenewals.length + dueCompliance.length === 1 ? "" : "s"}</h2><p>Renewals and recurring compliance remain connected to the same regulatory state.</p><div className="analysis-actions"><Link className="text-link" href={`/app/profiles/${id}/renewals`}>Renewals <ArrowRight size={14}/></Link><Link className="text-link" href={`/app/profiles/${id}/compliance`}>Compliance <ArrowRight size={14}/></Link></div></article>
      <article className="profile-card"><div className="icon-box"><Scale size={20}/></div><span className="card-kicker">Support</span><h2>{incentives.length} potential scheme{incentives.length === 1 ? "" : "s"}</h2><p>Only source-verified eligibility rules are used for support-scheme matching.</p><Link className="text-link" href={`/app/profiles/${id}/incentives`}>Review schemes <ArrowRight size={15}/></Link></article>
    </section>
  </div>;
}
