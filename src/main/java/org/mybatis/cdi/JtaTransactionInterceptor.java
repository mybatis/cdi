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

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

/**
 * Interceptor for JTA transactions. MyBatis should be configured to use the
 * {@code MANAGED} transaction manager.
 *
 * @author Eduardo Macarrón
 */
@Transactional
@Interceptor
public class JtaTransactionInterceptor extends LocalTransactionInterceptor {

  @Inject
  private UserTransaction transaction;

  @AroundInvoke
  @Override
  public Object invoke(InvocationContext ctx) throws Throwable {
    boolean nested = transaction.getStatus() == Status.STATUS_ACTIVE;
    if (!nested) {
      transaction.begin();
    }
    Object result = null;
    try {
      result = super.invoke(ctx);
      if (!nested) {
        transaction.commit();
      }
    }
    catch (Throwable ex) {
      if (!nested) {
        transaction.rollback();
      }
    }
    return result;
  }

}
