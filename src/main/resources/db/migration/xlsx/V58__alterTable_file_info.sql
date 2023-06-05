alter table file_info
    drop column set_current_date;
alter table file_info
    add column action_type varchar(50);