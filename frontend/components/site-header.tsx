"use client";

import Link from "next/link";
import { useState } from "react";
import { Brand } from "./brand";
import { Menu, X, ArrowRight } from "./icons";

export function SiteHeader({ transparent = false }: { transparent?: boolean }) {
  const [open, setOpen] = useState(false);
  return (
    <header className={`site-header ${transparent ? "site-header-transparent" : ""}`}>
      <div className="container header-inner">
        <Brand />
        <nav className={`site-nav ${open ? "is-open" : ""}`} aria-label="Primary navigation">
          <Link href="/#product" onClick={() => setOpen(false)}>Platform</Link>
          <Link href="/#workflow" onClick={() => setOpen(false)}>How it works</Link>
          <Link href="/#trust" onClick={() => setOpen(false)}>Trust</Link>
          <Link href="/privacy" onClick={() => setOpen(false)}>Privacy</Link>
        </nav>
        <div className="header-actions">
          <Link href="/login" className="button button-secondary button-small">Sign in</Link>
          <Link href="/app/profiles/new" className="button button-primary button-small">Get started <ArrowRight size={15}/></Link>
          <button className="mobile-menu" onClick={() => setOpen((v) => !v)} aria-label={open ? "Close menu" : "Open menu"}>
            {open ? <X size={19}/> : <Menu size={19}/>} 
          </button>
        </div>
      </div>
    </header>
  );
}
