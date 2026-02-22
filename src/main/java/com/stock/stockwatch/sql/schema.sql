CREATE TABLE daily_asset_stats(
  id BIGSERIAL PRIMARY KEY,
  trade_date DATE NOT NULL,
  asset_type VARCHAR(10) NOT NULL,
  rank INTEGER NOT NULL,
  code VARCHAR(20) NOT NULL,
  current_price NUMERIC(20, 2),
  change_rate NUMERIC(10, 2),
  total_usd NUMERIC(20, 2),
  total_volume_krw NUMERIC(30, 2)
);

ALTER TABLE daily_asset_stats add column trade_type varchar(10) NOT NULL;
CREATE INDEX idx_daily_stats_search ON daily_asset_stats(trade_date, asset_type, trade_type);
Alter table daily_asset_stats ADD COLUMN name VARCHAR(100);

INSERT INTO daily_asset_stats (trade_date, asset_type, trade_type, rank, code, name, current_price, change_rate, total_volume_krw)
VALUES
    ('2024-05-22', 'KR', 'BUY', 1, '005930', '삼성전자', 72400, 2.1, 1842000000),
    ('2024-05-22', 'KR', 'SELL', 1, '000660', 'SK하이닉스', 190000, -1.5, 950000000);

CREATE TABLE asset_trade_events (
    id BIGSERIAL PRIMARY KEY,
    asset_type VARCHAR(10),
    code VARCHAR(20) NOT NULL,
    price NUMERIC(20, 2),
    quantity NUMERIC(25, 8),
    total_amount_krw NUMERIC(20, 2),
    trade_timestamp TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_asset_trade_events_code ON asset_trade_events(code);
CREATE INDEX idx_daily_asset_stats_date ON daily_asset_stats(trade_date);