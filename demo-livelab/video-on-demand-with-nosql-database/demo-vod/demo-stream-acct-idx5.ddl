CREATE INDEX idx_country_genre ON stream_acct(
    info.country as string,
    info.shows[].genres[] as string)
