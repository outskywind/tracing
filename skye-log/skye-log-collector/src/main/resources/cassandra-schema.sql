CREATE KEYSPACE IF NOT EXISTS skye WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};
CREATE TABLE IF NOT EXISTS skye.traces_log (
  trace_id            text,
  ts_uuid             timeuuid,
  service_name         text,
  address             text,
  pid                 text,
  thread              text,
  logger_name         text,
  level               text,
  mdc                 map<text,text>,
  message             text,
PRIMARY KEY (trace_id, ts_uuid)
)
  WITH CLUSTERING ORDER BY (ts_uuid ASC )
AND compaction = {'class': 'org.apache.cassandra.db.compaction.TimeWindowCompactionStrategy'}
AND default_time_to_live =  604800;
