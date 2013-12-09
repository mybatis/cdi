package org.mybatis.cdi;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;

@ApplicationScoped
public class ManagerProducers {

  @Produces
  public SqlSessionManager createManager1() throws IOException {    
    Reader reader = Resources.getResourceAsReader("org/mybatis/cdi/mybatis-config.xml");    
    SqlSessionManager manager = SqlSessionManager.newInstance(reader);
    reader.close();
    
    SqlSession session = manager.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/mybatis/cdi/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
    
    return manager;
  }
}
