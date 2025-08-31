package cloud.emusic.emotionmusicapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EmotionmusicapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmotionmusicapiApplication.class, args);
	}
}
