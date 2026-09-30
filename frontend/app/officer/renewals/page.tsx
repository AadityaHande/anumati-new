"use client";

import { useEffect, useState } from "react";
import { platformClient, Renewal } from "../../../lib/api";
import { Calendar } from "../../../components/icons";

export default function OfficerRenewals() {
  const [items, setItems] = useState<Renewal[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    platformClient.departmentRenewals()
      .then(setItems)
      .catch((err) => setError(err instanceof Error ? err.message : "Could not load renewals."));
  }, []);

  return (
    <div className="workspace-page">
      <header className="workspace-header"><div><span className="eyebrow">Government operations</span><h1>Renewal register</h1><p>Validity and reminder state for approved application records.</p></div></header>
      {error ? <div className="form-alert" role="alert">{error}</div> : null}
      <section className="analysis-list">
        {items.length === 0 ? (
          <div className="empty-panel"><Calendar size={20}/><h3>No renewal records</h3><p>Approved applications with configured validity periods will appear here.</p></div>
        ) : items.map((renewal) => (
          <article className="analysis-row" key={renewal.id}>
            <div>
              <div className="analysis-title-line"><h3>{renewal.approvalName}</h3><span className="status-pill status-neutral">{renewal.status}</span></div>
              <p>{renewal.approvalCode} · Application {renewal.applicationId.slice(0, 8)}</p>
              <div className="analysis-meta"><span>Valid until {new Date(renewal.validUntil).toLocaleDateString("en-IN")}</span><span>Reminder {renewal.reminderDays} days before</span></div>
            </div>
          </article>
        ))}
      </section>
    </div>
  );
}
