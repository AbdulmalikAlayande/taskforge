delete from attachment;
delete from mention;
delete from comment;
delete from notification;
delete from task;
delete from invitation_roles;
delete from invitation;
delete from member_projects;
delete from project_members;
delete from member_roles;
delete from member;

--
-- drop table if exists attachment;
-- drop table if exists mention;
-- drop table if exists comment;
-- drop table if exists notification;
-- drop table if exists task;
-- drop table if exists invitation_roles;
-- drop table if exists invitation;
-- drop table if exists member_projects;
-- drop table if exists project_members;
-- drop table if exists member_roles;
-- drop table if exists member;
-- drop table if exists notification_preference;
-- drop table if exists project;

-- create table if not exists trade (
--     id        uuid primary key default gen_random_uuid(),
--     public_id uuid not null    default gen_random_uuid()
-- )