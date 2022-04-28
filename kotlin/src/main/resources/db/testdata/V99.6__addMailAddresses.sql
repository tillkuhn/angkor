-- user system should have an email address so we can use it for mock greenmail testing

UPDATE app_user set email='system@localhost' where id='00000000-0000-0000-0000-000000000001'::uuid;
