"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { Brand } from "./brand";
import { apiClient, logout, platformClient } from "../lib/api";
import {
  ArrowLeft, Bell, Calendar, ClipboardCheck, Landmark, LogOut, Menu,
  MessageSquare, PanelLeftClose, PanelLeftOpen, Shield, X
} from "./icons";

const links = [
  { href: "/officer", label: "Control tower", icon: Landmark, exact: true },
  { href: "/officer/inspections", label: "Inspection planning", icon: Calendar },
  { href: "/officer/renewals", label: "Renewal desk", icon: ClipboardCheck },
  { href: "/officer/grievances", label: "Grievance desk", icon: MessageSquare },
];

function pageMeta(pathname: string) {
  if (pathname === "/officer") return { label: "Control tower", parent: null as string | null, parentLabel: "" };
  if (pathname.startsWith("/officer/applications/")) return { label: "Application review", parent: "/officer", parentLabel: "Control tower" };
  if (pathname.startsWith("/officer/inspections")) return { label: "Inspection planning", parent: "/officer", parentLabel: "Control tower" };
  if (pathname.startsWith("/officer/renewals")) return { label: "Renewal desk", parent: "/officer", parentLabel: "Control tower" };
  if (pathname.startsWith("/officer/grievances")) return { label: "Grievance desk", parent: "/officer", parentLabel: "Control tower" };
  return { label: "Department operations", parent: "/officer", parentLabel: "Control tower" };
}

export function DepartmentShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [ready, setReady] = useState(false);
  const [notificationCount, setNotificationCount] = useState(0);
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    setCollapsed(localStorage.getItem("anumati_department_sidebar_collapsed") === "1");
    Promise.all([apiClient.verifySession(), platformClient.unreadNotifications()])
      .then(([session, unread]) => {
        const officer = session.roles.includes("DEPARTMENT_OFFICER") || session.roles.includes("ADMIN");
        if (!officer) { router.replace("/app"); return; }
        setIsAdmin(session.roles.includes("ADMIN"));
        setNotificationCount(unread);
        setReady(true);
      })
      .catch(() => router.replace("/login"));
  }, [router]);

  useEffect(() => {
    setMobileOpen(false);
  }, [pathname]);

  useEffect(() => {
    document.body.style.overflow = mobileOpen ? "hidden" : "";
    return () => { document.body.style.overflow = ""; };
  }, [mobileOpen]);

  const meta = useMemo(() => pageMeta(pathname), [pathname]);

  function toggle() {
    const isMobile = window.matchMedia("(max-width: 980px)").matches;
    if (isMobile) {
      setMobileOpen((value) => !value);
      return;
    }
    setCollapsed((value) => {
      const next = !value;
      localStorage.setItem("anumati_department_sidebar_collapsed", next ? "1" : "0");
      return next;
    });
  }

  if (!ready) return <div className="loading-state">Preparing department workspace...</div>;

  return (
    <div className={`department-shell ${collapsed ? "is-collapsed" : ""}`}>
      <div className={`department-backdrop ${mobileOpen ? "is-open" : ""}`} onClick={() => setMobileOpen(false)} aria-hidden="true" />
      <aside className={`department-sidebar ${mobileOpen ? "is-open" : ""}`} aria-label="Department navigation">
        <div className="department-sidebar-top">
          <div className="department-brand-row">
            <Brand compact />
            <button className="department-toggle" type="button" onClick={toggle} aria-label={collapsed ? "Expand department navigation" : "Collapse department navigation"} title={collapsed ? "Expand" : "Collapse"}>
              {collapsed ? <PanelLeftOpen size={17} /> : <PanelLeftClose size={17} />}
            </button>
          </div>
          <div className="department-identity">
            <span className="department-identity-kicker">Government workspace</span>
            <strong>Department operations</strong>
            <span>Shared view across approval workflows</span>
          </div>
        </div>

        <nav className="department-nav" aria-label="Department workspace">
          <span className="department-nav-label">Operations</span>
          {links.map(({ href, label, icon: Icon, exact }) => {
            const active = exact ? pathname === href : pathname === href || pathname.startsWith(`${href}/`);
            return (
              <Link key={href} href={href} className={`department-nav-item ${active ? "active" : ""}`} aria-current={active ? "page" : undefined} title={collapsed ? label : undefined}>
                <Icon size={18} /><span>{label}</span>
              </Link>
            );
          })}
          <span className="department-nav-label department-nav-label-secondary">Workspace</span>
          {isAdmin ? <Link href="/admin" className="department-nav-item" title={collapsed ? "Administration" : undefined}>
            <Shield size={18} /><span>Administration</span>
          </Link> : null}
          <Link href="/app/notifications" className="department-nav-item" title={collapsed ? "Notifications" : undefined}>
            <Bell size={18} /><span>Notifications</span>{notificationCount > 0 ? <em>{notificationCount}</em> : null}
          </Link>
        </nav>

        <div className="department-sidebar-bottom">
          <Link href="/privacy" className="department-nav-item"><Shield size={17}/><span>Privacy</span></Link>
          <button className="department-nav-item department-signout" onClick={async () => { await logout(); router.replace("/login"); }}><LogOut size={17}/><span>Sign out</span></button>
        </div>
      </aside>

      <main className="department-main">
        <header className="department-topbar">
          <div className="department-topbar-left">
            <button className="department-mobile-toggle" type="button" onClick={toggle} aria-label={mobileOpen ? "Close navigation" : "Open navigation"}>
              {mobileOpen ? <X size={18}/> : <Menu size={18}/>} 
            </button>
            {meta.parent ? <Link className="department-back" href={meta.parent}><ArrowLeft size={15}/><span>{meta.parentLabel}</span></Link> : <span className="department-section-label">Government operations</span>}
            <span className="department-breadcrumb-separator">/</span>
            <strong>{meta.label}</strong>
          </div>
          <div className="department-topbar-right">
            <Link href="/app/notifications" className="department-bell" aria-label="Notifications"><Bell size={17}/>{notificationCount > 0 ? <span>{notificationCount}</span> : null}</Link>
            {isAdmin ? <Link href="/admin" className="department-app-switch"><span className="department-app-switch-dot"/> Administration</Link> : <span className="department-app-switch department-app-switch-static"><span className="department-app-switch-dot"/> Department workspace</span>}
          </div>
        </header>
        {children}
      </main>
    </div>
  );
}

