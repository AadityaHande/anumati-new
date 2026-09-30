"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { apiClient, EvidenceGraph } from "../../../../lib/api";
import { ArrowLeft, ArrowRight, Database, FileText, GitBranch, Shield } from "../../../../components/icons";

const nodeLabels: Record<string, string> = {
  APPLICATION: "Application",
  APPROVAL: "Approval",
  RULE: "Rule",
  SOURCE: "Source",
  EVIDENCE: "Evidence",
  DOCUMENT: "Document",
  DEPENDENCY: "Dependency",
  QUERY: "Query",
  INSPECTION: "Inspection",
  DECISION: "Decision",
};

export default function EvidenceGraphPage() {
  const { id } = useParams<{ id: string }>();
  const [graph, setGraph] = useState<EvidenceGraph | null>(null);
  const [error, setError] = useState("");
  useEffect(() => { apiClient.evidenceGraph(id).then(setGraph).catch((err) => setError(err instanceof Error ? err.message : "Could not load the evidence graph.")); }, [id]);

  const grouped = useMemo(() => {
    if (!graph) return {} as Record<string, EvidenceGraph["nodes"]>;
    return graph.nodes.reduce<Record<string, EvidenceGraph["nodes"]>>((acc, node) => {
      (acc[node.type] ||= []).push(node);
      return acc;
    }, {});
  }, [graph]);

  return <div className="workspace-page">
    <header className="workspace-header"><div><span className="eyebrow">Application evidence graph</span><h1>Why this record can be trusted.</h1><p>Trace the application from approval and regulatory source through evidence, documents, queries, inspections, and decision records.</p></div><Link href={`/app/applications/${id}`} className="button button-secondary"><ArrowLeft size={16}/> Back to application</Link></header>
    {error ? <div className="form-alert">{error}</div> : null}
    {graph ? <>
      <section className="metric-grid"><article className="metric-card"><span>Nodes</span><strong>{graph.nodes.length}</strong></article><article className="metric-card"><span>Relationships</span><strong>{graph.edges.length}</strong></article><article className="metric-card"><span>Sources</span><strong>{grouped.SOURCE?.length || 0}</strong></article><article className="metric-card"><span>Evidence items</span><strong>{grouped.EVIDENCE?.length || 0}</strong></article></section>
      <section className="profile-grid secondary-grid">
        {Object.entries(grouped).map(([type, nodes]) => <article className="profile-card" key={type}><div className="icon-box">{type === "SOURCE" ? <Shield size={20}/> : type === "DOCUMENT" ? <FileText size={20}/> : type === "EVIDENCE" ? <Database size={20}/> : <GitBranch size={20}/>}</div><span className="card-kicker">{nodeLabels[type] || type}</span><h2>{nodes.length}</h2><div className="compact-list">{nodes.slice(0,6).map(node => <div key={node.id}><span>{node.label}</span><strong>{node.detail}</strong></div>)}</div></article>)}
      </section>
      <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Trace</span><h2>Relationships</h2></div></div><div className="analysis-list">{graph.edges.map((edge,index)=>{const from=graph.nodes.find(n=>n.id===edge.from);const to=graph.nodes.find(n=>n.id===edge.to);return <div className="analysis-row" key={`${edge.from}-${edge.to}-${index}`}><div><div className="analysis-title-line"><h3>{from?.label || edge.from}</h3><span className="status-pill status-neutral">{edge.relationship.replaceAll("_"," ")}</span></div><p>{from?.type} → {to?.type}: {to?.label || edge.to}</p><div className="analysis-meta"><span>{to?.detail}</span></div></div></div>})}</div></section>
    </> : <div className="loading-state">Building evidence graph...</div>}
  </div>;
}
