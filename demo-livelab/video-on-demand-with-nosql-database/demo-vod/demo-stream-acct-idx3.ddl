CREATE INDEX IF NOT EXISTS idx_showid_seasonNum_minWatched ON stream_acct(
    info.shows[].showId as INTEGER,
    info.shows[].seriesInfo[].seasonNum as INTEGER,
    info.shows[].seriesInfo[].episodes[].minWatched as INTEGER,
    info.shows[].seriesInfo[].episodes[].episodeID as INTEGER)
    WITH UNIQUE KEYS PER ROW
