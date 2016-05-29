package com.utad.cassandra.util;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

public class Utils {

	public static String clusterName = "utad";

	public static Keyspace keyspace = null;

	public static Keyspace getKeyspace(String server, String port,String name) {

		AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder().forCluster(clusterName).forKeyspace(name)
				.withAstyanaxConfiguration(
						new AstyanaxConfigurationImpl().setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE))
				.withConnectionPoolConfiguration(new ConnectionPoolConfigurationImpl("MyConnectionPool").setPort(Integer.intValue(port))
						.setMaxConnsPerHost(1).setSeeds(server+":"port))
				.withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
				.buildKeyspace(ThriftFamilyFactory.getInstance());

		context.start();

		keyspace = context.getClient();

		return keyspace;
	}
}
