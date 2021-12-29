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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.inject.Inject;

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
class FooServiceTest {

  // TODO Add the following once we drop cdi 1.1 support
  // @WeldSetup
  // public WeldInitiator weld = WeldInitiator.of(new Weld());

  @Inject
  private FooService fooService;

  @Inject
  private SerializableFooService serFooService;

  @Test
  void shouldGetAUser() {
    Assertions.assertEquals("1-User1", this.fooService.getUserFromSqlSession(1).getName());
    Assertions.assertEquals("1-User1", this.fooService.getUser(1).getName());
    Assertions.assertEquals("2-User2", this.fooService.getUser2(2).getName());
    Assertions.assertEquals("3-User3", this.fooService.getUser3(3).getName());
    Assertions.assertEquals("4-User1", this.fooService.getUserFromUnmanagedSqlSession(1).getName());
  }

  @Test
  void shouldInjectTheSameMapper() {
    Assertions.assertEquals(this.fooService.getUser2(1).getName(), this.fooService.getUserDummy(1).getName());
    Assertions.assertEquals(this.fooService.getUser2(2).getName(), this.fooService.getUserDummy(2).getName());
    Assertions.assertEquals(this.fooService.getUser2(3).getName(), this.fooService.getUserDummy(3).getName());
  }

  @Test
  void shouldInsertAUserAndCommit() {
    User user = new User();
    user.setId(20);
    user.setName("User20");
    this.fooService.insertUser(user);
    Assertions.assertEquals("User20", this.fooService.getUser(20).getName());
  }

  @Test
  void shouldInsertAUserThatFailsWithRuntimeAndRollItBack() {
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
  void shouldInsertAUserThatFailsWithACheckedAndCommit() {
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
  void shouldInsertAUserThatFailsWithACustomExceptionMarkedToRollbackAndRollItBack() {
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

  @Test
  void injectedMappersAreSerializable() throws Exception {
    try (ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream("target/mapper.ser"))) {
      oout.writeObject(this.serFooService);
    }
    try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream("target/mapper.ser"))) {
      SerializableFooService unserialized = (SerializableFooService) oin.readObject();
      Assertions.assertEquals(this.serFooService.getUser(1).getName(), unserialized.getUser(1).getName());
    }
  }

}
