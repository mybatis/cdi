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
import javax.inject.Named;
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
    if (managerAnnotation != null) {
      qualifiers.add(managerAnnotation);
    }
    else {
      qualifiers.add(new AnnotationLiteral<Default>() {});
      qualifiers.add(new AnnotationLiteral<Any>() {});
    }
    return qualifiers;
  }

  public Class getScope() {
    return Dependent.class;
  }

  public String getName() {
    if (managerAnnotation == null) {
      return mapperClass.getName();
    }
    else {
      if (managerAnnotation instanceof Named) {
        Named name = (Named) managerAnnotation;
        return name.value() + "." + mapperClass.getName();
      }
      else {
        return managerAnnotation.getClass().getName() + "." + mapperClass.getName();
      }
    }
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
    } 
    else if (managerAnnotation instanceof Named) {
      beans = beanManager.getBeans(((Named)managerAnnotation).value());
    }
    else {
      beans = beanManager.getBeans(SqlSessionManager.class, managerAnnotation);
    }
    Bean bean = beanManager.resolve(beans);    
    if (bean == null) {
      throw new MybatisCdiConfigurationException("There are no SqlSessionManager producers properly configured.");
    }
    return bean;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 61 * hash + (this.mapperClass != null ? this.mapperClass.hashCode() : 0);
    hash = 61 * hash + (this.managerAnnotation != null ? this.managerAnnotation.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MapperBean other = (MapperBean) obj;
    if (this.mapperClass != other.mapperClass && (this.mapperClass == null || !this.mapperClass.equals(other.mapperClass))) {
      return false;
    }
    if (this.managerAnnotation != other.managerAnnotation && (this.managerAnnotation == null || !this.managerAnnotation.equals(other.managerAnnotation))) {
      return false;
    }
    return true;
  }

  
}
