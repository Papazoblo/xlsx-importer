alter table webhook_statistic
alter column phone type varchar(50) using phone::varchar(50);

