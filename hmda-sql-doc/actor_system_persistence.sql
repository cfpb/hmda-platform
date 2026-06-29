CREATE TABLE IF NOT EXISTS event_journal(
 slice INT NOT NULL,
 entity_type VARCHAR(255) NOT NULL,
 persistence_id VARCHAR(255) NOT NULL,
 seq_nr BIGINT NOT NULL,
 db_timestamp timestamp with time zone NOT NULL,

 event_ser_id INTEGER NOT NULL,
 event_ser_manifest VARCHAR(255) NOT NULL,
 event_payload BYTEA NOT NULL,

 deleted BOOLEAN DEFAULT FALSE NOT NULL,
 writer VARCHAR(255) NOT NULL,
 adapter_manifest VARCHAR(255),
 tags TEXT ARRAY,

 meta_ser_id INTEGER,
 meta_ser_manifest VARCHAR(255),
 meta_payload BYTEA,

 PRIMARY KEY(persistence_id, seq_nr)
);

-- `event_journal_slice_idx` is only needed if the slice based queries are used
CREATE INDEX IF NOT EXISTS event_journal_slice_idx ON event_journal(slice, entity_type, db_timestamp, seq_nr);

CREATE TABLE IF NOT EXISTS snapshot(
 slice INT NOT NULL,
 entity_type VARCHAR(255) NOT NULL,
 persistence_id VARCHAR(255) NOT NULL,
 seq_nr BIGINT NOT NULL,
 db_timestamp timestamp with time zone,
 write_timestamp BIGINT NOT NULL,
 ser_id INTEGER NOT NULL,
 ser_manifest VARCHAR(255) NOT NULL,
 snapshot BYTEA NOT NULL,
 tags TEXT ARRAY,
 meta_ser_id INTEGER,
 meta_ser_manifest VARCHAR(255),
 meta_payload BYTEA,

 PRIMARY KEY(persistence_id)
);

CREATE TABLE IF NOT EXISTS durable_state (
 slice INT NOT NULL,
 entity_type VARCHAR(255) NOT NULL,
 persistence_id VARCHAR(255) NOT NULL,
 revision BIGINT NOT NULL,
 db_timestamp timestamp with time zone NOT NULL,

 state_ser_id INTEGER NOT NULL,
 state_ser_manifest VARCHAR(255),
 state_payload BYTEA NOT NULL,
 tags TEXT ARRAY,

 PRIMARY KEY(persistence_id)
);

-- `durable_state_slice_idx` is only needed if the slice based queries are used
CREATE INDEX IF NOT EXISTS durable_state_slice_idx ON durable_state(slice, entity_type, db_timestamp);

create table if not exists migration_failure(
	persistence_id varchar(255) not null,
	failure_type varchar(10) not null,
	primary key(persistence_id)
);

create index fail_type_idx on migration_failure(failure_type);