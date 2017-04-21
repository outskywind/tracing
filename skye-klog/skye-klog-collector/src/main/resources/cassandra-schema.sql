CREATE KEYSPACE IF NOT EXISTS skye WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};
CREATE TABLE IF NOT EXISTS skye.traces_log (
  trace_id            text,
  service_name        text,
  logger_name         text,
  address             inet,
  pid                 text,
  ts                  bigint,
  ts_uuid             timeuuid,
  level               text,
  message             text,
  mdc      map<text,text>,
PRIMARY KEY (trace_id, ts_uuid)
)
  WITH CLUSTERING ORDER BY (ts_uuid DESC)
AND compaction = {'class': 'org.apache.cassandra.db.compaction.TimeWindowCompactionStrategy'}
AND default_time_to_live =  604800;
