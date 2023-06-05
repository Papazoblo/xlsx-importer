create sequence if not exists auto_link_xlsx_field_seq_id;
create table auto_link_xlsx_field
(
    id            bigint      not null,
    field         varchar(30) not null,
    column_number integer     not null,
    primary key (id)
);

create sequence if not exists enabled_scenario_seq_id;
create table enabled_scenario
(
    id          bigint      not null,
    scenario_id bigint not null,
    bank_name   varchar(30),
    primary key (id)
);