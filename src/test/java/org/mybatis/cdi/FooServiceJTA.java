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

import javax.inject.Inject;
import javax.interceptor.Interceptors;

public class FooServiceJTA {

  @Inject
  @JtaManager
  private UserMapper userMapper;

  @Interceptors(JtaTransactionInterceptor.class)
  @Transactional
  public User getUserWithNoTransaction(int userId) {
    return this.userMapper.getUser(userId);
  }

  @Interceptors(JtaTransactionInterceptor.class)
  @Transactional
  public void insertUserWithTransactional(User user) {
    this.userMapper.insertUser(user);
  }

  @Interceptors(JtaTransactionInterceptor.class)
  @Transactional
  public void insertUserWithTransactionalAndFail(User user) {
    this.userMapper.insertUser(user);
    throw new RuntimeException("fail");
  }

}
