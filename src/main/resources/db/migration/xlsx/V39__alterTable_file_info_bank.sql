alter table file_info_bank
    drop column inn_filter;
alter table file_info_bank
    drop column phone_filter;


alter table download_filter
    rename column name to code;
alter table download_filter
    rename column filter to value;
alter table download_filter
    add bank varchar(50);


