//Script para crear la base de datos en cassandra
//cqlsh localhost 9160 -f ./setup.cql

//Creamos el keyspace
CREATE KEYSPACE IF NOT EXISTS stokedb
WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

//Fijamos por defecto el nuevo keyspace
use stokedb;

//creamos la tabla de empresas
CREATE TABLE IF NOT EXISTS stokedb.company
(
      companyid  text,
      name text,
      consumerKey text,
      consumerSecret text,
      accessToken text,
      accessTokenSecret text,
      words list<text>,
      hdfspath text,
      PRIMARY KEY (companyid )
);

//creamos la tabla de cotizaciones
CREATE TABLE IF NOT EXISTS stokedb.dailyindex
(
      companyid  text,
      indexdate     timestamp,
      price   double,
      volume    bigint,
      change   double,
      PRIMARY KEY (companyid, indexdate)
)WITH CLUSTERING ORDER BY (indexdate DESC);

//insertamos las tres empresas a estudiar Nike, Oracle e IBM
INSERT INTO stokedb.company (companyid, name, consumerKey, consumerSecret, accessToken, accessTokenSecret,words, hdfspath)
VALUES('ORCL', 'ORACLE', 'ClU9Hio0f6eiDpr9GpTel1UjJ', 'oirwX9wOlwmwwcTZMdQkmpApo2Wx9eUcIsm0zXKyjTCqSbFPhw', '4063282912-l8o1w1xvLNMxtpzD1my4S2Z1le4GWNM0JJhcGps', 'TWDQfQ6FMNnStZvGcRmGimdIoOwKIanprFTrxkSbZs2Sz', ['oracle', 'java', 'weblogic', 'exadata', 'exalogic', 'larryellison'], 'hdfs://localhost:8020/user/cloudera/twitter/ORCL' );

INSERT INTO stokedb.company (companyid, name, consumerKey, consumerSecret, accessToken, accessTokenSecret,words, hdfspath)
VALUES('NKE', 'NIKE', 'ClU9Hio0f6eiDpr9GpTel1UjJ', 'oirwX9wOlwmwwcTZMdQkmpApo2Wx9eUcIsm0zXKyjTCqSbFPhw', '4063282912-l8o1w1xvLNMxtpzD1my4S2Z1le4GWNM0JJhcGps', 'TWDQfQ6FMNnStZvGcRmGimdIoOwKIanprFTrxkSbZs2Sz', ['nike', 'justdoit', 'nikestore', 'NikeLab'], 'hdfs://localhost:8020/user/cloudera/twitter/NKE' );

INSERT INTO stokedb.company (companyid, name, consumerKey, consumerSecret, accessToken, accessTokenSecret,words, hdfspath)
VALUES('IBM', 'IBM', 'ClU9Hio0f6eiDpr9GpTel1UjJ', 'oirwX9wOlwmwwcTZMdQkmpApo2Wx9eUcIsm0zXKyjTCqSbFPhw', '4063282912-l8o1w1xvLNMxtpzD1my4S2Z1le4GWNM0JJhcGps', 'TWDQfQ6FMNnStZvGcRmGimdIoOwKIanprFTrxkSbZs2Sz', ['ibm', 'IBMWatson', 'websphere', 'datastage','mainframe'], 'hdfs://localhost:8020/user/cloudera/twitter/IBM' );

//salimos
exit;
