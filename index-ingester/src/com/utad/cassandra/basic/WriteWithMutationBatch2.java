package com.utad.cassandra.basic;

import java.util.Date;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.StringSerializer;
import com.utad.cassandra.util.Utils;

public class WriteWithMutationBatch2 {

	public static void main(String args[]) throws ConnectionException {

		// Conectamos y usamos un keyspace. Normalmente se usará un keyspace por
		// aplicación
		Keyspace ksUsers = Utils.getKeyspace("utad");

		ColumnFamily<String, String> cfUsers = new ColumnFamily<String, String>(
				"users", StringSerializer.get(), StringSerializer.get());

		// Necesitamos conocer de antemano la partition key
		String rowKey = "usersById";

		// escribimos de uno en uno
		System.out.println("empezando a escribir ... " + new Date());

		for (int i = 1; i <= 100000; i++) {
			// escribir un valor
			ksUsers.prepareColumnMutation(cfUsers, "usersById", "" + i)
					.putValue("user" + i + "@void.com", null).execute();
		}

		System.out.println("terminado!" + new Date());

		// leemos los resultados de uno en uno
		System.out.println("empezando a leer ..." + new Date());
		for (int i = 1; i <= 100000; i++) {
			Column<String> result = ksUsers.prepareQuery(cfUsers)
					.getKey(rowKey).getColumn("" + i).execute().getResult();
			String value = result.getStringValue();
		}

		System.out.println("terminado!" + new Date());
	}
}
