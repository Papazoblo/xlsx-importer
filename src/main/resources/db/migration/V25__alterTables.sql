alter table file_info alter column name type varchar(500) using name::varchar(500);

alter table contact alter column region type varchar(500) using region::varchar(500);