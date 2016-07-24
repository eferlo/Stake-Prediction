# Stake-Prediction

Proyecto de fin de master en big data para la UTAD: http://ec2-52-23-174-80.compute-1.amazonaws.com/index.html

El proyecto se organiza en varias carpetas que se corresponden con los diferentes módulos desarrollados.

## Cassandra	

Contiene las sentencias cql usadas para crear las tablas en la base de datos Cassandra. Se han creado las siguientes tablas:
  - company: tabla con el listado de empresas en estudio y algunos datos de configuración
  - dailyindex: tabla que almacena las cotizaciones diarias de cada empresa.
  - dailyprediction: tabla que almacena las predicciones para cada día, empresa y algoritmo de predicción.
  - dailyreport: tabla de cruce de las cotizaciones y las predicciones para hacer reporting.
  - report: tabla que guarda los datos históricos agregados con los aciertos y los fallos en las predicciones para su visualización en la Web.

## Flume
Contiene los ficheros de configuración de los agentes de Flume para la descarga de tweets.

## Hive
Contiene los scripts de creación de las diferentes tablas. Las tablas están separadas por empresa, existen tres tablas para cada empresa:
  - "NOMBRE DE EMPRESA": tabla con los tweets en bruto tal y como llegan de flume.
  - "NOMBRE DE EMPRESA"lingpipe: tabla con sólo los datos relevantes de cada tweet y el resultado de realizar el análisis de sentimiento. 
  - "NOMBRE DE EMPRESA"aggregated: tabla con los datos de tweets agregados por día.

## hive-corenlp 
Proyecto maven hecho en JAVA para poder utilizar lingpipe como función hql. Para ello la clase com.utad.lingpipe.SentimentLingPipe ha implementado el interfaz UDF de Hive.
 
## index-ingester 
Proyecto maven hecho en Scala/Spark que utiliza el API de Yahoo para descargar las cotizaciones bursátiles y las guarda en Cassandra.

## ml-predictor
Proyecto maven hecho en Scala/Spark que realiza todas las tareas relacionadas con machine learning y la preparación de los datos de reporte.
  - Clase MlPredictor: esta clase genera los modelos de aprendizaje. Se lanza a demanda cuando se considera que hay que revisar los modelos
  - Clase Predictor: esta clase genera la predicción para un día. Se lanza cada día unas horas antes de la apertura de Wall Street.
  - Clase Report: esta clase genera los datos necesarios para la visualización de información en la Web. Se lanza después de haber realizado las predicciones

## web
Conjunto de recursos web que se despliegan en el Apache para la consulta de las predicciones.

## scripts
Conjunto de scripts para el lanzamiento automático de procesos.
  - index-ingester.sh: lanza el proceso de recuperación de indices bursátiles.
  - aggregator.sh: lanza el tratamiento de tweets del día anterior (proceso hive). Para los días de diario lanza el proceso de predicción y datos de reporting (spark), por último exporta a csv los datos de reporting para su publicación en la web (cqlkit).
  - aggregator.hql: conjunto de sentencias hql para realizar el análisis de sentimiento y la agrupación de lo tweets por día.
El lanzamiento de scripts se ha programado en el crontab del cluster.
