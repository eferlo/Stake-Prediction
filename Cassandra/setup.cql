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

//creamos la tabla de predicciones
CREATE TABLE IF NOT EXISTS stokedb.dailyprediction
(
      companyid  text,
      indexdate     timestamp,
      regression int,
      svm    int,
      decisiontree int,
      PRIMARY KEY (companyid, indexdate)
)WITH CLUSTERING ORDER BY (indexdate DESC);

//creamos la tabla de reporte historico
CREATE TABLE IF NOT EXISTS stokedb.report
(
      companyid  text,
      metodo text,
      subeok int,
      bajaok    int,
      subeko int,
      bajako int,
      PRIMARY KEY (companyid, metodo)
)WITH CLUSTERING ORDER BY (metodo ASC);

//creamos la tabla de resultados diarios
CREATE TABLE IF NOT EXISTS stokedb.dailyreport
(
      companyid  text,
      indexdate  text,
      regression int,
      svm    int,
      decisiontree int,
      change   double,
      PRIMARY KEY (companyid, indexdate)
)WITH CLUSTERING ORDER BY (indexdate DESC);


//insertamos las tres empresas a estudiar Tesla, Oracle e IBM
INSERT INTO stokedb.company (companyid, name, consumerKey, consumerSecret, accessToken, accessTokenSecret,words, hdfspath)
VALUES('ORCL', 'ORACLE', 'ClU9Hio0f6eiDpr9GpTel1UjJ', 'oirwX9wOlwmwwcTZMdQkmpApo2Wx9eUcIsm0zXKyjTCqSbFPhw', '4063282912-l8o1w1xvLNMxtpzD1my4S2Z1le4GWNM0JJhcGps', 'TWDQfQ6FMNnStZvGcRmGimdIoOwKIanprFTrxkSbZs2Sz', ['oracle', 'java', 'weblogic', 'exadata', 'exalogic', 'larryellison'], 'hdfs://localhost:8020/user/cloudera/twitter/ORCL/ORCL' );

INSERT INTO stokedb.company (companyid, name, consumerKey, consumerSecret, accessToken, accessTokenSecret,words, hdfspath)
VALUES('IBM', 'IBM', 'ClU9Hio0f6eiDpr9GpTel1UjJ', 'oirwX9wOlwmwwcTZMdQkmpApo2Wx9eUcIsm0zXKyjTCqSbFPhw', '4063282912-l8o1w1xvLNMxtpzD1my4S2Z1le4GWNM0JJhcGps', 'TWDQfQ6FMNnStZvGcRmGimdIoOwKIanprFTrxkSbZs2Sz', ['ibm', 'IBMWatson', 'websphere', 'datastage','mainframe'], 'hdfs://localhost:8020/user/cloudera/twitter/IBM/IBM' );

INSERT INTO stokedb.company (companyid, name, consumerKey, consumerSecret, accessToken, accessTokenSecret,words, hdfspath)
VALUES('TSLA', 'TESLA', 'eo5XYvp0krehaMuBCuBWTj9zP', 'wgqtejzIDuWy5mHIUeDzw4Iiba4dKVYAHwBv4h3ZgKuDKb00xh', '745018762924023808-gxr2nIGB6D7w6Odg5pyP3hZGuwPw1oh', '6wSspsY3ukyk4EKmv2FOz7vKIww75Msuvx5UGvhhOOcvN', ['tesla', 'elonmusk','powerwall','SpaceX', 'SolarCity'], 'hdfs://localhost:8020/user/cloudera/twitter/TSLA/TSLA' );

//salimos
exit;
