package co.speedar.hedge.main;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	protected static final Logger log = Logger.getLogger(Main.class);

	@SuppressWarnings({ "unused", "resource" })
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		log.info("started...");
	}
}
