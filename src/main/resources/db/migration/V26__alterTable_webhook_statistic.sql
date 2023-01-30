alter table webhook_statistic
    add column full_name varchar(500);

alter table webhook_statistic
    add column comment_text varchar(1000);