package util;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BeanFactory {
	
	private static ApplicationContext context =null;

	public static void init(){
		context = new ClassPathXmlApplicationContext(new String[] {"spring-task-client.xml" });
	}
	
	public static Object getBeanFromFacotry(String beanName){
		if(context==null){
			init();
		}
		return context.getBean(beanName);
	}
}
