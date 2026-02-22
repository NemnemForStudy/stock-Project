export type TradeMode = "buy" | "sell"

export interface AssetEntry {
  rank: number
  name: string
  code: string
  price: string
  amount: string
  change: number
  changeType: string
}

// export const krStockBuyData: AssetEntry[] = [
//   { rank: 1, name: "삼성전자", code: "005930", price: "72,400", amount: "1,842억", change: 2.1 },
//   { rank: 2, name: "SK하이닉스", code: "000660", price: "198,500", amount: "1,523억", change: 3.4 },
//   { rank: 3, name: "LG에너지솔루션", code: "373220", price: "385,000", amount: "982억", change: -1.2 },
//   { rank: 4, name: "현대차", code: "005380", price: "245,000", amount: "876억", change: 1.8 },
//   { rank: 5, name: "셀트리온", code: "068270", price: "178,500", amount: "743억", change: 0.5 },
//   { rank: 6, name: "NAVER", code: "035420", price: "215,500", amount: "698억", change: -0.8 },
//   { rank: 7, name: "카카오", code: "035720", price: "52,300", amount: "612억", change: 1.3 },
//   { rank: 8, name: "포스코홀딩스", code: "005490", price: "312,000", amount: "587억", change: -2.1 },
//   { rank: 9, name: "삼성바이오로직스", code: "207940", price: "892,000", amount: "534억", change: 0.9 },
//   { rank: 10, name: "기아", code: "000270", price: "98,700", amount: "489억", change: 2.6 },
// ]
//
// export const krStockSellData: AssetEntry[] = [
//   { rank: 1, name: "한화에어로스페이스", code: "012450", price: "285,500", amount: "1,245억", change: -3.2 },
//   { rank: 2, name: "두산에너빌리티", code: "034020", price: "18,950", amount: "1,087억", change: -2.8 },
//   { rank: 3, name: "에코프로비엠", code: "247540", price: "162,300", amount: "923억", change: -4.1 },
//   { rank: 4, name: "LG화학", code: "051910", price: "345,000", amount: "812억", change: -1.5 },
//   { rank: 5, name: "삼성SDI", code: "006400", price: "412,500", amount: "756억", change: -2.3 },
//   { rank: 6, name: "KB금융", code: "105560", price: "78,200", amount: "698억", change: 0.3 },
//   { rank: 7, name: "신한지주", code: "055550", price: "52,100", amount: "623억", change: -0.7 },
//   { rank: 8, name: "삼성물산", code: "028260", price: "135,500", amount: "567억", change: -1.9 },
//   { rank: 9, name: "HD현대중공업", code: "329180", price: "178,000", amount: "512억", change: -3.5 },
//   { rank: 10, name: "SK이노베이션", code: "096770", price: "112,500", amount: "478억", change: -0.4 },
// ]
//
// export const usStockBuyData: AssetEntry[] = [
//   { rank: 1, name: "NVIDIA", code: "NVDA", price: "$142.50", amount: "$2.8B", change: 4.2 },
//   { rank: 2, name: "Apple", code: "AAPL", price: "$198.30", amount: "$2.1B", change: 1.5 },
//   { rank: 3, name: "Microsoft", code: "MSFT", price: "$425.80", amount: "$1.9B", change: 2.3 },
//   { rank: 4, name: "Tesla", code: "TSLA", price: "$245.60", amount: "$1.7B", change: 5.1 },
//   { rank: 5, name: "Amazon", code: "AMZN", price: "$189.40", amount: "$1.4B", change: 1.8 },
//   { rank: 6, name: "Meta", code: "META", price: "$512.30", amount: "$1.2B", change: 3.2 },
//   { rank: 7, name: "Alphabet", code: "GOOGL", price: "$175.90", amount: "$1.1B", change: 0.9 },
//   { rank: 8, name: "AMD", code: "AMD", price: "$168.20", amount: "$980M", change: 2.7 },
//   { rank: 9, name: "Broadcom", code: "AVGO", price: "$178.50", amount: "$870M", change: 1.4 },
//   { rank: 10, name: "Palantir", code: "PLTR", price: "$78.40", amount: "$760M", change: 6.8 },
// ]
//
// export const usStockSellData: AssetEntry[] = [
//   { rank: 1, name: "Intel", code: "INTC", price: "$24.80", amount: "$1.5B", change: -5.2 },
//   { rank: 2, name: "Pfizer", code: "PFE", price: "$28.90", amount: "$1.2B", change: -2.8 },
//   { rank: 3, name: "Disney", code: "DIS", price: "$98.50", amount: "$1.1B", change: -1.4 },
//   { rank: 4, name: "Nike", code: "NKE", price: "$76.30", amount: "$890M", change: -3.1 },
//   { rank: 5, name: "Boeing", code: "BA", price: "$185.60", amount: "$780M", change: -2.5 },
//   { rank: 6, name: "Starbucks", code: "SBUX", price: "$92.40", amount: "$650M", change: -1.8 },
//   { rank: 7, name: "PayPal", code: "PYPL", price: "$68.90", amount: "$580M", change: -0.9 },
//   { rank: 8, name: "Snap", code: "SNAP", price: "$12.30", amount: "$520M", change: -4.3 },
//   { rank: 9, name: "Roku", code: "ROKU", price: "$72.80", amount: "$460M", change: -2.1 },
//   { rank: 10, name: "Rivian", code: "RIVN", price: "$14.50", amount: "$390M", change: -6.7 },
// ]
//
// export const coinBuyData: AssetEntry[] = [
//   { rank: 1, name: "Bitcoin", code: "BTC", price: "$98,420", amount: "$4.2B", change: 3.8 },
//   { rank: 2, name: "Ethereum", code: "ETH", price: "$3,520", amount: "$2.8B", change: 5.2 },
//   { rank: 3, name: "Solana", code: "SOL", price: "$198.40", amount: "$1.5B", change: 8.1 },
//   { rank: 4, name: "XRP", code: "XRP", price: "$2.45", amount: "$1.2B", change: 4.5 },
//   { rank: 5, name: "Cardano", code: "ADA", price: "$0.98", amount: "$890M", change: 6.3 },
//   { rank: 6, name: "Avalanche", code: "AVAX", price: "$42.30", amount: "$720M", change: 7.2 },
//   { rank: 7, name: "Chainlink", code: "LINK", price: "$18.90", amount: "$650M", change: 3.1 },
//   { rank: 8, name: "Polkadot", code: "DOT", price: "$8.45", amount: "$540M", change: 2.8 },
//   { rank: 9, name: "Dogecoin", code: "DOGE", price: "$0.182", amount: "$480M", change: 12.5 },
//   { rank: 10, name: "Sui", code: "SUI", price: "$4.12", amount: "$420M", change: 9.8 },
// ]
//
// export const coinSellData: AssetEntry[] = [
//   { rank: 1, name: "Shiba Inu", code: "SHIB", price: "$0.0000245", amount: "$1.8B", change: -8.5 },
//   { rank: 2, name: "Aptos", code: "APT", price: "$9.80", amount: "$920M", change: -5.3 },
//   { rank: 3, name: "Arbitrum", code: "ARB", price: "$1.23", amount: "$780M", change: -4.2 },
//   { rank: 4, name: "Optimism", code: "OP", price: "$2.15", amount: "$650M", change: -3.8 },
//   { rank: 5, name: "Filecoin", code: "FIL", price: "$6.80", amount: "$540M", change: -2.9 },
//   { rank: 6, name: "NEAR", code: "NEAR", price: "$5.45", amount: "$480M", change: -1.5 },
//   { rank: 7, name: "Cosmos", code: "ATOM", price: "$9.20", amount: "$420M", change: -3.1 },
//   { rank: 8, name: "Algorand", code: "ALGO", price: "$0.32", amount: "$380M", change: -2.4 },
//   { rank: 9, name: "Internet Computer", code: "ICP", price: "$12.50", amount: "$340M", change: -6.1 },
//   { rank: 10, name: "Toncoin", code: "TON", price: "$5.80", amount: "$290M", change: -1.8 },
// ]
