// NEW: the ONE place the frontend talks to the backend.
// Handles CSRF, JSON, error messages and silent token refresh.

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

export type Me = {
  userId: string;
  email: string;
  role: string;
  organizationId: string;
  organizationName: string;
  baseCurrency: string;
};

function readCookie(name: string): string | null {
  const row = document.cookie.split("; ").find((c) => c.startsWith(`${name}=`));
  return row ? decodeURIComponent(row.substring(name.length + 1)) : null;
}

// WHY: Spring's csrf.spa() puts a token in a readable XSRF-TOKEN cookie.
// Every POST/PATCH/DELETE must echo it back in the X-XSRF-TOKEN header.
// An attacker's site can't read our cookies, so it can't forge that header.
async function csrfToken(): Promise<string> {
  let token = readCookie("XSRF-TOKEN");
  if (!token) {
    await fetch("/api/auth/csrf"); // makes Spring set the cookie
    token = readCookie("XSRF-TOKEN");
  }
  if (!token) throw new ApiError(0, "Could not get a security token. Refresh the page.");
  return token;
}

// Swaps the refresh cookie for a fresh access cookie. true = still logged in.
export async function refreshSession(): Promise<boolean> {
  const res = await fetch("/api/auth/refresh", {
    method: "POST",
    headers: { "X-XSRF-TOKEN": await csrfToken() },
  });
  return res.ok;
}

const NO_RETRY = ["/auth/login", "/auth/register", "/auth/refresh"];

type Options = { method?: string; body?: unknown; retry?: boolean };

export async function api<T>(path: string, { method = "GET", body, retry = true }: Options = {}): Promise<T> {
  const headers: Record<string, string> = {};
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (method !== "GET") headers["X-XSRF-TOKEN"] = await csrfToken();

  const res = await fetch(`/api${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  // WHY: the access cookie only lives 15 min. On a 401 we try ONE silent refresh,
  // then repeat the original request. Only if that fails do we send them to login.
  if (res.status === 401 && retry && !NO_RETRY.includes(path)) {
    if (await refreshSession()) return api<T>(path, { method, body, retry: false });
    window.location.href = "/login";
    throw new ApiError(401, "Your session has ended. Please log in again.");
  }

  if (!res.ok) {
    // Our backend sends ProblemDetail JSON: { title, detail, ... }
    const problem = await res.json().catch(() => null);
    throw new ApiError(res.status, problem?.detail ?? "Something went wrong. Please try again.");
  }

  if (res.status === 204) return undefined as T;
  return (await res.json()) as T;
}

// WHY: ?next=/transactions sends you back after login. But ?next=https://evil.com
// would be an "open redirect". Only allow paths on OUR site.
export function safeNext(value: string | undefined | null): string {
  return value && value.startsWith("/") && !value.startsWith("//") ? value : "/";
}