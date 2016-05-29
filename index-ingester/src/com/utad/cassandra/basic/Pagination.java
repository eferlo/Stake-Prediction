package com.utad.cassandra.basic;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.ColumnListMutation;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.utad.cassandra.util.Utils;

public class Pagination {
	public static void main(String args[]) throws ConnectionException {

		Keyspace ksUsers = Utils.getKeyspace("utad");

		ksUsers.dropColumnFamily("users");

		ColumnFamily<String, Integer> cfUsers = new ColumnFamily<String, Integer>(
				"users", StringSerializer.get(), IntegerSerializer.get());

		ksUsers.createColumnFamily(
				cfUsers,
				ImmutableMap.<String, Object> builder()
						.put("key_validation_class", "IntegerType")
						.put("comparator_type", "IntegerType").build());

		MutationBatch m = ksUsers.prepareMutationBatch();
		String rowKey = "usersById";

		ColumnListMutation<Integer> clm = m.withRow(cfUsers, rowKey);
		for (int i = 1; i <= 100000; i++) {
			clm.putColumn(i, "user" + i + "@void.com");
		}

		m.execute();

		System.out.println("finished writing");

		ColumnList<Integer> result = ksUsers.prepareQuery(cfUsers)
				.getKey(rowKey).execute().getResult();
		if (!result.isEmpty()) {
			for (int i = 0; i < result.size(); i++) {
				String value = result.getColumnByIndex(i).getStringValue();
				System.out.println("email for user " + i + " is: " + value);
			}
		}

		System.out.println("finished reading values");
	}
}
