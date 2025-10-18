"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { cn } from "@/lib/utils"
import { clearToken } from "@/lib/auth"
import { Button } from "@/components/ui/button"

const items = [
  { href: "/files", label: "My Files" },
  { href: "/upload", label: "Upload" },
  { href: "/billing", label: "Billing" }, // phase 2
]

export function Sidebar() {
  const pathname = usePathname()
  const router = useRouter()
  return (
    <aside className="w-full border-r bg-sidebar p-4 md:w-64">
      <div className="mb-6">
        <Link href="/files" className="font-semibold text-lg">
          FileDash
        </Link>
      </div>
      <nav className="flex flex-col gap-1">
        {items.map((it) => {
          const active = pathname === it.href || pathname?.startsWith(`${it.href}/`)
          return (
            <Link
              key={it.href}
              href={it.href}
              className={cn(
                "rounded-lg px-3 py-2 text-sm",
                active ? "bg-sidebar-accent text-sidebar-accent-foreground" : "hover:bg-muted",
              )}
            >
              {it.label}
            </Link>
          )
        })}
      </nav>
      <div className="mt-auto pt-6">
        <Button
          variant="outline"
          className="w-full bg-transparent"
          onClick={() => {
            clearToken()
            router.replace("/login")
          }}
        >
          Logout
        </Button>
      </div>
    </aside>
  )
}
