/*
 *    Copyright 2013-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.cdi;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @author Frank D. Martinez [mnesarco]
 */
public final class CDIUtils {

  private CDIUtils() {
    // this class cannot be instantiated
  }

  /**
   * Gets a CDI BeanManager instance
   *
   * @return BeanManager instance
   */
  private static BeanManager getBeanManager() {
    return CDI.current().getBeanManager();
  }

  /**
   * Gets the registry.
   *
   * @param creationalContext
   *          the creational context
   *
   * @return the registry
   */
  public static <T> SqlSessionManagerRegistry getRegistry(CreationalContext<T> creationalContext) {
    final BeanManager beanManager = getBeanManager();
    Iterator<Bean<? extends Object>> beans = beanManager.getBeans(SqlSessionManagerRegistry.class).iterator();
    return (SqlSessionManagerRegistry) beanManager.getReference(beans.next(), SqlSessionManagerRegistry.class,
        creationalContext);
  }

  /**
   * Find sql session factory.
   *
   * @param name
   *          the name
   * @param qualifiers
   *          the qualifiers
   * @param creationalContext
   *          the creational context
   *
   * @return the sql session factory
   */
  public static <T> SqlSessionFactory findSqlSessionFactory(String name, Set<Annotation> qualifiers,
      CreationalContext<T> creationalContext) {
    final BeanManager beanManager = getBeanManager();
    Set<Bean<? extends Object>> beans;
    if (name != null) {
      beans = beanManager.getBeans(name);
    } else {
      beans = beanManager.getBeans(SqlSessionFactory.class, qualifiers.toArray(new Annotation[] {}));
    }
    Bean<? extends Object> bean = beanManager.resolve(beans);
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
