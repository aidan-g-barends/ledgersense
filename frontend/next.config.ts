import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  experimental: {
    agentUpgrade: "security",
  },
  // NEW: forward /api/* to Spring Boot. Browser sees one origin -> cookies work, no CORS.
  async rewrites() {
    const apiUrl = process.env.API_URL ?? "http://localhost:8080";
    return [{ source: "/api/:path*", destination: `${apiUrl}/api/:path*` }];
  },
};

export default nextConfig;