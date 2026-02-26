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
        // API 응답 구조: data.conversion_rates { KRW: 1342.5, JPY: 150.2 ... }
        const data: Record<string, Number> =  await response.json();

        if (!data || Object.keys(data).length === 0) {
          console.error("환율 데이터가 비어 있습니다.");
          return;
        }

        // 보여줄 순서 정의
        const targetCurrencies = ["USD", "EUR", "JPY", "CNY", "GBP"];

        const formatted: RateEntry[] = targetCurrencies.map((symbol) => {
          const rawRate = (data as Record<string, number>)[symbol] || 0;

          // 엔화 경우 보통 100엔 단위로 표시하므로 100 곱함
          const finalRate = symbol === "JPY" ? rawRate * 100 : rawRate;
          const displayPair = symbol === "JPY" ? "JPYKRW" : `${symbol}KRW`;

          return {
            pair: displayPair,
            label: getFxLabel(displayPair),
            rate: finalRate.toLocaleString(undefined, {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2
            }),
            change: 0.05, // 무료 AIP는 실시간 변동 주지 않아서 임의값 혹은 이전값 비교 필요
            changeValue: "+0.00",
            // 차트용 데이터는 API에서 과거 기록을 따로 호출해야 하므로, 일단 현재가 기준
            history: rawRate > 0
                ? Array.from({ length: 10 }, () => finalRate + (Math.random() - 0.5) * 5)
                : Array(10).fill(0)
          };
        });
        setRates(formatted);
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