#!/bin/sh

# Lanza el proceso para descargar los indices bursatiles

source /etc/hadoop/conf/hadoop-env.sh
source /etc/spark/conf/spark-env.sh

spark-submit --class com.utad.index.IndexIngester --master yarn /home/centos/index-ingester-1.0-jar-with-dependencies.jar 172.31.4.172 9042


