# house_infos
insert into aptdb.house_infos (apt_seq, apt_nm, build_year, dong_cd, jibun, latitude, longitude, road_nm, sgg_cd, umd_cd, umd_nm)
select apt_seq, apt_nm, build_year, concat(sgg_cd, umd_cd), jibun, latitude, longitude, road_nm, sgg_cd, umd_cd, umd_nm
from ssafyhome.houseinfos;

# house_deals : 6409482
SELECT COUNT(*) AS total_rows
FROM house_deals;

insert into aptdb.house_deals (deal_amount, deal_day, deal_month, deal_year, exclu_use_ar, floor, apt_seq)
select deal_amount, deal_day, deal_month, deal_year, exclu_use_ar, floor, apt_seq
from aptdb.housedeals;

SET SQL_SAFE_UPDATES = 0;
delete from house_deals;

# apt_list table
CREATE TABLE house_summary (
    apt_seq BIGINT PRIMARY KEY,
    apt_nm VARCHAR(255),
    build_year YEAR,
    address VARCHAR(500),
    recent_deal_amount DECIMAL(10, 2),
    recent_deal_date DATE,
    exclusive_area DECIMAL(10, 2)
);

# recent deal table
CREATE TABLE recent_deals AS
SELECT 
	hd.deal_id,
    hd.apt_seq,
    hd.deal_amount,
    CONCAT(hd.deal_year, '-', LPAD(hd.deal_month, 2, '0'), '-', LPAD(hd.deal_day, 2, '0')) AS recent_deal_date,
    hd.exclu_use_ar,
    hd.floor
FROM 
    house_deals hd
JOIN (
    SELECT 
        apt_seq,
        MAX(CONCAT(deal_year, LPAD(deal_month, 2, '0'), LPAD(deal_day, 2, '0'))) AS max_date
    FROM 
        house_deals
    GROUP BY 
        apt_seq
) latest_deals
ON 
    hd.apt_seq = latest_deals.apt_seq
    AND CONCAT(hd.deal_year, LPAD(hd.deal_month, 2, '0'), LPAD(hd.deal_day, 2, '0')) = latest_deals.max_date;

-- 기본 키 추가
ALTER TABLE recent_deals
ADD PRIMARY KEY (deal_id);

-- 중복 제거
SELECT *
FROM recent_deals
WHERE deal_id NOT IN (
    SELECT deal_id
    FROM (
        SELECT 
            deal_id
        FROM 
            recent_deals
        WHERE deal_id IN (
            SELECT 
                MAX(deal_id) AS deal_id
            FROM (
                SELECT 
                    apt_seq,
                    recent_deal_date,
                    MAX(exclu_use_ar) AS max_exclu_use_ar,
                    MAX(floor) AS max_floor,
                    MAX(deal_id) AS deal_id
                FROM 
                    recent_deals
                GROUP BY 
                    apt_seq, recent_deal_date
            ) AS filtered_deals
            GROUP BY apt_seq, recent_deal_date
        )
    ) AS subquery
);

SET SQL_SAFE_UPDATES = 0;

DELETE FROM recent_deals
WHERE deal_id NOT IN (
    SELECT deal_id
    FROM (
        SELECT 
            deal_id
        FROM 
            recent_deals
        WHERE deal_id IN (
            SELECT 
                MAX(deal_id) AS deal_id
            FROM (
                SELECT 
                    apt_seq,
                    recent_deal_date,
                    MAX(exclu_use_ar) AS max_exclu_use_ar,
                    MAX(floor) AS max_floor,
                    MAX(deal_id) AS deal_id
                FROM 
                    recent_deals
                GROUP BY 
                    apt_seq, recent_deal_date
            ) AS filtered_deals
            GROUP BY apt_seq, recent_deal_date
        )
    ) AS subquery
);


# house summary
SELECT 
    hi.apt_seq,
    hi.apt_nm,
    hi.build_year,
    CONCAT(hi.umd_nm, ' ', hi.road_nm, ' ', hi.jibun) AS address,
    rd.deal_amount AS recent_deal_amount,
    rd.recent_deal_date,
    rd.exclu_use_ar AS exclusive_area,
    rd.floor AS recent_floor
FROM 
    house_infos hi
LEFT JOIN 
    recent_deals rd
ON 
    hi.apt_seq = rd.apt_seq;



# dong_code
insert into aptdb.dong_code (dong_cd, dong_name, gugun_name, sido_name)
select dong_code, dong_name, gugun_name, sido_name
from ssafyhome.dongcodes;

# sido_code
insert into aptdb.sido_code 
select *
from ssafyvue.sidocode;

# gugun_code
insert into aptdb.gugun_code 
select *
from ssafyvue.guguncode;




########################
# indexing
CREATE INDEX idx_house_deals_date ON house_deals (apt_seq, deal_year, deal_month, deal_day);

SELECT 
    hd.apt_seq,
    hd.deal_amount,
    CONCAT(hd.deal_year, '-', LPAD(hd.deal_month, 2, '0'), '-', LPAD(hd.deal_day, 2, '0')) AS recent_deal_date,
    hd.exclu_use_ar,
    hd.floor
FROM 
    house_deals hd
JOIN (
    SELECT 
        apt_seq,
        MAX(CONCAT(deal_year, LPAD(deal_month, 2, '0'), LPAD(deal_day, 2, '0'))) AS max_date
    FROM 
        house_deals
    GROUP BY 
        apt_seq
) latest_deals
ON 
    hd.apt_seq = latest_deals.apt_seq
    AND CONCAT(hd.deal_year, LPAD(hd.deal_month, 2, '0'), LPAD(hd.deal_day, 2, '0')) = latest_deals.max_date;

