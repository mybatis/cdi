/*
 *    Copyright 2013-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.cdi;

import com.arjuna.ats.internal.jdbc.ConnectionManager;
import com.arjuna.ats.jdbc.TransactionalDriver;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.sql.XADataSource;

public class NarayanaDataSourceWrapper implements DataSource {
  private final XADataSource xaDataSource;

  public NarayanaDataSourceWrapper(final XADataSource xaDataSource) {
    this.xaDataSource = xaDataSource;
  }

  @Override
  public Connection getConnection() throws SQLException {
    final var properties = new Properties();
    properties.put(TransactionalDriver.XADataSource, xaDataSource);
    return ConnectionManager.create(null, properties);
  }

  @Override
  public Connection getConnection(final String username, final String password) throws SQLException {
    final var properties = new Properties();
    properties.put(TransactionalDriver.XADataSource, xaDataSource);
    properties.put(TransactionalDriver.userName, username);
    properties.put(TransactionalDriver.password, password);
    return ConnectionManager.create(null, properties);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return xaDataSource.getLoginTimeout();
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return xaDataSource.getLogWriter();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return xaDataSource.getParentLogger();
  }

  @Override
  public boolean isWrapperFor(final Class<?> iface) throws SQLException {
    return iface.isAssignableFrom(getClass());
  }

  @Override
  public void setLoginTimeout(final int seconds) throws SQLException {
    xaDataSource.setLoginTimeout(seconds);
  }

  @Override
  public void setLogWriter(final PrintWriter out) throws SQLException {
    xaDataSource.setLogWriter(out);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T unwrap(final Class<T> iface) throws SQLException {
    if (isWrapperFor(iface)) {
      return (T) this;
    }
    throw new SQLException(getClass() + " is not a wrapper for " + iface);
  }
}
