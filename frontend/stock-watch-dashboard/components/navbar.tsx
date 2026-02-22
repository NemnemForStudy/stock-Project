"use client"

import { Search, Sun, Moon } from "lucide-react"

interface NavbarProps {
  searchQuery: string
  onSearchChange: (query: string) => void
  isDark: boolean
  onToggleDark: () => void
}

export function Navbar({ searchQuery, onSearchChange, isDark, onToggleDark }: NavbarProps) {
  return (
    <header className="sticky top-0 z-50 flex items-center justify-between border-b border-border bg-background/95 px-4 py-2.5 backdrop-blur-sm lg:px-6">
      <div className="flex items-center gap-2 shrink-0">
        <div className="flex items-center gap-1.5">
          <svg
            width="20"
            height="20"
            viewBox="0 0 24 24"
            fill="none"
            className="text-neon-green"
          >
            <path
              d="M3 17L9 11L13 15L21 7"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
            <path
              d="M17 7H21V11"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
          <h1 className="text-sm font-bold tracking-widest text-neon-green font-mono uppercase lg:text-base">
            Stock Watch
          </h1>
        </div>
      </div>

      <div className="relative mx-4 hidden max-w-md flex-1 sm:block">
        <Search className="absolute left-3 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground" />
        <input
          type="text"
          placeholder="Search assets..."
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          className="h-8 w-full rounded-md border border-border bg-secondary/50 pl-9 pr-3 text-xs text-foreground placeholder:text-muted-foreground outline-none transition-colors focus:border-neon-green/50 focus:ring-1 focus:ring-neon-green/30 font-mono"
        />
      </div>

      <button
        onClick={onToggleDark}
        className="flex items-center gap-2 shrink-0 rounded-md border border-border bg-secondary/30 px-3 py-1.5 text-xs font-mono font-bold text-muted-foreground transition-all duration-200 hover:text-foreground hover:bg-secondary/50"
        aria-label={isDark ? "Switch to light mode" : "Switch to dark mode"}
      >
        {isDark ? (
          <Sun className="h-3.5 w-3.5" />
        ) : (
          <Moon className="h-3.5 w-3.5" />
        )}
        <span className="hidden sm:inline">{isDark ? "Light" : "Dark"}</span>
      </button>
    </header>
  )
}
