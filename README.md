MyBatis CDI Extension
=====================

This CDI extension allows direct injection of Mybatis Mappers into CDI Beans.

Basic usage is:

Put the mybatis-cdi.jar it your .war applications.

Create a singleton or application scoped bean to provide your SqlSessionFactory.

	@ApplicationScoped
	public class SqlSessionFactoryProvider {
	
		@Produces
		public SqlSessionFactory produceFactory() {
			SqlSessionFactory factory = create the factory instance ....
			return factory;
		}
	
	}

Inject your mappers in your beans using the Mapper annotation

	@RequestScoped
	public class MyController {
	
		@Inject @Mapper
		MyMapperInterface mapper;
	
		public void doSomething() {
			mapper.myMethod(.....);
			.....
		}
	
	}

Each method call will use an isolated session. But if you want to enclose many method calls in a transaction, mark the method with @Transactional

    @Transactional
    public void doSomeTransaction() {
      ...
      mapper.insertMyBean(...);
      mapper.updateMyBean(...);
      ...  
    }

To enable transactions you need to activate the local transaction interceptor in beans.xml:

    <beans
       xmlns="http://java.sun.com/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
          http://java.sun.com/xml/ns/javaee
          http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">
       <interceptors>
          <class>org.mybatis.cdi.LocalTransactionInterceptor</class>
       </interceptors>
    </beans>

Or the Jta interceptor in the case you are using more than one datasource:

    <beans
       xmlns="http://java.sun.com/xml/ns/javaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="
          http://java.sun.com/xml/ns/javaee
          http://java.sun.com/xml/ns/javaee/beans_1_0.xsd">
       <interceptors>
          <class>org.mybatis.cdi.JtaTransactionInterceptor</class>
       </interceptors>
    </beans>

In this case, you must also configure mybatis to use the MANAGED transaction manager in mybatis-config.xml

