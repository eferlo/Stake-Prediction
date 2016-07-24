package com.utad.lingpipe;

import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LMClassifier;
import com.aliasi.util.AbstractExternalizable;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import java.io.File;
import java.io.IOException;

/**
 * Clase que encapsula lingpipe como funcion de HIVE
 * Created by Emilio Fernandez on 7/5/16.
 */
public class SentimentLingPipe extends UDF {

    private static LMClassifier classifier;
    private static String[] categories;

    static {
        try {
            //Cargamos el clasificador que tiene que estar accesible en el classpath
            ClassLoader classLoader = SentimentLingPipe.class.getClassLoader();
            File file = new File(classLoader.getResource("classifier.txt").getFile());

            classifier = (LMClassifier) AbstractExternalizable.readObject(file);

            //Cargamos las categorias
            categories = classifier.categories();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Funcion que implementa UDF de Hive. Dado un texto realiza el analisis de sentimiento del mismo
     * @param s Texto de entrada
     * @return 1 si el texto es positivo, 0 si es neutro y -1 si es negativo
     */
    public IntWritable evaluate(final Text s) {

        int retorno = 0;
        String cadena = "";

        //comprobamos que la cadena no sea nula
        if (s != null)
            cadena = s.toString();

        //realizamos el analisis de sentimiento
        ConditionalClassification classification = classifier.classify(cadena);

        //codificamos la salida como un entero
        switch (classification.bestCategory()) {
            case "pos":
                retorno = 1;
                break;
            case "neg":
                retorno = -1;
                break;
            default:
                retorno = 0;
                break;
        }

        return new IntWritable(retorno);

    }

}
