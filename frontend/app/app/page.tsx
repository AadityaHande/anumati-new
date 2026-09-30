"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { apiClient, BusinessProfile, ApplicationSummary } from "../../lib/api";
import { ArrowRight, Database, FileText, GitBranch, Layers, Plus, Shield } from "../../components/icons";

function formatCurrency(value: number) {
  return `₹${value.toLocaleString("en-IN")}`;
}

export default function WorkspacePage() {
  const [profiles, setProfiles] = useState<BusinessProfile[]>([]);
  const [error, setError] = useState("");
  const [applications, setApplications] = useState<ApplicationSummary[]>([]);

  useEffect(() => {
    apiClient.profiles().then((list) => {
      setProfiles(list);
      if (list[0]) apiClient.listApplications(list[0].id).then(setApplications).catch(() => undefined);
    }).catch((err) => setError(err instanceof Error ? err.message : "Could not load business profiles."));
  }, []);

  const current = profiles[0];

  return (
    <div className="workspace-page">
      <header className="workspace-hero">
        <div className="workspace-hero-copy">
          <span className="eyebrow">Applicant workspace</span>
          <h1>Regulatory operations, with a clear next step.</h1>
          <p>Start from the current business state, understand what applies, prepare the evidence, and move the work through its approval lifecycle.</p>
          <div className="workspace-hero-actions">
            {current ? (
              <>
                <Link href={`/app/profiles/${current.id}`} className="button button-primary">Open regulatory map <ArrowRight size={16}/></Link>
                <Link href={`/app/profiles/${current.id}/preflight`} className="button button-secondary">Open pre-flight</Link>
              </>
            ) : (
              <Link href="/app/profiles/new" className="button button-primary">Create business profile <Plus size={16}/></Link>
            )}
          </div>
        </div>
        <div className="workspace-hero-aside">
          <div className="workspace-hero-aside-label">Current operating context</div>
          <strong>{current?.businessName || "No business profile yet"}</strong>
          <span>{current ? `${current.sector} · ${current.district}` : "Create a profile to begin the regulatory journey."}</span>
        </div>
      </header>

      {error ? <div className="form-alert" role="alert">{error}</div> : null}

      {!current ? (
        <section className="dashboard-empty-grid">
          <article className="workspace-card workspace-card-primary workspace-card-large">
            <div className="icon-box"><Layers size={21}/></div>
            <span className="card-kicker">Start here</span>
            <h2>Create the business regulatory twin</h2>
            <p>Capture the facts that influence applicability, evidence requirements, applications, compliance and support schemes.</p>
            <Link href="/app/profiles/new" className="text-link">Create a profile <ArrowRight size={15}/></Link>
          </article>
          <div className="workspace-card-stack">
            <article className="workspace-card">
              <div className="workspace-card-inline"><Shield size={18}/><div><span className="card-kicker">What comes next</span><h2>Analyse before submission</h2></div></div>
              <p>Use the saved business state to run source-backed regulatory analysis and prepare the relevant evidence.</p>
            </article>
            <article className="workspace-card">
              <div className="workspace-card-inline"><Database size={18}/><div><span className="card-kicker">Traceability</span><h2>Keep evidence connected</h2></div></div>
              <p>Verified information can be reused across applications without rewriting the business facts every time.</p>
            </article>
          </div>
        </section>
      ) : (
        <>
          <section className="workspace-context-bar">
            <div className="workspace-context-main">
              <div className="icon-box icon-box-gradient"><Layers size={20}/></div>
              <div>
                <span className="card-kicker">Current business</span>
                <h2>{current.businessName}</h2>
                <p>{current.activity} · {current.district} · {current.sector}</p>
              </div>
            </div>
            <div className="workspace-context-actions">
              <span className="status-pill status-neutral">Profile v{current.versionNumber}</span>
              <Link href={`/app/profiles/${current.id}`} className="button button-secondary button-small">Open regulatory map <ArrowRight size={15}/></Link>
            </div>
          </section>

          <section className="dashboard-section">
            <div className="section-bar"><div><span className="card-kicker">Business at a glance</span><h2>Operating context</h2></div></div>
            <div className="metric-grid workspace-metrics">
              <article className="metric-card"><span>Business stage</span><strong>{current.businessStage}</strong><small>Saved profile state</small></article>
              <article className="metric-card"><span>Investment</span><strong>{formatCurrency(current.investmentInr)}</strong><small>Declared business investment</small></article>
              <article className="metric-card"><span>Employees</span><strong>{current.employees}</strong><small>Current workforce</small></article>
              <article className="metric-card"><span>Power usage</span><strong>{current.powerUsageKw} kW</strong><small>Declared connected load</small></article>
            </div>
          </section>

          <section className="dashboard-grid dashboard-grid-primary">
            <article className="workspace-card dashboard-next-step">
              <div className="dashboard-card-heading"><div><span className="card-kicker">Recommended next step</span><h2>Run the regulatory map for this business</h2></div><span className="status-pill status-positive">Ready to continue</span></div>
              <p>Review applicability, the reason behind each result, required evidence, and the official source before moving into pre-flight or submission.</p>
              <div className="dashboard-actions"><Link href={`/app/profiles/${current.id}`} className="button button-primary button-small">Open regulatory map <ArrowRight size={15}/></Link><Link href={`/app/profiles/${current.id}/evidence`} className="button button-secondary button-small">Evidence passport</Link></div>
            </article>
            <article className="workspace-card dashboard-journey">
              <span className="card-kicker">Lifecycle</span><h2>One path through the work</h2>
              <div className="journey-steps">
                <Link href={`/app/profiles/${current.id}`}><span>01</span><strong>Understand</strong><small>Applicability</small></Link>
                <Link href={`/app/profiles/${current.id}/documents`}><span>02</span><strong>Prepare</strong><small>Evidence</small></Link>
                <Link href={`/app/profiles/${current.id}/preflight`}><span>03</span><strong>Validate</strong><small>Pre-flight</small></Link>
                <Link href={`/app/profiles/${current.id}/applications`}><span>04</span><strong>Execute</strong><small>Applications</small></Link>
              </div>
            </article>
          </section>

          <section className="dashboard-section">
            <div className="section-bar"><div><span className="card-kicker">Application desk</span><h2>Recent work</h2></div><Link className="text-link" href={`/app/profiles/${current.id}/applications`}>Open application desk <ArrowRight size={15}/></Link></div>
            <div className="analysis-list">
              {applications.length === 0 ? <div className="empty-panel">No applications have been started for this business yet.</div> : applications.slice(0, 4).map((application) => (
                <Link className="analysis-row application-row" href={`/app/applications/${application.id}`} key={application.id}>
                  <div><div className="analysis-title-line"><h3>{application.approvalName}</h3><span className={`status-pill ${application.status === "APPROVED" ? "status-positive" : application.status === "QUERY_RAISED" ? "status-warning" : "status-neutral"}`}>{application.status.replaceAll("_", " ")}</span></div><p>{application.externalReference || application.approvalCode}</p><div className="analysis-meta"><span>Profile v{application.profileVersion}</span><span>Updated {new Date(application.updatedAt).toLocaleString("en-IN")}</span></div></div><ArrowRight size={16}/></Link>
              ))}
            </div>
          </section>

          <section className="dashboard-section">
            <div className="section-bar"><div><span className="card-kicker">Businesses</span><h2>Your operating contexts</h2></div><span className="status-pill status-neutral">{profiles.length}</span></div>
            <div className="analysis-list">
              {profiles.map((profile) => (
                <article className="analysis-row application-row" key={profile.id}>
                  <div>
                    <div className="analysis-title-line"><h3>{profile.businessName}</h3><span className="status-pill status-neutral">v{profile.versionNumber}</span></div>
                    <p>{profile.activity} · {profile.district} · {profile.sector}</p>
                    <div className="analysis-meta"><span>{profile.businessStage}</span><span>{profile.employees} employees</span><span>{formatCurrency(profile.investmentInr)}</span></div>
                  </div>
                  <div className="analysis-actions"><Link className="text-link" href={`/app/profiles/${profile.id}`}>Open <ArrowRight size={15}/></Link></div>
                </article>
              ))}
            </div>
          </section>

          <section className="dashboard-grid dashboard-grid-three">
            <Link href={`/app/profiles/${current.id}/preflight`} className="workspace-card workspace-card-link">
              <div className="icon-box"><GitBranch size={19}/></div><span className="card-kicker">Prepare</span><h2>Application pre-flight</h2><p>Check evidence, readiness gaps and dependencies before submitting an approval.</p><span className="text-link">Open pre-flight <ArrowRight size={15}/></span>
            </Link>
            <Link href={`/app/profiles/${current.id}/evidence`} className="workspace-card workspace-card-link">
              <div className="icon-box"><Database size={19}/></div><span className="card-kicker">Evidence</span><h2>Evidence passport</h2><p>Keep reusable business facts connected to their verifying documents and profile version.</p><span className="text-link">Open evidence <ArrowRight size={15}/></span>
            </Link>
            <Link href={`/app/profiles/${current.id}/applications`} className="workspace-card workspace-card-link">
              <div className="icon-box"><FileText size={19}/></div><span className="card-kicker">Lifecycle</span><h2>Applications</h2><p>Follow queries, inspections, decisions, service state and renewals.</p><span className="text-link">Open applications <ArrowRight size={15}/></span>
            </Link>
          </section>
        </>
      )}
    </div>
  );
}
