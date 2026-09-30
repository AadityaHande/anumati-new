"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { apiClient, KnowledgeTrace } from "../../../../../lib/api";
import { ArrowRight, FileText, GitBranch, Landmark, Shield } from "../../../../../components/icons";

export default function KnowledgeTracePage() {
  const { id } = useParams<{ id: string }>();
  const [data, setData] = useState<KnowledgeTrace | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    apiClient.knowledgeTrace(id).then(setData).catch((err) => setError(err instanceof Error ? err.message : "Could not load the regulatory trace."));
  }, [id]);

  if (error) return <div className="empty-state"><h1>Regulatory trace unavailable</h1><p>{error}</p><Link href="/app" className="button button-secondary">Back to workspace</Link></div>;
  if (!data) return <div className="loading-state">Loading regulatory trace...</div>;

  return (
    <div className="workspace-page">
      <header className="workspace-header">
        <div><span className="eyebrow">Regulatory Knowledge Fabric</span><h1>{data.approvalName}</h1><p>{data.approvalCode} · {data.authority}</p></div>
        <Link href="/app" className="button button-secondary">Workspace</Link>
      </header>
      <section className="profile-grid secondary-grid">
        <article className="profile-card"><div className="icon-box"><Shield size={20}/></div><span className="card-kicker">Rules</span><h2>{data.rules.length}</h2><p>Active source-backed rules linked to this approval.</p></article>
        <article className="profile-card"><div className="icon-box"><FileText size={20}/></div><span className="card-kicker">Documents</span><h2>{data.documents.length}</h2><p>Configured document requirements with authoritative sources.</p></article>
        <article className="profile-card"><div className="icon-box"><GitBranch size={20}/></div><span className="card-kicker">Dependencies</span><h2>{data.dependencies.length}</h2><p>Active prerequisite relationships for this approval.</p></article>
      </section>
      <section className="analysis-section">
        <div className="section-bar"><div><span className="card-kicker">Traceability</span><h2>Source to workflow</h2></div></div>
        <div className="analysis-list">
          {data.rules.map((rule) => <article className="analysis-row" key={rule.ruleId}><div><div className="analysis-title-line"><h3>{rule.name}</h3><span className="status-pill status-neutral">v{rule.version}</span></div><p>Rule {rule.code} · {rule.outcome}</p><div className="analysis-reason"><strong>Source</strong><span>{rule.sourceTitle}</span></div></div>{rule.sourceUrl ? <a className="source-link" href={rule.sourceUrl} target="_blank" rel="noreferrer">Open source <ArrowRight size={14}/></a> : null}</article>)}
          {data.documents.map((doc) => <article className="analysis-row" key={doc.documentRequirementId}><div><div className="analysis-title-line"><h3>{doc.name}</h3><span className={`status-pill ${doc.mandatory ? "status-warning" : "status-neutral"}`}>{doc.mandatory ? "Mandatory" : "Optional"}</span></div><div className="analysis-reason"><strong>Source</strong><span>{doc.sourceTitle}</span></div></div></article>)}
          {data.dependencies.map((dependency) => <article className="analysis-row" key={`${dependency.approvalId}-${dependency.type}`}><div><div className="analysis-title-line"><h3>Depends on {dependency.approvalName}</h3><span className="status-pill status-neutral">{dependency.type}</span></div><p>{dependency.approvalCode}</p></div><GitBranch size={17}/></article>)}
        </div>
      </section>
      <section className="product-note"><Landmark size={18}/><span>This trace explains the configured regulatory catalogue. It does not replace a statutory decision by the responsible authority.</span></section>
    </div>
  );
}
