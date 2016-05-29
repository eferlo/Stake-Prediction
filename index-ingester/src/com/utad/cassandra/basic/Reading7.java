package com.utad.cassandra.basic;

import java.util.ArrayList;
import java.util.List;

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

public class Reading7 {

public static void main(String args[]) throws ConnectionException {
		
	List<Integer> ids1 = new ArrayList<Integer>();
	ids1.add(1);
	ids1.add(3);
	ids1.add(5);
	ids1.add(7);
	ids1.add(11);
		
	Keyspace ksUsers = Utils.getKeyspace("utad");
		
		// tipos para el 
		// 1. row key
		// 2. column key
		ColumnFamily<String, Integer> cfUsers = new ColumnFamily<String, Integer>(
		"users", StringSerializer.get(), IntegerSerializer.get());

		
		String rowKey = "usersById";
		
		RowQuery<String, Integer> query = ksUsers.prepareQuery(cfUsers).getKey(rowKey).withColumnSlice(ids1).withColumnRange(new RangeBuilder().setStart(5).build());
		
		ColumnList<Integer> columns = query.execute().getResult();
				
		for  (Column<Integer> c : columns) {
				System.out.println("email for user "+c.getName() + " is: " + c.getStringValue());
		}

	}
}