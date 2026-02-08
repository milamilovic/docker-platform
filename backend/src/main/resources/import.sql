-- =========================
-- USERS (2 ADMIN USERS)
-- =========================
insert into users (id, username, password, email, role, badge) values ('11111111-1111-1111-1111-111111111111', 'admin1', '$2a$10$XvKh04Pz4jUmYIbpD3YqfugLjcE.y4JcHF0Z1Rza1b2R60oxGV0te', 'admin1@docker.dev', 'ADMIN', 'DOCKER_OFFICIAL_IMAGE');
insert into users (id, username, password, email, role, badge) values ('22222222-2222-2222-2222-222222222222', 'admin2', '$2a$10$XvKh04Pz4jUmYIbpD3YqfugLjcE.y4JcHF0Z1Rza1b2R60oxGV0te', 'admin2@docker.dev', 'ADMIN', 'DOCKER_OFFICIAL_IMAGE');
insert into users (id, username, password, email, role, badge) values ('33333333-3333-3333-3333-333333333333', 'user', '$2a$10$XvKh04Pz4jUmYIbpD3YqfugLjcE.y4JcHF0Z1Rza1b2R60oxGV0te', 'user@docker.dev', 'REGULAR', null);

-- =========================
-- REPOSITORIES (10 TOTAL)
-- =========================
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('aaaaaaaa-0001-0000-0000-000000000001', 'nginx', '11111111-1111-1111-1111-111111111111', 'admin1', 'Official NGINX image', 1700000000000, 1700000005000, 1200000, 8500, true, false, 'SPONSORED_OSS');
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('aaaaaaaa-0002-0000-0000-000000000002', 'redis', '11111111-1111-1111-1111-111111111111', 'admin1', 'Redis in-memory store', 1700000000000, 1700000006000, 980000, 7200, true, false, 'SPONSORED_OSS');
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('aaaaaaaa-0003-0000-0000-000000000003', 'postgres', '11111111-1111-1111-1111-111111111111', 'admin1', 'PostgreSQL database', 1700000000000, 1700000007000, 870000, 6900, true, false, 'SPONSORED_OSS');
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('aaaaaaaa-0004-0000-0000-000000000004', 'rabbitmq', '11111111-1111-1111-1111-111111111111', 'admin1', 'RabbitMQ broker', 1700000000000, 1700000008000, 450000, 3900, true, false, 'SPONSORED_OSS');
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('aaaaaaaa-0005-0000-0000-000000000005', 'elasticsearch', '11111111-1111-1111-1111-111111111111', 'admin1', 'Elastic search engine', 1700000000000, 1700000009000, 620000, 5100, true, false, 'VERIFIED_PUBLISHER');
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('bbbbbbbb-0001-0000-0000-000000000001', 'node', '22222222-2222-2222-2222-222222222222', 'admin2', 'Node.js runtime', 1700000000000, 1700000010000, 1100000, 8100, true, true, 'VERIFIED_PUBLISHER');
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('bbbbbbbb-0002-0000-0000-000000000002', 'python2', '22222222-2222-2222-2222-222222222222', 'admin2', 'Python 2 base image', 1700000000000, 1700000011000, 105000, 456, true, true, 'DOCKER_OFFICIAL_IMAGE');
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('bbbbbbbb-0002-0000-0000-000000000006', 'python3', '22222222-2222-2222-2222-222222222222', 'admin2', 'Python 3 base image', 1700000000000, 1700000011000, 2230000, 652, true, true, 'DOCKER_OFFICIAL_IMAGE');
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('bbbbbbbb-0003-0000-0000-000000000003', 'openjdk', '22222222-2222-2222-2222-222222222222', 'admin2', 'OpenJDK runtime', 1700000000000, 1700000012000, 910000, 6400, true, true, 'DOCKER_OFFICIAL_IMAGE');
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('bbbbbbbb-0004-0000-0000-000000000004', 'alpine', '22222222-2222-2222-2222-222222222222', 'admin2', 'Minimal Alpine Linux', 1700000000000, 1700000013000, 1400000, 9200, true, true, 'DOCKER_OFFICIAL_IMAGE');
insert into repository (id, name, owner_id, owner_username, description, created_at, modified_at, number_of_pulls, number_of_stars, is_public, is_official, badge) values ('bbbbbbbb-0005-0000-0000-000000000005', 'busybox', '22222222-2222-2222-2222-222222222222', 'admin2', 'BusyBox utilities', 1700000000000, 1700000014000, 500000, 4100, true, false, 'DOCKER_OFFICIAL_IMAGE');

-- =========================
-- TAGS (2–8 PER REPO)
-- =========================
-- nginx
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111111', 'latest', 'sha256:nginx1', 28000000, 1700000000000, 1700000100000, 'aaaaaaaa-0001-0000-0000-000000000001');
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111112', '1.25', 'sha256:nginx2', 27900000, 1700000000000, 1700000200000, 'aaaaaaaa-0001-0000-0000-000000000001');
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111113', '1.24', 'sha256:nginx3', 27800000, 1700000000000, 1700000300000, 'aaaaaaaa-0001-0000-0000-000000000001');

-- redis
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111114', 'latest', 'sha256:redis1', 32000000, 1700000000000, 1700000100000, 'aaaaaaaa-0002-0000-0000-000000000002');
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111115', '7.2', 'sha256:redis2', 31800000, 1700000000000, 1700000200000, 'aaaaaaaa-0002-0000-0000-000000000002');

-- postgres
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111116', 'latest', 'sha256:pg1', 210000000, 1700000000000, 1700000100000, 'aaaaaaaa-0003-0000-0000-000000000003');
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111117', '16', 'sha256:pg2', 208000000, 1700000000000, 1700000200000, 'aaaaaaaa-0003-0000-0000-000000000003');
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111118', '15', 'sha256:pg3', 205000000, 1700000000000, 1700000300000, 'aaaaaaaa-0003-0000-0000-000000000003');

-- node
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111119', 'latest', 'sha256:node1', 180000000, 1700000000000, 1700000100000, 'bbbbbbbb-0001-0000-0000-000000000001');
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111120', '20', 'sha256:node2', 178000000, 1700000000000, 1700000200000, 'bbbbbbbb-0001-0000-0000-000000000001');
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111121', '18', 'sha256:node3', 175000000, 1700000000000, 1700000300000, 'bbbbbbbb-0001-0000-0000-000000000001');

-- alpine
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111122', 'latest', 'sha256:alp1', 7500000, 1700000000000, 1700000100000, 'bbbbbbbb-0004-0000-0000-000000000004');
insert into tag (id, name, digest, size, created_at, pushed_at, repository_id) values ('11111111-1111-1111-1111-111111111123', '3.19', 'sha256:alp2', 7400000, 1700000000000, 1700000200000, 'bbbbbbbb-0004-0000-0000-000000000004');

