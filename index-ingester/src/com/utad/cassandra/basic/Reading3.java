package com.utad.cassandra.basic;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.query.RowQuery;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.util.RangeBuilder;
import com.utad.cassandra.util.Utils;

public class Reading3 {

	public static void main(String args[]) throws ConnectionException {
		Keyspace ksUsers = Utils.getKeyspace("utad");
		
		// tipos para el 
		// 1. row key
		// 2. column key
		ColumnFamily<String, Integer> cfUsers = new ColumnFamily<String, Integer>(
		"users", StringSerializer.get(), IntegerSerializer.get());

		
		String rowKey = "usersById";
		
		RowQuery<String, Integer> query = ksUsers.prepareQuery(cfUsers).getKey(rowKey)
				.withColumnRange(new RangeBuilder().setStart(0).setStart(50).build());
		
		ColumnList<Integer> columns = query.execute().getResult();
				
		for  (Column<Integer> c : columns) {
				System.out.println("email for user "+c.getName() + " is: " + c.getStringValue());
		}

	}
}
