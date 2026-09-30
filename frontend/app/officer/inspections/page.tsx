"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { platformClient, InspectionPlanner } from "../../../lib/api";
import { ArrowLeft, Calendar, Network } from "../../../components/icons";

export default function InspectionPlannerPage() {
  const [data, setData] = useState<InspectionPlanner | null>(null);
  const [error, setError] = useState("");
  useEffect(() => { platformClient.inspectionPlanner().then(setData).catch((err) => setError(err instanceof Error ? err.message : "Could not load inspection planning.")); }, []);
  return <div className="workspace-page">
    <header className="workspace-header"><div><span className="eyebrow">Government operations</span><h1>Inspection planning.</h1><p>View scheduled inspections and identify opportunities for coordinated visits without overriding departmental authority.</p></div><Link href="/officer" className="button button-secondary"><ArrowLeft size={15}/> Control tower</Link></header>
    {error ? <div className="form-alert">{error}</div> : null}
    {data ? <>
      <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Coordination</span><h2>Potential joint planning</h2></div></div><div className="analysis-list">{data.coordinationOpportunities.length===0?<div className="empty-panel"><Network size={20}/><h3>No coordination opportunity detected.</h3><p>Opportunities appear when multiple authorities have inspections for the same business within the configured planning window.</p></div>:data.coordinationOpportunities.map((item,index)=><article className="profile-card" key={`${item.businessProfileId}-${index}`}><div className="analysis-title-line"><h3>{item.businessName}</h3><span className="status-pill status-warning">Review coordination</span></div><p>{item.district} · {item.inspectionCount} inspections between {new Date(item.windowStart).toLocaleDateString("en-IN")} and {new Date(item.windowEnd).toLocaleDateString("en-IN")}.</p><div className="analysis-meta">{item.authorities.map((authority)=><span key={authority}>{authority}</span>)}</div></article>)}</div></section>
      <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Schedule</span><h2>Upcoming inspections</h2></div><span className="status-pill status-neutral">{data.inspections.length}</span></div><div className="analysis-list">{data.inspections.length===0?<div className="empty-panel"><Calendar size={20}/><h3>No upcoming inspections.</h3></div>:data.inspections.map(item=><Link href={`/officer/applications/${item.applicationId}`} className="analysis-row" key={item.inspectionId}><div><div className="analysis-title-line"><h3>{item.businessName}</h3><span className="status-pill status-neutral">{item.outcome}</span></div><p>{item.authority} · {item.district}</p><div className="analysis-meta"><span>{new Date(item.scheduledAt).toLocaleString("en-IN")}</span><span>{item.assignedOfficer}</span></div></div></Link>)}</div></section>
    </> : null}
  </div>;
}
