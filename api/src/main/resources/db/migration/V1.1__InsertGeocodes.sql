-- * Bangkok Latitude and longitude coordinates are: 13.736717, 100.523186.
-- * @13.7244416,100.3529157 Bangkok **(Lat, Lon)!!!**
-- * geojson coordinates = [100.523186, 13.736717]  **(Lon,Lat)!!!**

INSERT INTO geocode (code,parent_code,name,level) values ('eu','Europe','earth','planet');
INSERT INTO geocode (code,parent_code,name,level) values ('asia-se','asia','Southeast asia','continent_section');
INSERT INTO geocode (code,parent_code,name,level) values ('asia','earth','Asia','continent');
INSERT INTO geocode (code,parent_code,name,level) values ('eu-sc','eu','Scandinavia','continent_section');
INSERT INTO geocode (code,parent_code,name,level) values ('de','eu','Germany','country');
INSERT INTO geocode (code,parent_code,name,level) values ('se','eu-sc','Sweden','country');
INSERT INTO geocode (code,parent_code,name,level) values ('it','eu','Italy','country');
INSERT INTO geocode (code,parent_code,name,level) values ('gr','eu','Greece','country');
INSERT INTO geocode (code,parent_code,name,level) values ('th','asia-se','Thailand','country');
INSERT INTO geocode (code,parent_code,name,level) values ('my','asia-se','Myanmar','country');
INSERT INTO geocode (code,parent_code,name,level) values ('kh','asia-se','Cambodia','country'   );
