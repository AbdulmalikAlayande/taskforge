-- ORGANIZATION --
-- ALTER TABLE organization
-- ADD CONSTRAINT organization_name_slug_unique UNIQUE (name, slug);
--
-- INSERT INTO organization (
--     id, public_id, deleted, version, name, slug, description, industry, country, time_zone, contact_email, contact_phone, logo_url, website_url
-- ) VALUES (
--     gen_random_uuid(),
--     gen_random_uuid(),
--     false,
--     0,
--     'Acme Corp',
--     'acme-corp',
--     'A leading provider of widgets.',
--     'Manufacturing',
--     'USA',
--     'America/New_York',
--     'info@acme.com',
--     '+1234567890',
--     'https://acme.com/logo.png',
--     'https://acme.com'
-- )
-- ON CONFLICT (name, slug) DO NOTHING
-- RETURNING *;
--
-- INSERT INTO organization (
--     id, public_id, deleted, version, name, slug, description, industry, country, time_zone, contact_email, contact_phone, logo_url, website_url
-- ) VALUES (
--     gen_random_uuid(),
--     gen_random_uuid(),
--     false,
--     0,
--     'Beta LLC',
--     'beta-llc',
--     'A startup focused on innovative solutions.',
--     'Technology',
--     'Canada',
--     'America/Toronto',
--     'info@betallc.com',
--     '+1987654321',
--     'https://betallc.com/logo.png',
--     'https://betallc.com'
-- )
-- ON CONFLICT (name, slug) DO NOTHING
-- RETURNING *;
--
-- INSERT INTO organization (
--     id, public_id, deleted, version, name, slug, description, industry, country, time_zone, contact_email, contact_phone, logo_url, website_url
-- ) VALUES (gen_random_uuid(),
--           gen_random_uuid(),
--           false,
--           0,
--           'Gamma Inc',
--           'gamma-inc',
--           'A global leader in tech solutions.',
--           'Information Technology',
--           'UK',
--           'Europe/London',
--           'info@gammainc.com',
--           '+441234567890',
--           'https://gammainc.com/logo.png',
--           'https://gammainc.com')
--   ON CONFLICT (name, slug) DO NOTHING
--   RETURNING *;
--
-- INSERT INTO organization (
--     id, public_id, deleted, version, name, slug, description, industry, country, time_zone, contact_email, contact_phone, logo_url, website_url
-- ) VALUES (
--           gen_random_uuid(),
--           gen_random_uuid(),
--           false,
--           0,
--           'Blue Corp',
--           'blue-cap-corp',
--           'A bluey film corporation',
--           'Entertainment',
--           'Nigeria',
--           'Africa/Lagos',
--           'info@bluecorp.com',
--           '+23470000123',
--           'https://bluecorp.ng',
--           'https://bluecorp.ng/logo.jpg'
--          )
--   ON CONFLICT (name, slug) DO NOTHING
--   RETURNING *;

-- PROJECT --
INSERT INTO project (
     archived, version, deleted, end_date, start_date, created_at, last_modified_at, version, category, created_by, description, id, modified_by, name, organization_id, public_id, status
) VALUES (
        true,
          0,
          false,
          null,
          null,
          null,
          null,
          1,
          'OTHER',
          null,
          'A new Project',
          gen_random_uuid(),
          null,
          'Carmet',
          '742458c2-fd39-46e7-a7b8-319ef3f02564',
          gen_random_uuid(),
          'ACTIVE'
    )