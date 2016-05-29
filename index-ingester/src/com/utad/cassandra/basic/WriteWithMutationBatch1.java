package com.utad.cassandra.basic;

import java.util.Date;

import com.netflix.astyanax.ColumnListMutation;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.StringSerializer;
import com.utad.cassandra.util.Utils;

public class WriteWithMutationBatch1 {

	public static void main(String args[]) throws ConnectionException {

		// Conectamos y usamos un keyspace. Normalmente se usará un keyspace por aplicación		
		Keyspace ksUsers = Utils.getKeyspace("utad");

		ColumnFamily<String, String> cfUsers = new ColumnFamily<String, String>(
				"users", StringSerializer.get(), StringSerializer.get());

		// Necesitamos conocer de antemano la partition key
		String rowKey = "usersById";
		
		// Creamos un mutation, para escribir en batch
		// Mucho más eficiente que escribir uno a uno!
		MutationBatch m = ksUsers.prepareMutationBatch();

		// Preparamos el mutation para trabajar con la partition key y el column family
		ColumnListMutation<String> clm = m.withRow(cfUsers, rowKey);

		// escribimos datos en el mutation
		System.out.println("empezando a escribir ..." + new Date());
		for (int i = 1; i <= 100000; i++) {
			clm.putColumn(i + "", "user" + i + "@void.com");
		}
		System.out.println("terminado!" + new Date());

		// escribimos los datos del mutation en Cassandra
		m.execute();

		
		// leemos los resultados y los guardamos en un objeto
		System.out.println("empezando a leer ..." + new Date());
		ColumnList<String> result = ksUsers.prepareQuery(cfUsers)
				.getKey(rowKey).execute().getResult();
		System.out.println("terminado!" + new Date());
		
		//Iteramos por los resultados para imprimirlos
		if (!result.isEmpty()) {
			for (int i = 0; i < result.size(); i++) {
				String value = result.getColumnByIndex(i).getStringValue();
				//System.out.println("email for user " + result.getColumnByIndex(i).getName() + " is: " + value);
			}
		}
	}
}
