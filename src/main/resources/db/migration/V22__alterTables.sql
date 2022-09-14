alter table contact alter column org_name type varchar(1000) using org_name::varchar(1000);
alter table contact alter column address type varchar(1000) using address::varchar(1000);

update file_info
set processing_step = 'IN_QUEUE'
where processing_step = 'DOWNLOADED';