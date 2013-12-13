/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import org.apache.ibatis.session.SqlSessionManager;

/**
 * Internal CDI medatata for a mapper bean
 * 
 * @author Frank David Martínez
 */
public class MapperBean implements Bean {

  final Class mapperClass;

  final Annotation managerAnnotation;

  final BeanManager beanManager;

  public MapperBean(Class mapperClass, Annotation managerAnnotation, BeanManager beanManager) {
    this.mapperClass = mapperClass;
    this.managerAnnotation = managerAnnotation;
    this.beanManager = beanManager;
  }

  public Set getTypes() {
    Set<Type> types = new HashSet<Type>();
    types.add(mapperClass);
    return types;
  }

  public Set getQualifiers() {
    Set<Annotation> qualifiers = new HashSet<Annotation>();
    qualifiers.add(new AnnotationLiteral<Default>() {
    });
    qualifiers.add(new AnnotationLiteral<Any>() {
    });
    return qualifiers;
  }

  public Class getScope() {
    return Dependent.class;
  }

  public String getName() {
    // TODO add the manager name
    return mapperClass.getName();
  }

  public Set getStereotypes() {
    return Collections.emptySet();
  }

  public Class getBeanClass() {
    return mapperClass;
  }

  public boolean isAlternative() {
    return false;
  }

  public boolean isNullable() {
    return false;
  }

  public Set getInjectionPoints() {
    return Collections.emptySet();
  }

  public Object create(CreationalContext creationalContext) {
    Bean managerBean = findSqlSessionManagerBean();
    SqlSessionManager manager = (SqlSessionManager) beanManager.getReference(managerBean, SqlSessionManager.class, creationalContext);
    return manager.getMapper(mapperClass);
  }

  public void destroy(Object instance, CreationalContext creationalContext) {
    creationalContext.release();
  }

  private Bean findSqlSessionManagerBean() {
    Set<Bean<?>> beans;
    if (managerAnnotation == null) {
      beans = beanManager.getBeans(SqlSessionManager.class);
    } else {
      beans = beanManager.getBeans(SqlSessionManager.class, managerAnnotation);
    }
    if (beans.size() == 1) {
      return beanManager.resolve(beans);
    } else {
      throw new MybatisCdiConfigurationException("There are no SqlSessionManager producers properly configured.");
    }
  }

}
