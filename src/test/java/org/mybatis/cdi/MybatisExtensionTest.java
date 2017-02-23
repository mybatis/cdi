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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MybatisExtensionTest {

  @Test
  public <T> void mappersFoundAfterTheBeanUsingTheMapperInAnInjectionPointHasBeenScannedShouldBeInstantiated()
      throws Exception {

    MybatisExtension extension = new MybatisExtension();
    Type type = UserMapper.class;

    projectInjectionTarget(extension, type);

    processAnnotatedType(extension, type);

    AfterBeanDiscovery afterBeanDiscovery = mock(AfterBeanDiscovery.class);
    BeanManager beanManager = mock(BeanManager.class);
    extension.afterBeanDiscovery(afterBeanDiscovery, beanManager);

    verify(afterBeanDiscovery).addBean((Bean<?>) any());

  }

  private <T> void projectInjectionTarget(MybatisExtension extension, Type type) {
    ProcessInjectionTarget<T> event = mock(ProcessInjectionTarget.class);
    InjectionTarget<T> injectTarget = mock(InjectionTarget.class);
    Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();

    InjectionPoint injectionPoint = mock(InjectionPoint.class);
    Annotated annotated = mock(Annotated.class);

    when(injectionPoint.getAnnotated()).thenReturn(annotated);

    when(annotated.getBaseType()).thenReturn(type);
    when(annotated.getAnnotations()).thenReturn(new HashSet<Annotation>());

    injectionPoints.add(injectionPoint);

    when(event.getInjectionTarget()).thenReturn(injectTarget);
    when(injectTarget.getInjectionPoints()).thenReturn(injectionPoints);

    extension.processInjectionTarget(event);
  }

  private <T> void processAnnotatedType(MybatisExtension extension, Type type) {
    ProcessAnnotatedType<T> pat = mock(ProcessAnnotatedType.class);
    AnnotatedType<T> annotatedType = mock(AnnotatedType.class);

    when(annotatedType.isAnnotationPresent(Mapper.class)).thenReturn(true);
    when(pat.getAnnotatedType()).thenReturn(annotatedType);
    when(annotatedType.getBaseType()).thenReturn(type);
    when(annotatedType.getJavaClass()).thenReturn((Class<T>) UserMapper.class);

    extension.processAnnotatedType(pat);
  }

}