package com.utad.ml

import java.text.SimpleDateFormat
import java.util.{Calendar}

import com.utad.ml.MlPredictor.Day
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.classification.{LogisticRegressionModel, SVMModel, LogisticRegressionWithLBFGS, SVMWithSGD}
import org.apache.spark.mllib.evaluation.{MulticlassMetrics, BinaryClassificationMetrics}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.rdd.RDD


//importar los implicitos
import com.datastax.spark.connector._


/**
  * Clase que realiza las predicciones utilizando los modelos almacenados en el HDFS
  * Created by Emilio Fernandez on 7/21/16.
  */
object Predictor {

  /**
    * Metodo main para lanzar el proceso
    *
    * @param args Recibe como parametros:
    *             IP cassandra,
    *             Puerto cassandra,
    *             Cadena de conexion a Hive,
    *             Ruta en HDFS donde se guardaran los modelos,
    *             Empresa para la que se obtendran los modelos
    *             Fecha para la que se realizara la prediccion
    */

  def main(args: Array[String]) {
    //configuramos el entorno de Spark
    val conf = new SparkConf()
    conf.set("spark.app.name", "Predictor")
    conf.set("spark.master", "local[2]")
    conf.set("spark.ui.port", "36000")

    //configuramos cassandra
    conf.set("spark.cassandra.connection.host", args(0));
    conf.set("spark.cassandra.connection.port",args(1));
    conf.set("hive.metastore.uris",args(2))

    val ruta=args(3)
    val company=args(4)
    val fecha=args(5)

    //creamos el contexto de spark
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.hive.HiveContext(sc)

    val keystore = "stokedb"

    //formato de fecha por defecto
    val sdf = new SimpleDateFormat("yyyy-MM-dd");

    //funcion auxiliar que nos permiten sumar o restar dias a una fecha
    def suma(x: String,y: Int) = {val cal = Calendar.getInstance();

      cal.setTime(sdf.parse(x));
      cal.add(Calendar.DATE,y);
      sdf.format(cal.getTime());
    }

    // this is used to implicitly convert an RDD to a DataFrame.
    import sqlContext.implicits._

    //leemos el ultimo indice bursatil previo a la fecha de la prediccion
    val index= sc.cassandraTable(keystore, "dailyindex").select("indexdate","change").where("companyid = ? and indexdate <?", company,sdf.parse(fecha)).limit(1).map(reg=>(sdf.format(reg.getDate("indexdate")), reg.getDouble("change"))).collect()

    //variable auxiliar para acumular
    val aux=Day("0001-01-01",0,0,0,0,0,0,0);

    //obtenemos los datos de los tweets de los tres dias anteriores a la prediccion
    //Los tweets de cada dia se tienen en cuenta durante 3 dias. Para poder agruparlos emitimos cada dia
    //3 veces con diferentes claves y lo aplanamos con un flatMap
    //Con un aggregateByKey los volvemos a agrupar ordenandolos descedentemente por fecha
    //Por ultimo filtramos aquellos dias para los que no tenga informacion de los tres dias anterios
    val df = sqlContext.sql("SELECT created_at, total, positive, neutral, negative, positiveurls, neutralurls, negativeurls  FROM "+
      company.toLowerCase()+"aggregated "+"WHERE created_at in ('"+suma(fecha,-1)+"','"+suma(fecha,-2)+"','"+suma(fecha,-3)+"')")
      .flatMap(x=>
        List(
          (suma(x.getString(0),1),Day(suma(x.getString(0),0),x.getLong(1),x.getDouble(2),x.getDouble(3),x.getDouble(4),x.getLong(5),x.getLong(6),x.getLong(7))),
          (suma(x.getString(0),2),Day(suma(x.getString(0),0),x.getLong(1),x.getDouble(2),x.getDouble(3),x.getDouble(4),x.getLong(5),x.getLong(6),x.getLong(7))),
          (suma(x.getString(0),3),Day(suma(x.getString(0),0),x.getLong(1),x.getDouble(2),x.getDouble(3),x.getDouble(4),x.getLong(5),x.getLong(6),x.getLong(7))))
      ).aggregateByKey((aux,aux,aux))(
      (u, v) => {val values = List(u._1,u._2,u._3,v);
        val aux= values.sorted(Ordering[Day].reverse)
        (aux(0), aux(1), aux(2))
      },
      (u,v)=> {val values = List(u._1,u._2,u._3,v._1,v._2,v._3);
        val aux= values.sorted(Ordering[Day].reverse)
        (aux(0), aux(1), aux(2))
      }
    ).filter(y=>y._2._3.fecha.equals("0001-01-01")!=true)

    //obtenemos el vector de aprendizaje
    val data=df.map(x=>Vectors.dense(Array(index(0)._2)++x._2._1.toDoubleArray()++x._2._2.toDoubleArray()++x._2._3.toDoubleArray())).collect()(0)

    //cargamos lod diferentes modelos y realizamos la prediccion
    val regression = LogisticRegressionModel.load(sc, ruta+company+"/regression").predict(data)

    var svm = 0;

    if (SVMModel.load(sc, ruta+company+"/svm").predict(data)>=0)
      svm=1;

    val decisiontree = DecisionTreeModel.load(sc,ruta+company+"/decisiontree").predict(data)

    println("Prediccion del "+fecha+" para "+company+": "+regression+","+svm+","+decisiontree)

    //guardamos los datos de prediccion en cassandra
    sc.parallelize(Seq((company,fecha,regression,svm,decisiontree))).saveToCassandra(keystore, "dailyprediction", SomeColumns("companyid", "indexdate", "regression","svm","decisiontree"))

  }

}
