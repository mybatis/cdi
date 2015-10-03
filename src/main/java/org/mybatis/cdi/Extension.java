/**
 *    Copyright 2013-2015 the original author or authors.
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.apache.ibatis.session.SqlSession;

/**
 * MyBatis CDI extension
 * 
 * @author Frank D. Martinez [mnesarco]
 */
public class Extension implements javax.enterprise.inject.spi.Extension {

  private static final Logger logger = Logger.getLogger(Extension.class.getName());

  private final Set<BeanKey> mappers = new HashSet<BeanKey>();

  public <X> void processInjectionTarget(@Observes ProcessInjectionTarget<X> event) {
    final InjectionTarget<X> it = event.getInjectionTarget();
    for (final InjectionPoint ip : it.getInjectionPoints()) {
      if (ip.getAnnotated().isAnnotationPresent(Mapper.class) || SqlSession.class.equals(ip.getAnnotated().getBaseType())) {
        this.mappers.add(new BeanKey((Class<Type>) ip.getAnnotated().getBaseType(), ip.getAnnotated().getAnnotations()));
      }
    }
  }

  public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
    logger.log(Level.INFO, "MyBatis CDI Module - Activated");
    for (BeanKey key : this.mappers) {
      logger.log(Level.INFO, "MyBatis CDI Module - Mapper dependency discovered: {0}", key.getKey());
      abd.addBean(key.createBean(bm));
    }
    this.mappers.clear();
  }

  public static class BeanKey implements Comparable<BeanKey> {

    private final String key;

    private final List<Annotation> qualifiers;

    private final Class<Type> type;

    private final String sqlSessionManagerName;

    public BeanKey(Class<Type> type, Set<Annotation> annotations) {
      this.type = type;
      this.qualifiers = sort(filterQualifiers(annotations));

      // Create key = type(.qualifier)*(.name)?
      final StringBuilder sb = new StringBuilder();
      String name = null;
      sb.append(type.getName());
      for (Annotation q : this.qualifiers) {
        if (q instanceof Named) {
          name = ((Named) q).value();
        } else {
          sb.append(".").append(q.annotationType().getSimpleName());
        }
      }
      if (name != null) {
        sb.append("_").append(name);
      }
      this.key = sb.toString();
      this.sqlSessionManagerName = name;
    }

    private Set<Annotation> filterQualifiers(Set<Annotation> annotations) {
      final Set<Annotation> set = new HashSet<Annotation>();
      for (Annotation a : annotations) {
        if (a.annotationType().isAnnotationPresent(Qualifier.class)) {
          set.add(a);
        }
      }
      return set;
    }

    private List<Annotation> sort(Set<Annotation> annotations) {
      final List<Annotation> list = new ArrayList<Annotation>(annotations);
      Collections.sort(list, new Comparator<Annotation>() {
        @Override
        public int compare(Annotation a, Annotation b) {
          return a.getClass().getName().compareTo(b.getClass().getName());
        }
      });
      return list;
    }

    @Override
    public int compareTo(BeanKey o) {
      return this.key.compareTo(o.key);
    }

    @Override
    public int hashCode() {
      int hash = 3;
      hash = 43 * hash + (this.key != null ? this.key.hashCode() : 0);
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
      final BeanKey other = (BeanKey) obj;
      return !(this.key == null ? other.key != null : !this.key.equals(other.key));
    }

    public Bean createBean(BeanManager bm) {
      return new MyBatisBean(this.type, new HashSet<Annotation>(this.qualifiers), this.sqlSessionManagerName, bm);
    }

    public String getKey() {
      return this.key;
    }

  }

}
