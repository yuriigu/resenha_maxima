package br.checker.monitor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.docker.compose.skip.in-tests=false")
class MonitorApplicationTests {

	@Test
	void contextLoads() {
	}

}
