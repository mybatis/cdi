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

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import javax.annotation.PostConstruct;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;

@ApplicationScoped
public class ManagerProducers {

  private SqlSessionManager manager1;

  private SqlSessionManager manager2;

  private SqlSessionManager manager3;

  @PostConstruct
  public void init() {
    try {
      manager1 = createSessionManager(1);
      manager2 = createSessionManager(2);
      manager3 = createSessionManager(3);
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private SqlSessionManager createSessionManager(int n) throws IOException {
    Reader reader = Resources.getResourceAsReader("org/mybatis/cdi/mybatis-config_" + n + ".xml");
    SqlSessionManager manager = SqlSessionManager.newInstance(reader);
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

  @Named("manager1")
  @Produces
  public SqlSessionManager createManager1() throws IOException {
    return manager1;
  }

  @Named("manager2")
  @Produces
  public SqlSessionManager createManager2() throws IOException {
    return manager2;
  }

  @Produces
  @MySpecialManager
  @OtherQualifier
  public SqlSessionManager createManager3() throws IOException {
    return manager3;
  }

  public void disposes(@Disposes SqlSessionManager m) {
    assert m.isManagedSessionStarted() == false : "Leaked SqlSession";
  }

}
