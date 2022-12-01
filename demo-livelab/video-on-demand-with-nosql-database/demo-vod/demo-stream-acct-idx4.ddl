CREATE INDEX idx_country_showid_date ON stream_acct(
    info.country as string,
    info.shows[].showId as integer,
    info.shows[].seriesInfo[].episodes[].date as string)
