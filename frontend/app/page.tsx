import Image from "next/image";
import Link from "next/link";
import { ArrowRight, ArrowUpRight, Check, ChevronRight, FileText, GitBranch, Layers, Landmark, Network, Scale, Shield } from "../components/icons";
import { SiteHeader } from "../components/site-header";
import { Footer } from "../components/footer";
import { Reveal, Stagger, StaggerItem } from "../components/motion";

const capabilities = [
  { number: "01", label: "Business state", title: "A versioned starting point", text: "Capture the business facts that regulatory decisions depend on, without losing the history of previous states.", icon: Landmark, href: "/login" },
  { number: "02", label: "Regulatory plan", title: "What applies, and why", text: "Evaluate configured rules against the current business state and keep each result tied to its source and reasoning.", icon: GitBranch, href: "/login" },
  { number: "03", label: "Evidence", title: "Evidence before submission", text: "Turn approval requirements into an actionable evidence set, then check uploaded documents against known profile facts.", icon: Shield, href: "/login" },
  { number: "04", label: "Workflow", title: "From readiness to decision", text: "Carry the same context into applications, queries, inspections, service timelines and post-approval work.", icon: Network, href: "/login" },
];

const lifecycle = [
  ["01", "Understand", "Establish the current business state and the regulatory plan.", "#business-regulatory-twin"],
  ["02", "Prepare", "Build evidence and clear readiness blockers before submission.", "#evidence-passport"],
  ["03", "Execute", "Move applications through queries, inspections and decisions.", "#application-preflight"],
  ["04", "Continue", "Carry the same verified foundation into compliance and renewals.", "#trust"],
];

export default function HomePage() {
  return (
    <main className="public-site">
      <div className="hero-stage public-hero-v13">
        <SiteHeader transparent />
        <section className="hero hero-cinematic">
          <div className="hero-bg" aria-hidden="true"><Image src="/hero-industrial.jpg" alt="Industrial landscape" fill priority sizes="100vw" /></div>
          <div className="hero-overlay" aria-hidden="true" />
          <div className="container hero-v13-grid">
            <Reveal className="hero-copy">
              <span className="hero-index">REGULATORY OPERATIONS / 01</span>
              <div className="hero-rule" />
              <h1>Know what applies.<br/><em>Prepare it properly.</em><br/>Move it forward.</h1>
              <p className="hero-lead">Anumati connects business state, source-backed regulatory analysis, evidence readiness and application operations around the government systems that remain the statutory systems of record.</p>
              <div className="hero-actions">
                <Link href="/app/profiles/new" className="button button-primary button-large">Create business profile <ArrowRight size={17}/></Link>
                <Link href="#product" className="button button-ghost button-large">See how it works <ArrowDownCue /></Link>
              </div>
            </Reveal>

            <Reveal className="hero-brief" delay={0.08}>
              <div className="hero-brief-top"><span>Operating model</span><span>04 stages</span></div>
              <div className="hero-brief-main"><strong>Business → Regulation → Evidence → Workflow</strong><p>One traceable chain from the current business state to the next action.</p></div>
              <div className="hero-brief-stack">
                <div><span className="brief-no">01</span><span><strong>Business state</strong><small>Versioned inputs</small></span><Check size={15}/></div>
                <div><span className="brief-no">02</span><span><strong>Regulatory plan</strong><small>Rule + source</small></span><Check size={15}/></div>
                <div><span className="brief-no">03</span><span><strong>Evidence readiness</strong><small>Pre-flight checks</small></span><span className="brief-state">Current</span></div>
                <div><span className="brief-no">04</span><span><strong>Department workflow</strong><small>Queries + inspection + decision</small></span><ArrowUpRight size={15}/></div>
              </div>
            </Reveal>
          </div>
          <div className="container hero-footline">
            <span><b>Source-backed</b> regulatory logic</span><span><b>Versioned</b> business state</span><span><b>Evidence-aware</b> readiness</span><span><b>Human</b> statutory authority</span>
          </div>
        </section>
      </div>

      <section id="product" className="section section-light section-v13">
        <div className="container product-v13">
          <Reveal className="section-marker"><span>02</span><span>The platform</span></Reveal>
          <Reveal className="section-intro-v13"><h2>Less searching. More certainty about the next step.</h2><p>Anumati is designed around decisions that have to be made in sequence: what applies, what evidence is needed, whether the package is ready, and what happens after submission.</p></Reveal>
          <Stagger className="capability-rows">
            {capabilities.map(({ number, label, title, text, icon: Icon, href }) => (
              <StaggerItem key={title}>
                <Link href={href} className="capability-row" id={title.toLowerCase().replace(/[^a-z0-9]+/g, "-")}>
                  <span className="capability-number">{number}</span>
                  <div className="capability-icon"><Icon size={18}/></div>
                  <div className="capability-copy"><span>{label}</span><h3>{title}</h3><p>{text}</p></div>
                  <ArrowUpRight className="capability-arrow" size={19}/>
                </Link>
              </StaggerItem>
            ))}
          </Stagger>
        </div>
      </section>

      <section id="workflow" className="section workflow-v13">
        <div className="container workflow-v13-grid">
          <Reveal className="workflow-v13-intro">
            <span className="section-marker section-marker-dark"><span>03</span><span>The lifecycle</span></span>
            <h2>From business state to continuous compliance.</h2>
            <p>The same verified foundation travels with the work. That means each important action can be traced to the state, rule, evidence and source behind it.</p>
          </Reveal>
          <Stagger className="workflow-rows-v13">
            {lifecycle.map(([number, title, text, href]) => (
              <StaggerItem key={title}>
                <Link href={href} className="workflow-row-v13">
                  <span className="workflow-number-v13">{number}</span>
                  <div><h3>{title}</h3><p>{text}</p></div>
                  <ChevronRight size={18}/>
                </Link>
              </StaggerItem>
            ))}
          </Stagger>
        </div>
      </section>

      <section id="trust" className="section section-light section-v13">
        <div className="container trust-v13">
          <Reveal className="section-marker"><span>04</span><span>Trust model</span></Reveal>
          <div className="trust-v13-layout">
            <Reveal className="section-intro-v13"><h2>Explain the path, not just the outcome.</h2><p>The product is deliberately built so the user can inspect the reasoning behind a regulatory result and see where evidence and workflow state came from.</p></Reveal>
            <Stagger className="trust-v13-list">
              <StaggerItem className="trust-v13-item"><FileText size={18}/><div><strong>Source-backed</strong><p>Regulatory results stay connected to their supporting source and version.</p></div></StaggerItem>
              <StaggerItem className="trust-v13-item"><GitBranch size={18}/><div><strong>Version-aware</strong><p>Business changes create new versions instead of rewriting history.</p></div></StaggerItem>
              <StaggerItem className="trust-v13-item"><Layers size={18}/><div><strong>Evidence-led</strong><p>Documents are evidence inputs, not automatic legal certification.</p></div></StaggerItem>
              <StaggerItem className="trust-v13-item"><Scale size={18}/><div><strong>Human authority</strong><p>Statutory decisions remain with authorised officers.</p></div></StaggerItem>
            </Stagger>
          </div>
        </div>
      </section>

      <section className="final-section final-v13">
        <div className="container final-v13-inner"><div><span className="section-marker section-marker-dark"><span>05</span><span>Start here</span></span><h2>Turn the current business state into a clear regulatory journey.</h2></div><Link href="/app/profiles/new" className="button button-dark button-large">Create business profile <ArrowRight size={17}/></Link></div>
      </section>
      <Footer />
    </main>
  );
}

function ArrowDownCue() {
  return <span aria-hidden="true" style={{ display: "inline-flex", transform: "rotate(90deg)" }}><ArrowRight size={15}/></span>;
}
