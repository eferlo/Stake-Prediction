package com.utad.ml

import java.text.SimpleDateFormat
import java.util.{Calendar}

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, SVMWithSGD}
import org.apache.spark.mllib.evaluation.{MulticlassMetrics, BinaryClassificationMetrics}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.rdd.RDD


//importar los implicitos
import com.datastax.spark.connector._


/**
  * Clase que genera los modelos de aprendizaje, los modelos generados se guardan en el HDFS
  * Created by Emilio Fernandez on 7/13/16.
  */
object MlPredictor {

  /**
    * Metodo main para lanzar el proceso
    *
    * @param args Recibe como parametros:
    *             IP cassandra,
    *             Puerto cassandra,
    *             Cadena de conexion a Hive,
    *             Ruta en HDFS donde se guardaran los modelos,
    *             Empresa para la que se obtendran los modelos
    */
  def main(args: Array[String]) {

    //configuramos el entorno de Spark, por si no se hubieran confirado en el comando spark-submit
    val conf = new SparkConf()
    conf.set("spark.app.name", "MlPredictor")
    conf.set("spark.master", "local[2]")
    conf.set("spark.ui.port", "36000")

    //configuramos cassandra
    conf.set("spark.cassandra.connection.host", args(0))
    conf.set("spark.cassandra.connection.port", args(1))

    //configuramos hive
    conf.set("hive.metastore.uris", args(2))

    val ruta = args(3)
    val company = args(4)

    //borramos el modelo anterior para poder guardar el nuevo
    val hadoopConf = new org.apache.hadoop.conf.Configuration()
    val hadoop = org.apache.hadoop.fs.FileSystem.get(hadoopConf)
    try {
      hadoop.delete(new org.apache.hadoop.fs.Path(ruta + company), true)
    } catch {
      case _: Throwable => {}
    }


    //creamos el contexto de spark y de Hive
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.hive.HiveContext(sc)

    val keystore = "stokedb"

    //formato de fecha por defecto
    val sdf = new SimpleDateFormat("yyyy-MM-dd")

    import sqlContext.implicits._

    //Recuperamos los indices bursatiles de la empresa
    val indexes = sc.cassandraTable(keystore, "dailyindex").select("indexdate", "change").where("companyid = ?", company).map(reg => (sdf.format(reg.getDate("indexdate")), reg.getDouble("change")))

    //funcion auxiliar que nos permiten sumar o restar dias a una fecha
    def suma(x: String, y: Int) = {
      val cal = Calendar.getInstance();

      cal.setTime(sdf.parse(x));
      cal.add(Calendar.DATE, y);
      sdf.format(cal.getTime());
    }

    //funcion auxiliar que nos permite calcular el siguiente dia habil a partir de una fecha dada
    //se entiende por día habil el proximo dia en el abrira Wall Street
    def diaSiguiente(x: String) = {

      val cal = Calendar.getInstance()
      cal.setTime(sdf.parse(x))
      cal.add(Calendar.DATE, 1)
      //listado de dias festivos en EEUU
      val fiestas = Array("2016-07-04", "2016-09-05", "2016-10-12")
      var retorno = sdf.format(cal.getTime())

      //Iteramos hasta entonontrar un dia laborable no festivo.
      while ((cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) ||
        (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) ||
        fiestas.contains(retorno)) {
        cal.add(Calendar.DATE, 1)
        retorno = sdf.format(cal.getTime())
      }
      retorno
    }

    //este RDD representa las cotizaciones del anterior dia habil que forman parte de vector de caracteristicas
    val indexesPrev = indexes.map(x => (diaSiguiente(x._1), x._2))

    //Mapeamos el cambio en la cotizacion como 0 si bajo o 1 en caso contrario.
    //Esto sera nuestro label en los LabeledPoints
    val indexesTot = indexes.map(x => {
      var y = 0; if (x._2 >= 0) y = 1; (x._1, y)
    })

    //variable auxiliar para inicializar el aggregatebykey
    val aux = Day("0001-01-01", 0, 0, 0, 0, 0, 0, 0);

    //recuperamos los datos agregados de Hive de todos los dias que tenemos para realizar el aprendizaje
    //Los tweets de cada dia se tienen en cuenta durante 3 dias. Para poder agruparlos emitimos cada dia
    //3 veces con diferentes claves y lo aplanamos con un flatMap
    //Con un aggregateByKey los volvemos a agrupar ordenandolos descedentemente por fecha
    //Por ultimo filtramos aquellos dias para los que no tenga informacion de los tres dias anterios
    val df = sqlContext.sql("SELECT created_at, total, positive, neutral, negative, positiveurls, neutralurls, negativeurls  FROM " + company.toLowerCase() + "aggregated")
      .flatMap(x =>
        List(
          (suma(x.getString(0), 1), Day(suma(x.getString(0), 0), x.getLong(1), x.getDouble(2), x.getDouble(3), x.getDouble(4), x.getLong(5), x.getLong(6), x.getLong(7))),
          (suma(x.getString(0), 2), Day(suma(x.getString(0), 0), x.getLong(1), x.getDouble(2), x.getDouble(3), x.getDouble(4), x.getLong(5), x.getLong(6), x.getLong(7))),
          (suma(x.getString(0), 3), Day(suma(x.getString(0), 0), x.getLong(1), x.getDouble(2), x.getDouble(3), x.getDouble(4), x.getLong(5), x.getLong(6), x.getLong(7))))
      ).aggregateByKey((aux, aux, aux))(
      (u, v) => {
        val values = List(u._1, u._2, u._3, v);
        val aux = values.sorted(Ordering[Day].reverse)
        (aux(0), aux(1), aux(2))
      },
      (u, v) => {
        val values = List(u._1, u._2, u._3, v._1, v._2, v._3);
        val aux = values.sorted(Ordering[Day].reverse)
        (aux(0), aux(1), aux(2))
      }
    ).filter(y => y._2._3.fecha.equals("0001-01-01") != true)

    //hacemos join con los datos de cotizacion conocidos para formar el labeled point
    val data = indexesTot.join(indexesPrev).join(df).map(x => LabeledPoint(x._2._1._1, Vectors.dense(Array(x._2._1._2) ++ x._2._2._1.toDoubleArray() ++ x._2._2._2.toDoubleArray() ++ x._2._2._3.toDoubleArray())))

    //realizamos el proceso de aprendizaje y guardamos los modelos.
    regression(data).save(sc, ruta + company + "/regression")
    svm(data).save(sc, ruta + company + "/svm")
    decisiontree(data).save(sc, ruta + company + "/decisiontree")
  }

  //clase nos ayuda a representar los datos de un dia
  case class Day(fecha: String, total: Long, positive: Double, neutral: Double, negative: Double, positiveurls: Long, neutralurls: Long, negativeurls: Long) extends Ordered[Day] {
    //implementamos compare para poder ordenar los registros por fecha
    def compare(that: Day): Int = (this.fecha compare that.fecha)

    //se sobreescribe el metodo toString para mejorar la lectura del resultado
    override def toString(): String = "(" + fecha + "," + total + ")";

    def toDoubleArray(): Array[Double] = Array(total, positive, neutral, negative, positiveurls, neutralurls, negativeurls);
  }

  /**
    * Utiliza regresion logistica para inferir un modelo
    * @param data RDD con los datos historicos
    * @return Modelo obtenido
    */
  def regression(data: RDD[LabeledPoint]) = {
    //partimos el conjunto en entrenamiento y test
    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)

    //Lanzamos el aprendizaje
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(10)
      .run(training)

    //Comparamos los resultados
    val predictionAndLabels = test.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }
    predictionAndLabels.foreach(println)

    //Evaluamos la precision
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val precision = metrics.precision
    println("Precision = " + precision)

    model
  }

  /**
    * Utiliza svm para inferir un modelo
    * @param data RDD con los datos historicos
    * @return Modelo obtenido
    */
  def svm(data: RDD[LabeledPoint]) = {

    // Separamos en entrenamiento y test
    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0).cache()
    val test = splits(1)

    //lanzamos en el entrenamiento
    val numIterations = 100
    val model = SVMWithSGD.train(training, numIterations)
    model.clearThreshold()

    //utilizamos el conjunto de test
    val scoreAndLabels = test.map { point =>
      val score = model.predict(point.features)
      (score, point.label)
    }

    scoreAndLabels.foreach(println)

    //Evaluamos las metricas
    val metrics = new BinaryClassificationMetrics(scoreAndLabels)

    val auROC = metrics.areaUnderROC()

    println("Area bajo ROC = " + auROC)

    val auPR = metrics.areaUnderPR()

    println("Area bajo PR = " + auPR)

    model
  }

  /**
    * Utiliza arboles de decision
    * @param data RDD con los datos historicos
    * @return Modelo obtenido
    */
  def decisiontree(data: RDD[LabeledPoint]) = {

    //separamos en entrenamiento y test
    val splits = data.randomSplit(Array(0.6, 0.4))
    val (trainingData, testData) = (splits(0), splits(1))

    //entrenamos el modelo
    val numClasses = 2
    val categoricalFeaturesInfo = Map[Int, Int]()
    val impurity = "gini"
    val maxDepth = 5
    val maxBins = 32

    val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
      impurity, maxDepth, maxBins)

    //utilizamos el conjunto de test
    val scoreAndLabels = testData.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

    scoreAndLabels.foreach(println)
    //evaluamos la precision
    val testErr = scoreAndLabels.filter(r => r._1 != r._2).count().toDouble / testData.count()
    println("Test Error = " + testErr)

    model
  }

}
