"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import { getToken, parseToken } from "@/lib/auth"

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const [ready, setReady] = useState(false)
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    const t = getToken()
    const payload = parseToken(t)
    if (!payload) {
      router.replace("/login?next=" + encodeURIComponent(pathname || "/files"))
    } else {
      setReady(true)
    }
  }, [router, pathname])

  if (!ready) return null
  return <>{children}</>
}
