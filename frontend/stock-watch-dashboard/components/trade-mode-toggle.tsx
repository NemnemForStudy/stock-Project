"use client"

import type { TradeMode } from "@/lib/stock-data"

interface TradeModeToggleProps {
  mode: TradeMode
  onModeChange: (mode: TradeMode) => void
}

export function TradeModeToggle({ mode, onModeChange }: TradeModeToggleProps) {
  return (
    <div className="flex items-center justify-center px-4 py-3">
      <div className="flex items-center gap-0 rounded-lg border border-border bg-card p-0.5">
        <button
          onClick={() => onModeChange("buy")}
          className={`relative rounded-md px-6 py-2 text-xs font-bold tracking-wide transition-all duration-300 font-mono ${
            mode === "buy"
              ? "bg-neon-green/15 text-neon-green shadow-[0_0_16px_rgba(0,255,100,0.12)] border border-neon-green/40"
              : "border border-transparent text-muted-foreground hover:text-foreground hover:bg-secondary/50"
          }`}
        >
          {mode === "buy" && (
            <span className="absolute inset-0 rounded-md bg-neon-green/5 animate-pulse" />
          )}
          <span className="relative flex items-center gap-1.5">
            <span className={`inline-block h-1.5 w-1.5 rounded-full ${mode === "buy" ? "bg-neon-green shadow-[0_0_6px_rgba(0,255,100,0.6)]" : "bg-muted-foreground/40"}`} />
            {'매입 Buy'}
          </span>
        </button>
        <button
          onClick={() => onModeChange("sell")}
          className={`relative rounded-md px-6 py-2 text-xs font-bold tracking-wide transition-all duration-300 font-mono ${
            mode === "sell"
              ? "bg-neon-red/15 text-neon-red shadow-[0_0_16px_rgba(255,80,80,0.12)] border border-neon-red/40"
              : "border border-transparent text-muted-foreground hover:text-foreground hover:bg-secondary/50"
          }`}
        >
          {mode === "sell" && (
            <span className="absolute inset-0 rounded-md bg-neon-red/5 animate-pulse" />
          )}
          <span className="relative flex items-center gap-1.5">
            <span className={`inline-block h-1.5 w-1.5 rounded-full ${mode === "sell" ? "bg-neon-red shadow-[0_0_6px_rgba(255,80,80,0.6)]" : "bg-muted-foreground/40"}`} />
            {'매각 Sell'}
          </span>
        </button>
      </div>
    </div>
  )
}
