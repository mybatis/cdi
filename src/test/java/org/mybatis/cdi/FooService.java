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
import javax.inject.Named;

@Transactional
public class FooService {

  @Inject
  @Mapper
  private UserMapper userMapper;
  
  @Inject
  @Mapper
  //@Named("manager2.UserMapper")
  private UserMapper userMapper2;

  public User doSomeBusinessStuff(int userId) {
    return this.userMapper.getUser(userId);
  }

  public User doSomeBusinessStuff2(int userId) {
    return this.userMapper2.getUser(userId);
  }

}
