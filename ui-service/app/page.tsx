"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { getToken, parseToken } from "@/lib/auth"

export default function Home() {
  const router = useRouter()
  useEffect(() => {
    const t = getToken()
    const p = parseToken(t)
    router.replace(p ? "/files" : "/login")
  }, [router])
  return null
}
