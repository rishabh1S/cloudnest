import type React from "react"
import AuthGuard from "@/components/auth-guard"
import { TopNav } from "@/components/top-nav"
import { Sidebar } from "@/components/sidebar"

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <AuthGuard>
      <div className="grid min-h-dvh grid-rows-[auto_1fr] md:grid-cols-[16rem_1fr] md:grid-rows-1">
        <div className="hidden md:block">
          <Sidebar />
        </div>
        <div className="md:hidden">
          <TopNav />
        </div>
        <main className="overflow-hidden">
          <div className="hidden md:flex">
            <TopNav />
          </div>
          <div className="p-4 md:p-6">{children}</div>
        </main>
      </div>
    </AuthGuard>
  )
}
