"use client"

import { useState, useEffect} from "react"
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
  const [rates, setRates] = useState<RateEntry[]>([])

  useEffect(() => {
    const fetchRates = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/exchange-rates")
        const data = await response.json();

        if (!Array.isArray(data)) {
          console.error("데이터 형식이 배열이 아닙니다:", data);
          return;
        }

        const formatted = data.map((item: any, index: number) => {
          const info = item.output1;
          const history = item.output2 || [];

          if (!info || !info.stck_shrn_iscd) {
            return null; // 데이터가 부실하면 제외
          }

          const symbol = info.stck_shrn_iscd;

          return {
            pair: symbol as string,
            label: getFxLabel(symbol),
            rate: Number(info.ovrs_nmix_prpr).toLocaleString(undefined, { minimumFractionDigits: 2 }),
            change: Number(info.prdy_ctrt),
            changeValue: (info.prdy_vrss_sign === "1" || info.prdy_vrss_sign === "2" ? "+" : "") + info.ovrs_nmix_prdy_vrss,
            history: history.map((h: any) => Number(h.ovrs_nmix_prpr)).reverse() as number[]
          };
        })
        .filter((v): v is RateEntry => v !== null);

        setRates(formatted)
      } catch (error) {
        console.error("환율 데이터 로드 실패 : ", error)
      }
    }

    fetchRates()
    const timer = setInterval(fetchRates, 60000)
    return () => window.clearInterval(timer)
  }, [])

  if(rates.length === 0) return null

  return (
    <div className="px-3 pt-3 lg:px-4 lg:pt-4">
      <div className="flex items-center gap-2 pb-2">
        <span className="text-[10px] font-bold uppercase tracking-widest text-muted-foreground font-mono">
          Exchange Rates
        </span>
        <div className="h-px flex-1 bg-border" />
      </div>
      <div className="grid grid-cols-2 gap-2 sm:grid-cols-3 lg:grid-cols-6 lg:gap-3">
        {rates.map((rate, index) => {
          const isPositive = rate.change > 0
          return (
            <div
              key={`${rate.pair}-${index}`}
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
                        isPositive ? "bg-neon-green/10 text-neon-green" : "bg-neon-red/10 text-neon-red"
                    }`}
                >
                  {isPositive ? <ArrowUpRight className="h-2.5 w-2.5" /> : <ArrowDownRight className="h-2.5 w-2.5" />}
                  {isPositive ? "+" : ""}{rate.change}%
                </div>
              </div>

              {/* Price */}
              <div className="px-3">
                <span className="text-sm font-mono font-bold text-foreground">
                  {rate.rate}
                </span>
                <span className={`ml-1.5 text-[10px] font-mono font-semibold ${
                    isPositive ? "text-neon-green" : "text-neon-red"
                }`}>
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

// 코드에 따른 한글 라벨 반환
function getFxLabel(code: string) {
  const labels: Record<string, string> = {
    USDKRW: "달러/원",
    EURKRW: "유로/원",
    JPYKRW: "엔/원",
    CNYKRW: "위안/원",
    GBPKRW: "파운드/원"
  }
  return labels[code] || "외환"
}