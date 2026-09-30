"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { platformClient, ApprovalCatalog, RegulatorySource, SourceChange } from "../../lib/api";
import { ArrowRight, Database, GitCompare, Landmark, Shield } from "../../components/icons";

export default function AdminOverviewPage() {
  const [sources, setSources] = useState<RegulatorySource[]>([]);
  const [approvals, setApprovals] = useState<ApprovalCatalog[]>([]);
  const [changes, setChanges] = useState<SourceChange[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([platformClient.regulatorySources(), platformClient.approvalsCatalog(), platformClient.sourceChanges()])
      .then(([sourceList, approvalList, changeList]) => {
        setSources(sourceList); setApprovals(approvalList); setChanges(changeList);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Could not load administration data."));
  }, []);

  const verified = sources.filter((source) => source.verificationStatus === "VERIFIED").length;
  const changed = changes.filter((change) => change.contentChanged).length;

  return (
    <div className="admin-page">
      <header className="admin-hero">
        <div>
          <span className="eyebrow">REGULATORY GOVERNANCE</span>
          <h1>Keep the decision layer controlled.</h1>
          <p>Manage the verified material, applicability rules, evidence requirements and service configuration that power Anumati's operational workflows.</p>
        </div>
        <div className="admin-hero-side">
          <span className="status-pill status-positive">Admin session</span>
          <strong>Sources before decisions.</strong>
          <span>Only verified catalogue records should influence operational outcomes.</span>
        </div>
      </header>

      {error ? <div className="form-alert" role="alert">{error}</div> : null}

      <section className="admin-metrics">
        <article><span>Verified sources</span><strong>{verified}</strong><small>Eligible to support decisions</small></article>
        <article><span>Approval catalogue</span><strong>{approvals.length}</strong><small>Configured approval definitions</small></article>
        <article><span>Source families changed</span><strong>{changed}</strong><small>Content changes requiring review</small></article>
      </section>

      <section className="admin-work-grid">
        <Link className="admin-work-panel admin-work-panel-primary" href="/admin/regulatory">
          <span className="panel-index">01</span>
          <div className="admin-panel-icon"><Database size={19}/></div>
          <span className="card-kicker">Governance desk</span>
          <h2>Regulatory catalogue</h2>
          <p>Register official sources, approvals, applicability rules, evidence requirements, SLAs and support schemes.</p>
          <span className="text-link">Open catalogue <ArrowRight size={15}/></span>
        </Link>
        <Link className="admin-work-panel" href="/officer">
          <span className="panel-index">02</span>
          <div className="admin-panel-icon"><Landmark size={19}/></div>
          <span className="card-kicker">Operations</span>
          <h2>Department control tower</h2>
          <p>Review the application workload, queries, inspections, service timelines and escalations powered by the catalogue.</p>
          <span className="text-link">Open control tower <ArrowRight size={15}/></span>
        </Link>
        <Link className="admin-work-panel" href="/app">
          <span className="panel-index">03</span>
          <div className="admin-panel-icon"><Shield size={19}/></div>
          <span className="card-kicker">Connected experience</span>
          <h2>Applicant workspace</h2>
          <p>View the same regulatory model from the business side without confusing governance controls with applicant work.</p>
          <span className="text-link">Open applicant view <ArrowRight size={15}/></span>
        </Link>
        <div className="admin-work-panel admin-work-panel-note">
          <span className="panel-index">04</span>
          <div className="admin-panel-icon"><GitCompare size={19}/></div>
          <span className="card-kicker">Control principle</span>
          <h2>Traceable changes</h2>
          <p>Changes to source material are surfaced as version families so governance review can happen before new records influence analysis.</p>
          <span className="status-pill status-neutral">Governance boundary</span>
        </div>
      </section>
    </div>
  );
}
