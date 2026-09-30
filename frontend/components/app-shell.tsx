"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useMemo, useRef, useState } from "react";
import { Brand } from "./brand";
import { apiClient, platformClient, logout, BusinessProfile } from "../lib/api";
import {
  Bell, ClipboardCheck, Database, FileText, GitBranch,
  Layers, Network, Scale, Shield, LogOut, ArrowLeft,
  ChevronDown, PanelLeftClose, PanelLeftOpen, Calendar, MessageSquare
} from "./icons";

function activeProfileId(pathname: string): string | null {
  const match = pathname.match(/\/app\/profiles\/([^/]+)/);
  return match?.[1] || null;
}

function ProfileSwitcher({
  profiles,
  currentProfile,
  collapsed,
  onSelect,
}: {
  profiles: BusinessProfile[];
  currentProfile?: BusinessProfile;
  collapsed: boolean;
  onSelect: (id: string) => void;
}) {
  const [open, setOpen] = useState(false);
  const rootRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    setOpen(false);
  }, [currentProfile?.id, collapsed]);

  useEffect(() => {
    if (!open) return;
    const onPointerDown = (event: PointerEvent) => {
      if (!rootRef.current?.contains(event.target as Node)) setOpen(false);
    };
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") setOpen(false);
    };
    document.addEventListener("pointerdown", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("pointerdown", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [open]);

  if (!currentProfile) return null;

  return (
    <div ref={rootRef} className={`profile-switcher ${collapsed ? "is-collapsed" : ""}`}>
      <button
        type="button"
        className="profile-switcher-trigger"
        aria-expanded={open}
        aria-haspopup="listbox"
        aria-label={`Current business: ${currentProfile.businessName}`}
        title={collapsed ? currentProfile.businessName : undefined}
        onClick={() => setOpen((value) => !value)}
      >
        <span className="profile-switcher-mark">{currentProfile.businessName.slice(0, 1).toUpperCase()}</span>
        <span className="profile-switcher-copy">
          <span className="profile-switcher-label">Current business</span>
          <strong>{currentProfile.businessName}</strong>
        </span>
        <ChevronDown size={15} className="profile-switcher-chevron" />
      </button>

      {open ? (
        <div className="profile-switcher-menu" role="listbox" aria-label="Choose business profile">
          {profiles.map((profile) => (
            <button
              key={profile.id}
              type="button"
              role="option"
              aria-selected={profile.id === currentProfile.id}
              className={`profile-switcher-option ${profile.id === currentProfile.id ? "selected" : ""}`}
              onClick={() => onSelect(profile.id)}
            >
              <span className="profile-switcher-option-mark">{profile.businessName.slice(0, 1).toUpperCase()}</span>
              <span>{profile.businessName}</span>
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [ready, setReady] = useState(false);
  const [roles, setRoles] = useState<string[]>([]);
  const [profiles, setProfiles] = useState<BusinessProfile[]>([]);
  const [notificationCount, setNotificationCount] = useState(0);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  useEffect(() => {
    if (typeof window === "undefined") return;
    setSidebarCollapsed(localStorage.getItem("anumati_sidebar_collapsed") === "1");
  }, []);

  useEffect(() => {
    setMobileOpen(false);
  }, [pathname]);

  useEffect(() => {
    document.body.style.overflow = mobileOpen ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [mobileOpen]);

  useEffect(() => {
    Promise.all([apiClient.verifySession(), apiClient.profiles()])
      .then(([session, profileList]) => {
        setRoles(session.roles);
        if (session.roles.includes("ADMIN")) {
          router.replace("/admin");
          return null;
        }
        if (session.roles.includes("DEPARTMENT_OFFICER")) {
          router.replace("/officer");
          return null;
        }
        if (!session.roles.includes("APPLICANT")) {
          router.replace("/login");
          return null;
        }
        setProfiles(profileList);
        setReady(true);
        if (profileList[0] && typeof window !== "undefined" && !localStorage.getItem("anumati_last_profile")) {
          localStorage.setItem("anumati_last_profile", profileList[0].id);
        }
        return platformClient.unreadNotifications();
      })
      .then((count) => { if (typeof count === "number") setNotificationCount(count); })
      .catch(() => router.replace("/login"));
  }, [router]);

  const profileId = activeProfileId(pathname);
  const currentProfile = useMemo(
    () => profiles.find((profile) => profile.id === profileId)
      || profiles.find((profile) => profile.id === (typeof window !== "undefined" ? localStorage.getItem("anumati_last_profile") : null))
      || profiles[0],
    [profiles, profileId]
  );

  if (!ready) return <div className="loading-state">Preparing workspace...</div>;

  const profileBase = currentProfile ? `/app/profiles/${currentProfile.id}` : "/app";
  const starts = (href: string) => pathname === href || pathname.startsWith(`${href}/`);
  const exact = (href: string) => pathname === href;

  const primary = [
    { href: "/app", label: "Overview", icon: Layers, match: () => exact("/app") },
    {
      href: profileBase,
      label: "Regulatory map",
      icon: GitBranch,
      match: () => exact(profileBase) || starts(`${profileBase}/twin`),
    },
    {
      href: `${profileBase}/preflight`,
      label: "Application pre-flight",
      icon: ClipboardCheck,
      match: () => starts(`${profileBase}/preflight`),
    },
    {
      href: `${profileBase}/evidence`,
      label: "Evidence passport",
      icon: Shield,
      match: () => starts(`${profileBase}/evidence`),
    },
  ];

  const lifecycle = [
    { href: `${profileBase}/documents`, label: "Documents", icon: FileText },
    { href: `${profileBase}/applications`, label: "Applications", icon: ClipboardCheck },
    { href: `${profileBase}/impact`, label: "Regulatory impact", icon: Network },
    { href: `${profileBase}/incentives`, label: "Support schemes", icon: Scale },
    { href: `${profileBase}/renewals`, label: "Renewals", icon: Calendar },
    { href: `${profileBase}/compliance`, label: "Compliance", icon: Database },
    { href: `${profileBase}/grievances`, label: "Grievances", icon: MessageSquare },
  ];

  const chooseProfile = (id: string) => {
    localStorage.setItem("anumati_last_profile", id);
    router.push(`/app/profiles/${id}`);
    setMobileOpen(false);
  };

  const toggleShell = () => {
    const isMobile = typeof window !== "undefined" && window.matchMedia("(max-width: 960px)").matches;
    if (isMobile) {
      setMobileOpen((value) => !value);
      return;
    }
    setSidebarCollapsed((value) => {
      const next = !value;
      localStorage.setItem("anumati_sidebar_collapsed", next ? "1" : "0");
      return next;
    });
  };

  const closeSidebarOnMobile = () => setMobileOpen(false);
  const parentContext = (() => {
    if (pathname === "/app") return null;
    if (pathname === "/app/notifications") return { href: "/app", label: "Overview" };
    if (profileId && pathname.startsWith(`${profileBase}/`)) return { href: profileBase, label: "Regulatory map" };
    if (pathname.startsWith("/app/applications/")) return { href: "/app", label: "Overview" };
    return { href: "/app", label: "Overview" };
  })();

  return (
    <div className={`app-shell ${sidebarCollapsed ? "sidebar-collapsed" : ""}`}>
      <div className={mobileOpen ? "sidebar-backdrop is-open" : "sidebar-backdrop"} onClick={closeSidebarOnMobile} aria-hidden="true" />
      <aside className={`sidebar ${mobileOpen ? "is-open" : ""}`} aria-label="Anumati workspace navigation">
        <div className="sidebar-top">
          <div className="sidebar-brand-row">
            <Brand compact />
            <button
              type="button"
              className="sidebar-toggle"
              onClick={toggleShell}
              aria-label={sidebarCollapsed ? "Expand navigation" : "Collapse navigation"}
              aria-expanded={!sidebarCollapsed}
              title={sidebarCollapsed ? "Expand navigation" : "Collapse navigation"}
            >
              {sidebarCollapsed ? <PanelLeftOpen size={17} /> : <PanelLeftClose size={17} />}
            </button>
          </div>

          <ProfileSwitcher
            profiles={profiles}
            currentProfile={currentProfile}
            collapsed={sidebarCollapsed}
            onSelect={chooseProfile}
          />
        </div>

        <nav className="app-nav" aria-label="Workspace navigation">
          <div className="nav-label">Workspace</div>
          {primary.map((item) => {
            const Icon = item.icon;
            const itemActive = item.match();
            return (
              <Link
                key={item.label}
                href={item.href}
                onClick={closeSidebarOnMobile}
                aria-current={itemActive ? "page" : undefined}
                className={`app-nav-item ${itemActive ? "active" : ""}`}
                title={sidebarCollapsed ? item.label : undefined}
              >
                <Icon size={18} /><span>{item.label}</span>
              </Link>
            );
          })}

          <div className="nav-label">Lifecycle</div>
          {lifecycle.map((item) => {
            const Icon = item.icon;
            const disabled = !currentProfile;
            const itemActive = starts(item.href);
            return disabled
              ? <span key={item.label} className="app-nav-item nav-disabled" title={sidebarCollapsed ? item.label : undefined}><Icon size={18}/><span>{item.label}</span></span>
              : <Link key={item.label} href={item.href} onClick={closeSidebarOnMobile} aria-current={itemActive ? "page" : undefined} className={`app-nav-item ${itemActive ? "active" : ""}`} title={sidebarCollapsed ? item.label : undefined}><Icon size={18}/><span>{item.label}</span></Link>;
          })}

          <Link href="/app/notifications" onClick={closeSidebarOnMobile} aria-current={exact("/app/notifications") ? "page" : undefined} className={`app-nav-item ${exact("/app/notifications") ? "active" : ""}`} title={sidebarCollapsed ? "Notifications" : undefined}>
            <Bell size={18}/><span>Notifications</span>{notificationCount > 0 ? <span className="nav-count">{notificationCount}</span> : null}
          </Link>

        </nav>

        <div className="sidebar-bottom">
          <Link href="/privacy" onClick={closeSidebarOnMobile} className="app-nav-item" title={sidebarCollapsed ? "Privacy" : undefined}><Shield size={18}/><span>Privacy</span></Link>
          <Link href="/terms" onClick={closeSidebarOnMobile} className="app-nav-item" title={sidebarCollapsed ? "Terms" : undefined}><FileText size={18}/><span>Terms</span></Link>
          <button className="app-nav-item app-nav-button" onClick={async () => { await logout(); router.replace("/login"); }} title={sidebarCollapsed ? "Sign out" : undefined}><LogOut size={18}/><span>Sign out</span></button>
        </div>
      </aside>

      <main className="app-main">
        <header className="app-topbar">
          <div className="app-topbar-context">
            <button
              className="workspace-menu-button"
              type="button"
              onClick={toggleShell}
              aria-label={sidebarCollapsed ? "Expand navigation" : "Collapse navigation"}
              aria-expanded={!sidebarCollapsed || mobileOpen}
              title={sidebarCollapsed ? "Expand navigation" : "Collapse navigation"}
            >
              {sidebarCollapsed ? <PanelLeftOpen size={17} /> : <PanelLeftClose size={17} />}
            </button>
            {parentContext ? <Link href={parentContext.href} className="app-topbar-back"><ArrowLeft size={14}/><span>{parentContext.label}</span></Link> : null}
            <div><span className="topbar-kicker">Applicant workspace</span><strong>Business operations</strong></div>
          </div>
          <div className="app-topbar-actions">
            <Link href="/app/notifications" className="app-nav-item topbar-notification" aria-label="Notifications"><Bell size={18}/>{notificationCount > 0 ? <span className="nav-count">{notificationCount}</span> : null}</Link>
            <div className="app-user-chip">
              <div className="app-avatar">{(currentProfile?.businessName?.[0] || "A").toUpperCase()}</div>
              <div className="app-user-copy"><strong>{currentProfile?.businessName || "Anumati workspace"}</strong><span>Applicant workspace</span></div>
            </div>
          </div>
        </header>
        {children}
      </main>
    </div>
  );
}
