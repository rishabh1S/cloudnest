import { getToken } from "./auth"

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost"

export async function api(input: RequestInfo, init?: RequestInit) {
  const token = getToken()
  const headers = new Headers(init?.headers || {})
  if (token) headers.set("Authorization", `Bearer ${token}`)
  if (!headers.has("Content-Type") && !(init?.body instanceof FormData)) {
    headers.set("Content-Type", "application/json")
  }

  // Resolve relative URLs to backend
  const url = typeof input === "string" && input.startsWith("/") ? `${API_BASE}${input}` : input

  console.log("API Request:", url, init)
  console.log("API Headers:", Object.fromEntries(headers.entries()))

  const res = await fetch(url, { ...init, headers })
  if (!res.ok) {
    const msg = await res.text().catch(() => res.statusText)
    throw new Error(msg || `Request failed: ${res.status}`)
  }
  const ct = res.headers.get("content-type") || ""
  if (ct.includes("application/json")) return res.json()
  return res.text()
}

export const swrFetcher = async (url: string) => api(url)
