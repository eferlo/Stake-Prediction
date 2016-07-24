#!/bin/sh

# Lanza los procesos de agregacion de tweets, prediccion de resultados y reporting

source /etc/hadoop/conf/hadoop-env.sh
source /etc/spark/conf/spark-env.sh

# Calculamos la fecha actual y la del dia anterior
FECHAANT=\'$(date --date="1 days ago" +"%Y-%m-%d")\'
FECHA=$(date  +"%Y-%m-%d")

# Para cada empresa lanzamos el proceso
for i in 'tsla'  'ibm' 'orcl'
do
# Todos los dias realizamos la agregacion de tweets
  /usr/bin/hive -hiveconf DATE=$FECHAANT -hiveconf COMPANY=$i -f /home/centos/aggregator.hql

# Solo los dias de diario realizamos los procesos de prediccion y reporting
  if [[ $(date +%u) -lt 6 ]] ; then
      
      OUTPUT=$(echo "$i" | awk '{print toupper($0)}')

#Prediccion      
      spark-submit --class com.utad.ml.Predictor --master yarn ~/ml-prediction/target/ml-prediction-1.0-SNAPSHOT-jar-with-dependencies.jar 172.31.4.172 9042  thrift://ip-172-31-3-204.ec2.internal:9083 /user/centos/models/ $OUTPUT $FECHA
      
#Reporing
      spark-submit --class com.utad.ml.Report --master yarn ~/ml-prediction/target/ml-prediction-1.0-SNAPSHOT-jar-with-dependencies.jar 172.31.4.172 9042 $OUPUT

#Extraccion de los datos de reporting a csv
      ~/cqlkit/bin/cql2csv --cqlshrc ~/cqlkit/bin/cqlshrc -q "select metodo,subeok as ACIERTO_SUBE,bajaok as ACIERTO_BAJA,subeko as FALLO_SUBE,bajako as FALLO_BAJA from stokedb.report where companyid='$OUTPUT'" > /var/www/html/"$i".csv
      
      ~/cqlkit/bin/cql2csv --cqlshrc ~/cqlkit/bin/cqlshrc -q "select indexdate as fecha, change as cambio, decisiontree, regression, svm from stokedb.dailyreport where companyid='$OUPUT' limit 7" > /var/www/html/"$i"daily.csv
   
   fi
done

