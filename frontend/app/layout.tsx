import type { Metadata } from "next";
import "./globals.css";

const siteUrl = process.env.NEXT_PUBLIC_SITE_URL || "http://localhost:3000";

export const metadata: Metadata = {
  metadataBase: new URL(siteUrl),
  title: {
    default: "Anumati | Regulatory operations for industry",
    template: "%s | Anumati",
  },
  description:
    "Anumati helps industrial businesses understand what applies, prepare verified evidence, coordinate approvals, and stay compliant throughout the business lifecycle.",
  applicationName: "Anumati",
  robots: { index: true, follow: true },
  icons: { icon: "/icon.svg" },
  openGraph: {
    title: "Anumati | Industrial approval readiness",
    description:
      "A professional regulatory operations workspace for industrial approvals, evidence, compliance and government support.",
    url: siteUrl,
    siteName: "Anumati",
    type: "website",
  },
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
