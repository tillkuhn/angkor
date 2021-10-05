-- New Service Users

INSERT INTO app_user (id,login,first_name,last_name,name,emoji)
VALUES ('00000000-0000-0000-0000-000000000002','eventservice','Event','Service','Event Service Account','🎫');
INSERT INTO app_user (id,login,first_name,last_name,name,emoji)
VALUES ('00000000-0000-0000-0000-000000000003','tourservice','Tour','Service','Tour Service Account','🗺️');
INSERT INTO app_user (id,login,first_name,last_name,name,emoji)
VALUES ('00000000-0000-0000-0000-000000000004','imagine','Imagine','All The People','Imagine Service Account','🎨');
INSERT INTO app_user (id,login,first_name,last_name,name,emoji)
VALUES ('00000000-0000-0000-0000-000000000005','healthbells','Health','Bells','Healthbells Service Account','❤️');
INSERT INTO app_user (id,login,first_name,last_name,name,emoji)
VALUES ('00000000-0000-0000-0000-000000000006','polly','Polly','Poller','Polly Service Account','🐔️');
-- remindabot already exists
UPDATE app_user SET id='00000000-0000-0000-0000-000000000007' where name = 'remindabot';
INSERT INTO app_user (id,login,first_name,last_name,name,emoji)
VALUES ('00000000-0000-0000-0000-000000000008','appctl','Application','Control','AppControl Service Account','🛂');

