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

import java.io.Reader;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class ConnectionFactory {
  public static SqlSessionFactory getSqlSessionFactory() {
    try {
      String resource = "org/mybatis/cdi/mybatis-config_5.xml";
      Reader reader = Resources.getResourceAsReader(resource);
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);

      // Automatically run database schema creation script on startup
      try (Connection conn = sqlSessionFactory.openSession().getConnection()) {
        ScriptRunner runner = new ScriptRunner(conn);
        runner.setLogWriter(null); // Optional: suppress script logs
        Reader schemaReader = Resources.getResourceAsReader("org/mybatis/cdi/CreateDB_5.sql");
        runner.runScript(schemaReader);
      }

      return sqlSessionFactory;
    } catch (Exception e) {
      throw new RuntimeException("Error building SqlSessionFactory and initializing schema.", e);
    }
  }
}
