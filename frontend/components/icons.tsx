import type { SVGProps } from "react";

type IconProps = SVGProps<SVGSVGElement> & { size?: number };

function IconBase({ size = 18, children, ...props }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" {...props}>
      {children}
    </svg>
  );
}

export function ArrowRight(props: IconProps) { return <IconBase {...props}><path d="M5 12h14"/><path d="m13 6 6 6-6 6"/></IconBase>; }

export function ArrowLeft(props: IconProps) { return <IconBase {...props}><path d="M19 12H5"/><path d="m11 18-6-6 6-6"/></IconBase>; }
export function PanelLeftClose(props: IconProps) { return <IconBase {...props}><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M9 4v16M15 9l-3 3 3 3"/></IconBase>; }
export function PanelLeftOpen(props: IconProps) { return <IconBase {...props}><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M9 4v16M12 9l3 3-3 3"/></IconBase>; }
export function ArrowUpRight(props: IconProps) { return <IconBase {...props}><path d="M7 17 17 7"/><path d="M7 7h10v10"/></IconBase>; }
export function Check(props: IconProps) { return <IconBase {...props}><path d="m5 12 4 4L19 6"/></IconBase>; }
export function ChevronDown(props: IconProps) { return <IconBase {...props}><path d="m6 9 6 6 6-6"/></IconBase>; }
export function ChevronRight(props: IconProps) { return <IconBase {...props}><path d="m9 6 6 6-6 6"/></IconBase>; }
export function FileText(props: IconProps) { return <IconBase {...props}><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8Z"/><path d="M14 2v6h6"/><path d="M8 13h8M8 17h6"/></IconBase>; }
export function GitBranch(props: IconProps) { return <IconBase {...props}><circle cx="6" cy="6" r="2.5"/><circle cx="18" cy="18" r="2.5"/><circle cx="18" cy="6" r="2.5"/><path d="M8.5 6H14a4 4 0 0 1 4 4v5.5"/><path d="M8.5 6h4a4 4 0 0 1 4 4"/></IconBase>; }
export function Landmark(props: IconProps) { return <IconBase {...props}><path d="m3 10 9-6 9 6"/><path d="M5 10v8M9 10v8M15 10v8M19 10v8M3 18h18M2 22h20"/></IconBase>; }
export function Layers(props: IconProps) { return <IconBase {...props}><path d="m12 3 9 5-9 5-9-5 9-5Z"/><path d="m3 12 9 5 9-5"/><path d="m3 16 9 5 9-5"/></IconBase>; }
export function Lock(props: IconProps) { return <IconBase {...props}><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/></IconBase>; }
export function LogOut(props: IconProps) { return <IconBase {...props}><path d="M10 17l5-5-5-5"/><path d="M15 12H3"/><path d="M21 5v14a2 2 0 0 1-2 2h-6"/></IconBase>; }
export function Menu(props: IconProps) { return <IconBase {...props}><path d="M4 7h16M4 12h16M4 17h16"/></IconBase>; }
export function Search(props: IconProps) { return <IconBase {...props}><circle cx="11" cy="11" r="6.5"/><path d="m16 16 5 5"/></IconBase>; }
export function Settings(props: IconProps) { return <IconBase {...props}><path d="M12 3v3M12 18v3M4.2 6.2l2.1 2.1M17.7 15.7l2.1 2.1M3 12h3M18 12h3M4.2 17.8l2.1-2.1M17.7 8.3l2.1-2.1"/><circle cx="12" cy="12" r="4"/></IconBase>; }
export function Shield(props: IconProps) { return <IconBase {...props}><path d="M12 3 19 6v5c0 4.8-2.9 8.2-7 10-4.1-1.8-7-5.2-7-10V6l7-3Z"/><path d="m9 12 2 2 4-4"/></IconBase>; }
export function Upload(props: IconProps) { return <IconBase {...props}><path d="M12 16V4M7 9l5-5 5 5"/><path d="M5 20h14"/></IconBase>; }
export function User(props: IconProps) { return <IconBase {...props}><circle cx="12" cy="8" r="3.5"/><path d="M5 20c.9-3.1 3.2-5 7-5s6.1 1.9 7 5"/></IconBase>; }
export function X(props: IconProps) { return <IconBase {...props}><path d="m6 6 12 12M18 6 6 18"/></IconBase>; }

export function Bell(props: IconProps) { return <IconBase {...props}><path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/></IconBase>; }
export function Calendar(props: IconProps) { return <IconBase {...props}><rect x="3" y="4" width="18" height="17" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></IconBase>; }
export function AlertTriangle(props: IconProps) { return <IconBase {...props}><path d="m12 3 9 17H3L12 3Z"/><path d="M12 9v5M12 17h.01"/></IconBase>; }
export function Scale(props: IconProps) { return <IconBase {...props}><path d="M12 3v18M6 6h12M5 6l-3 6a4 4 0 0 0 6 0L5 6ZM19 6l-3 6a4 4 0 0 0 6 0l-3-6ZM5 21h14"/></IconBase>; }
export function RotateCcw(props: IconProps) { return <IconBase {...props}><path d="M4 12a8 8 0 1 0 2.4-5.7L4 8.5"/><path d="M4 4v4.5h4.5"/></IconBase>; }
export function MessageSquare(props: IconProps) { return <IconBase {...props}><path d="M20 15a4 4 0 0 1-4 4H8l-4 3v-7a4 4 0 0 1-1-3V7a4 4 0 0 1 4-4h9a4 4 0 0 1 4 4v8Z"/><path d="M8 9h8M8 13h5"/></IconBase>; }
export function Plus(props: IconProps) { return <IconBase {...props}><path d="M12 5v14M5 12h14"/></IconBase>; }
export function Minus(props: IconProps) { return <IconBase {...props}><path d="M5 12h14"/></IconBase>; }
export function ExternalLink(props: IconProps) { return <IconBase {...props}><path d="M14 5h5v5"/><path d="M10 14 19 5"/><path d="M19 13v5a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h5"/></IconBase>; }
export function GitCompare(props: IconProps) { return <IconBase {...props}><path d="M8 7h9"/><path d="m13 3 4 4-4 4"/><path d="M16 17H7"/><path d="m11 13-4 4 4 4"/></IconBase>; }
export function Network(props: IconProps) { return <IconBase {...props}><circle cx="6" cy="12" r="2"/><circle cx="18" cy="6" r="2"/><circle cx="18" cy="18" r="2"/><path d="M8 11 16 7M8 13l8 4"/></IconBase>; }
export function Database(props: IconProps) { return <IconBase {...props}><ellipse cx="12" cy="5" rx="7" ry="3"/><path d="M5 5v7c0 1.7 3.1 3 7 3s7-1.3 7-3V5"/><path d="M5 12v7c0 1.7 3.1 3 7 3s7-1.3 7-3v-7"/></IconBase>; }
export function ClipboardCheck(props: IconProps) { return <IconBase {...props}><rect x="5" y="4" width="14" height="17" rx="2"/><path d="M9 4.5V3h6v1.5M8 12l2.5 2.5L16 9"/></IconBase>; }
export function Clock(props: IconProps) { return <IconBase {...props}><circle cx="12" cy="12" r="8.5"/><path d="M12 7v5l3 2"/></IconBase>; }

export function Sparkles(props: IconProps) { return <IconBase {...props}><path d="m12 3 1.7 5.3L19 10l-5.3 1.7L12 17l-1.7-5.3L5 10l5.3-1.7L12 3ZM19 16l.8 2.2L22 19l-2.2.8L19 22l-.8-2.2L16 19l2.2-.8L19 16Z"/></IconBase>; }
