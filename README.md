MyBatis CDI Extension
=====================

[![Java CI](https://github.com/mybatis/cdi/workflows/Java%20CI/badge.svg)](https://github.com/mybatis/cdi/workflows/Java%20CI)
[![Coverage Status](https://coveralls.io/repos/mybatis/cdi/badge.svg?branch=master&service=github)](https://coveralls.io/github/mybatis/cdi?branch=master)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis-cdi/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.mybatis/mybatis-cdi)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/org.mybatis/mybatis-cdi.svg)](https://oss.sonatype.org/content/repositories/snapshots/org/mybatis/mybatis-cdi/)
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

![mybatis-logo](http://mybatis.github.io/images/mybatis-logo.png)

MyBatis-CDI extension takes care of the lifecycle of MyBatis mappers and SqlSessions. MyBatis components are directly injected into your
CDI beans ready to be used, there is no need to create or destroy them. It also provides local and JTA transaction support based on the
@Transactional annotation.

Compatibility
-------------

| CDI API VERSION         | JDK 17 | JDK 11  | JDK 8  |
| ----------------------- | ------ | ------- | ------ |
| cdi-1.0 (not supported) | N      | N       | N      |
| cdi-1.1                 | Y      | Y       | Y      |
| cdi-1.2                 | Y      | Y       | Y      |
| cdi-2.0 (preferred)     | Y      | Y       | Y      |
| cdi-3.0 (not supported) | N      | N       | N      |
| cdi-4.0 (not supported) | N      | N       | N      |

Jakarta EE support is not yet provided by this framework.

Jdk17+ requires jboss-classfilewriter to be at 1.2.5 or it will error, [classfilewriter](https://github.com/jbossas/jboss-classfilewriter/issues/24)

Building
--------

Due to maven inability to auto activate multiple profiles from same pom, we have no default now so that jdk 17+ works while maintaining jdk 8 build support.  This means to run this project a profile must be provided or it will fail to find classes.  We are unable to simply default classes either since java EE switched to jakarta EE and we use both apis now.

The cdi profile can be cdi-1.1, cdi-1.2, or cdi-2.0.

jdk 8 through 16

- mvn clean install -P"cdi-2.0"

jdk 17+

- mvn clean install -P"jdk17on" -P"cdi-2.0"

Essentials
----------

- [See the docs](http://mybatis.github.io/cdi/)

Contributed Examples
--------------------

- Ready to deploy sample web app (Jee7): [https://github.com/mnesarco/mybatis-cdi-samples](https://github.com/mnesarco/mybatis-cdi-samples/)
