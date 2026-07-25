/*
 *    Copyright 2013-2026 the original author or authors.
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

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.DataSourceFactory;
import org.h2.jdbcx.JdbcDataSource;

public class JtaDatasourceFactory implements DataSourceFactory {
  private final DataSource dataSource;
  private final JdbcDataSource h2DataSource;

  public JtaDatasourceFactory() {
    this.h2DataSource = new JdbcDataSource();
    this.dataSource = new NarayanaDataSourceWrapper(h2DataSource);
  }

  @Override
  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public void setProperties(final Properties properties) {
    h2DataSource.setURL(properties.getProperty("URL"));
  }
}
