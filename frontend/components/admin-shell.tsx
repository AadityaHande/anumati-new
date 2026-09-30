"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { Brand } from "./brand";
import { apiClient, logout, platformClient } from "../lib/api";
import {
  ArrowLeft, Bell, Database, Landmark, LogOut, Menu,
  PanelLeftClose, PanelLeftOpen, Shield, X, Settings
} from "./icons";

const links = [
  { href: "/admin", label: "Admin overview", icon: Settings, exact: true },
  { href: "/admin/regulatory", label: "Regulatory catalogue", icon: Database },
];

function pageMeta(pathname: string) {
  if (pathname === "/admin") return { label: "Admin overview", parent: null as string | null, parentLabel: "" };
  if (pathname.startsWith("/admin/regulatory")) return { label: "Regulatory catalogue", parent: "/admin", parentLabel: "Admin overview" };
  return { label: "Administration", parent: "/admin", parentLabel: "Admin overview" };
}

export function AdminShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [ready, setReady] = useState(false);
  const [notificationCount, setNotificationCount] = useState(0);
  const meta = useMemo(() => pageMeta(pathname), [pathname]);

  useEffect(() => {
    setCollapsed(localStorage.getItem("anumati_admin_sidebar_collapsed") === "1");
    apiClient.verifySession().then(async (session) => {
      if (!session.roles.includes("ADMIN")) {
        router.replace(session.roles.includes("DEPARTMENT_OFFICER") ? "/officer" : "/app");
        return;
      }
      try { setNotificationCount(await platformClient.unreadNotifications()); } catch { setNotificationCount(0); }
      setReady(true);
    }).catch(() => router.replace("/login"));
  }, [router]);

  useEffect(() => { setMobileOpen(false); }, [pathname]);
  useEffect(() => {
    document.body.style.overflow = mobileOpen ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [mobileOpen]);

  const toggle = () => {
    const mobile = window.matchMedia("(max-width: 980px)").matches;
    if (mobile) { setMobileOpen((v) => !v); return; }
    setCollapsed((v) => {
      const next = !v;
      localStorage.setItem("anumati_admin_sidebar_collapsed", next ? "1" : "0");
      return next;
    });
  };

  if (!ready) return <div className="loading-state">Preparing administration workspace...</div>;

  return (
    <div className={`admin-shell ${collapsed ? "is-collapsed" : ""}`}>
      <div className={`admin-backdrop ${mobileOpen ? "is-open" : ""}`} onClick={() => setMobileOpen(false)} aria-hidden="true" />
      <aside className={`admin-sidebar ${mobileOpen ? "is-open" : ""}`} aria-label="Administration navigation">
        <div className="admin-sidebar-top">
          <div className="admin-brand-row">
            <Brand compact />
            <button className="admin-toggle" type="button" onClick={toggle} aria-label={collapsed ? "Expand administration navigation" : "Collapse administration navigation"} title={collapsed ? "Expand" : "Collapse"}>
              {collapsed ? <PanelLeftOpen size={17} /> : <PanelLeftClose size={17} />}
            </button>
          </div>
          <div className="admin-identity">
            <span className="admin-identity-kicker">Administration</span>
            <strong>Regulatory governance</strong>
            <span>Sources, rules, evidence requirements and service configuration</span>
          </div>
        </div>

        <nav className="admin-nav" aria-label="Administration workspace">
          <span className="admin-nav-label">Administration</span>
          {links.map(({ href, label, icon: Icon, exact }) => {
            const active = exact ? pathname === href : pathname === href || pathname.startsWith(`${href}/`);
            return <Link key={href} href={href} className={`admin-nav-item ${active ? "active" : ""}`} aria-current={active ? "page" : undefined} title={collapsed ? label : undefined}>
              <Icon size={18}/><span>{label}</span>
            </Link>;
          })}

          <span className="admin-nav-label admin-nav-label-secondary">Connected workspaces</span>
          <Link href="/officer" className="admin-nav-item" title={collapsed ? "Department operations" : undefined}><Landmark size={18}/><span>Department operations</span></Link>
          <Link href="/app/notifications" className="admin-nav-item" title={collapsed ? "Notifications" : undefined}><Bell size={18}/><span>Notifications</span>{notificationCount > 0 ? <em>{notificationCount}</em> : null}</Link>
        </nav>

        <div className="admin-sidebar-bottom">
          <Link href="/privacy" className="admin-nav-item"><Shield size={17}/><span>Privacy</span></Link>
          <button className="admin-nav-item admin-signout" onClick={async () => { await logout(); router.replace("/login"); }}><LogOut size={17}/><span>Sign out</span></button>
        </div>
      </aside>

      <main className="admin-main">
        <header className="admin-topbar">
          <div className="admin-topbar-left">
            <button className="admin-mobile-toggle" type="button" onClick={toggle} aria-label={mobileOpen ? "Close navigation" : "Open navigation"}>
              {mobileOpen ? <X size={18}/> : <Menu size={18}/>} 
            </button>
            {meta.parent ? <Link className="admin-back" href={meta.parent}><ArrowLeft size={15}/><span>{meta.parentLabel}</span></Link> : <span className="admin-section-label">Administration</span>}
            <span className="admin-breadcrumb-separator">/</span>
            <strong>{meta.label}</strong>
          </div>
          <div className="admin-topbar-right">
            <Link href="/app/notifications" className="admin-bell" aria-label="Notifications"><Bell size={17}/>{notificationCount > 0 ? <span>{notificationCount}</span> : null}</Link>
            <Link href="/officer" className="admin-workspace-switch">Department operations</Link>
          </div>
        </header>
        {children}
      </main>
    </div>
  );
}
