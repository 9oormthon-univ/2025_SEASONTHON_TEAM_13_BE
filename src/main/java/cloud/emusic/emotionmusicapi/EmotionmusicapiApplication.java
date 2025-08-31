package cloud.emusic.emotionmusicapi;

import cloud.emusic.emotionmusicapi.service.UserService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EmotionmusicapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmotionmusicapiApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(UserService userService) {
		return args -> {
			System.out.println("===== Inserting Test User Data =====");
			userService.signUp();
			System.out.println("====================================");
		};
	}

}
