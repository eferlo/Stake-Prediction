package com.utad.ml

import java.text.SimpleDateFormat
import java.util.Calendar

import com.datastax.spark.connector.SomeColumns
import com.utad.ml.MlPredictor.Day
import org.apache.spark.mllib.classification.{SVMModel, LogisticRegressionModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.tree.model.DecisionTreeModel
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.catalyst.plans._

//importar los implicitos
import com.datastax.spark.connector._


/**
  * Clase que realiza la preparacion de los datos de reporte
  * Created by Emilio Fernandez on 7/22/16.
  */
object Report {
  /**
    * Lanza el proceso
    * @param args Recibe como parametros:
    *             IP cassandra,
    *             Puerto cassandra,
    *             Empresa para la que se obtendran los modelos
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
    val company=args(2)

    //creamos el contexto de spark
    val sc = new SparkContext(conf)


    val keystore = "stokedb"
    //formato de fecha por defecto
    val sdf = new SimpleDateFormat("yyyy-MM-dd");

    //leemos los datos historicos de cotizaciones
    val indexes= sc.cassandraTable(keystore, "dailyindex").select("indexdate","change").where("companyid = ?", company)
      .flatMap(reg=>{val fecha=sdf.format(reg.getDate("indexdate"))
        Seq(((fecha, "decisiontree"), reg.getDouble("change")),
          ((fecha, "regression"),reg.getDouble("change")),
          ((fecha, "svm"),reg.getDouble("change")))})

    //leemos los datos historicos de predicciones
    val predictions= sc.cassandraTable(keystore, "dailyprediction").select("indexdate","decisiontree","regression","svm").where("companyid = ?", company)
      .flatMap(reg=>{val fecha=sdf.format(reg.getDate("indexdate"))
                     Seq(((fecha, "decisiontree"), reg.getInt("decisiontree")),
                       ((fecha, "regression"),reg.getInt("regression")),
                       ((fecha, "svm"),reg.getInt("svm")))})

    //hacemos el join entre cotizaciones y predicciones contabilizando error y aciertos en la prediccion
    //el resultado se agrupo por algoritmo de aprendizaje.
    //Obteniendo para cada algorimo la matriz de confusion como un vector
    //El resultado se guarda en Cassandra
    indexes.join(predictions).map(x=>(x._1._2,x._2)).aggregateByKey((0,0,0,0))(
      (u,x) => {val v=confusion(x)
        (u._1+v._1,u._2+v._2,u._3+v._3,u._4+v._4)
      },
      (u,v)=> (u._1+v._1,u._2+v._2,u._3+v._3,u._4+v._4)
    ).map(line => (company,line._1,line._2._1,line._2._2,line._2._3,line._2._4)).saveToCassandra(
      keystore, "report", SomeColumns("companyid","metodo","subeok", "bajaok","subeko","bajako"))

    //obtenemos las predicciones y cotizaciones de los ultimos 7 dias para publicarlas como una tabla
    val indexes7= sc.cassandraTable(keystore, "dailyindex").select("indexdate","change").where("companyid = ?", company).limit(7)
      .map(reg=>((sdf.format(reg.getDate("indexdate")), (reg.getDouble("change")))))

    val predictions7= sc.cassandraTable(keystore, "dailyprediction").select("indexdate","decisiontree","regression","svm")
      .where("companyid = ?", company).limit(7)
      .map(reg=>(sdf.format(reg.getDate("indexdate")),
                (reg.getInt("decisiontree"),
                reg.getInt("regression"),
                reg.getInt("svm"))))

    //se hace un left join para que aparezca el dia actual para el que tenemos prediccion pero todavia no tenemos
    //la cotizacion definitiva
    //guardamos el resultado en cassandra
    predictions7.leftOuterJoin(indexes7)
      .map(x=>(company,x._1,x._2._1._1,x._2._1._2,x._2._1._3,customToString(x._2._2)))
      .saveToCassandra(keystore, "dailyreport", SomeColumns("companyid", "indexdate", "regression","svm","decisiontree","change"))
  }

  /**
    * Computa un registro en la matriz de confusion
    * @param x Double cambio en la cotizacion, Int prediccion
    * @return vector para sumar una unidad en el lugar adecuado a la matriz de confusion
    */
  def confusion(x: ((Double,Int))) = {
    var retorno=(0,0,0,0)

    if (x._1>=0 & x._2==1)
      retorno=(1,0,0,0)
    else if (x._1<0 & x._2==0)
      retorno=(0,1,0,0)
    else if (x._1>=0 & x._2==0)
      retorno=(0,0,1,0)
    else if (x._1<0 & x._2==1)
      retorno=(0,0,0,1)


    retorno
  }

  /**
    * Funcion customizada de toString para guardar los datos correctamente en cassandra
    * Al hacer un leftouterjoin, Spark utiliza el objeto Option. Con este metodo transformamos dicho objeto en un String
    * @param in
    * @return
    */
  def customToString(in: Option[Double]): String = {
    var retorno="";
    if (in.isDefined)
        retorno=in.get.toString
    retorno
  }

}
