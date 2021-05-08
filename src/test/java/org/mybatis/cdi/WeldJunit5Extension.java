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

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstantiationException;

//TODO Remove this class once we drop cdi 1.1 support
public class WeldJunit5Extension implements TestInstanceFactory {

  private Weld weld;
  private WeldContainer container;

  // This constructor is invoked by JUnit Jupiter via reflection or ServiceLoader.
  public WeldJunit5Extension() {
    this.weld = new Weld();
    this.container = this.weld.initialize();
  }

  @Override
  public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
      throws TestInstantiationException {
    return this.container.instance().select(factoryContext.getTestClass()).get();
  }

}
