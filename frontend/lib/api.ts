export type BusinessStage = "IDEA" | "SETUP" | "OPERATING" | "EXPANSION";
export type ApplicationStatus = "SUBMITTED" | "UNDER_SCRUTINY" | "QUERY_RAISED" | "RESUBMITTED" | "INSPECTION_SCHEDULED" | "INSPECTION_COMPLETE" | "DECISION" | "APPROVED" | "REJECTED" | "RENEWAL_DUE";

export type Session = { username: string; roles: string[]; expiresAt?: string | null };
export type BusinessProfile = { id: string; businessName: string; sector: string; activity: string; district: string; midcUnit: boolean; investmentInr: number; panNumber?: string | null; gstin?: string | null; employees: number; regulatoryAttributes: Record<string,string>; powerUsageKw: number; businessStage: BusinessStage; versionNumber: number; createdAt: string; updatedAt: string };
export type AnalysisResult = { approvalId: string; approvalCode: string; approvalName: string; authority: string; status: "APPLICABLE" | "CONDITIONAL" | "NOT_APPLICABLE"; ruleCode?: string | null; sourceId?: string | null; sourceTitle?: string | null; sourceUrl?: string | null; reason: string; basis: string };
export type AnalysisRun = { id: string; businessProfileId: string; profileVersion: number; ruleSetVersion: string; evaluatedAt: string; scrutinyTier: string; scrutinyReasons: string[]; results: AnalysisResult[] };
export type Readiness = { businessProfileId: string; analysisRunId: string; overallScore: number; knownRequirements: number; satisfiedRequirements: number; approvals: Array<{ approvalId: string; approvalCode: string; approvalName: string; status: string; score: number; mandatoryKnown: number; mandatorySatisfied: number; requirements: Array<{ requirementId: string; category: string; documentName: string; mandatory: boolean; satisfied: boolean; status: string; matchingDocumentIds: string[] }> }> };
export type DocumentRecord = { id: string; businessProfileId: string; category: string; originalFilename: string; contentType: string; declaredSizeBytes: number; storedSizeBytes?: number | null; status: "CREATED" | "UPLOADED" | "NEEDS_REVIEW" | "READY"; eTag?: string | null; uploadedAt?: string | null; createdAt: string };
export type PresignResponse = { documentId: string; objectKey: string; uploadUrl: string; expiresAt: string; status: string };
export type DocumentAnalysis = { documentId: string; extractionStatus: "COMPLETED" | "FAILED"; engine: string; extractedFields: Record<string, string>; consistency: Array<{ fieldName: string; profileValue?: string | null; documentValue?: string | null; status: "MATCH" | "MISMATCH" | "NOT_FOUND"; reason: string }>; overallStatus: string; analyzedAt: string; errorMessage?: string | null };
export type ApplicationSummary = { id: string; externalReference?: string | null; approvalId: string; approvalCode: string; approvalName: string; status: ApplicationStatus; analysisRunId: string; profileVersion: number; submittedAt?: string | null; updatedAt: string; slaDueAt?: string | null };
export type Application = ApplicationSummary & { businessProfileId: string; authority: string; slaDueAt?: string | null; matchedRuleCode?: string | null; sourceId?: string | null; createdAt: string; statusHistory: Array<{ id: string; fromStatus?: ApplicationStatus | null; toStatus: ApplicationStatus; actor: string; reason?: string | null; createdAt: string }> };
export type ApplicationTimeline = { applicationId: string; currentStatus: ApplicationStatus; statusHistory: Application["statusHistory"]; queries: Array<{ id: string; subject: string; queryText: string; raisedBy: string; raisedAt: string; status: "OPEN" | "RESPONDED" | "CLOSED"; responses: Array<{ id: string; responseText: string; responder: string; attachmentDocumentIds?: string[]; createdAt: string }> }>; inspections: Array<{ id: string; scheduledAt: string; assignedOfficer: string; outcome?: string | null; outcomeNotes?: string | null; createdAt: string; completedAt?: string | null }>; decision?: { id: string; outcome: "APPROVED" | "REJECTED" | "CONDITIONAL"; decisionNotes?: string | null; decidedBy: string; citedRuleCode?: string | null; citedSourceId?: string | null; decidedAt: string; validFrom?: string | null; validUntil?: string | null } | null };
export type BusinessProfileRequest = Omit<BusinessProfile, "id" | "versionNumber" | "createdAt" | "updatedAt">;
export type Evidence = { id:string; fieldName:string; fieldValue:string; sourceDocumentId:string; sourceDocumentName:string; verifiedBy:string; verifiedAt:string; profileVersion:number; status:string };
export type Sla = { applicationId:string; status:"NOT_CONFIGURED"|"ON_TRACK"|"AT_RISK"|"OVERDUE"|"COMPLETE"; submittedAt:string; dueAt?:string|null; remainingSeconds:number; targetHours?:number|null; warningHours?:number|null };
export type Renewal = { id:string; applicationId:string; businessProfileId:string; approvalCode:string; approvalName:string; validFrom?:string|null; validUntil:string; reminderDays:number; status:"UPCOMING"|"DUE"|"IN_PROGRESS"|"COMPLETED"|"EXPIRED"; sourceId?:string|null; updatedAt:string };
export type IncentiveMatch = { id:string; code:string; name:string; authority:string; benefitSummary:string; applicationUrl?:string|null; sourceId:string; sourceTitle:string; sourceUrl:string; reason:string; conditions:string[] };
export type Grievance = { id:string; businessProfileId:string; applicationId?:string|null; subject:string; description:string; department?:string|null; priority:"LOW"|"MEDIUM"|"HIGH"; status:"OPEN"|"ASSIGNED"|"IN_PROGRESS"|"RESOLVED"|"CLOSED"; assignedTo?:string|null; resolution?:string|null; createdAt:string; updatedAt:string; closedAt?:string|null };
export type Notification = { id:string; type:string; title:string; body:string; entityType?:string|null; entityId?:string|null; readAt?:string|null; createdAt:string };
export type ApprovalSlaConfig = { id:string; approvalId:string; approvalCode:string; approvalName:string; targetHours:number; warningHours:number; sourceId:string; sourceTitle:string; active:boolean; scopeAttributes:Record<string,string>; maxInvestmentInr?:number|null; updatedAt:string };
export type Compliance = { id:string; businessProfileId:string; applicationId?:string|null; approvalId?:string|null; name:string; authority:string; description?:string|null; dueDate:string; reminderDays:number; frequency:"ONE_TIME"|"MONTHLY"|"QUARTERLY"|"HALF_YEARLY"|"ANNUAL"; status:"UPCOMING"|"DUE"|"OVERDUE"|"COMPLETED"; sourceId?:string|null; completedAt?:string|null; updatedAt:string };
export type PreflightCheck = { code:string; severity:"BLOCKER"|"WARNING"; title:string; detail:string };
export type Preflight = { businessProfileId:string; analysisRunId:string; profileVersion:number; status:"READY_TO_SUBMIT"|"ACTION_REQUIRED"; blockerCount:number; warningCount:number; checks:PreflightCheck[]; approvals:Array<{approvalId:string;approvalCode:string;approvalName:string;readinessStatus:string;hasBlockingDependencies:boolean;blockers:string[]}> };
export type EvidencePassport = { businessProfileId:string; profileVersion:number; verifiedFields:number; supportedFields:number; entries:Array<{id:string;fieldName:string;fieldValue:string;status:string;profileVersion:number;sourceDocumentId:string;sourceDocumentName:string;verifiedBy:string;verifiedAt:string}> };
export type ImpactRequest = { sector?:string; activity?:string; district?:string; midcUnit?:boolean; investmentInr?:number; employees?:number; powerUsageKw?:number; businessStage?:BusinessStage };
export type ImpactChange = { approvalCode:string; approvalName:string; authority:string; beforeStatus:string; afterStatus:string; beforeReason:string; afterReason:string; afterSourceUrl?:string|null };
export type ImpactResponse = { added:ImpactChange[]; removed:ImpactChange[]; changed:ImpactChange[]; totalChanged:number };

export type DepartmentDashboard = { totalApplications:number; pendingScrutiny:number; queriesAwaitingApplicant:number; slaAtRisk:number; overdue:number; inspectionsPending:number; approved:number; rejected:number; openGrievances:number; complianceDue:number; byStatus:Record<string,number>; byDepartment:Record<string,number> };


export type BusinessRegulatoryTwin = { businessProfileId:string; regulatoryStateVersion:number; stateCapturedAt:string; currentProfile:BusinessProfile };
export type BusinessProfileVersion = { id:string; businessProfileId:string; versionNumber:number; snapshot:Record<string,unknown>; changeType:string; capturedBy:string; capturedAt:string };
export type KnowledgeTrace = { approvalId:string; approvalCode:string; approvalName:string; authority:string; rules:Array<{ruleId:string;code:string;name:string;outcome:string;version:number;sourceId:string;sourceTitle:string;sourceUrl:string}>; documents:Array<{documentRequirementId:string;name:string;mandatory:boolean;sourceId:string;sourceTitle:string;sourceUrl:string}>; dependencies:Array<{approvalId:string;approvalCode:string;approvalName:string;type:string}> };

export type RegulatorySource = { id:string; title:string; url:string; sourceType:string; verificationStatus:string; publishedOn?:string|null; effectiveFrom?:string|null; expiresOn?:string|null; verifiedAt?:string|null; contentHash?:string|null };
export type ControlTower = { total:number; atRisk:number; overdue:number; blocked:number; lanes:Array<{applicationId:string;reference:string;approvalCode:string;approvalName:string;authority:string;businessName:string;district:string;status:string;slaStatus:string;slaDueAt?:string|null;blockers:string[];nextAction:string;updatedAt:string}> };
export type InspectionPlanner = { inspections:Array<{inspectionId:string;applicationId:string;applicationReference:string;businessName:string;district:string;authority:string;scheduledAt:string;assignedOfficer:string;outcome:string}>; coordinationOpportunities:Array<{businessProfileId:string;businessName:string;district:string;windowStart:string;windowEnd:string;inspectionCount:number;authorities:string[]}> };
export type EvidenceGraph = { applicationId:string; nodes:Array<{id:string;type:string;label:string;detail:string}>; edges:Array<{from:string;to:string;relationship:string}> };

export type ApprovalCatalog = { id:string; code:string; name:string; authority:string; purpose?:string|null; sourceId:string; sourceTitle:string; sourceUrl:string; active:boolean };
export type DocumentRequirement = { id:string; approvalId:string; approvalCode:string; category:string; documentName:string; description?:string|null; mandatory:boolean; active:boolean; sourceId:string };
export type SourceType = "ACT"|"RULE"|"NOTIFICATION"|"CIRCULAR"|"OFFICIAL_PORTAL"|"GUIDELINE"|"OTHER";
export type SourceVerificationStatus = "UNVERIFIED"|"VERIFIED"|"EXPIRED"|"RETIRED";
export type ConditionField = "SECTOR"|"ACTIVITY"|"DISTRICT"|"MIDC_UNIT"|"INVESTMENT_INR"|"EMPLOYEES"|"POWER_USAGE_KW"|"BUSINESS_STAGE"|"REGULATORY_ATTRIBUTE";
export type ConditionOperator = "EQ"|"NEQ"|"GT"|"GTE"|"LT"|"LTE"|"IN"|"NOT_IN";
export type ValueType = "STRING"|"NUMBER"|"BOOLEAN";
export type SourceChange = { sourceFamily:string; contentChanged:boolean; versions:Array<{id:string;title:string;url:string;verificationStatus:string;publishedOn?:string|null;effectiveFrom?:string|null;expiresOn?:string|null;contentHash?:string|null}> };

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

function csrfToken(): string | null {
  if (typeof document === "undefined") return null;
  const raw = document.cookie.split(";").map((value) => value.trim()).find((value) => value.startsWith("XSRF-TOKEN="));
  return raw ? decodeURIComponent(raw.substring("XSRF-TOKEN=".length)) : null;
}

export async function ensureCsrf() {
  await fetch(`${API_URL}/api/v1/session/csrf`, { method: "GET", credentials: "include", cache: "no-store" });
}

export async function login(username: string, password: string): Promise<Session> {
  await ensureCsrf();
  const token = csrfToken();
  const headers = new Headers({ "Content-Type": "application/json" });
  if (token) headers.set("X-XSRF-TOKEN", token);
  const response = await fetch(`${API_URL}/api/v1/session/login`, { method: "POST", credentials: "include", headers, body: JSON.stringify({ username, password }), cache: "no-store" });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(typeof payload?.detail === "string" ? payload.detail : typeof payload?.message === "string" ? payload.message : typeof payload?.title === "string" ? payload.title : "The credentials could not be verified.");
  return payload as Session;
}

export async function uploadDirect(uploadUrl: string, file: File): Promise<void> {
  let token = csrfToken();
  if (!token) {
    await ensureCsrf();
    token = csrfToken();
  }
  const headers = new Headers({ "Content-Type": file.type || "application/octet-stream" });
  if (token) headers.set("X-XSRF-TOKEN", token);
  const response = await fetch(uploadUrl, {
    method: "PUT",
    body: file,
    headers,
    credentials: "include",
  });
  if (!response.ok) throw new Error("The file upload could not be completed.");
}

export async function logout() {
  await ensureCsrf();
  const token = csrfToken();
  const headers = new Headers();
  if (token) headers.set("X-XSRF-TOKEN", token);
  await fetch(`${API_URL}/api/v1/session/logout`, { method: "POST", credentials: "include", headers, cache: "no-store" }).catch(() => undefined);
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (!(options.body instanceof FormData)) headers.set("Content-Type", "application/json");
  const method = (options.method || "GET").toUpperCase();
  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    let token = csrfToken();
    if (!token) {
      await ensureCsrf();
      token = csrfToken();
    }
    if (token) headers.set("X-XSRF-TOKEN", token);
  }
  const response = await fetch(`${API_URL}${path}`, { ...options, headers, credentials: "include", cache: "no-store" });
  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("json") ? await response.json() : await response.text();
  if (!response.ok) {
    const message = typeof payload === "object" && payload
      ? ("detail" in payload ? String(payload.detail) : "message" in payload ? String(payload.message) : "title" in payload ? String(payload.title) : "Request failed")
      : String(payload || "Request failed");
    throw new Error(message);
  }
  return payload as T;
}


export const platformClient = {
  evidence: (profileId: string) => api<Evidence[]>(`/api/v1/business-profiles/${profileId}/evidence`),
  evidencePassport: (profileId: string) => api<EvidencePassport>(`/api/v1/business-profiles/${profileId}/evidence/passport`),
  preflight: (profileId:string, analysisRunId:string) => api<Preflight>(`/api/v1/preflight/business-profiles/${profileId}/analysis/${analysisRunId}`),
  impact: (profileId:string, body:ImpactRequest) => api<ImpactResponse>(`/api/v1/impact/business-profiles/${profileId}/simulate`, {method:"POST", body:JSON.stringify(body)}),
  promoteEvidence: (profileId:string, documentId:string) => api<Evidence>(`/api/v1/business-profiles/${profileId}/evidence/from-document/${documentId}`, {method:"POST", body:"{}"}),
  sla: (applicationId:string) => api<Sla>(`/api/v1/sla/applications/${applicationId}`),
  renewals: (profileId:string) => api<Renewal[]>(`/api/v1/renewals/business-profiles/${profileId}`),
  incentives: (profileId:string) => api<IncentiveMatch[]>(`/api/v1/incentives/business-profiles/${profileId}`),
  compliance: (profileId:string) => api<Compliance[]>(`/api/v1/compliance/business-profiles/${profileId}`),
  grievances: (profileId:string) => api<Grievance[]>(`/api/v1/grievances/business-profiles/${profileId}`),
  createGrievance: (body:unknown) => api<Grievance>(`/api/v1/grievances`, {method:"POST", body:JSON.stringify(body)}),
  notifications: () => api<Notification[]>(`/api/v1/notifications`),
  unreadNotifications: () => api<number>(`/api/v1/notifications/unread-count`),
  departmentDashboard: () => api<DepartmentDashboard>(`/api/v1/analytics/department`),
  departmentGrievances: () => api<Grievance[]>(`/api/v1/grievances`),
  departmentRenewals: () => api<Renewal[]>(`/api/v1/renewals`),
  departmentCompliance: () => api<Compliance[]>(`/api/v1/compliance`),
  departmentSlaConfigs: () => api<ApprovalSlaConfig[]>(`/api/v1/sla/configs`),
  controlTower: () => api<ControlTower>(`/api/v1/operations/control-tower`),
  inspectionPlanner: () => api<InspectionPlanner>(`/api/v1/operations/inspections/planner`),
  markNotificationRead: (id:string) => api<void>(`/api/v1/notifications/${id}/read`, {method:"POST", body:"{}"}),
  regulatorySources: () => api<RegulatorySource[]>(`/api/v1/regulatory/admin/sources`),
  sourceChanges: () => api<SourceChange[]>(`/api/v1/regulatory/admin/source-changes`),
  approvalsCatalog: () => api<ApprovalCatalog[]>(`/api/v1/regulatory/approvals`),
  documentRequirementsCatalog: () => api<DocumentRequirement[]>(`/api/v1/regulatory/document-requirements`),
  createSource: (body: unknown) => api<RegulatorySource>(`/api/v1/regulatory/admin/sources`, {method:"POST", body:JSON.stringify(body)}),
  createApproval: (body: unknown) => api<ApprovalCatalog>(`/api/v1/regulatory/admin/approvals`, {method:"POST", body:JSON.stringify(body)}),
  createRule: (body: unknown) => api(`/api/v1/regulatory/admin/rules`, {method:"POST", body:JSON.stringify(body)}),
  createDocumentRequirement: (body: unknown) => api<DocumentRequirement>(`/api/v1/regulatory/document-requirements`, {method:"POST", body:JSON.stringify(body)}),
  createSlaConfig: (body: {approvalId:string; targetHours:number; warningHours:number; sourceId:string; active:boolean; scopeAttributes?:Record<string,string>; maxInvestmentInr?:number|null}) => api<ApprovalSlaConfig>(`/api/v1/sla/configs`, {method:"POST", body:JSON.stringify(body)}),
  createIncentiveScheme: (body: unknown) => api(`/api/v1/incentives/admin/schemes`, {method:"POST", body:JSON.stringify(body)}),
};

export const apiClient = {
  verifySession: () => api<Session>("/api/v1/session"),
  profiles: () => api<BusinessProfile[]>("/api/v1/business-profiles"),
  createProfile: (body: BusinessProfileRequest) => api<BusinessProfile>("/api/v1/business-profiles", { method: "POST", body: JSON.stringify(body) }),
  getProfile: (id: string) => api<BusinessProfile>(`/api/v1/business-profiles/${id}`),
  updateProfile: (id: string, body: BusinessProfileRequest) => api<BusinessProfile>(`/api/v1/business-profiles/${id}`, { method: "PUT", body: JSON.stringify(body) }),
  regulatoryTwin: (id: string) => api<BusinessRegulatoryTwin>(`/api/v1/business-profiles/${id}/regulatory-twin`),
  profileVersions: (id: string) => api<BusinessProfileVersion[]>(`/api/v1/business-profiles/${id}/versions`),
  knowledgeTrace: (approvalId: string) => api<KnowledgeTrace>(`/api/v1/knowledge/approvals/${approvalId}/trace`),
  analyse: (profileId: string) => api<AnalysisRun>(`/api/v1/analysis/business-profiles/${profileId}`, { method: "POST", body: "{}" }),
  latestAnalysis: (profileId: string) => api<AnalysisRun>(`/api/v1/analysis/business-profiles/${profileId}/latest`),
  readiness: (businessProfileId: string, analysisRunId: string) => api<Readiness>(`/api/v1/readiness?businessProfileId=${encodeURIComponent(businessProfileId)}&analysisRunId=${encodeURIComponent(analysisRunId)}`),
  listDocuments: (businessProfileId: string) => api<DocumentRecord[]>(`/api/v1/documents?businessProfileId=${encodeURIComponent(businessProfileId)}`),
  presignDocument: (body: { businessProfileId: string; category: string; originalFilename: string; contentType: string; sizeBytes: number }) => api<PresignResponse>("/api/v1/documents/presign", { method: "POST", body: JSON.stringify(body) }),
  completeDocument: (id: string) => api<DocumentRecord>(`/api/v1/documents/${id}/complete`, { method: "POST" }),
  analyzeDocument: (id: string) => api<DocumentAnalysis>(`/api/v1/documents/${id}/analyze`, { method: "POST" }),
  getDocumentAnalysis: (id: string) => api<DocumentAnalysis>(`/api/v1/documents/${id}/analysis`),
  listApplications: (businessProfileId: string) => api<ApplicationSummary[]>(`/api/v1/applications/business-profiles/${businessProfileId}`),
  listDepartmentApplications: () => api<ApplicationSummary[]>("/api/v1/applications"),
  createApplication: (body: { businessProfileId: string; analysisRunId: string; approvalId: string }) => api<Application>("/api/v1/applications", { method: "POST", body: JSON.stringify(body) }),
  getApplication: (id: string) => api<Application>(`/api/v1/applications/${id}`),
  timeline: (id: string) => api<ApplicationTimeline>(`/api/v1/applications/${id}/timeline`),
  evidenceGraph: (id:string) => api<EvidenceGraph>(`/api/v1/knowledge/applications/${id}/evidence-graph`),
  transitionApplication: (id: string, targetStatus: ApplicationStatus, reason?: string) => api<Application>(`/api/v1/applications/${id}/status`, { method: "PATCH", body: JSON.stringify({ targetStatus, reason }) }),
  raiseQuery: (id: string, subject: string, queryText: string) => api(`/api/v1/applications/${id}/queries`, { method: "POST", body: JSON.stringify({ subject, queryText }) }),
  respondToQuery: (id: string, queryId: string, responseText: string, attachmentDocumentIds: string[] = []) => api(`/api/v1/applications/${id}/queries/${queryId}/responses`, { method: "POST", body: JSON.stringify({ responseText, attachmentDocumentIds }) }),
  scheduleInspection: (id: string, scheduledAt: string, assignedOfficer: string) => api(`/api/v1/applications/${id}/inspections`, { method: "POST", body: JSON.stringify({ scheduledAt, assignedOfficer }) }),
  completeInspection: (id: string, inspectionId: string, outcome: string, outcomeNotes?: string) => api(`/api/v1/applications/${id}/inspections/${inspectionId}`, { method: "PATCH", body: JSON.stringify({ outcome, outcomeNotes }) }),
  decide: (id: string, body: { outcome: string; decisionNotes?: string; citedRuleCode?: string; citedSourceId?: string; validFrom?: string; validUntil?: string }) => api<Application>(`/api/v1/applications/${id}/decision`, { method: "POST", body: JSON.stringify(body) }),
};
