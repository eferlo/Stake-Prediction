--serde para tratar json
ADD JAR hdfs:///user/centos/hiveresources/hive-serdes-1.0-SNAPSHOT.jar;

--libreria java para dar de alta una funcion HIVE para el analisis de sentimiento
ADD JAR hdfs:///user/centos/hiveresources/hive-corenlp-1.0-jar-with-dependencies.jar;

--clasificador para el analisis de sentimiento
ADD FILE hdfs:///user/centos/hiveresources/classifier.txt;

--creacion de la funcion hive para analisis de sentimiento
create temporary function sentimentlingpipe as 'com.utad.lingpipe.SentimentLingPipe';

--TABLAS DE TWEETS EN BRUTO

CREATE EXTERNAL TABLE tesla (
   id BIGINT,
   created_at STRING,
   source STRING,
   favorited BOOLEAN,
   lang STRING,
   retweet_count INT,
   retweeted_status STRUCT<
      text:STRING,
      user:STRUCT<screen_name:STRING,name:STRING>>,
   entities STRUCT<
      urls:ARRAY<STRUCT<expanded_url:STRING>>,
      user_mentions:ARRAY<STRUCT<screen_name:STRING,name:STRING>>,
      hashtags:ARRAY<STRUCT<text:STRING>>>,
   text STRING,
   user STRUCT<
      screen_name:STRING,
      name:STRING,
      friends_count:INT,
      followers_count:INT,
      statuses_count:INT,
      verified:BOOLEAN,
      utc_offset:INT,
      time_zone:STRING>,
   in_reply_to_screen_name STRING
) 
ROW FORMAT SERDE 'com.cloudera.hive.serde.JSONSerDe'
LOCATION '/user/cloudera/twitter/tesla';

CREATE EXTERNAL TABLE orcl (
   id BIGINT,
   created_at STRING,
   source STRING,
   favorited BOOLEAN,
   lang STRING,
   retweet_count INT,
   retweeted_status STRUCT<
      text:STRING,
      user:STRUCT<screen_name:STRING,name:STRING>>,
   entities STRUCT<
      urls:ARRAY<STRUCT<expanded_url:STRING>>,
      user_mentions:ARRAY<STRUCT<screen_name:STRING,name:STRING>>,
      hashtags:ARRAY<STRUCT<text:STRING>>>,
   text STRING,
   user STRUCT<
      screen_name:STRING,
      name:STRING,
      friends_count:INT,
      followers_count:INT,
      statuses_count:INT,
      verified:BOOLEAN,
      utc_offset:INT,
      time_zone:STRING>,
   in_reply_to_screen_name STRING
) 
ROW FORMAT SERDE 'com.cloudera.hive.serde.JSONSerDe'
LOCATION '/user/cloudera/twitter/orcl';

CREATE EXTERNAL TABLE ibm (
   id BIGINT,
   created_at STRING,
   source STRING,
   favorited BOOLEAN,
   lang STRING,
   retweet_count INT,
   retweeted_status STRUCT<
      text:STRING,
      user:STRUCT<screen_name:STRING,name:STRING>>,
   entities STRUCT<
      urls:ARRAY<STRUCT<expanded_url:STRING>>,
      user_mentions:ARRAY<STRUCT<screen_name:STRING,name:STRING>>,
      hashtags:ARRAY<STRUCT<text:STRING>>>,
   text STRING,
   user STRUCT<
      screen_name:STRING,
      name:STRING,
      friends_count:INT,
      followers_count:INT,
      statuses_count:INT,
      verified:BOOLEAN,
      utc_offset:INT,
      time_zone:STRING>,
   in_reply_to_screen_name STRING
) 
ROW FORMAT SERDE 'com.cloudera.hive.serde.JSONSerDe'
LOCATION '/user/cloudera/twitter/ibm';


--TABLAS DE TWEETS CON ANALISIS DE SENTIMIENTO 

create table ibmlingpipe as select user.followers_count followers, user.friends_count friends, sentimentlingpipe(text) sentiment, size(entities.urls) urls,
TO_DATE(from_unixtime(UNIX_TIMESTAMP(created_at, 'E MMM dd HH:mm:ss z yyyy'))) created_at from ibm where lang='en' and TO_DATE(from_unixtime(UNIX_TIMESTAMP(created_at, 'E MMM dd HH:mm:ss z yyyy'))) < '2016-07-19';

create table orcllingpipe as select user.followers_count followers, user.friends_count friends, sentimentlingpipe(text) sentiment, size(entities.urls) urls,
TO_DATE(from_unixtime(UNIX_TIMESTAMP(created_at, 'E MMM dd HH:mm:ss z yyyy'))) created_at from orcl where lang='en' and TO_DATE(from_unixtime(UNIX_TIMESTAMP(created_at, 'E MMM dd HH:mm:ss z yyyy'))) < '2016-07-19';

create table teslalingpipe as select user.followers_count followers, user.friends_count friends, sentimentlingpipe(text) sentiment, size(entities.urls) urls,
TO_DATE(from_unixtime(UNIX_TIMESTAMP(created_at, 'E MMM dd HH:mm:ss z yyyy'))) created_at from tesla where lang='en' and TO_DATE(from_unixtime(UNIX_TIMESTAMP(created_at, 'E MMM dd HH:mm:ss z yyyy'))) < '2016-07-19';


--TABLAS DE TWEETS AGREGADOS 

create table ibmaggregated as select  created_at, count(1) total,
sum(case sentiment when 1 then log2(friends+1)+followers else 0 end) positive, 
sum(case sentiment when 0 then log2(friends+1)+followers else 0 end) neutral, 
sum(case sentiment when -1 then log2(friends+1)+followers else 0 end) negative, 
sum(case sentiment when 1 then urls else 0 end) positiveurls, 
sum(case sentiment when 0 then urls else 0 end) neutralurls, 
sum(case sentiment when -1 then urls else 0 end) negativeurls 
from ibmlingpipe 
group by created_at;

create table teslaaggregated as select  created_at, count(1) total,
sum(case sentiment when 1 then log2(friends+1)+followers else 0 end) positive, 
sum(case sentiment when 0 then log2(friends+1)+followers else 0 end) neutral, 
sum(case sentiment when -1 then log2(friends+1)+followers else 0 end) negative, 
sum(case sentiment when 1 then urls else 0 end) positiveurls, 
sum(case sentiment when 0 then urls else 0 end) neutralurls, 
sum(case sentiment when -1 then urls else 0 end) negativeurls 
from teslalingpipe 
group by created_at;

create table orclaggregated as select  created_at, count(1) total,
sum(case sentiment when 1 then log2(friends+1)+followers else 0 end) positive, 
sum(case sentiment when 0 then log2(friends+1)+followers else 0 end) neutral, 
sum(case sentiment when -1 then log2(friends+1)+followers else 0 end) negative, 
sum(case sentiment when 1 then urls else 0 end) positiveurls, 
sum(case sentiment when 0 then urls else 0 end) neutralurls, 
sum(case sentiment when -1 then urls else 0 end) negativeurls 
from orcllingpipe
group by created_at;


