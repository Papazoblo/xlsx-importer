create table system_variable
(
    setting_name  varchar(100) not null,
    setting_value text,
    primary key (setting_name)
);