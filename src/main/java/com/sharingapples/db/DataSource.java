package com.sharingapples.db;

import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by ranjan on 1/26/16.
 */
public class DataSource {
  private final PoolingDataSource<PoolableConnection> dataSource;

  public DataSource(int poolSize, String connectionURI, Properties connectionProps) {
    ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
            connectionURI, connectionProps
    );

    PoolableConnectionFactory poolableConnectionFactory =
            new PoolableConnectionFactory(connectionFactory, null);

    GenericObjectPoolConfig config = new GenericObjectPoolConfig();
    config.setMaxTotal(poolSize);

    ObjectPool<PoolableConnection> connectionPool =
            new GenericObjectPool<>(poolableConnectionFactory, config);

    poolableConnectionFactory.setPool(connectionPool);

    this.dataSource =
            new PoolingDataSource<>(connectionPool);
  }

  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }
}
