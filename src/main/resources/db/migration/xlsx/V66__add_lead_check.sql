create sequence if not exists lead_check_info_seq_id;
create table lead_check_info
(
    id                bigint      not null,
    inn               varchar(12) not null,
    request_id        bigint,
    file_info_id      bigint      not null,
    file_info_bank_id bigint      not null,
    check_status      varchar(50),
    create_at         timestamp   not null,
    primary key (id),
    foreign key (request_id) references opening_request (id),
    foreign key (file_info_id) references file_info (id)
);