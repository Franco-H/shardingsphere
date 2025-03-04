+++
pre = "<b>5.4.2. </b>"
title = "Manual"
weight = 2
+++

## Manual

### Environment

JAVA，JDK 1.8+.

The migration scene we support:

| Source                     | Target          | Support |
| -------------------------- | -------------------- | ------- |
| MySQL(5.1.15 ~ 5.7.x)      | ShardingSphere-Proxy | Yes     |
| PostgreSQL(9.4 ~ )         | ShardingSphere-Proxy | Yes     |

**Attention**: 

If the backend database is MySQL, please download [mysql-connector-java-5.1.47.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar) and put it into `${shardingsphere-scaling}\lib directory`.

### Privileges

MySQL need to open `binlog`, and `binlog format` should be Row model. Privileges of users scaling used should include Replication privileges.

```
+-----------------------------------------+---------------------------------------+
| Variable_name                           | Value                                 |
+-----------------------------------------+---------------------------------------+
| log_bin                                 | ON                                    |
| binlog_format                           | ROW                                   |
| binlog_row_image                        | FULL                                  |
+-----------------------------------------+---------------------------------------+

+------------------------------------------------------------------------------+
|Grants for ${username}@${host}                                                |
+------------------------------------------------------------------------------+
|GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO ${username}@${host}     |
|.......                                                                       |
+------------------------------------------------------------------------------+
```

PostgreSQL need to support and open [test_decoding](https://www.postgresql.org/docs/9.4/test-decoding.html) feature.

### API

ShardingSphere-Scaling provides a simple HTTP API

#### Start scaling job

Interface description: POST /scaling/job/start

Body:

| Parameter                                         | Describe                                                     |
| ------------------------------------------------- | ------------------------------------------------------------ |
| ruleConfig.source                                 | source data source configuration                             |
| ruleConfig.target                                 | target data source configuration                             |
| handleConfig.concurrency                          | sync task proposed concurrency                               |

Data source configuration:

| Parameter                                         | Describe                                                     |
| ------------------------------------------------- | ------------------------------------------------------------ |
| type                                              | data source type(available parameters:shardingSphereJdbc,jdbc)|
| parameter                                         | data source parameter                                        |

*** Notice ***

Currently, source type and target type must be shardingSphereJdbc

Example:

```
curl -X POST \
  http://localhost:8888/scaling/job/start \
  -H 'content-type: application/json' \
  -d '{
        "ruleConfig": {
          "source": {
            "schemaName": "sharding_db",
            "type": "shardingSphereJdbc",
            "parameter": "
                dataSources:
                  ds_0:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    jdbcUrl: jdbc:mysql://127.0.0.1:3306/scaling_0?useSSL=false
                    username: root
                    password: root
                  ds_1:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    jdbcUrl: jdbc:mysql://127.0.0.1:3306/scaling_1?useSSL=false
                    username: root
                    password: root
                rules:
                - !SHARDING
                  tables:
                    t_order:
                      actualDataNodes: ds_$->{0..1}.t_order_$->{0..1}
                      databaseStrategy:
                        standard:
                          shardingColumn: user_id
                          shardingAlgorithmName: t_order_db_algorith
                      logicTable: t_order
                      tableStrategy:
                        standard:
                          shardingColumn: order_id
                          shardingAlgorithmName: t_order_tbl_algorith
                  shardingAlgorithms:
                    t_order_db_algorith:
                      type: INLINE
                      props:
                        algorithm-expression: ds_$->{user_id % 2}
                    t_order_tbl_algorith:
                      type: INLINE
                      props:
                        algorithm-expression: t_order_$->{order_id % 2}
                "
          },
          "target": {
            "schemaName": "sharding_db",
            "type": "shardingSphereJdbc",
            "parameter": "
                dataSources:
                  ds_0:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    jdbcUrl: jdbc:mysql://127.0.0.1:3306/scaling_2?useSSL=false
                    username: root
                    password: root
                  ds_1:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    jdbcUrl: jdbc:mysql://127.0.0.1:3306/scaling_3?useSSL=false
                    username: root
                    password: root
                  ds_2:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    jdbcUrl: jdbc:mysql://127.0.0.1:3306/scaling_4?useSSL=false
                    username: root
                    password: root
                  ds_3:
                    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
                    jdbcUrl: jdbc:mysql://127.0.0.1:3306/scaling_5?useSSL=false
                    username: root
                    password: root
                rules:
                - !SHARDING
                  tables:
                    t_order:
                      actualDataNodes: ds_$->{0..3}.t_order_$->{0..3}
                      databaseStrategy:
                        standard:
                          shardingColumn: user_id
                          shardingAlgorithmName: t_order_db_algorith
                      logicTable: t_order
                      tableStrategy:
                        standard:
                          shardingColumn: order_id
                          shardingAlgorithmName: t_order_tbl_algorith
                  shardingAlgorithms:
                    t_order_db_algorith:
                      type: INLINE
                      props:
                        algorithm-expression: ds_$->{user_id % 4}
                    t_order_tbl_algorith:
                      type: INLINE
                      props:
                        algorithm-expression: t_order_$->{order_id % 4}
                "
          }
        },
        "handleConfig":{
          "concurrency":"3"
        }
      }'
```

Response:

```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

#### Get scaling progress

Interface description: GET /scaling/job/progress/{jobId}

Example:
```
curl -X GET \
  http://localhost:8888/scaling/job/progress/1
```

Response:
```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": {
        "id": 1,
        "jobName": "Local Sharding Scaling Job",
        "status": "RUNNING/STOPPED"
        "syncTaskProgress": [{
            "id": "127.0.0.1-3306-test",
            "status": "PREPARING/MIGRATE_HISTORY_DATA/SYNCHRONIZE_REALTIME_DATA/STOPPING/STOPPED",
            "historySyncTaskProgress": [{
                "id": "history-test-t1#0",
                "estimatedRows": 41147,
                "syncedRows": 41147
            }, {
                "id": "history-test-t1#1",
                "estimatedRows": 42917,
                "syncedRows": 42917
            }, {
                "id": "history-test-t1#2",
                "estimatedRows": 43543,
                "syncedRows": 43543
            }, {
                "id": "history-test-t2#0",
                "estimatedRows": 39679,
                "syncedRows": 39679
            }, {
                "id": "history-test-t2#1",
                "estimatedRows": 41483,
                "syncedRows": 41483
            }, {
                "id": "history-test-t2#2",
                "estimatedRows": 42107,
                "syncedRows": 42107
            }],
            "realTimeSyncTaskProgress": {
                "id": "realtime-test",
                "delayMillisecond": 1576563771372,
                "position": {
                    "filename": "ON.000007",
                    "position": 177532875,
                    "serverId": 0
                }
            }
        }]
   }
}
```

#### List scaling jobs

Interface description: GET /scaling/job/list

Example:
```
curl -X GET \
  http://localhost:8888/scaling/job/list
```

Response:

```
{
  "success": true,
  "errorCode": 0,
  "model": [
    {
      "jobId": 1,
      "jobName": "Local Sharding Scaling Job",
      "status": "RUNNING"
    }
  ]
}
```

#### Stop scaling job

Interface description: GET /scaling/job/stop

Body:

| Parameter | Describe |
| --------- | -------- |
| jobId     | job id   |

Example:
```
curl -X GET \
  http://localhost:8888/scaling/job/stop/1
```
Response:
```
{
   "success": true,
   "errorCode": 0,
   "errorMsg": null,
   "model": null
}
```

### Operate through the UI interface

We provide user interface in ShardingSphere-UI, so all the operations related can be implemented with a click of the UI interface.
For more information, please refer to the ShardingSphere-UI module.
