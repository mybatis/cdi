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

import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WeldJUnit4Runner.class)
public class TestingIoC {

  @Inject
  private FooService fooService;

  @Test
  public void shouldGetAUser() {
    Assert.assertEquals("1-User1", fooService.getUserFromSqlSession(1).getName());
    Assert.assertEquals("1-User1", fooService.getUser(1).getName());
    Assert.assertEquals("2-User2", fooService.getUser2(2).getName());
    Assert.assertEquals("3-User3", fooService.getUser3(3).getName());
  }

  @Test
  public void shouldInjectTheSameMapper() {
    Assert.assertEquals(fooService.getUser2(1).getName(), fooService.getUserDummy(1).getName());
    Assert.assertEquals(fooService.getUser2(2).getName(), fooService.getUserDummy(2).getName());
    Assert.assertEquals(fooService.getUser2(3).getName(), fooService.getUserDummy(3).getName());
  }

  @Test
  public void shouldInsertAUserAndCommit() {
    User user = new User();
    user.setId(20);
    user.setName("User20");
    fooService.insertUser(user);
    Assert.assertEquals("User20", fooService.getUser(20).getName());
  }

  @Test
  public void shouldInsertAUserThatFailsWithRuntimeAndRollItBack() {
    User user = new User();
    user.setId(30);
    user.setName("User40");
    try {
      fooService.insertUserAndThrowARuntime(user);
    } catch (Exception ignore) {
      // ignored
    }
    Assert.assertNull(fooService.getUser(40));
  }

  @Test
  public void shouldInsertAUserThatFailsWithACheckedAndCommit() {
    User user = new User();
    user.setId(30);
    user.setName("User30");
    try {
      fooService.insertUserAndThrowACheckedThatShouldNotRollback(user);
    } catch (Exception ignore) {
      // ignored
    }
    Assert.assertEquals("User30", fooService.getUser(30).getName());
  }

  @Test
  public void shouldInsertAUserThatFailsWithACustomExceptionMarkedToRollbackAndRollItBack() {
    User user = new User();
    user.setId(30);
    user.setName("User30");
    try {
      fooService.insertUserAndThrowACheckedThatShouldRollback(user);
    } catch (Exception ignore) {
      // ignored
    }
    Assert.assertNull(fooService.getUser(30));
  }

  // TEST JTA

  @Inject
  private FooServiceJTA fooServiceJTA;

  @Inject
  private UserTransaction userTransaction;

  @Test
  public void jtaShouldGetAUserWithNoTX() throws Exception {
    userTransaction.begin();
    Assert.assertEquals("1-User1", fooServiceJTA.getUserWithNoTransaction(1).getName());
    userTransaction.commit();
  }

  @Test
  public void jtaShouldInsertAUserAndCommit() {
    User user = new User();
    user.setId(20);
    user.setName("User20");
    fooServiceJTA.insertUserWithTransactional(user);
    Assert.assertEquals(user.getName(), fooServiceJTA.getUserWithNoTransaction(user.getId()).getName());
  }

  @Test
  public void jtaShouldInsertAUserAndRollItBack() {
    User user = new User();
    user.setId(30);
    user.setName("User30");
    try {
      fooServiceJTA.insertUserWithTransactionalAndFail(user);
    } catch (Exception ignore) {
      // ignored
    }
    Assert.assertNull(fooServiceJTA.getUserWithNoTransaction(user.getId()));
  }

  @Test
  public void jtaShouldInsertAUserWithExistingJtaTxAndCommit() throws Exception {
    User user = new User();
    user.setId(40);
    user.setName("User40");
    userTransaction.begin();
    fooServiceJTA.insertUserWithTransactional(user);
    userTransaction.commit();
    Assert.assertEquals(user.getName(), fooServiceJTA.getUserWithNoTransaction(user.getId()).getName());
  }

  @Test
  public void jtaShouldInsertAUserWithExistingJtaTxAndRollItBack() throws Exception {
    User user = new User();
    user.setId(50);
    user.setName("User50");
    userTransaction.begin();
    fooServiceJTA.insertUserWithTransactional(user);
    userTransaction.rollback();
    Assert.assertNull(fooServiceJTA.getUserWithNoTransaction(user.getId()));
  }

}