"use client"

import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { clearToken, getToken, parseToken } from "@/lib/auth"
import { useRouter } from "next/navigation"

export function TopNav() {
  const router = useRouter()
  const payload = parseToken(getToken())
  const initials =
    payload?.name
      ?.split(" ")
      .map((s) => s[0])
      .join("")
      .slice(0, 2)
      .toUpperCase() || "U"

  return (
    <header className="flex items-center justify-between border-b bg-background px-4 py-3">
      <h1 className="text-balance text-lg font-semibold">Dashboard</h1>
      <DropdownMenu>
        <DropdownMenuTrigger className="rounded-full outline-none ring-0">
          <Avatar className="h-8 w-8">
            <AvatarFallback>{initials}</AvatarFallback>
          </Avatar>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuLabel className="max-w-[220px] truncate">{payload?.name || "User"}</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuItem
            onClick={() => {
              clearToken()
              router.replace("/login")
            }}
          >
            Logout
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </header>
  )
}
