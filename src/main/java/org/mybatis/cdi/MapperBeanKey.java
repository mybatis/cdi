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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Named;
import javax.inject.Qualifier;

/**
 *
 * @author Frank D. Martinez [mnesarco]
 */
public class MapperBeanKey implements Comparable<MapperBeanKey> {

  private final String key;
  
  private final List<Annotation> qualifiers;
  
  private final Class<?> type;
  
  private final String sqlSessionManagerName;

  public MapperBeanKey(Class<?> type, Set<Annotation> annotations) {
    this.type = type;
    this.qualifiers = sort(filterQualifiers(annotations));
    
    // Create key = type(.qualifier)*(.name)?
    final StringBuilder sb = new StringBuilder();
    String name = null;
    sb.append(type.getName());
    for (Annotation q : this.qualifiers) {
      if (q instanceof Named) {
        name = ((Named)q).value();
      }
      else {
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
      public int compare(Annotation a, Annotation b) {
        return a.getClass().getName().compareTo(b.getClass().getName());
      }
    });
    return list;
  }
  
  public int compareTo(MapperBeanKey o) {
    return key.compareTo(o.key);
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
    final MapperBeanKey other = (MapperBeanKey) obj;
    return !((this.key == null) ? (other.key != null) : !this.key.equals(other.key));
  }
  
  public Bean createBean(BeanManager bm) {
    return new MapperBean(type, new HashSet<Annotation>(qualifiers), sqlSessionManagerName, bm);
  }

  public String getKey() {
    return key;
  }
  
}
