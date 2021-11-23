-- Public Tour
INSERT INTO public.location (id, etype, external_id, name, summary, notes, primary_url, image_url, area_code,
                             coordinates, tags, auth_scope, created_at, updated_at, created_by, updated_by, version,
                             properties, been_there, ltype)
VALUES ('e4c92eec-9d4f-4a00-a5e7-8e5f0a849fc0',
        'Tour',
        '486328584',
        '🌞 Tour de Test', null, null,
        'https://www.tour.de/tour/12345',
        'https://photos.tour.de/www/maps/12345.jpg',
        null,
        '{99.44395200000000000,88.63230200000000000}',
        '{hike}',
        'PUBLIC',
        '2021-10-04 17:11:04.301566',
        '2021-10-04 17:11:04.301566',
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001', 0,
        'alt => 342',
        '2021-09-12',
        'PLACE');

-- Public Video
INSERT INTO public.location (id, etype, external_id, name, summary, notes, primary_url, image_url, area_code,
                             coordinates, tags, auth_scope, created_at, updated_at, created_by, updated_by, version,
                             properties, been_there, ltype)
VALUES ('e4c92eec-9d4f-4a00-a5e7-8e5f0a849fc1',
        'Video',
        '486328584',
        '🌞 Video de Test', null, null,
        'https://www.tour.de/tour/12345',
        'https://photos.tour.de/www/maps/12345.jpg',
        null,
        '{99.44395200000000000,88.63230200000000000}',
        '{hike}',
        'PUBLIC',
        '2021-10-04 17:11:04.301566',
        '2021-10-04 17:11:04.301566',
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001', 0,
        'alt => 342',
        '2021-09-12',
        'PLACE');

-- Restricted Tour
INSERT INTO public.location (id, etype, external_id, name, summary, notes, primary_url, image_url, area_code,
                             coordinates, tags, auth_scope, created_at, updated_at, created_by, updated_by, version,
                             properties, been_there, ltype)
VALUES ('f4c92ffc-9d4f-4a00-a5e7-8e5f0a849fc9',
        'Tour',
        '486328599',
        '🌞 Private Tour de Test (Restricted)', null, null,
        'https://www.tour.de/tour/999999',
        'https://photos.tour.de/www/maps/999999.jpg',
        null,
        '{11.44395200000000000,44.63230200000000000}',
        '{hike}',
        'RESTRICTED',
        '2021-10-04 17:11:04.301566',
        '2021-10-04 17:11:04.301566',
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001', 0,
        'alt => 342',
        '2021-09-12',
        'PLACE');

-- Public Post
INSERT INTO public.location (id, etype, external_id, name, summary, notes, primary_url, image_url, area_code,
                             coordinates, tags, auth_scope, created_at, updated_at, created_by, updated_by, version,
                             properties, been_there, ltype, rating)
VALUES ('67000b89-5cd2-4a03-8c87-8e1b9dd3bd3d', 'Post', 'https://testxxx.wordpress.com/?p=9733',
        'Marina di Alberese Test', null, null,
        'https://testxxx.wordpress.com/2021/01/17/maremma/', null, null, '{99.10701900000000000,88.66588800000000000}',
        '{wp}', 'PUBLIC', '2021-11-05 19:34:37.501695', '2021-11-05 19:34:37.501695',
        '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 0, '', null, 'PLACE', 0);

-- Public Place
INSERT INTO public.location (id, name, summary, notes, area_code, primary_url, image_url, tags, auth_scope, created_at,
                             updated_at, created_by, updated_by, ltype, coordinates, been_there, etype)
VALUES ('362df904-6c53-4726-ab02-3bd62d99dccd', 'Bamboo Delight Cooking School and Restaurant',
        'Hosting food-loving visitors since March 2013',
        'Our cooking school has been hosting food-loving visitors since 2013',
        'mm', 'https://bamboodelight.wordpress.com/',
        '/imagine/places/362df904-6c53-4726-ab02-3bd62d99dccd/food.jpg?large', '{cooking,myanmar}',
        'PUBLIC',
        '2020-12-05 09:49:14.015500', '2021-02-17 20:40:49.583806', '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001', 'BARFOOD', '{96.94126160000000000,20.65587820000000000}', null,
        'Place');

-- Public Photo
INSERT INTO public.location (id, etype, external_id, name, summary, notes, primary_url, image_url, area_code,
                             coordinates, tags, auth_scope, created_at, updated_at, created_by, updated_by, version,
                             properties, been_there, ltype, rating, geo_address)
VALUES ('5f22bbe5-d775-4274-adcc-041171302ad9', 'Photo', 'https://999px.com/photo/255192249/Easy-Lion--by-Photo-Graph',
        'Easy, Lion! 🇲🇲', null, null, 'https://999px.com/photo/255192249/Easy-Tiger--by-Photo-Graph',
        'https://drscdn.999px.org/photo/255192249/q%3D50_h%3D450/v2?sig=something',
        'mm', '{95.95622300000000000,21.91396500000000000}', '{}', 'PUBLIC', '2021-11-23 20:14:01.507723',
        '2021-11-23 20:36:13.052120', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 1,
        '', null, 'PLACE', 0, null);
