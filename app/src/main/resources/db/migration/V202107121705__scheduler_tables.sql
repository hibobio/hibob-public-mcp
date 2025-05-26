create table scheduler_job
(
    id            bigserial primary key not null,
    name          text                  not null,
    time          timestamp             not null,
    executor_name text                  not null,
    parameters    text,
    locked_by     text,
    locked_at     timestamp,
    failures      int,
    hijacks       int,
    priority      int
);

create unique index scheduler_job_name
    on scheduler_job (name);

create index scheduler_job_time
    on scheduler_job (time);

create table scheduler_executor
(
    id         bigserial primary key not null,
    uuid       text                  not null,
    last_alive timestamp             not null
);

create unique index scheduler_executor_uuid on scheduler_executor (uuid);

create table scheduler_history
(
    id             bigserial primary key not null,
    name           text                  not null,
    scheduled_time timestamp             not null,
    executor_name  text                  not null,
    parameters     text,
    executed_by    text                  not null,
    start_time     timestamp             not null,
    end_time       timestamp             not null,
    error          text
);

create index scheduler_history_name_idx on scheduler_history (name, start_time);

create index scheduler_history_time_idx on scheduler_history (start_time);
