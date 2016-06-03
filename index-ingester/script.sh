#scp -r -i ~/clouderakey1.pem ~/Stake-Prediction  centos@ec2-54-85-192-174.compute-1.amazonaws.com:~/

#mvn clean compile assembly:single

#!/bin/bash
echo "RUNNING JOB"
spark-submit --class com.utad.index.IndexIngester --master local[2] ~/Stake-Prediction/index-ingester/target/index-ingester-1.0-jar-with-dependencies.jar localhost 9042

