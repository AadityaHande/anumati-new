"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowRight, ChevronDown } from "../../../../components/icons";
import { apiClient, BusinessProfileRequest, BusinessStage } from "../../../../lib/api";

const initial: BusinessProfileRequest = {
  businessName: "",
  sector: "",
  activity: "",
  district: "",
  midcUnit: false,
  investmentInr: 0,
  panNumber: "",
  gstin: "",
  employees: 0,
  powerUsageKw: 0,
  businessStage: "SETUP",
  regulatoryAttributes: {},
};

export default function NewProfilePage() {
  const router = useRouter();
  const [form, setForm] = useState(initial);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  function update<K extends keyof BusinessProfileRequest>(key: K, value: BusinessProfileRequest[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setBusy(true);
    try {
      const profile = await apiClient.createProfile({ ...form, investmentInr: Number(form.investmentInr), employees: Number(form.employees), powerUsageKw: Number(form.powerUsageKw) });
      if (typeof window !== "undefined") localStorage.setItem("anumati_last_profile", profile.id);
      router.push(`/app/profiles/${profile.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "We could not create the profile.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="form-page">
      <header className="workspace-header form-header"><div><span className="eyebrow">Business profile</span><h1>Create a business profile.</h1><p>Use the facts that shape regulatory applicability. You can revise the profile later.</p></div></header>
      <form className="form-card" onSubmit={submit}>
        {error ? <div className="form-alert" role="alert">{error}</div> : null}
        <div className="form-section"><div><h2>Business identity</h2><p>Basic information about the unit.</p></div><div className="form-grid">
          <label className="field field-span-2"><span>Business name</span><input value={form.businessName} onChange={(e) => update("businessName", e.target.value)} placeholder="Registered business or unit name" required /></label>
          <label className="field"><span>Sector</span><input value={form.sector} onChange={(e) => update("sector", e.target.value)} placeholder="Manufacturing, processing, services" required /></label>
          <label className="field"><span>Activity</span><input value={form.activity} onChange={(e) => update("activity", e.target.value)} placeholder="Primary business activity" required /></label>
          <label className="field"><span>District</span><input value={form.district} onChange={(e) => update("district", e.target.value)} placeholder="District" required /></label>
          <label className="field"><span>Business stage</span><span className="select-wrap"><select value={form.businessStage} onChange={(e) => update("businessStage", e.target.value as BusinessStage)}><option value="IDEA">Idea</option><option value="SETUP">Setup</option><option value="OPERATING">Operating</option><option value="EXPANSION">Expansion</option></select><ChevronDown size={16}/></span></label>
        </div></div>
        <div className="form-section"><div><h2>Operating context</h2><p>These values are used by the rules engine where relevant.</p></div><div className="form-grid">
          <label className="field"><span>Investment (INR)</span><input type="number" min="0" value={form.investmentInr} onChange={(e) => update("investmentInr", Number(e.target.value))} required /></label>
          <label className="field"><span>Employees</span><input type="number" min="0" value={form.employees} onChange={(e) => update("employees", Number(e.target.value))} required /></label>
          <label className="field"><span>Power usage (kW)</span><input type="number" min="0" step="0.01" value={form.powerUsageKw} onChange={(e) => update("powerUsageKw", Number(e.target.value))} required /></label>
          <label className="field field-check"><input type="checkbox" checked={form.midcUnit} onChange={(e) => update("midcUnit", e.target.checked)} /><span>Unit is located in an MIDC area</span></label>
        </div></div>
        
        <div className="form-section"><div><h2>Regulatory characteristics</h2><p>Explicit business characteristics can drive source-backed regulatory rules. They are stored as part of the versioned business state.</p></div><div className="form-grid"><label className="field"><span>Environmental / regulatory category</span><span className="select-wrap"><select value={form.regulatoryAttributes?.pollutionCategory || ""} onChange={(e) => update("regulatoryAttributes", { ...form.regulatoryAttributes, pollutionCategory: e.target.value })}><option value="">Not specified</option><option value="GREEN">Green</option><option value="ORANGE">Orange</option><option value="RED">Red</option></select><ChevronDown size={16}/></span></label><label className="field field-check"><input type="checkbox" checked={form.regulatoryAttributes?.environmentalConsentRequired === "true"} onChange={(e) => update("regulatoryAttributes", { ...form.regulatoryAttributes, environmentalConsentRequired: e.target.checked ? "true" : "false" })} /><span>Environmental consent may be relevant</span></label><label className="field field-check"><input type="checkbox" checked={form.regulatoryAttributes?.hazardousProcess === "true"} onChange={(e) => update("regulatoryAttributes", { ...form.regulatoryAttributes, hazardousProcess: e.target.checked ? "true" : "false" })} /><span>Hazardous process characteristic</span></label></div></div>
<div className="form-section"><div><h2>Tax identifiers</h2><p>Optional for the profile. They can help later with document consistency checks.</p></div><div className="form-grid">
          <label className="field"><span>PAN</span><input maxLength={10} value={form.panNumber || ""} onChange={(e) => update("panNumber", e.target.value.toUpperCase())} placeholder="ABCDE1234F" /></label>
          <label className="field"><span>GSTIN</span><input maxLength={15} value={form.gstin || ""} onChange={(e) => update("gstin", e.target.value.toUpperCase())} placeholder="27ABCDE1234F1Z5" /></label>
        </div></div>
        <div className="form-footer"><p>Save the profile first. Regulatory analysis will run from the saved version of these facts.</p><button type="submit" className="button button-primary" disabled={busy}>{busy ? "Saving profile..." : "Save profile"} <ArrowRight size={17}/></button></div>
      </form>
    </div>
  );
}
