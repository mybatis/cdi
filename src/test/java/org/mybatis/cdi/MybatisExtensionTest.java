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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisExtensionTest {

  @Test
  void mappersFoundAfterTheBeanUsingTheMapperInAnInjectionPointHasBeenScannedShouldBeInstantiated() throws Exception {

    MybatisExtension extension = new MybatisExtension();
    Type type = UserMapper.class;

    projectInjectionTarget(extension, type);

    processAnnotatedType(extension, type);

    AfterBeanDiscovery afterBeanDiscovery = mock(AfterBeanDiscovery.class);
    extension.afterBeanDiscovery(afterBeanDiscovery);

    verify(afterBeanDiscovery).addBean((Bean<Object>) any());

  }

  private <T> void projectInjectionTarget(MybatisExtension extension, Type type) {
    ProcessInjectionTarget<T> event = mock(ProcessInjectionTarget.class);
    InjectionTarget<T> injectTarget = mock(InjectionTarget.class);
    Set<InjectionPoint> injectionPoints = new HashSet<>();

    InjectionPoint injectionPoint = mock(InjectionPoint.class);
    Annotated annotated = mock(Annotated.class);

    when(injectionPoint.getAnnotated()).thenReturn(annotated);

    when(annotated.getBaseType()).thenReturn(type);
    when(annotated.getAnnotations()).thenReturn(new HashSet<>());

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
