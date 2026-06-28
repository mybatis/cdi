/*
 *    Copyright 2013-2026 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessInjectionTarget;
import jakarta.enterprise.inject.spi.ProcessProducer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CoverageImprovementTest {

  interface ThrowingMapper {
    void boom();
  }

  static class ThrowingMapperImpl implements ThrowingMapper {
    @Override
    public void boom() {
      throw new IllegalStateException("boom");
    }
  }

  static class ProducerSamples {
    @SessionFactoryProvider
    public String invalid() {
      return null;
    }

    public SqlSessionFactory unannotated() {
      return null;
    }
  }

  @Test
  void mybatisConfigurationExceptionShouldPreserveMessage() {
    MybatisCdiConfigurationException exception = new MybatisCdiConfigurationException("bad config");
    assertEquals("bad config", exception.getMessage());
  }

  @SuppressWarnings("unchecked")
  @Test
  void myBatisBeanShouldAddDefaultQualifiersWhenNoQualifiersProvided() {
    MyBatisBean bean = new MyBatisBean("id", (Class<Type>) (Type) UserMapper.class, Set.of(), null);

    assertTrue(bean.getQualifiers().stream().anyMatch(a -> a.annotationType() == Default.class));
    assertTrue(bean.getQualifiers().stream().anyMatch(a -> a.annotationType() == Any.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void serializableMapperProxyShouldUnwrapInvocationTargetException() throws Throwable {
    MyBatisBean bean = new MyBatisBean("id", (Class<Type>) (Type) ThrowingMapper.class, Set.of(), null);
    CreationalContext<Object> creationalContext = mock(CreationalContext.class);
    SqlSessionFactory sessionFactory = mock(SqlSessionFactory.class);
    SqlSessionManagerRegistry registry = mock(SqlSessionManagerRegistry.class);
    SqlSessionManager manager = mock(SqlSessionManager.class);

    when(registry.getManager(sessionFactory)).thenReturn(manager);
    when(manager.getMapper(ThrowingMapper.class)).thenReturn(new ThrowingMapperImpl());

    try (MockedStatic<CDIUtils> cdiUtils = mockStatic(CDIUtils.class)) {
      cdiUtils.when(() -> CDIUtils.findSqlSessionFactory(null, bean.getQualifiers(), creationalContext))
          .thenReturn(sessionFactory);
      cdiUtils.when(() -> CDIUtils.getRegistry(creationalContext)).thenReturn(registry);

      SerializableMapperProxy<Object> proxy = new SerializableMapperProxy<>(bean, creationalContext);
      Method method = ThrowingMapper.class.getMethod("boom");

      Throwable thrown = assertThrows(IllegalStateException.class, () -> proxy.invoke(null, method, null));
      assertEquals("boom", thrown.getMessage());
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void cdiUtilsShouldThrowWhenNoSqlSessionFactoryBeanIsResolved() {
    BeanManager beanManager = mock(BeanManager.class);
    CDI<Object> cdi = mock(CDI.class);
    CreationalContext<Object> creationalContext = mock(CreationalContext.class);
    when(cdi.getBeanManager()).thenReturn(beanManager);
    when(beanManager.getBeans(eq(SqlSessionFactory.class), any(Annotation[].class))).thenReturn(Set.of());
    when(beanManager.resolve(anySet())).thenReturn(null);

    try (MockedStatic<CDI> cdiStatic = mockStatic(CDI.class)) {
      cdiStatic.when(CDI::current).thenReturn(cdi);

      assertThrows(MybatisCdiConfigurationException.class,
          () -> CDIUtils.findSqlSessionFactory(null, Set.of(), creationalContext));
    }
  }

  @Test
  void sqlSessionManagerRegistryShouldFailWhenNoFactoryIsAvailable() throws Exception {
    SqlSessionManagerRegistry registry = new SqlSessionManagerRegistry();
    Instance<SqlSessionFactory> factories = mock(Instance.class);
    when(factories.isUnsatisfied()).thenReturn(true);
    setField(registry, "factories", factories);

    assertThrows(MybatisCdiConfigurationException.class, registry::init);
  }

  @SuppressWarnings("unchecked")
  @Test
  void mybatisExtensionShouldRejectInvalidSessionFactoryProducerReturnType() throws Exception {
    MybatisExtension extension = new MybatisExtension();
    ProcessProducer<Object, Object> processProducer = mock(ProcessProducer.class);
    AnnotatedMember<Object> annotatedMember = mock(AnnotatedMember.class);
    Method producerMethod = ProducerSamples.class.getMethod("invalid");

    when(processProducer.getAnnotatedMember()).thenReturn(annotatedMember);
    when(annotatedMember.isAnnotationPresent(SessionFactoryProvider.class)).thenReturn(true);
    when(annotatedMember.getBaseType()).thenReturn(String.class);
    when(annotatedMember.getJavaMember()).thenReturn(producerMethod);

    extension.processProducer(processProducer);

    verify(processProducer).addDefinitionError(any(MybatisCdiConfigurationException.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  void mybatisExtensionShouldIgnoreUnannotatedSqlSessionFactoryProducer() throws Exception {
    MybatisExtension extension = new MybatisExtension();
    ProcessProducer<Object, Object> processProducer = mock(ProcessProducer.class);
    AnnotatedMember<Object> annotatedMember = mock(AnnotatedMember.class);
    Method producerMethod = ProducerSamples.class.getMethod("unannotated");

    when(processProducer.getAnnotatedMember()).thenReturn(annotatedMember);
    when(annotatedMember.isAnnotationPresent(SessionFactoryProvider.class)).thenReturn(false);
    when(annotatedMember.getBaseType()).thenReturn(SqlSessionFactory.class);
    when(annotatedMember.getJavaMember()).thenReturn(producerMethod);

    extension.processProducer(processProducer);

    verify(processProducer, never()).addDefinitionError(any());
  }

  @SuppressWarnings("unchecked")
  @Test
  void mybatisExtensionShouldNotCreateMapperBeansWhenMapperAnnotationIsMissing() {
    MybatisExtension extension = new MybatisExtension();

    ProcessInjectionTarget<Object> pit = mock(ProcessInjectionTarget.class);
    InjectionTarget<Object> injectionTarget = mock(InjectionTarget.class);
    InjectionPoint injectionPoint = mock(InjectionPoint.class);
    Annotated annotated = mock(Annotated.class);
    when(injectionPoint.getAnnotated()).thenReturn(annotated);
    when(annotated.getBaseType()).thenReturn(UserMapper.class);
    when(annotated.getAnnotations()).thenReturn(Set.of());
    when(injectionTarget.getInjectionPoints()).thenReturn(Set.of(injectionPoint));
    when(pit.getInjectionTarget()).thenReturn(injectionTarget);
    extension.processInjectionTarget(pit);

    ProcessAnnotatedType<Object> pat = mock(ProcessAnnotatedType.class);
    AnnotatedType<Object> annotatedType = mock(AnnotatedType.class);
    when(annotatedType.isAnnotationPresent(Mapper.class)).thenReturn(false);
    when(pat.getAnnotatedType()).thenReturn(annotatedType);
    extension.processAnnotatedType(pat);

    AfterBeanDiscovery afterBeanDiscovery = mock(AfterBeanDiscovery.class);
    extension.afterBeanDiscovery(afterBeanDiscovery);

    verify(afterBeanDiscovery, never()).addBean(any());
  }

  @SuppressWarnings("unchecked")
  @Test
  void beanKeyShouldSupportComparisonEqualityAndStringRepresentation() throws Exception {
    Class<?> beanKeyClass = Class.forName("org.mybatis.cdi.MybatisExtension$BeanKey");
    Constructor<?> constructor = beanKeyClass.getDeclaredConstructor(Class.class, Set.class);
    constructor.setAccessible(true);

    Set<Annotation> manager1Annotations = new HashSet<>(
        Arrays.asList(FooService.class.getDeclaredField("userMapper").getAnnotations()));
    Set<Annotation> manager2Annotations = new HashSet<>(
        Arrays.asList(FooService.class.getDeclaredField("userMapper2").getAnnotations()));

    Object manager1Key = constructor.newInstance((Class<Type>) (Type) UserMapper.class, manager1Annotations);
    Object manager1KeyCopy = constructor.newInstance((Class<Type>) (Type) UserMapper.class, manager1Annotations);
    Object manager2Key = constructor.newInstance((Class<Type>) (Type) UserMapper.class, manager2Annotations);

    Method compareTo = beanKeyClass.getDeclaredMethod("compareTo", beanKeyClass);
    compareTo.setAccessible(true);
    Method getKey = beanKeyClass.getDeclaredMethod("getKey");
    getKey.setAccessible(true);

    assertEquals(0, compareTo.invoke(manager1Key, manager1KeyCopy));
    assertNotEquals(0, compareTo.invoke(manager1Key, manager2Key));
    assertEquals(manager1Key, manager1KeyCopy);
    assertNotEquals(manager1Key, manager2Key);
    assertNotEquals(manager1Key, null);
    assertNotEquals(manager1Key, "other");
    assertEquals(manager1Key.hashCode(), manager1KeyCopy.hashCode());
    assertEquals(getKey.invoke(manager1Key), manager1Key.toString());
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
