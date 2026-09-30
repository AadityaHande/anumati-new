"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { Brand } from "../../components/brand";
import { ArrowRight, Check, Lock, Shield } from "../../components/icons";
import { login } from "../../lib/api";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setBusy(true);
    try {
      const session = await login(username.trim(), password);
      setPassword("");
      if (session.roles.includes("ADMIN")) {
        router.replace("/admin");
      } else if (session.roles.includes("DEPARTMENT_OFFICER")) {
        router.replace("/officer");
      } else {
        router.replace("/app");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "The credentials could not be verified.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-page">
      <div className="auth-layout">
        <section className="auth-side">
          <Brand />
          <div className="auth-side-content">
            <span className="eyebrow eyebrow-light">Regulatory operations workspace</span>
            <h1>Move from regulatory uncertainty to an evidence-backed workflow.</h1>
            <p>Access the business, evidence, approvals, inspections and continuity views configured for this Anumati deployment.</p>
            <div className="auth-side-list">
              <div><Check size={16}/><span>Source-linked regulatory analysis</span></div>
              <div><Shield size={16}/><span>Versioned evidence and audit history</span></div>
              <div><Check size={16}/><span>Applicant and department workflows</span></div>
            </div>
          </div>
          <span className="auth-side-foot">Local development workspace</span>
        </section>

        <section className="auth-panel">
          <div className="auth-card">
            <div className="auth-icon"><Lock size={19}/></div>
            <span className="eyebrow">Workspace access</span>
            <h2>Sign in to Anumati</h2>
            <p>Use the account configured for this deployment.</p>
            <form onSubmit={submit} className="auth-form">
              {error ? <div className="form-alert" role="alert">{error}</div> : null}
              <label className="field"><span>Username</span><input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" autoFocus required /></label>
              <label className="field"><span>Password</span><input value={password} onChange={(e) => setPassword(e.target.value)} type="password" autoComplete="current-password" required /></label>
              <button className="button button-primary auth-submit" type="submit" disabled={busy}>{busy ? "Signing in…" : "Sign in"}<ArrowRight size={17}/></button>
            </form>
            <div className="auth-note">Access is controlled by the backend session and role configuration.</div>
          </div>
        </section>
      </div>
    </main>
  );
}
