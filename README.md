MyBatis CDI Extension
=====================

MyBatis-CDI extension takes care of the lifecycle of MyBatis mappers and SqlSessions. MyBatis components are directly injected into your CDI beans ready to be used, there is no need to create or destroy them. It also provides local and JTA transaction support based on the @Transactional annotation.

See the docs at http://mybatis.github.io/cdi/

