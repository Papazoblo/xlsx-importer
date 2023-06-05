insert into system_variable(setting_name, setting_value)
values ('CHECK_LIMIT_ALFA', '38000');

create table bank_count_sended_request
(
    bank                  varchar(50) not null,
    crete_request         bigint      not null default 0,
    result_create_request bigint      not null default 0,
    check_request         bigint      not null default 0,
    result_check_request  bigint      not null default 0,
    create_at             timestamp   not null,
    primary key (bank, create_at)
);