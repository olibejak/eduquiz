package cz.cvut.fel.bp.deckservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DeckServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeckServiceApplication.class, args);
	}

}
