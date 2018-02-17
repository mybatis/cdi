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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.ibatis.session.SqlSessionFactory;

/**
 *
 * @author Frank D. Martinez [mnesarco]
 */
public final class CDIUtils {

  private static class CDI {

    private static final Method getBeanManager;

    private static final Method current;

    private static final String DEFAULT_JNDI_NAME = "java:comp/BeanManager";

    private static final String NAME;

    static {

      // Portable 1.1+ CDI Lookup ----------------------------------------------
      Method currentM, getBeanManagerM;
      try {
        Class c = Class.forName("javax.enterprise.inject.spi.CDI");
        currentM = c.getMethod("current");
        getBeanManagerM = c.getMethod("getBeanManager");
      } catch (Exception ex) {
        currentM = null;
        getBeanManagerM = null;
      }
      current = currentM;
      getBeanManager = getBeanManagerM;

      // JNDI Based Lookup fallback --------------------------------------------
      if (current == null) {
        String jndiName = DEFAULT_JNDI_NAME;
        try {
          if (InitialContext.doLookup("java:comp/env/BeanManager") != null) {
            jndiName = "java:comp/env/BeanManager";
          }
        } catch (NamingException e) {
          // Fallback to default, do nothing
        }
        NAME = jndiName;
      } else {
        NAME = null;
      }
    }

    static BeanManager getBeanManager() {
      if (current != null) {
        try {
          Object cdi = current.invoke(null);
          return (BeanManager) getBeanManager.invoke(cdi);
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
      try {
        return InitialContext.doLookup(NAME);
      } catch (NamingException e) {
        throw new RuntimeException(e);
      }
    }

  }

  private CDIUtils() {
    // this class cannot be instantiated
  }

  /**
   * Gets a CDI BeanManager instance
   *
   * @return BeanManager instance
   */
  private static BeanManager getBeanManager() {
    return CDI.getBeanManager();
  }

  /**
   * Gets the registry.
   *
   * @param creationalContext the creational context
   * @return the registry
   */
  public static SqlSessionManagerRegistry getRegistry(CreationalContext creationalContext) {
    final BeanManager beanManager = getBeanManager();
    Iterator<Bean<?>> beans = beanManager.getBeans(SqlSessionManagerRegistry.class).iterator();
    return (SqlSessionManagerRegistry) beanManager.getReference(beans.next(), SqlSessionManagerRegistry.class,
        creationalContext);
  }

  /**
   * Find sql session factory.
   *
   * @param name the name
   * @param qualifiers the qualifiers
   * @param creationalContext the creational context
   * @return the sql session factory
   */
  public static SqlSessionFactory findSqlSessionFactory(String name, Set<Annotation> qualifiers,
      CreationalContext creationalContext) {
    final BeanManager beanManager = getBeanManager();
    Set<Bean<?>> beans;
    if (name != null) {
      beans = beanManager.getBeans(name);
    } else {
      beans = beanManager.getBeans(SqlSessionFactory.class, qualifiers.toArray(new Annotation[] {}));
    }
    Bean bean = beanManager.resolve(beans);
    if (bean == null) {
      throw new MybatisCdiConfigurationException("There are no SqlSessionFactory producers properly configured.");
    }
    return (SqlSessionFactory) beanManager.getReference(bean, SqlSessionFactory.class, creationalContext);
  }

  public static class SerializableDefaultAnnotationLiteral extends AnnotationLiteral<Default> {
    private static final long serialVersionUID = 1L;
  }

  public static class SerializableAnyAnnotationLiteral extends AnnotationLiteral<Any> {
    private static final long serialVersionUID = 1L;
  }

}
