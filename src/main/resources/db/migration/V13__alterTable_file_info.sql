alter table file_info
    add column source varchar(20);

update file_info
set source = 'TELEGRAM'
where 1 = 1;

alter table file_info
    add column processing_step varchar(30);

alter table file_info
    add column column_info text;