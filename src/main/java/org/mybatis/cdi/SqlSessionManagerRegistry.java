/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.cdi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;

/**
 *
 * @author Frank D. Martinez [mnesarco]
 */
@ApplicationScoped
public class SqlSessionManagerRegistry {

  private Map<SqlSessionFactory, SqlSessionManager> managers;
  
  @Inject
  private BeanManager bm;
  
  @PostConstruct
  public void init() {
    Set<Bean<?>> beans = bm.getBeans(SqlSessionFactory.class, new AnnotationLiteral<Any>() {});
    if (beans.isEmpty()) {
      throw new MybatisCdiConfigurationException("There are no SqlSessionFactory producers properly configured.");
    } 
    else {
      Map<SqlSessionFactory, SqlSessionManager> m = new HashMap<SqlSessionFactory, SqlSessionManager>();
      for (Bean<?> bean : beans) {
        SqlSessionFactory factory =
          (SqlSessionFactory) bm.getReference(bean, SqlSessionFactory.class, bm.createCreationalContext(bean));
        SqlSessionManager manager = SqlSessionManager.newInstance(factory);
        m.put(factory, manager);
      }
      managers = Collections.unmodifiableMap(m);
    }
  }
  
  public SqlSessionManager getManager(SqlSessionFactory factory) {
    return managers.get(factory);
  }
  
  public Collection<SqlSessionManager> getManagers() {
    return managers.values();
  }

}
