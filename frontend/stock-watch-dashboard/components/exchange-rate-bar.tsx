"use client"

import { ArrowUpRight, ArrowDownRight } from "lucide-react"
import { AreaChart, Area, ResponsiveContainer, YAxis } from "recharts"

interface RateEntry {
  pair: string
  label: string
  rate: string
  change: number
  changeValue: string
  history: number[]
}

const exchangeRates: RateEntry[] = [
  {
    pair: "USD/KRW",
    label: "달러/원",
    rate: "1,342.50",
    change: 0.32,
    changeValue: "+4.30",
    history: [1335, 1338, 1336, 1340, 1337, 1339, 1341, 1338, 1340, 1343, 1341, 1344, 1342, 1343],
  },
  {
    pair: "EUR/KRW",
    label: "유로/원",
    rate: "1,458.20",
    change: -0.18,
    changeValue: "-2.65",
    history: [1465, 1462, 1464, 1460, 1463, 1461, 1459, 1462, 1460, 1457, 1459, 1456, 1458, 1458],
  },
  {
    pair: "JPY/KRW",
    label: "엔/원",
    rate: "8.95",
    change: 0.51,
    changeValue: "+0.05",
    history: [8.88, 8.90, 8.89, 8.91, 8.90, 8.92, 8.91, 8.93, 8.92, 8.94, 8.93, 8.94, 8.96, 8.95],
  },
  {
    pair: "CNY/KRW",
    label: "위안/원",
    rate: "185.30",
    change: -0.24,
    changeValue: "-0.45",
    history: [186.2, 186.0, 186.3, 185.8, 186.1, 185.7, 185.9, 185.5, 185.8, 185.4, 185.6, 185.2, 185.4, 185.3],
  },
  {
    pair: "GBP/KRW",
    label: "파운드/원",
    rate: "1,698.40",
    change: 0.15,
    changeValue: "+2.50",
    history: [1694, 1695, 1693, 1696, 1694, 1697, 1695, 1696, 1698, 1697, 1699, 1697, 1698, 1698],
  },
  {
    pair: "BTC/USD",
    label: "비트코인",
    rate: "98,420",
    change: 3.80,
    changeValue: "+3,605",
    history: [94200, 94800, 95100, 94600, 95500, 96200, 95800, 96800, 97100, 97500, 96900, 97800, 98100, 98420],
  },
]

function SparkChart({ data, isPositive }: { data: number[]; isPositive: boolean }) {
  const chartData = data.map((value, i) => ({ v: value, i }))
  const greenColor = "oklch(0.82 0.19 145)"
  const redColor = "oklch(0.60 0.22 25)"
  const color = isPositive ? greenColor : redColor

  return (
    <ResponsiveContainer width="100%" height="100%">
      <AreaChart data={chartData} margin={{ top: 2, right: 0, bottom: 0, left: 0 }}>
        <defs>
          <linearGradient id={`grad-${isPositive ? "up" : "down"}`} x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor={color} stopOpacity={0.3} />
            <stop offset="100%" stopColor={color} stopOpacity={0.02} />
          </linearGradient>
        </defs>
        <YAxis domain={["dataMin", "dataMax"]} hide />
        <Area
          type="monotone"
          dataKey="v"
          stroke={color}
          strokeWidth={1.5}
          fill={`url(#grad-${isPositive ? "up" : "down"})`}
          isAnimationActive={false}
        />
      </AreaChart>
    </ResponsiveContainer>
  )
}

export function ExchangeRateBar() {
  return (
    <div className="px-3 pt-3 lg:px-4 lg:pt-4">
      <div className="flex items-center gap-2 pb-2">
        <span className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground font-mono">
          Exchange Rates
        </span>
        <div className="h-px flex-1 bg-border" />
      </div>
      <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 lg:grid-cols-6 lg:gap-3">
        {exchangeRates.map((rate) => {
          const isPositive = rate.change > 0
          return (
            <div
              key={rate.pair}
              className="flex flex-col overflow-hidden rounded-lg border border-border bg-card"
            >
              {/* Top info */}
              <div className="flex items-center justify-between px-3 pt-2.5 pb-1">
                <div className="min-w-0">
                  <div className="text-[10px] font-mono font-bold text-muted-foreground truncate">
                    {rate.pair}
                  </div>
                  <div className="text-[9px] font-mono text-muted-foreground/60">
                    {rate.label}
                  </div>
                </div>
                <div
                  className={`flex items-center gap-0.5 rounded-sm px-1.5 py-0.5 text-[10px] font-mono font-bold ${
                    isPositive
                      ? "bg-neon-green/10 text-neon-green"
                      : "bg-neon-red/10 text-neon-red"
                  }`}
                >
                  {isPositive ? (
                    <ArrowUpRight className="h-2.5 w-2.5" />
                  ) : (
                    <ArrowDownRight className="h-2.5 w-2.5" />
                  )}
                  {isPositive ? "+" : ""}
                  {rate.change}%
                </div>
              </div>

              {/* Price */}
              <div className="px-3">
                <span className="text-sm font-mono font-bold text-foreground">
                  {rate.rate}
                </span>
                <span
                  className={`ml-1.5 text-[10px] font-mono font-semibold ${
                    isPositive ? "text-neon-green" : "text-neon-red"
                  }`}
                >
                  {rate.changeValue}
                </span>
              </div>

              {/* Chart */}
              <div className="h-10 w-full mt-1">
                <SparkChart data={rate.history} isPositive={isPositive} />
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
