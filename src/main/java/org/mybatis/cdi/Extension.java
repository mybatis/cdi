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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

/**
 * MyBatis CDI extension
 * 
 * @author Frank David Martínez
 */
public class Extension implements javax.enterprise.inject.spi.Extension {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final Set<MapperBeanKey> mappers = new HashSet<MapperBeanKey>();

  public <X> void processAnnotatedType(@Observes ProcessInjectionTarget<X> event) {
    final InjectionTarget<X> it = event.getInjectionTarget();
    for (final InjectionPoint ip : it.getInjectionPoints()) {
      if (ip.getAnnotated().isAnnotationPresent(Mapper.class)) {
        mappers.add(new MapperBeanKey(
          (Class<?>)ip.getAnnotated().getBaseType(), 
          ip.getAnnotated().getAnnotations()));
      }
    }
  }
  
  public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm) {
    logger.log(Level.INFO, "MyBatis CDI Module - Activated");
    for (MapperBeanKey key : mappers) {
      logger.log(Level.INFO, "MyBatis CDI Module - Mapper dependency discovered: {0}", key.getKey());
      abd.addBean(key.createBean(bm));
    }
    mappers.clear();
  }

}
