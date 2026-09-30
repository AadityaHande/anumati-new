"use client";

import { ChangeEvent, useEffect, useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { apiClient, BusinessProfile, DocumentAnalysis, DocumentRecord, Readiness, uploadDirect } from "../../../../../lib/api";
import { platformClient, Evidence } from "../../../../../lib/api";
import { ArrowLeft, ArrowRight, FileText, Upload } from "../../../../../components/icons";

function statusClass(status: string) {
  if (status === "READY" || status === "MATCH") return "status-positive";
  if (status === "NEEDS_REVIEW" || status === "MISMATCH") return "status-warning";
  return "status-neutral";
}

export default function DocumentsPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const [profile, setProfile] = useState<BusinessProfile | null>(null);
  const [documents, setDocuments] = useState<DocumentRecord[]>([]);
  const [readiness, setReadiness] = useState<Readiness | null>(null);
  const [category, setCategory] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [analyses, setAnalyses] = useState<Record<string, DocumentAnalysis>>({});
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [evidence, setEvidence] = useState<Evidence[]>([]);
  const [analysisRunId, setAnalysisRunId] = useState<string | null>(null);

  useEffect(() => {
    apiClient.getProfile(id).then(setProfile).catch((err) => setError(err instanceof Error ? err.message : "Could not load the profile."));
    apiClient.listDocuments(id).then(setDocuments).catch((err) => setError(err instanceof Error ? err.message : "Could not load documents."));
    platformClient.evidence(id).then(setEvidence).catch(() => undefined);
  }, [id]);

  const categories = useMemo(() => {
    const values = new Set<string>();
    readiness?.approvals.forEach((approval) => approval.requirements.forEach((requirement) => values.add(requirement.category)));
    documents.forEach((document) => values.add(document.category));
    return Array.from(values).filter(Boolean);
  }, [documents, readiness]);

  useEffect(() => {
    const savedRun = typeof window !== "undefined" ? sessionStorage.getItem(`anumati_analysis_${id}`) : null;
    setAnalysisRunId(savedRun);
    if (savedRun) apiClient.readiness(id, savedRun).then(setReadiness).catch(() => undefined);
  }, [id]);

  async function runAnalysis() {
    setBusy(true); setError("");
    try {
      const run = await apiClient.analyse(id);
      if (typeof window !== "undefined") sessionStorage.setItem(`anumati_analysis_${id}`, run.id);
      setAnalysisRunId(run.id);
      setReadiness(await apiClient.readiness(id, run.id));
    } catch (err) {
      setError(err instanceof Error ? err.message : "The regulatory analysis could not be completed.");
    } finally {
      setBusy(false);
    }
  }

  async function handleUpload(event: ChangeEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!file || !category) return;
    setBusy(true); setError("");
    try {
      const presigned = await apiClient.presignDocument({ businessProfileId: id, category, originalFilename: file.name, contentType: file.type || "application/octet-stream", sizeBytes: file.size });
      await uploadDirect(presigned.uploadUrl, file);
      const completed = await apiClient.completeDocument(presigned.documentId);
      setDocuments((current) => [completed, ...current]);
      setFile(null);
      const analysis = await apiClient.analyzeDocument(completed.id);
      setAnalyses((current) => ({ ...current, [completed.id]: analysis }));
    } catch (err) {
      setError(err instanceof Error ? err.message : "The document could not be processed.");
    } finally { setBusy(false); }
  }

  async function promote(documentId: string) {
    setError("");
    try { const created = await platformClient.promoteEvidence(id, documentId); setEvidence((current) => [...current.filter((item) => item.fieldName !== created.fieldName), created]); } catch (err) { setError(err instanceof Error ? err.message : "Could not promote evidence."); }
  }

  async function analyseDocument(documentId: string) {
    setError("");
    try {
      const analysis = await apiClient.analyzeDocument(documentId);
      setAnalyses((current) => ({ ...current, [documentId]: analysis }));
      setDocuments((current) => current.map((item) => item.id === documentId ? { ...item, status: analysis.overallStatus as DocumentRecord["status"] } : item));
    } catch (err) { setError(err instanceof Error ? err.message : "Document analysis failed."); }
  }

  if (!profile) return <div className="loading-state">Loading documents...</div>;

  return (
    <div className="profile-page">
      <header className="workspace-header">
        <div><span className="eyebrow">Evidence · {profile.businessName}</span><h1>Document readiness.</h1><p>Upload the evidence required by the current regulatory analysis. Extraction checks information against the saved profile.</p></div>
        <Link className="button button-secondary" href={`/app/profiles/${id}`}><ArrowLeft size={16}/> Back to analysis</Link>
      </header>
      {error ? <div className="form-alert" role="alert">{error}</div> : null}
      <section className="document-prep-bar" aria-label="Document preparation steps">
        <div className="document-prep-item"><span className="document-prep-index">01</span><div><strong>Current regulatory analysis</strong><span>{analysisRunId ? `Analysis run ${analysisRunId.slice(0, 8)} · profile v${profile.versionNumber}` : "Run the rule engine before choosing evidence categories."}</span></div></div>
        <div className="document-prep-item"><span className="document-prep-index">02</span><div><strong>Verified evidence</strong><span>{categories.length ? `${categories.length} evidence categories are available for the current requirements.` : "No categories are loaded yet."}</span></div></div>
      </section>
      <section className="form-card upload-panel">
        <div className="form-section">
          <div><h2>Add evidence</h2><p>Use the requirement category from your analysis. Supported text extraction depends on the file type.</p></div>
          <form className="form-grid" onSubmit={handleUpload}>
            <label className="field"><span>Requirement category</span><select value={category} onChange={(e) => setCategory(e.target.value)} required disabled={categories.length === 0}><option value="">{categories.length ? "Select a category" : "No verified requirements configured"}</option>{categories.map((item) => <option key={item} value={item}>{item}</option>)}</select></label>
            <label className="field"><span>File</span><input type="file" accept="application/pdf,image/png,image/jpeg,text/plain,.docx" onChange={(e) => setFile(e.target.files?.[0] || null)} required /></label>
            <div className="form-footer form-footer-span">{categories.length ? <p>Local judging mode stores the uploaded file through the same object-storage contract used by the production adapter. The regulatory decision remains rule-based.</p> : <div className="document-empty-action"><p>No verified document requirements are attached to this view yet. Run the current analysis to load the evidence checklist.</p><button className="button button-secondary button-small" type="button" onClick={runAnalysis} disabled={busy}>{busy ? "Running analysis..." : "Run analysis"} <ArrowRight size={15}/></button></div>}<button className="button button-primary" type="submit" disabled={busy || !file || !category}><Upload size={16}/>{busy ? "Working..." : "Upload and check"}</button></div>
          </form>
        </div>
      </section>

      <section className="analysis-section">
        <div className="section-bar"><div><span className="card-kicker">Stored evidence</span><h2>Documents</h2></div><span className="status-pill status-neutral">{documents.length} uploaded</span></div>
        <div className="analysis-list">
          {documents.length === 0 ? <div className="empty-panel">No documents have been uploaded for this business profile.</div> : documents.map((document) => {
            const analysis = analyses[document.id];
            return <article className="analysis-row" key={document.id}>
              <div>
                <div className="analysis-title-line"><h3>{document.originalFilename}</h3><span className={`status-pill ${statusClass(document.status)}`}>{document.status.replaceAll("_", " ")}</span></div>
                <p>{document.category} · {document.contentType}</p>
                {analysis ? <div className="analysis-meta"><span>Extraction: {analysis.extractionStatus}</span><span>Engine: {analysis.engine}</span><span>Checked: {new Date(analysis.analyzedAt).toLocaleString()}</span></div> : null}
                {analysis?.consistency?.length ? <div className="consistency-list">{analysis.consistency.map((check) => <div key={check.fieldName} className="consistency-row"><span>{check.fieldName}</span><span className={`status-pill ${statusClass(check.status)}`}>{check.status.replaceAll("_", " ")}</span><small>{check.reason}</small></div>)}</div> : null}
              </div>
              <div className="analysis-actions"><button className="button button-secondary button-small" onClick={() => analyseDocument(document.id)}><FileText size={15}/> Analyse</button>{document.status === "READY" && analysis ? <button className="button button-primary button-small" onClick={() => promote(document.id)}>Promote matched evidence</button> : null}</div>
            </article>;
          })}
        </div>
      </section>

      <section className="analysis-section"><div className="section-bar"><div><span className="card-kicker">Verified facts</span><h2>Reusable evidence</h2></div><span className="status-pill status-positive">{evidence.length} verified</span></div><div className="analysis-list">{evidence.length === 0 ? <div className="empty-panel">No verified evidence has been promoted from matched documents yet.</div> : evidence.map(item => <article className="analysis-row" key={item.id}><div><div className="analysis-title-line"><h3>{item.fieldName}</h3><span className="status-pill status-positive">Verified</span></div><p>{item.fieldValue}</p><div className="analysis-meta"><span>Source document: {item.sourceDocumentName}</span><span>Verified against profile v{item.profileVersion}</span></div></div></article>)}</div></section>
    </div>
  );
}
