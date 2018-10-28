/**
 *    Copyright 2013-2018 the original author or authors.
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@EnableWeld
public class TestingIoC {

  @Inject
  private FooService fooService;

  @Inject
  private SerializableFooService serFooService;

  @WeldSetup
  public WeldInitiator weld = WeldInitiator.of(new Weld());

  @Test
  public void shouldGetAUser() {
    Assertions.assertEquals("1-User1", this.fooService.getUserFromSqlSession(1).getName());
    Assertions.assertEquals("1-User1", this.fooService.getUser(1).getName());
    Assertions.assertEquals("2-User2", this.fooService.getUser2(2).getName());
    Assertions.assertEquals("3-User3", this.fooService.getUser3(3).getName());
    Assertions.assertEquals("4-User1", this.fooService.getUserFromUnmanagedSqlSession(1).getName());
  }

  @Test
  public void shouldInjectTheSameMapper() {
    Assertions.assertEquals(this.fooService.getUser2(1).getName(), this.fooService.getUserDummy(1).getName());
    Assertions.assertEquals(this.fooService.getUser2(2).getName(), this.fooService.getUserDummy(2).getName());
    Assertions.assertEquals(this.fooService.getUser2(3).getName(), this.fooService.getUserDummy(3).getName());
  }

  @Test
  public void shouldInsertAUserAndCommit() {
    User user = new User();
    user.setId(20);
    user.setName("User20");
    this.fooService.insertUser(user);
    Assertions.assertEquals("User20", this.fooService.getUser(20).getName());
  }

  @Test
  public void shouldInsertAUserThatFailsWithRuntimeAndRollItBack() {
    User user = new User();
    user.setId(30);
    user.setName("User40");
    try {
      this.fooService.insertUserAndThrowARuntime(user);
    } catch (Exception ignore) {
      // ignored
    }
    Assertions.assertNull(this.fooService.getUser(40));
  }

  @Test
  public void shouldInsertAUserThatFailsWithACheckedAndCommit() {
    User user = new User();
    user.setId(30);
    user.setName("User30");
    try {
      this.fooService.insertUserAndThrowACheckedThatShouldNotRollback(user);
    } catch (Exception ignore) {
      // ignored
    }
    Assertions.assertEquals("User30", this.fooService.getUser(30).getName());
  }

  @Test
  public void shouldInsertAUserThatFailsWithACustomExceptionMarkedToRollbackAndRollItBack() {
    User user = new User();
    user.setId(30);
    user.setName("User30");
    try {
      this.fooService.insertUserAndThrowACheckedThatShouldRollback(user);
    } catch (Exception ignore) {
      // ignored
    }
    Assertions.assertNull(this.fooService.getUser(30));
  }

  // TEST JTA

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

  @Test
  public void injectedMappersAreSerializable() throws Exception {
    ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream("target/mapper.ser"));
    oout.writeObject(this.serFooService);
    oout.close();
    ObjectInputStream oin = new ObjectInputStream(new FileInputStream("target/mapper.ser"));
    SerializableFooService unserialized = (SerializableFooService) oin.readObject();
    oin.close();
    Assertions.assertEquals(this.serFooService.getUser(1).getName(), unserialized.getUser(1).getName());
  }

}