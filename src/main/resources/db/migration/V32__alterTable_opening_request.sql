alter table opening_request
    add column retry_request_count integer default 0;

