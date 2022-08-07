alter table file_info
    add column with_header boolean;

alter table file_info
    add column ask_column_number bigint;

create sequence if not exists file_request_empty_require_column_seq;

create table file_request_empty_require_column
(
    id          bigint      not null,
    file_id     bigint      not null,
    "column"    varchar(30) not null,
    have_answer boolean,
    primary key (id),
    foreign key (file_id) references file_info (id)
);
