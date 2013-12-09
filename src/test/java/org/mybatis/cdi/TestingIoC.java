package org.mybatis.cdi;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WeldJUnit4Runner.class)
public class TestingIoC {

  @Inject
  private FooService fooService;

  @Test
  public void shouldGetAUser() {
    Assert.assertEquals("User1", fooService.doSomeBusinessStuff(1).getName());
  }
}