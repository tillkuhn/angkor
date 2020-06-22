-- * Bangkok Latitude and longitude coordinates are: 13.736717, 100.523186.
-- * @13.7244416,100.3529157 Bangkok **(Lat, Lon)!!!**
-- * geojson coordinates = [100.523186, 13.736717]  **(Lon,Lat)!!!**

INSERT INTO geocode (code,parent_code,name) values ('eu','Europe','earth');
INSERT INTO geocode (code,parent_code,name) values ('se-asia','asia','Southeast asia');
INSERT INTO geocode (code,parent_code,name) values ('asia','earth','Earth');
INSERT INTO geocode (code,parent_code,name) values ('de','eu','Germany');
INSERT INTO geocode (code,parent_code,name) values ('it','eu','Italy');
INSERT INTO geocode (code,parent_code,name) values ('gr','eu','Greece');
INSERT INTO geocode (code,parent_code,name) values ('th','se-asia','Thailand');
INSERT INTO geocode (code,parent_code,name) values ('my','se-asia','Myanmar');
INSERT INTO geocode (code,parent_code,name) values ('kh','se-asia','Cambodia');
