"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { apiClient, BusinessProfileVersion } from "../../../../../lib/api";
import { ArrowLeft, ArrowRight, Clock } from "../../../../../components/icons";

export default function ProfileHistoryPage() {
  const { id } = useParams<{ id: string }>();
  const [versions, setVersions] = useState<BusinessProfileVersion[]>([]);
  const [error, setError] = useState("");
  useEffect(() => { apiClient.profileVersions(id).then(setVersions).catch((err) => setError(err instanceof Error ? err.message : "Could not load profile history.")); }, [id]);

  return <div className="workspace-page">
    <header className="workspace-header"><div><span className="eyebrow">Business state history</span><h1>Profile versions.</h1><p>Every regulatory-relevant business change is preserved as a versioned state.</p></div><Link href={`/app/profiles/${id}`} className="button button-secondary"><ArrowLeft size={15}/> Back to regulatory map</Link></header>
    {error ? <div className="form-alert">{error}</div> : null}
    <section className="timeline-list">{versions.length === 0 ? <div className="empty-panel"><Clock size={20}/><h3>No version history.</h3></div> : versions.map((version) => <article className="profile-card" key={version.id}><div className="panel-topline"><span>Version {version.versionNumber}</span><span className="status-pill status-neutral">{version.changeType}</span></div><h2>{new Date(version.capturedAt).toLocaleString("en-IN")}</h2><p>Captured by {version.capturedBy}.</p><div className="compact-list">{Object.entries(version.snapshot).map(([key, value]) => <div key={key}><span>{key.replaceAll(/([A-Z])/g, " $1")}</span><strong>{String(value)}</strong></div>)}</div></article>)}</section>
    <div className="product-note"><ArrowRight size={17}/><span>Analysis runs and evidence are pinned to these versions so historical decisions remain reproducible.</span></div>
  </div>;
}
