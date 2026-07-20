
INSERT INTO categories (category_name) VALUES
                                           ('Comedy'), ('Horror'), ('Biography'), ('Adventure'), ('Romance'),
                                           ('Mystery'), ('Crime'), ('Family'), ('Drama'), ('History'),
                                           ('Animation'), ('SciFi-Movies'), ('Thriller-Movies'), ('Comic-Series');


INSERT INTO words (word, category_id, plot_hint, imdb_id, popularity_votes)
VALUES
    ('Ferdinand', 11, NULL, 'tt3411444', 71588),
    ('Hamilton', 3, NULL, 'tt8503618', 140444),
    ('TheLastEmperor', 3, NULL, 'tt0093389', 120076),
    ('FightingwithMyFamily', 3, NULL, 'tt6513120', 93407),
    ('WhatWomenWant', 1, NULL, 'tt0207201', 239228),
    ('ManchesterbytheSea', 9, NULL, 'tt4034228', 358844),
    ('CabinFever', 2, NULL, 'tt0303816', 89073),
    ('EuropaReport', 6, NULL, 'tt2051879', 79676),
    ('VanWilder', 5, NULL, 'tt0283111', 120942),
    ('TheGame', 13, NULL, 'tt0119174', 473308);

