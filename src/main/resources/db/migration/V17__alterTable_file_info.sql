alter table file_info
    add column project_id varchar(20);
alter table file_info
    add column enable_whats_app_link boolean;
alter table file_info
    add column field_links text;
alter table file_info
    add column org_tags text;