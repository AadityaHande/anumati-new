"use client";

import { useEffect, useState } from "react";
import { platformClient, Grievance } from "../../../lib/api";
import { MessageSquare } from "../../../components/icons";

export default function OfficerGrievances() {
  const [items, setItems] = useState<Grievance[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    platformClient.departmentGrievances()
      .then(setItems)
      .catch((err) => setError(err instanceof Error ? err.message : "Could not load grievances."));
  }, []);

  return (
    <div className="workspace-page">
      <header className="workspace-header"><div><span className="eyebrow">Government operations</span><h1>Grievance desk</h1><p>Open and active issues raised by business users.</p></div></header>
      {error ? <div className="form-alert" role="alert">{error}</div> : null}
      <section className="analysis-list">
        {items.length === 0 ? (
          <div className="empty-panel"><MessageSquare size={20}/><h3>No grievances</h3><p>No issue records are currently present.</p></div>
        ) : items.map((grievance) => (
          <article className="analysis-row" key={grievance.id}>
            <div>
              <div className="analysis-title-line"><h3>{grievance.subject}</h3><span className="status-pill status-neutral">{grievance.status}</span></div>
              <p>{grievance.department || "Department not specified"}</p>
              <div className="analysis-meta"><span>{grievance.priority}</span><span>{new Date(grievance.createdAt).toLocaleString("en-IN")}</span></div>
              <p>{grievance.description}</p>
            </div>
          </article>
        ))}
      </section>
    </div>
  );
}
