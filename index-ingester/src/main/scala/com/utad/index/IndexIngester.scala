package com.utad.index


import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._     //importar los implicitos
import com.datastax.spark.connector._

/**
  * Clase obtiene los indices bursatiles de las empresas en estudio. Recibe dos parametros:
  * - nombre del servidor de cassandra
  * - puerto del servidor de cassandra
  */
object IndexIngester {
  def main(args: Array[String]) {

    //configuramos el entorno de Spark
    val conf = new SparkConf()
    conf.set("spark.app.name", "IndexIngester")
    conf.set("spark.master", "local[2]")
    conf.set("spark.ui.port", "36000")
    //configuramos cassandra
    conf.set("spark.cassandra.connection.host", args(0));
    conf.set("spark.cassandra.connection.port",args(1));


    //creamos el contexto de spark
    val sc = new SparkContext(conf)
    val keystore = "stokedb"

    //leemos el listado de empresas en estudio
    val companies= sc.cassandraTable(keystore, "company").map(reg=>reg.getString("companyid")).reduce((a, b) => a +"," + b)

    //descargamos el fichero csv con las cotizaciones
    val csv = sc.parallelize(scala.io.Source.fromURL("http://download.finance.yahoo.com/d/quotes.csv?s="+companies+"&f=sd1l1vc1").mkString.split("\n"))

    //eliminamos las comillas y separamos por la coma
    val lines = csv.map(line => line.replaceAll("\"","").split(","))

    //establecemos los formatos de fecha de entrada y salida
    val dateInput = new java.text.SimpleDateFormat("MM/dd/yyyy")
    val dateOutput = new java.text.SimpleDateFormat("yyyy-MM-dd")

    //guardamos en cassandra IDEMPRESA, NOMBRE, FECHA, PRECIO, VOLUMEN DE NEGOCIACION, VARIACION DEL PRECIO
    lines.map(line => (line(0), dateOutput.format(dateInput.parse(line(1))), line(2), line(3), line(4))).saveToCassandra(
      keystore, "dailyindex", SomeColumns("companyid", "indexdate", "price","volume","change"))

  }


}