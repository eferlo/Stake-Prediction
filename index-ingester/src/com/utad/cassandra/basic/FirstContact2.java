package com.utad.cassandra.basic;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.StringSerializer;
import com.utad.cassandra.util.Utils;

public class FirstContact2 {

	public static void main(String args[]) throws ConnectionException {

		String keyspaceName = "utad";
		String columnFamilyName = "users";
		
		// Normalmente usaremos un keyspace para toda la app
		Keyspace ksUsers = Utils.getKeyspace(keyspaceName);
		
		// Creamos el objeto column family para usarlo.
		ColumnFamily<String, String> cfUsers = new ColumnFamily<String, String>(
				columnFamilyName, StringSerializer.get(), StringSerializer.get());

		// escribir un valor
		ksUsers.prepareColumnMutation(cfUsers, "usersById", "2")
				.putValue("user2@void.com", null).execute();

		// leer un valor
		Column<String> result = ksUsers.prepareQuery(cfUsers)
				.getKey("usersById").getColumn("2").execute().getResult();
		String value = result.getStringValue();

		System.out.println("email for user 2 is: " + value);
	}
}
