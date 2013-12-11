/*
 * Copyright 2013 MyBatis.org.
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

import javax.annotation.Resource;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSessionManager;

@Transactional
@Interceptor
public class JtaTransactionalInterceptor {
  
  @Resource
  UserTransaction transaction;
  
  @Inject 
  BeanManager beanManager;
  
  @AroundInvoke
  public Object invoke(InvocationContext ctx) throws Exception {
    Transactional t = ctx.getMethod().getAnnotation(Transactional.class);
    if (t == null) {
      t = ctx.getMethod().getDeclaringClass().getAnnotation(Transactional.class);
    }
    SqlSessionManager manager = findSqlSessionManager(t.manager());
    if (transaction.getStatus() == Status.STATUS_ACTIVE) {
      return ctx.proceed();
    }
    else {
      transaction.begin();
      manager.startManagedSession(t.executor(), t.level());
      Object result = null;
      try {
        result = ctx.proceed();
        transaction.commit();
      }
      catch (Throwable ex) {
        transaction.rollback();
        throw new RuntimeException(ExceptionUtil.unwrapThrowable(ex));
      }
      finally {
        manager.close();
      }
      return result;    
    }
  }
  
  private SqlSessionManager findSqlSessionManager(String name) {
    Bean b;
    if ("".equals(name) || name == null) {
      b = beanManager.resolve(beanManager.getBeans(SqlSessionManager.class));
    }
    else {
      b = beanManager.resolve(beanManager.getBeans(name));
    }
    if (b == null) {
      throw new MybatisCdiConfigurationException("There are no SqlSessionManager producers properly configured.");
    }
    else {
      return (SqlSessionManager)beanManager.getReference(b, SqlSessionManager.class, beanManager.createCreationalContext(b));
    }
  }
  
}
