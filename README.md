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

| CDI API VERSION     | Oracle JDK 8 | OpenJDK 8 | OpenJDK 7 |
| ------------------- | ------------ | --------- | --------- |
| cdi-1.0             | N            | N         | N         |
| cdi-1.1             | Y            | Y         | Y         |
| cdi-1.2 (preferred) | Y            | Y         | Y         |
| cdi-2.0             | Y            | Y         | N         |


Essentials
----------

* [See the docs](http://mybatis.github.io/cdi/)

Contributed Examples
--------------------

* Ready to deploy sample web app (Jee7): [https://github.com/mnesarco/mybatis-cdi-samples](https://github.com/mnesarco/mybatis-cdi-samples/)

