/*
 *    Copyright 2013 the original author or authors.
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

import com.atomikos.jdbc.AtomikosDataSourceBean;

public class AtomikosDatasourceFactory implements DataSourceFactory {

  AtomikosDataSourceBean ds; 
  
  public AtomikosDatasourceFactory() {
    ds = new AtomikosDataSourceBean(); 
  }
  
  public void setProperties(Properties props) {
    ds.setUniqueResourceName(props.getProperty("uniqueResourceName"));
    props.remove("uniqueResourceName");
    ds.setXaDataSourceClassName(props.getProperty("xaDataSourceClassName"));
    props.remove("xaDataSourceClassName");
    ds.setXaProperties(props);
  }

  public DataSource getDataSource() {
    return ds;
  }

}
