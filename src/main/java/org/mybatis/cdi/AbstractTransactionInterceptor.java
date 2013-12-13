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

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSessionManager;

/**
 * 
 * @author Eduardo Macarrón
 */
public abstract class AbstractTransactionInterceptor {

  @Inject
  private BeanManager beanManager;

  @AroundInvoke
  public Object invoke(InvocationContext ctx) throws Exception {
    Transactional t = getTransactionalAnnotation(ctx);
    Set<SqlSessionManager> managers = findSqlSessionManagers();
    start();
    boolean started = start(managers, t);
    Object result;
    try {
      result = ctx.proceed();
      if (started && !t.rollbackOnly()) {
        commit(managers, t);
        commit();
      }
    } catch (Throwable ex) {
      if (started) rollback();
      throw new RuntimeException(ExceptionUtil.unwrapThrowable(ex));
    } finally {
      if (started) close(managers);
    }
    return result;
  }
  
  abstract protected void start() throws Exception;
  abstract protected void commit() throws Exception;
  abstract protected void rollback() throws Exception;
  
  protected Transactional getTransactionalAnnotation(InvocationContext ctx) {
    Transactional t = ctx.getMethod().getAnnotation(Transactional.class);
    if (t == null) {
      t = ctx.getMethod().getDeclaringClass().getAnnotation(Transactional.class);
    }
    return t;
  }

  private Set<SqlSessionManager> findSqlSessionManagers() {
    Set<Bean<?>> beans = beanManager.getBeans(SqlSessionManager.class);
    Set<SqlSessionManager> managers = new HashSet<SqlSessionManager>();
    for (Bean bean : beans) {
      SqlSessionManager manager = (SqlSessionManager) beanManager.getReference(
          bean, SqlSessionManager.class,
          beanManager.createCreationalContext(bean));
      managers.add(manager);
    }
    return managers;
  }

  private boolean start(Set<SqlSessionManager> managers, Transactional t) {
    boolean started = false;
    for (SqlSessionManager manager : managers) {
      if (!manager.isManagedSessionStarted()) {
        manager.startManagedSession(t.executorType(), t.isolation().getTransactionIsolationLevel());
        started = true;
      }
    }
    return started;
  }

  private void commit(Set<SqlSessionManager> managers, Transactional t) {
    for (SqlSessionManager manager : managers) {
      manager.commit(t.force());
    }
  }

  private void close(Set<SqlSessionManager> managers) {
    for (SqlSessionManager manager : managers) {
      manager.close();
    }
  }

}
