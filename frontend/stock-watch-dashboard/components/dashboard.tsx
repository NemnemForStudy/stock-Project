"use client"

import { useState, useEffect } from "react"
import { Navbar } from "@/components/navbar"
import { AssetTable } from "@/components/asset-table"
import { StatusBar } from "@/components/status-bar"
import { ExchangeRateBar } from "@/components/exchange-rate-bar"
import { TradeModeToggle } from "@/components/trade-mode-toggle"
import {
  AssetEntry,
  type TradeMode,
} from "@/lib/stock-data"
import {index} from "d3-array";

export function Dashboard() {
  const [mode, setMode] = useState<TradeMode>("buy")
  const [searchQuery, setSearchQuery] = useState("")
  const [isDark, setIsDark] = useState(true)

  useEffect(() => {
    // 두 개의 useEffect를 하나로 합쳐 실행 순서 문제 원천 차단.
    const root = window.document.documentElement;

    if(isDark) {
      root.classList.add("dark")
    } else {
      root.classList.remove("dark")
    }
  }, [isDark]);

  // 실시간 국내 주식 데이터 담을 상태 추가
  const [realTimeKrData, setRealTimeKrData] = useState<{ buy: AssetEntry[], sell: AssetEntry[] }> ({
    buy: [],
    sell: []
  });

  useEffect(() => {
    const fetchKrData = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/stocks/kr/top10");
        const data = await response.json();

        const formattedData = (list: any[]) => (list || []).map((item: any, index: number) => ({
          rank: index + 1,
          code: item.code,
          name: item.name,
          price: item.price.toLocaleString(undefined, {
            minimumFractionDigits: 0,
            maximumFractionDigits: item.price >= 100 ? 2 : 8,
          }),
          change: Number((item.changeRate * 100).toFixed(2)),
          amount: item.volume.toLocaleString() + "주",
          changeType: item.changeType
        }));

        setRealTimeKrData({
          buy: formattedData(data.buy),
          sell: formattedData(data.sell)
        })
      } catch (error) {
        console.error('국내 주식 데이터 로드 실패:', error)
      }
    }

    fetchKrData()
    // 10초마다 갱신해 "실시간" 느낌을 줌.
    const timer = setInterval(fetchKrData, 1000)
    return () => clearInterval(timer)
  }, [])

  const [realTimeUSData, setRealTimeUsData] = useState<{ buy: AssetEntry[], sell: AssetEntry[] }> ({
    buy: [],
    sell: []
  });

  useEffect(() => {
    const fetchUsData = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/stocks/us/top10");
        const data = await response.json();
        console.log("data : " + JSON.stringify(data, null, 2));

        const formattedData = (list: any[]) => (list || []).map((item: any, index: number)=> ({
          rank: index + 1,
          code: item.code,
          name: item.name,
          price: item.price.toLocaleString(undefined, {
            minimumFractionDigits: 0,
            maximumFractionDigits: item.price >= 100 ? 2 : 8,
          }),
          change: Number((item.changeRate * 100).toFixed(2)),
          amount: item.volume >= 1_000_000
              ? `${(item.volume / 1_000_000).toFixed(1)}M` // 100만 주 이상은 M단위
              : `${Math.floor(item.volume / 1_000).toLocaleString()}K`,
          changeType: item.changeType
        }));

        setRealTimeUsData({
          buy: formattedData(data.buy),
          sell: formattedData(data.sell)
        })
      } catch (error) {
        console.error('미국 주식 데이터 로드 실패', error);
      }
    }
    fetchUsData()
    // 10초마다 갱신해 "실시간" 느낌을 줌.
    const timer = setInterval(fetchUsData, 1000)
    return () => clearInterval(timer)
  }, [])

  // 실시간 코인 데이터를 담을 상태 추가
  const [realTimeCoinData, setRealTimeCoinData] = useState<{ buy: AssetEntry[], sell: AssetEntry[] }>({
    buy: [],
    sell: []
  });

  useEffect(() => {
    // 백엔드에서 데이터 가져오는 함수
    const fetchUpbitData = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/upbit/top10");
        const data = await response.json();

        // AssetTable이 이해할 수 있는 형식으로 데이터 변환(Mapping)
        // 백엔드의 KoreanName -> name, tradePrice24h -> price
        const formattedData = (list: any[]) => (list || []).map((item: any, index: number) => ({
          rank: index + 1,
          code: item.code,
          name: item.name,
          price: item.price.toLocaleString(undefined, {
            minimumFractionDigits: 0,
            maximumFractionDigits: item.price >= 100 ? 2 : 8,
          }),
          change: Number((item.changeRate * 100).toFixed(2)),
          amount: `${Math.floor(item.volume / 100_000_000).toLocaleString()}억`,
          changeType: item.changeType
        }));

        setRealTimeCoinData({
          buy: formattedData(data.buy),
          sell: formattedData(data.sell)
        })
      } catch (error) {
        console.error('코인 데이터 로드 실패:', error)
      }
    }
    fetchUpbitData()
    // 10초마다 갱신해 "실시간" 느낌을 줌.
    const timer = setInterval(fetchUpbitData, 1000)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => {
    document.documentElement.classList.add("dark")
  }, [])

  const krData = mode === "buy" ? realTimeKrData.buy : realTimeKrData.sell
  const usData = mode === "buy" ? realTimeUSData.buy : realTimeUSData.sell

  // Buy 모드일 땐 상승/보합(RISE, EVEN), Sell 모드일 땐 하락(FALL)만 필터링
  const coinData = mode === "buy" ? realTimeCoinData.buy : realTimeCoinData.sell;

  return (
    <div className="flex h-dvh flex-col bg-background">
      <Navbar
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        isDark={isDark}
        onToggleDark={() => setIsDark((d) => !d)}
      />

      <TradeModeToggle mode={mode} onModeChange={setMode} />

      <main className="flex-1 overflow-y-auto">
        <ExchangeRateBar />

        <div className="grid grid-cols-1 gap-3 p-3 md:grid-cols-2 lg:grid-cols-3 lg:gap-4 lg:p-4">
          <AssetTable
            title="국내 주식"
            subtitle="KR STOCK / TOP 10"
            data={krData}
            mode={mode}
            currencyLabel="KRW"
            searchQuery={searchQuery}
          />
          <AssetTable
            title="미국 주식"
            subtitle="US STOCK / TOP 10"
            data={usData}
            mode={mode}
            currencyLabel="USD"
            searchQuery={searchQuery}
          />
          <AssetTable
            title="코인"
            subtitle="CRYPTO / TOP 10"
            data={coinData}
            mode={mode}
            currencyLabel="USD"
            searchQuery={searchQuery}
          />
        </div>
      </main>

      <StatusBar mode={mode} />
    </div>
  )
}
