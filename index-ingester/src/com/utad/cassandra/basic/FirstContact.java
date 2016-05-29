package com.utad.cassandra.basic;

import com.google.common.collect.ImmutableMap;
import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

public class FirstContact {

	public static void main(String args[]) throws ConnectionException {

		String clusterName = "utad";
		String keyspaceName = "utad";
		String columnFamilyName = "users";

		AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
				.forCluster(clusterName)
				.forKeyspace(keyspaceName)
				.withAstyanaxConfiguration(
						new AstyanaxConfigurationImpl()
								.setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE))
				.withConnectionPoolConfiguration(
						new ConnectionPoolConfigurationImpl("MyConnectionPool")
								.setPort(9160).setMaxConnsPerHost(1)
								.setSeeds("127.0.0.1:9160"))
				.withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
				.buildKeyspace(ThriftFamilyFactory.getInstance());

		context.start();

		Keyspace ksUsers = context.getClient();

		try {
			ksUsers.createKeyspace(ImmutableMap
					.<String, Object> builder()
					.put("strategy_options",
							ImmutableMap.<String, Object> builder()
									.put("replication_factor", "1").build())
					.put("strategy_class", "SimpleStrategy").build());

			
		} catch (Exception e) {
			System.out.println("ya existe el keyspace " + keyspaceName);
		}
		
		
		
		ColumnFamily<String, String> cfUsers = new ColumnFamily<String, String>(
				columnFamilyName, StringSerializer.get(), StringSerializer.get());

		
		
		try {
			ksUsers.createColumnFamily(
					cfUsers,
					ImmutableMap.<String, Object> builder()
							.put("key_validation_class", "BytesType")
							.put("comparator_type", "BytesType").build());
		} catch (Exception e) {
			System.out.println("ya existe el column family " + columnFamilyName);
		}

		// escribir un valor
		ksUsers.prepareColumnMutation(cfUsers, "usersById", "1")
				.putValue("user1@void.com", null).execute();

		// leer un valor
		Column<String> result = ksUsers.prepareQuery(cfUsers)
				.getKey("usersById").getColumn("1").execute().getResult();
		String value = result.getStringValue();

		System.out.println("email for user 1 is: " + value);
	}
}
