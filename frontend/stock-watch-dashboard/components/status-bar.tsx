"use client"

import { useEffect, useState } from "react"
import type { TradeMode } from "@/lib/stock-data"

interface StatusBarProps {
  mode: TradeMode
}

export function StatusBar({ mode }: StatusBarProps) {
  const [time, setTime] = useState("")

  useEffect(() => {
    const update = () => {
      const now = new Date()
      setTime(
        now.toLocaleTimeString("ko-KR", {
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
          hour12: false,
        })
      )
    }
    update()
    const interval = setInterval(update, 1000)
    return () => clearInterval(interval)
  }, [])

  return (
    <footer className="flex items-center justify-between border-t border-border bg-card px-4 py-1.5">
      <div className="flex items-center gap-4 text-[10px] font-mono text-muted-foreground">
        <span className="flex items-center gap-1.5">
          <span className="relative flex h-1.5 w-1.5">
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-neon-green opacity-75" />
            <span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-neon-green" />
          </span>
          LIVE
        </span>
        <span>KST {time}</span>
        <span className="hidden sm:inline">KOSPI 2,687.45</span>
        <span className="hidden md:inline">S&P 500 5,842.31</span>
      </div>
      <div className="flex items-center gap-4 text-[10px] font-mono text-muted-foreground">
        <span className="hidden sm:inline">
          USD/KRW{" "}
          <span className="text-neon-green">1,342.50</span>
        </span>
        <span>
          Mode:{" "}
          <span className={mode === "buy" ? "text-neon-green font-bold" : "text-neon-red font-bold"}>
            {mode === "buy" ? "BUY" : "SELL"}
          </span>
        </span>
      </div>
    </footer>
  )
}
