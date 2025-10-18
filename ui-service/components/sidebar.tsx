"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { cn } from "@/lib/utils"
import { clearToken } from "@/lib/auth"
import { Button } from "@/components/ui/button"
import { FileText, Upload, CreditCard, LogOut } from "lucide-react"

const items = [
  { href: "/files", label: "My Files", icon: FileText },
  { href: "/upload", label: "Upload", icon: Upload },
  { href: "/billing", label: "Billing", icon: CreditCard },
]

export function Sidebar() {
  const pathname = usePathname()
  const router = useRouter()

  return (
    <aside className="fixed left-0 top-0 z-40 h-screen w-64 border-r border-sidebar-border bg-sidebar p-6 flex flex-col md:relative md:z-auto">
      <div className="mb-8">
        <Link href="/files" className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-primary to-accent flex items-center justify-center">
            <FileText className="h-5 w-5 text-primary-foreground" />
          </div>
          <span className="font-bold text-lg text-sidebar-foreground">Cloudnest</span>
        </Link>
      </div>

      <nav className="flex flex-col gap-2 flex-1">
        {items.map((it) => {
          const active = pathname === it.href || pathname?.startsWith(`${it.href}/`)
          const Icon = it.icon
          return (
            <Link
              key={it.href}
              href={it.href}
              className={cn(
                "flex items-center gap-3 rounded-lg px-4 py-3 text-sm font-medium transition-all duration-200",
                active
                  ? "bg-sidebar-accent text-sidebar-accent-foreground shadow-lg"
                  : "text-sidebar-foreground hover:bg-sidebar-accent/50 hover:text-sidebar-accent-foreground",
              )}
            >
              <Icon className="h-5 w-5" />
              {it.label}
            </Link>
          )
        })}
      </nav>

      <Button
        variant="outline"
        className="w-full bg-transparent border-sidebar-border hover:bg-sidebar-accent/50"
        onClick={() => {
          clearToken()
          router.replace("/login")
        }}
      >
        <LogOut className="h-4 w-4 mr-2" />
        Logout
      </Button>
    </aside>
  )
}
