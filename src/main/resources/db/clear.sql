delete from invitation;

delete from project_members
where project_id in (
    select id from project
);
delete from project;

delete from member;

drop table if exists attachment;
drop table if exists comment;
drop table if exists notification;
drop table if exists task;
drop table if exists invitation;
drop table if exists member_projects;
drop table if exists project_members;
drop table if exists member;
drop table if exists project;

-- create table if not exists trade (
--     id        uuid primary key default gen_random_uuid(),
--     public_id uuid not null    default gen_random_uuid()
-- )