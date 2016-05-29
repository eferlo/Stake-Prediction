package com.utad.cassandra.basic;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.query.RowQuery;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.util.RangeBuilder;
import com.utad.cassandra.util.Utils;

public class Pagination2 {
	public static void main(String args[]) throws ConnectionException {

	int pageSize = 10000;
		
		Keyspace ksUsers = Utils.getKeyspace("utad");

		
		String rowKey = "usersById";

		ColumnFamily<String, Integer> cfUsers = new ColumnFamily<String, Integer>(
				"users", StringSerializer.get(), IntegerSerializer.get());
		
		// leer los datos

		RowQuery<String,Integer> query = ksUsers.prepareQuery(cfUsers)
				.getKey(rowKey).autoPaginate(true).withColumnRange(new RangeBuilder().setLimit(pageSize).build());
		
		ColumnList <Integer> columns;
		while (!(columns = query.execute().getResult()).isEmpty()) {
			for (int i = 0; i < columns.size(); i++) {
				String value = columns.getColumnByIndex(i).getStringValue();
				System.out.println("email for user " + i + " is: " + value);
			}
		}
		
		System.out.println("finished reading paginated values");
	}
}
