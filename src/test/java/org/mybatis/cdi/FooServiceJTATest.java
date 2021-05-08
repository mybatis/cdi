/*
 *    Copyright 2013-2021 the original author or authors.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@TestInstance(Lifecycle.PER_CLASS)
// TODO Add the following once we drop cdi 1.1 support
// @EnableWeld
// TODO Remove the following once we drop cdi 1.1 support
@ExtendWith(WeldJunit5Extension.class)
public class FooServiceJTATest {

  // TODO Add the following once we drop cdi 1.1 support
  // @WeldSetup
  // public WeldInitiator weld = WeldInitiator.of(new Weld());

  @Inject
  private FooServiceJTA fooServiceJTA;

  @Inject
  private UserTransaction userTransaction;

  @Test
  public void jtaShouldGetAUserWithNoTX() throws Exception {
    this.userTransaction.begin();
    Assertions.assertEquals("1-User1", this.fooServiceJTA.getUserWithNoTransaction(1).getName());
    this.userTransaction.commit();
  }

  @Test
  public void jtaShouldInsertAUserAndCommit() {
    User user = new User();
    user.setId(20);
    user.setName("User20");
    this.fooServiceJTA.insertUserWithTransactional(user);
    Assertions.assertEquals(user.getName(), this.fooServiceJTA.getUserWithNoTransaction(user.getId()).getName());
  }

  @Test
  public void jtaShouldInsertAUserAndRollItBack() {
    User user = new User();
    user.setId(30);
    user.setName("User30");
    try {
      this.fooServiceJTA.insertUserWithTransactionalAndFail(user);
    } catch (Exception ignore) {
      // ignored
    }
    Assertions.assertNull(this.fooServiceJTA.getUserWithNoTransaction(user.getId()));
  }

  @Test
  public void jtaShouldInsertAUserWithExistingJtaTxAndCommit() throws Exception {
    User user = new User();
    user.setId(40);
    user.setName("User40");
    this.userTransaction.begin();
    this.fooServiceJTA.insertUserWithTransactional(user);
    this.userTransaction.commit();
    Assertions.assertEquals(user.getName(), this.fooServiceJTA.getUserWithNoTransaction(user.getId()).getName());
  }

  @Test
  public void jtaShouldInsertAUserWithExistingJtaTxAndRollItBack() throws Exception {
    User user = new User();
    user.setId(50);
    user.setName("User50");
    this.userTransaction.begin();
    this.fooServiceJTA.insertUserWithTransactional(user);
    this.userTransaction.rollback();
    Assertions.assertNull(this.fooServiceJTA.getUserWithNoTransaction(user.getId()));
  }

}