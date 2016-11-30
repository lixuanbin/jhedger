package co.speedar.hedge.service;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class CheckDataTest {
	@Autowired
	private CheckData check;

	@Test
	public void testCheck() throws IOException {
		check.doCheck();
		// System.in.read(); // test jframe's hide_on_close action
	}
}
