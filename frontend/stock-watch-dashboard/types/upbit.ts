// 백엔드 UpbitMarketDto와 동일한 구조임.
export interface UpbitCoin {
    market: string;
    koreanName: string;
    tradePrice24h: number;
}