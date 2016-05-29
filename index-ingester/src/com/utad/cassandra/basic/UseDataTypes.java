package com.utad.cassandra.basic;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.ColumnListMutation;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.StringSerializer;
import com.utad.cassandra.util.Utils;

public class UseDataTypes {

	public static void main(String args[]) throws Exception {

		Keyspace ksUsers = Utils.getKeyspace("utad");
		
		// tipos para el 
		// 1. row key
		// 2. column key
		ColumnFamily<String, String> cfUsers = new ColumnFamily<String, String>(
				"users2", StringSerializer.get(), StringSerializer.get());
		
		String rowKey = "usersById";		
		try {
			ksUsers.createColumnFamily(
					cfUsers,
					ImmutableMap.<String, Object> builder()
							// cómo almacenará cassandra internamente las column keys
							.put("key_validation_class", "BytesType")
							// cómo se ordenarán las column keys
							.put("comparator_type", "BytesType").build());
			
			
			MutationBatch m = ksUsers.prepareMutationBatch();
			
			ColumnListMutation<String> clm = m.withRow(cfUsers, rowKey);

			for (int i = 0; i < 20; i++) {
				clm.putColumn(i + "", "user" + i + "@void.com");
			}

			m.execute();
			
		} catch (Exception e) {
			System.out.println("ya existe el column family users2");
		}

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
