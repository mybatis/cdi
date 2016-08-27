/**
 *    Copyright 2013-2016 the original author or authors.
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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;

/**
 * Internal CDI metadata for a mapper bean.
 *
 * @author Frank D. Martinez [mnesarco]
 */
public class MyBatisBean implements Bean, Serializable {

  private static final long serialVersionUID = 1L;

  protected final Class<Type> type;

  protected final Set<Annotation> qualifiers;

  protected final BeanManager beanManager;

  protected final String sqlSessionFactoryName;
  
  public MyBatisBean(Class<Type> type, Set<Annotation> qualifiers, String sqlSessionFactoryName, BeanManager beanManager) {  
    this.type = type;
    this.sqlSessionFactoryName = sqlSessionFactoryName;
    this.beanManager = beanManager;    
    if (qualifiers == null || qualifiers.isEmpty()) {
      this.qualifiers = new HashSet<Annotation>();
      this.qualifiers.add(new CDIUtils.SerializableDefaultAnnotationLiteral());
      this.qualifiers.add(new CDIUtils.SerializableAnyAnnotationLiteral());
    } else {
      this.qualifiers = qualifiers;
    }
  }

  @Override
  public Set<Type> getTypes() {
    Set<Type> types = new HashSet<Type>();
    types.add(this.type);
    return types;
  }

  @Override
  public Set<Annotation> getQualifiers() {
    return this.qualifiers;
  }

  @Override
  public Class<Dependent> getScope() {
    return Dependent.class;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public Set<Object> getStereotypes() {
    return Collections.emptySet();
  }

  @Override
  public Class<Type> getBeanClass() {
    return this.type;
  }

  @Override
  public boolean isAlternative() {
    return false;
  }

  @Override
  public boolean isNullable() {
    return false;
  }

  @Override
  public Set<Object> getInjectionPoints() {
    return Collections.emptySet();
  }

  @Override
  public Object create(CreationalContext creationalContext) {
    if (SqlSession.class.equals(this.type)) {
      return findSqlSessionManager(creationalContext);
    }
    ErrorContext.instance().reset();
    return Proxy.newProxyInstance(
        SqlSessionFactory.class.getClassLoader(), 
        new Class[] {this.type}, 
        new SerializableMapperProxy(this, creationalContext));
  }

  @Override
  public void destroy(Object instance, CreationalContext creationalContext) {
    creationalContext.release();
  }

  private SqlSessionManager findSqlSessionManager(CreationalContext creationalContext) {
    SqlSessionFactory factory = CDIUtils.findSqlSessionFactory(this.sqlSessionFactoryName, this.qualifiers, this.beanManager, creationalContext);
    return CDIUtils.getRegistry(this.beanManager, creationalContext).getManager(factory);
  }

}
