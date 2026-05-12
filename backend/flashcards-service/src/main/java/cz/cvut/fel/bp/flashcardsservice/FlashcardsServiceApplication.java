package cz.cvut.fel.bp.flashcardsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FlashcardsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashcardsServiceApplication.class, args);
	}

}
