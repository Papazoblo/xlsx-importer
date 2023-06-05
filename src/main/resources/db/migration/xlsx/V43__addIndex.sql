create index contact_new_inn_index on contact_new (inn);

create index webhook_statistic_status_index on webhook_statistic (status);

create index opening_request_status_index on opening_request (status);

create index file_info_status_processing_step_source_index on file_info (status, source, processing_step);

create index file_info_bank_download_status_index on file_info_bank (download_status);

create index contact_download_info_request_id_index on contact_download_info (request_id);