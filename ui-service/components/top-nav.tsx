"use client";

import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { clearToken, getToken, parseToken } from "@/lib/auth";
import { useRouter } from "next/navigation";
import { Search, Bell, Sun, Moon, Monitor } from "lucide-react";
import { Input } from "@/components/ui/input";
import { useTheme } from "next-themes";

export function TopNav() {
  const router = useRouter();
  const { theme, setTheme } = useTheme();
  const payload = parseToken(getToken());
  const initials =
    payload?.name
      ?.split(" ")
      .map((s) => s[0])
      .join("")
      .slice(0, 2)
      .toUpperCase() || "U";

  return (
    <header className="sticky top-0 z-30 flex items-center justify-between border-b border-border bg-background/80 backdrop-blur-md px-6 py-4 min-w-full">
      <div className="flex items-center gap-4 flex-1">
        <div className="relative hidden md:flex items-center flex-1 max-w-md">
          <Search className="absolute left-3 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search files..."
            className="pl-10 bg-card border-border focus:border-primary"
          />
        </div>
      </div>

      <div className="flex items-center gap-4">
        <button className="relative p-2 hover:bg-card rounded-lg transition-colors">
          <Bell className="h-5 w-5 text-muted-foreground hover:text-foreground" />
          <span className="absolute top-1 right-1 h-2 w-2 bg-primary rounded-full"></span>
        </button>

        <DropdownMenu>
          <DropdownMenuTrigger className="rounded-full outline-none ring-0 hover:ring-2 hover:ring-primary/50 transition-all">
            <Avatar className="h-9 w-9 border border-border">
              <AvatarFallback className="bg-gradient-to-br from-primary to-accent text-primary-foreground font-semibold">
                {initials}
              </AvatarFallback>
            </Avatar>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56">
            <DropdownMenuLabel className="max-w-[220px] truncate">
              {payload?.name || "User"}
            </DropdownMenuLabel>
            <DropdownMenuLabel className="text-xs text-muted-foreground font-normal">
              {payload?.email || "user@example.com"}
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <div className="flex gap-2">
              <DropdownMenuLabel className="text-xs py-3 text-muted-foreground tracking-wider px-3">
                Theme
              </DropdownMenuLabel>
              <div className="px-2 py-1.5 flex items-center gap-2 bg-muted/30 rounded-lg">
                <button
                  onClick={() => setTheme("light")}
                  className={`flex items-center gap-1 px-2.5 py-1.5 rounded-md transition-all text-sm font-medium ${
                    theme === "light"
                      ? "bg-background text-foreground shadow-sm"
                      : "text-muted-foreground hover:text-foreground"
                  }`}
                  title="Light mode"
                >
                  <Sun className="h-3.5 w-3.5" />
                </button>
                <button
                  onClick={() => setTheme("dark")}
                  className={`flex items-center gap-1 px-2.5 py-1.5 rounded-md transition-all text-sm font-medium ${
                    theme === "dark"
                      ? "bg-background text-foreground shadow-sm"
                      : "text-muted-foreground hover:text-foreground"
                  }`}
                  title="Dark mode"
                >
                  <Moon className="h-3.5 w-3.5" />
                </button>
                <button
                  onClick={() => setTheme("system")}
                  className={`flex items-center gap-1 px-2.5 py-1.5 rounded-md transition-all text-sm font-medium ${
                    theme === "system"
                      ? "bg-background text-foreground shadow-sm"
                      : "text-muted-foreground hover:text-foreground"
                  }`}
                  title="System mode"
                >
                  <Monitor className="h-3.5 w-3.5" />
                </button>
              </div>
            </div>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              onClick={() => {
                clearToken();
                router.replace("/login");
              }}
            >
              Logout
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
