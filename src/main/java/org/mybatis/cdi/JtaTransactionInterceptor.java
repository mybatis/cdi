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

import javax.annotation.Resource;
import javax.interceptor.Interceptor;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

/**
 * Interceptor for JTA transactions
 * 
 * @author Frank David Martínez
 */
@Transactional
@Interceptor
public class JtaTransactionInterceptor extends AbstractTransactionInterceptor {

  @Resource
  private UserTransaction transaction;

  @Override
  protected void start() throws Exception {
    if (transaction.getStatus() != Status.STATUS_ACTIVE) {
      transaction.begin();
    }
  }

  @Override
  protected void commit() throws Exception {
    transaction.commit();    
  }

  @Override
  protected void rollback() throws Exception {
    transaction.rollback();
  }

}
