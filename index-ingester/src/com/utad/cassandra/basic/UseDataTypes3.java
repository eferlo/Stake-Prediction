package com.utad.cassandra.basic;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.StringSerializer;
import com.utad.cassandra.util.Utils;


public class UseDataTypes3 {

	public static void main(String args[]) throws Exception {
		
		
		Keyspace ksUsers = Utils.getKeyspace("utad");

		// tipos para el 
				// 1. row key
				// 2. column key
				ColumnFamily<String, String> cfUsers = new ColumnFamily<String, String>(
						"users3", StringSerializer.get(), StringSerializer.get());
				
				String rowKey = "usersById";		

				ColumnList<String> result = ksUsers.prepareQuery(cfUsers)
						.getKey(rowKey).execute().getResult();
				if (!result.isEmpty()) {
					for (int i = 0; i < result.size(); i++) {
						String value = result.getColumnByIndex(i).getStringValue();
						System.out.println("email for user " + result.getColumnByIndex(i).getName() + " is: " + value);
					}
				}

	}
}
