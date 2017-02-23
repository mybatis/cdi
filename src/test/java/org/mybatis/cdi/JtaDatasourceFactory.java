/**
 *    Copyright 2013-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class JtaDatasourceFactory implements DataSourceFactory {

  PoolingDataSource ds;

  public JtaDatasourceFactory() {
    this.ds = new PoolingDataSource();
  }

  @Override
  public void setProperties(Properties props) {
    this.ds.setUniqueName(props.getProperty("resourceName"));
    props.remove("resourceName");
    this.ds.setClassName(props.getProperty("driver"));
    props.remove("driver");
    this.ds.setMaxPoolSize(Integer.valueOf(props.getProperty("maxPoolSize")));
    props.remove("maxPoolSize");
    this.ds.setAllowLocalTransactions(Boolean.valueOf(props.getProperty("allowLocalTransactions")));
    props.remove("allowLocalTransactions");
    this.ds.setDriverProperties(props);
  }

  @Override
  public DataSource getDataSource() {
    return this.ds;
  }

}
