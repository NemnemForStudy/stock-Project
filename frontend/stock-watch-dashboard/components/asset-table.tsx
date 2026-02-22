"use client"

import { useEffect, useRef, useState } from "react"
import type { AssetEntry, TradeMode } from "@/lib/stock-data"
import { TrendingUp, TrendingDown } from "lucide-react"

interface AssetTableProps {
  title: string
  subtitle: string
  data: AssetEntry[]
  mode: TradeMode
  currencyLabel: string
  searchQuery: string
}

export function AssetTable({ title, subtitle, data, mode, currencyLabel, searchQuery }: AssetTableProps) {
  const [animating, setAnimating] = useState(false)
  const prevModeRef = useRef(mode)

  useEffect(() => {
    if (prevModeRef.current !== mode) {
      setAnimating(true)
      const timer = setTimeout(() => setAnimating(false), 400)
      prevModeRef.current = mode
      return () => clearTimeout(timer)
    }
  }, [mode])

  const filteredData = searchQuery
    ? data.filter(
        (item) =>
          item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          item.code.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : data

  return (
    <div className="flex flex-col overflow-hidden rounded-lg border border-border bg-card">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-border px-4 py-3">
        <div className="flex items-center gap-2.5">
          <div
            className={`h-2 w-2 rounded-full ${
              mode === "buy" ? "bg-neon-green shadow-[0_0_6px_rgba(0,255,100,0.4)]" : "bg-neon-red shadow-[0_0_6px_rgba(255,80,80,0.4)]"
            }`}
          />
          <div>
            <h2 className="text-sm font-bold text-foreground tracking-wide">
              {title}
            </h2>
            <p className="text-[10px] text-muted-foreground font-mono">
              {subtitle}
            </p>
          </div>
        </div>
        <span
          className={`rounded-sm px-2 py-0.5 text-[10px] font-bold font-mono tracking-wider ${
            mode === "buy"
              ? "bg-neon-green/10 text-neon-green"
              : "bg-neon-red/10 text-neon-red"
          }`}
        >
          {mode === "buy" ? "NET BUY" : "NET SELL"}
        </span>
      </div>

      {/* Table header */}
      <div className="grid grid-cols-[2rem_1fr_5.5rem_5.5rem] items-center gap-1 border-b border-border/60 px-4 py-2 text-[10px] font-bold uppercase tracking-widest text-muted-foreground font-mono">
        <span>#</span>
        <span>Asset</span>
        <span className="text-right">Price</span>
        <span className="text-right">{currencyLabel}</span>
      </div>

      {/* Table rows */}
      <div className="flex-1 overflow-y-auto">
        {filteredData.length === 0 ? (
          <div className="flex items-center justify-center py-12 text-xs text-muted-foreground font-mono">
            No results found
          </div>
        ) : (
          filteredData.map((item, index) => (
            <div
              key={`${mode}-${item.code}`}
              className={`group grid grid-cols-[2rem_1fr_5.5rem_5.5rem] items-center gap-1 border-b border-border/30 px-4 py-2 transition-all duration-200 hover:bg-accent/50 ${
                animating
                  ? "animate-in fade-in slide-in-from-bottom-1"
                  : ""
              }`}
              style={{
                animationDelay: animating ? `${index * 30}ms` : "0ms",
                animationFillMode: "both",
              }}
            >
              {/* Rank */}
              <span
                className={`text-xs font-bold font-mono ${
                  item.rank <= 3
                    ? mode === "buy"
                      ? "text-neon-green"
                      : "text-neon-red"
                    : "text-muted-foreground"
                }`}
              >
                {String(item.rank).padStart(2, "0")}
              </span>

              {/* Name + Code */}
              <div className="min-w-0">
                <div className="flex items-center gap-1.5">
                  <span className="truncate text-xs font-semibold text-foreground">
                    {item.name}
                  </span>
                  {item.change > 0 ? (
                    <TrendingUp className="h-3 w-3 shrink-0 text-neon-green" />
                  ) : (
                    <TrendingDown className="h-3 w-3 shrink-0 text-neon-red" />
                  )}
                </div>
                <span className="text-[10px] text-muted-foreground font-mono">
                  {item.code}
                </span>
              </div>

              {/* Price */}
              <div className="text-right">
                <span className="text-xs font-mono text-foreground">
                  {item.price}
                </span>
                <div
                  className={`text-[10px] font-mono font-semibold ${
                    item.change > 0 ? "text-neon-green" : item.change < 0 ?"text-neon-red" : "text-muted-foreground" 
                  }`}
                >
                  {item.change > 0 ? "+" : ""}{item.change}%
                </div>
              </div>

              {/* Amount */}
              <span
                className={`text-right text-xs font-mono font-semibold ${
                  mode === "buy" ? "text-neon-green/80" : "text-neon-red/80"
                }`}
              >
                {item.amount}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
