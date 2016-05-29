package com.utad.cassandra.basic;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.StringSerializer;
import com.utad.cassandra.util.Utils;

public class CompositeKeys1 {
	public static void main(String args[]) throws ConnectionException {

		// productos visitados por el usuario 1
		String[] products1 = { "1", "2", "3", "1", "2" };
		// productos visitados por el usuario 2
		String[] products2 = { "1", "1", "3", "1", "6" };
		// productos visitados por el usuario 3
		String[] products3 = { "1", "2", "5", "7", "8" };
		// productos visitados por el usuario 4
		String[] products4 = { "1", "2", "2", "8", "9" };
		// productos visitados por el usuario 5
		String[] products5 = { "4", "5", "6", "7", "7" };

		String[][] userVisitsProduct = { products1, products2, products3,
				products4, products5 };


		Keyspace ksUsers = Utils.getKeyspace("utad");
		
		
		// tipos para el 
		// 1. row key
		// 2. column key
		ColumnFamily<String, String> cfUsersProducts = new ColumnFamily<String, String>(
				"usersproducts2", StringSerializer.get(), StringSerializer.get());
	
		ksUsers.dropColumnFamily("usersproducts2");
		
		try {
			ksUsers.createColumnFamily(
					cfUsersProducts,
					ImmutableMap.<String, Object> builder()
							// cómo almacenará cassandra internamente las column keys
							.put("default_validation_class", "CounterColumnType")
							// cómo se ordenarán las column keys
							.put("replicate_on_write", true)
							.put("comparator_type", "BytesType").build());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ya existe el column family users3");
		}		


		String rowKey = "userById";
		
		for (int i = 0; i < userVisitsProduct.length; i++) {
			String user = (i + 1) + "";
			
			for (int j=0; j < userVisitsProduct[i].length;j++){
				ksUsers.prepareColumnMutation(cfUsersProducts, rowKey,user+":"+userVisitsProduct[i][j]).incrementCounterColumn(1).execute();

				
			}
		}
		

		
	}
}
