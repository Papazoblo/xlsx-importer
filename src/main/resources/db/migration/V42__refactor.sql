alter table contact
    drop column opening_request_id;

drop table contact_file_info;

alter table public.contact
    drop column file_info_bank_id;