import Link from "next/link";

export function Brand({ compact = false, inverted = false }: { compact?: boolean; inverted?: boolean }) {
  return (
    <Link href="/" className={`brand ${compact ? "brand-compact" : ""} ${inverted ? "brand-inverted" : ""}`} aria-label="Anumati home">
      <span className="brand-mark" aria-hidden="true">
        <svg viewBox="0 0 56 56" fill="none">
          <defs>
            <linearGradient id="brandGradient" x1="8" y1="7" x2="48" y2="49" gradientUnits="userSpaceOnUse">
              <stop stopColor="#246BFD"/>
              <stop offset="0.56" stopColor="#19A7A1"/>
              <stop offset="1" stopColor="#78D44E"/>
            </linearGradient>
          </defs>
          <path d="M8 42.5 24.2 10h7.2l16.6 32.5h-8.1l-3.7-7.7H19.3l-3.6 7.7H8Zm14.4-14.2h10.2l-5.1-10.6-5.1 10.6Z" fill="url(#brandGradient)"/>
          <path d="m25.5 33.8 8.6-11.1 7.2 11.1" stroke="#0C2039" strokeWidth="3.1" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
      </span>
      <span className="brand-wordmark">
        <span className="brand-name">Anumati</span>
        <span className="brand-tagline">REGULATORY OPERATIONS</span>
      </span>
    </Link>
  );
}
