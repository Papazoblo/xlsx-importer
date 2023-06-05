drop table parts_load_webhook;
alter table webhook_statistic
    add column part_load_id bigint;
alter table webhook_statistic
    add constraint webhook_part_load_fk
        foreign key (part_load_id) references parts_load (id);