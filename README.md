cdi
===

MyBatis CDI Extension

Basic usage is:

1. Put the mybatis-cdi.jar it your .war applications (duh)
2. Create a singleton or application scoped bean to provide your Session Manager:


    @ApplicationScoped
    public class SessionManagerProvider {
      private SqlSessionManager manager;
      @PostConstruct
      public void init() {
        manager = build the SqlSessionManager ....
      }
      @Produces
      public SqlSessionManager getManager() {
        return manager;
      }
    }


3. Inject your mappers in your beans using the Mapper annotation


    @RequestScoped
    public class MyController {
      @Inject @Mapper
      MyMapperInterface mapper;
      public void doSomething() {
        mapper.myMethod(.....);
        .....
      }
    }


each method call will use an isolated session. But if you want to enclose many method calls in a transaction, mark the method with @Transactional

    @Transactional
    public void doSomeTransaction() {
      ...
      mapper.insertMyBean(...);
      mapper.updateMyBean(...);
      ...  
    }

To support this kind of transactional behaviour, you need to activate the interceptor in beans.xml

    <beans
       xmlns="http://java.sun.com/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
          http://java.sun.com/xml/ns/javaee
          http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">
       <interceptors>
          <class>org.mybatis.cdi.TransactionalInterceptor</class>
       </interceptors>
    </beans>
