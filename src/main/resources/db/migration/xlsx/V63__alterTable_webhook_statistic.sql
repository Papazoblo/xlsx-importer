alter table webhook_statistic
    add column region_id bigint;
alter table webhook_statistic
    add constraint webhook_region_fk
        foreign key (region_id) references region (id);

alter table webhook_statistic
    add column contact_new_id bigint;
alter table webhook_statistic
    add constraint webhook_contact_fk
        foreign key (contact_new_id) references contact_new (id);

alter table webhook_statistic
    add column city_id bigint;
alter table webhook_statistic
    add constraint webhook_city_fk
        foreign key (city_id) references city (id);