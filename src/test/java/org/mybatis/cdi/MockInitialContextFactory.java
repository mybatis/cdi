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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
* Provides a means to mock InitialContext JNDI lookups.
* Also see src/test/resources/jndi.properties
*
* @author Michael Lynch [mwlynch]
*/
public class MockInitialContextFactory implements InitialContextFactory {

  @Mock
  public static InitialContext initialContext;

  public MockInitialContextFactory() {
    if (initialContext == null) {
      synchronized (MockInitialContextFactory.class) {
        if (initialContext == null) {
          MockitoAnnotations.initMocks(this);
        }
      }
    }
  }

  @Override
  public Context getInitialContext(Hashtable<?, ?> envmt) throws NamingException {
    return initialContext;
  }

}
