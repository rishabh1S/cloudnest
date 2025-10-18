export type AuthPayload = {
  userId: string
  name: string
  email: string
}

const STORAGE_KEY = "fm_jwt"

export function parseToken(token?: string | null): AuthPayload | null {
  if (!token) return null
  
  try {
    // Check if it's a JWT (has 3 parts separated by dots)
    if (token.includes('.')) {
      // Real JWT format: header.payload.signature
      const parts = token.split('.')
      if (parts.length !== 3) return null
      
      // Decode the payload (second part)
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
      const decoded = atob(base64)
      const obj = JSON.parse(decoded)
      
      // Check token expiration
      if (obj.exp && obj.exp * 1000 < Date.now()) {
        return null
      }
      
      // Map JWT claims to AuthPayload
      return {
        userId: obj.userId,
        name: obj.name,
        email: obj.email
      }
    } else {
      // Old demo format (simple base64) - fallback for backwards compatibility
      const raw = decodeURIComponent(escape(atob(token)))
      const obj = JSON.parse(raw)
      return { userId: obj.userId, name: obj.name, email: obj.email }
    }
  } catch (error) {
    console.error('Failed to parse token:', error)
    return null
  }
}

export function setToken(token: string) {
  if (typeof window !== "undefined") {
    localStorage.setItem(STORAGE_KEY, token)
  }
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem(STORAGE_KEY)
}

export function clearToken() {
  if (typeof window !== "undefined") {
    localStorage.removeItem(STORAGE_KEY)
  }
}

export function isAuthenticated(): boolean {
  const token = getToken()
  return parseToken(token) !== null
}