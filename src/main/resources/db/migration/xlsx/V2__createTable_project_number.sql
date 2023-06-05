create sequence if not exists project_number_seq_id;

CREATE TABLE project_number
(
    id     bigint      NOT NULL,
    number varchar(20) NOT NULL,
    date   timestamp   NOT NULL,
    primary key (id)
);