import Link from "next/link";
import { Brand } from "./brand";

export function Footer() {
  return (
    <footer className="site-footer">
      <div className="container footer-grid">
        <div>
          <Brand compact />
          <p className="footer-copy">Regulatory intelligence and approval readiness for industrial businesses.</p>
        </div>
        <div className="footer-links">
          <div><span>Product</span><Link href="/#product">Overview</Link><Link href="/#workflow">Workflow</Link><Link href="/#trust">Trust</Link></div>
          <div><span>Legal</span><Link href="/privacy">Privacy policy</Link><Link href="/terms">Terms & conditions</Link></div>
        </div>
      </div>
      <div className="container footer-bottom">
        <span>© {new Date().getFullYear()} Anumati</span>
        <span>Built for traceable regulatory workflows.</span>
      </div>
    </footer>
  );
}
