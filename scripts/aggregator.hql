ADD JAR hdfs:///user/centos/hiveresources/hive-serdes-1.0-SNAPSHOT.jar;

ADD JAR hdfs:///user/centos/hiveresources/hive-corenlp-1.0-jar-with-dependencies.jar;

ADD FILE hdfs:///user/centos/hiveresources/classifier.txt;

create temporary function sentimentlingpipe as 'com.utad.lingpipe.SentimentLingPipe';

insert into table ${hiveconf:COMPANY}lingpipe select user.followers_count followers, user.friends_count friends, sentimentlingpipe(text) sentiment, size(entities.urls) urls,
TO_DATE(from_unixtime(UNIX_TIMESTAMP(created_at, 'E MMM dd HH:mm:ss z yyyy'))) created_at from ${hiveconf:COMPANY} where lang='en' 
and TO_DATE(from_unixtime(UNIX_TIMESTAMP(created_at, 'E MMM dd HH:mm:ss z yyyy'))) = ${hiveconf:DATE};

insert into table  ${hiveconf:COMPANY}aggregated select  created_at, count(1) total,
sum(case sentiment when 1 then log2(friends+1)+followers else 0 end) positive, 
sum(case sentiment when 0 then log2(friends+1)+followers else 0 end) neutral, 
sum(case sentiment when -1 then log2(friends+1)+followers else 0 end) negative, 
sum(case sentiment when 1 then urls else 0 end) positiveurls, 
sum(case sentiment when 0 then urls else 0 end) neutralurls, 
sum(case sentiment when -1 then urls else 0 end) negativeurls 
from ${hiveconf:COMPANY}lingpipe 
where created_at = ${hiveconf:DATE}
group by created_at;
