alter table contact
    drop column check_status;

alter table opening_request
    add column last_check timestamp;