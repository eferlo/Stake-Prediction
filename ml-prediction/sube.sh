mvn clean
scp -r -i ~/clouderakey1.pem ~/Stake-Prediction/ml-prediction/src/* centos@ec2-52-23-174-80.compute-1.amazonaws.com:~/ml-prediction/src
scp -r -i ~/clouderakey1.pem ~/Stake-Prediction/ml-prediction/pom.xml centos@ec2-52-23-174-80.compute-1.amazonaws.com:~/ml-prediction/pom.xml

