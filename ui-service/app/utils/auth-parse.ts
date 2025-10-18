import type { NextRequest } from "next/server"
import { parseToken } from "@/lib/auth"

export function parseBearer(req: NextRequest) {
  const auth = req.headers.get("authorization") || ""
  const [scheme, token] = auth.split(" ")
  if (scheme?.toLowerCase() !== "bearer" || !token) return null
  return parseToken(token)
}
