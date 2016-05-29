package com.utad.cassandra.basic;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.util.RangeBuilder;
import com.utad.cassandra.util.Utils;

public class CompositeKeys2 {
	public static void main(String args[]) throws ConnectionException {


		Keyspace ksUsers = Utils.getKeyspace("utad");
		String rowKey = "userById";
		
		// tipos para el 
		// 1. row key
		// 2. column key
		ColumnFamily<String, String> cfUsersProducts = new ColumnFamily<String, String>(
				"usersproducts2", StringSerializer.get(), StringSerializer.get());
		
		ColumnList<String> result = ksUsers.prepareQuery(cfUsersProducts)
				.getKey(rowKey).withColumnRange(new RangeBuilder().setStart("3:").setEnd("3:\uffff").build()).execute().getResult();
		
		if (!result.isEmpty()) {
			for (int i = 0; i < result.size(); i++) {
				
				Long value = result.getColumnByIndex(i).getLongValue();
				System.out.println("Product " + result.getColumnByIndex(i).getName() + " is: " + value);
			}
		}
		

	}
}
