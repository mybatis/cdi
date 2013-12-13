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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import org.apache.ibatis.session.ExecutorType;

/**
 * Adds transaction demarcation to the annotated method.
 * 
 * @author Frank David Martínez
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

  /**
   * Returns the constant indicating the myBatis executor type.
   *
   * @return the constant indicating the myBatis executor type.
   */
  @Nonbinding
  ExecutorType executorType() default ExecutorType.SIMPLE;

  /**
   * Returns the constant indicating the transaction isolation level.
   *
   * @return the constant indicating the transaction isolation level.
   */
  @Nonbinding
  Isolation isolation() default Isolation.DEFAULT;

  /**
   * Flag to indicate that myBatis has to force the transaction {@code commit().}
   *
   * @return false by default, user defined otherwise.
   */
  @Nonbinding
  boolean force() default false;

  /**
   * If true, the transaction will never committed but rather rolled back, useful for testing purposes.
   *
   * This parameter is false by default.
   *
   * @return if true, the transaction will never committed but rather rolled back, useful for testing purposes.
   */
  @Nonbinding
  boolean rollbackOnly() default false;
  
}
