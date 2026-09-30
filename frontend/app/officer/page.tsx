"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { platformClient, ControlTower, DepartmentDashboard, Grievance, Renewal } from "../../lib/api";
import { AlertTriangle, ArrowRight, Calendar, ClipboardCheck, Landmark, MessageSquare, Network } from "../../components/icons";

function MetricStrip({ label, value, tone = "neutral", detail }: { label: string; value: number | string; tone?: "neutral" | "warning" | "positive"; detail?: string }) {
  return (
    <article className={`department-metric ${tone}`}>
      <div><span>{label}</span><strong>{value}</strong></div>
      {detail ? <small>{detail}</small> : null}
    </article>
  );
}

export default function OfficerPage() {
  const [dashboard, setDashboard] = useState<DepartmentDashboard | null>(null);
  const [tower, setTower] = useState<ControlTower | null>(null);
  const [grievances, setGrievances] = useState<Grievance[]>([]);
  const [renewals, setRenewals] = useState<Renewal[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([
      platformClient.departmentDashboard(),
      platformClient.controlTower(),
      platformClient.departmentGrievances(),
      platformClient.departmentRenewals(),
    ])
      .then(([d, t, g, r]) => { setDashboard(d); setTower(t); setGrievances(g); setRenewals(r); })
      .catch((e) => setError(e instanceof Error ? e.message : "Could not load the department workspace."));
  }, []);

  const activeGrievances = useMemo(() => grievances.filter((g) => ["OPEN", "ASSIGNED", "IN_PROGRESS"].includes(g.status)).length, [grievances]);
  const renewalAttention = useMemo(() => renewals.filter((r) => ["DUE", "EXPIRED"].includes(r.status)).length, [renewals]);
  const urgentLanes = useMemo(() => (tower?.lanes || []).filter((lane) => lane.blockers.length || lane.slaStatus === "AT_RISK" || lane.slaStatus === "OVERDUE").slice(0, 6), [tower]);

  return (
    <div className="department-page">
      <header className="department-page-header">
        <div>
          <span className="eyebrow">Government operations</span>
          <h1>Control tower</h1>
          <p>A working view of applications that need attention, with the context to act on them.</p>
        </div>
        <div className="department-page-header-actions">
          <Link href="/officer/inspections" className="button button-secondary">Plan inspections <ArrowRight size={15}/></Link>
          <Link href="/app" className="button button-primary">Applicant view <ArrowRight size={15}/></Link>
        </div>
      </header>

      {error ? <div className="form-alert" role="alert">{error}</div> : null}

      {dashboard ? (
        <section className="department-metric-strip" aria-label="Department summary">
          <MetricStrip label="Applications" value={dashboard.totalApplications} detail="Across active approval workflows" />
          <MetricStrip label="Pending scrutiny" value={dashboard.pendingScrutiny} detail="Waiting for department action" />
          <MetricStrip label="Queries" value={dashboard.queriesAwaitingApplicant} detail="Awaiting applicant response" />
          <MetricStrip label="SLA at risk" value={dashboard.slaAtRisk} tone="warning" detail="Approaching configured warning threshold" />
          <MetricStrip label="Inspections" value={dashboard.inspectionsPending} detail="Pending field coordination" />
          <MetricStrip label="Grievances" value={activeGrievances} tone={activeGrievances ? "warning" : "positive"} detail="Open or in progress" />
        </section>
      ) : null}

      <section className="department-main-grid">
        <div className="department-primary-column">
          <section className="department-section-block">
            <div className="department-section-heading">
              <div><span className="card-kicker">Attention queue</span><h2>Work that needs a next action</h2></div>
              {tower ? <span className="section-count">{tower.total} applications</span> : null}
            </div>
            <div className="attention-list">
              {!tower || urgentLanes.length === 0 ? (
                <div className="department-empty"><span className="department-empty-mark">✓</span><div><strong>No urgent lane right now</strong><p>The control tower has no blocked, at-risk or overdue application in the current reference dataset.</p></div></div>
              ) : urgentLanes.map((lane) => (
                <Link key={lane.applicationId} href={`/officer/applications/${lane.applicationId}`} className="attention-row">
                  <div className="attention-status"><span className={lane.blockers.length ? "status-dot dot-red" : "status-dot dot-amber"}/><small>{lane.blockers.length ? "Blocked" : lane.slaStatus.replaceAll("_", " ")}</small></div>
                  <div className="attention-main"><strong>{lane.businessName}</strong><span>{lane.approvalName} · {lane.authority}</span><small>{lane.district} · {lane.reference}</small></div>
                  <div className="attention-next"><span>{lane.blockers.length ? lane.blockers[0] : lane.nextAction}</span><ArrowRight size={17}/></div>
                </Link>
              ))}
            </div>
          </section>

          <section className="department-section-block">
            <div className="department-section-heading"><div><span className="card-kicker">Application flow</span><h2>All application lanes</h2></div></div>
            <div className="department-table-wrap">
              <div className="department-table-head"><span>Business</span><span>Approval</span><span>Status</span><span>Service clock</span><span>Next action</span></div>
              <div className="department-table-body">
                {!tower || tower.lanes.length === 0 ? <div className="department-empty">No application records are available.</div> : tower.lanes.slice(0, 30).map((lane) => (
                  <Link href={`/officer/applications/${lane.applicationId}`} className="department-table-row" key={lane.applicationId}>
                    <div><strong>{lane.businessName}</strong><small>{lane.district} · {lane.reference}</small></div>
                    <div><strong>{lane.approvalName}</strong><small>{lane.authority}</small></div>
                    <div><span className={`status-pill ${lane.blockers.length ? "status-warning" : lane.status === "APPROVED" ? "status-positive" : "status-neutral"}`}>{lane.blockers.length ? "Blocked" : lane.status.replaceAll("_", " ")}</span></div>
                    <div><strong className={lane.slaStatus === "OVERDUE" || lane.slaStatus === "AT_RISK" ? "text-warning" : ""}>{lane.slaStatus.replaceAll("_", " ")}</strong><small>{lane.slaDueAt ? new Date(lane.slaDueAt).toLocaleDateString("en-IN") : "No due date"}</small></div>
                    <div className="table-next"><span>{lane.nextAction}</span><ArrowRight size={15}/></div>
                  </Link>
                ))}
              </div>
            </div>
          </section>
        </div>

        <aside className="department-side-column">
          <section className="desk-panel">
            <div className="desk-panel-head"><span className="card-kicker">Desk overview</span><Landmark size={18}/></div>
            <div className="desk-stat"><span>Blocked</span><strong>{tower?.blocked || 0}</strong><small>Dependency-driven</small></div>
            <div className="desk-stat"><span>Renewals</span><strong>{renewalAttention}</strong><small>Due or expired</small></div>
            <div className="desk-stat"><span>Grievances</span><strong>{activeGrievances}</strong><small>Open or in progress</small></div>
            <div className="desk-stat"><span>Overdue</span><strong>{dashboard?.overdue || 0}</strong><small>Past configured SLA</small></div>
          </section>

          <section className="desk-actions">
            <span className="card-kicker">Quick desks</span>
            <Link href="/officer/inspections" className="desk-action"><Calendar size={17}/><span><strong>Inspection planning</strong><small>Coordinate overlapping visits</small></span><ArrowRight size={15}/></Link>
            <Link href="/officer/renewals" className="desk-action"><ClipboardCheck size={17}/><span><strong>Renewal desk</strong><small>{renewalAttention ? `${renewalAttention} need attention` : "Nothing due"}</small></span><ArrowRight size={15}/></Link>
            <Link href="/officer/grievances" className="desk-action"><MessageSquare size={17}/><span><strong>Grievance desk</strong><small>{activeGrievances ? `${activeGrievances} active issues` : "No active issues"}</small></span><ArrowRight size={15}/></Link>
          </section>

          <section className="department-note">
            <AlertTriangle size={17}/>
            <div><strong>Operational note</strong><p>The control tower surfaces workflow context. Statutory decisions remain with authorised department officers.</p></div>
          </section>
        </aside>
      </section>
    </div>
  );
}
