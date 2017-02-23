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
import javax.inject.Named;
import javax.interceptor.Interceptors;

import org.apache.ibatis.session.SqlSession;

@Interceptors(LocalTransactionInterceptor.class)
@Transactional(rollbackFor = RollbackException.class)
public class FooService {

  @Inject
  @Named("manager1")
  private SqlSession sqlSession;

  @Inject
  @Named("manager1")
  private UserMapper userMapper;

  @Inject
  @MySpecialManager
  @OtherQualifier
  private UserMapper userMapper3;

  @Inject
  @Named("manager2")
  private UserMapper userMapper2;

  @Inject
  @Named("manager2")
  private UserMapper dummyUserMapper;

  @Inject
  @Named("unmanaged")
  private SqlSession unmanagedSqlSession;

  public User getUserFromSqlSession(int userId) {
    return this.sqlSession.selectOne("getUser", userId);
  }

  public User getUserFromUnmanagedSqlSession(int userId) {
    return this.unmanagedSqlSession.selectOne("getUser", userId);
  }

  public User getUser(int userId) {
    return this.userMapper.getUser(userId);
  }

  public User getUser2(int userId) {
    return this.userMapper2.getUser(userId);
  }

  public User getUserDummy(int userId) {
    return this.dummyUserMapper.getUser(userId);
  }

  public User getUser3(int userId) {
    return this.userMapper3.getUser(userId);
  }

  public void insertUser(User user) {
    this.userMapper.insertUser(user);
  }

  public void insertUserAndThrowARuntime(User user) {
    this.userMapper.insertUser(user);
    throw new RuntimeException("fail");
  }

  public void insertUserAndThrowACheckedThatShouldNotRollback(User user) throws NoRollbackException {
    this.userMapper.insertUser(user);
    throw new NoRollbackException();
  }

  public void insertUserAndThrowACheckedThatShouldRollback(User user) throws RollbackException {
    this.userMapper.insertUser(user);
    throw new RollbackException();
  }

}
