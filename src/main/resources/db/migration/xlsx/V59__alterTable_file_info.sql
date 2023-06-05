alter table contact_new
    add column file_info_id bigint;

alter table contact_new
    add constraint contact_file_info_fk
        foreign key (file_info_id) references file_info (id);