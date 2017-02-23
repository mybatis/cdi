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

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSessionManager;

public class ManagerProducers {

  private SqlSessionFactory createSessionManager(int n) throws IOException {
    Reader reader = Resources.getResourceAsReader("org/mybatis/cdi/mybatis-config_" + n + ".xml");
    SqlSessionFactory manager = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    SqlSession session = manager.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/mybatis/cdi/CreateDB_" + n + ".sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();

    return manager;
  }

  private SqlSessionFactory createSessionManagerJTA() throws IOException {
    Reader reader = Resources.getResourceAsReader("org/mybatis/cdi/mybatis-config_jta.xml");
    SqlSessionFactory manager = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    SqlSession session = manager.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/mybatis/cdi/CreateDB_JTA.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();

    return manager;
  }

  @ApplicationScoped
  @Named("manager1")
  @Produces
  @SessionFactoryProvider
  public SqlSessionFactory createManager1() throws IOException {
    return createSessionManager(1);
  }

  @ApplicationScoped
  @Named("manager2")
  @Produces
  @SessionFactoryProvider
  public SqlSessionFactory createManager2() throws IOException {
    return createSessionManager(2);
  }

  @ApplicationScoped
  @Produces
  @MySpecialManager
  @OtherQualifier
  @SessionFactoryProvider
  public SqlSessionFactory createManager3() throws IOException {
    return createSessionManager(3);
  }

  @ApplicationScoped
  @Produces
  @JtaManager
  @SessionFactoryProvider
  public SqlSessionFactory createManagerJTA() throws IOException {
    return createSessionManagerJTA();
  }

  @ApplicationScoped
  @Produces
  @Named("unmanaged")
  public SqlSession createNonCdiManagedSession() throws IOException {
    return SqlSessionManager.newInstance(createSessionManager(4));
  }

  @ApplicationScoped
  @Named("unmanaged")
  public void closeNonCdiManagedSession(@Disposes SqlSession session) {
    session.close();
  }

}
