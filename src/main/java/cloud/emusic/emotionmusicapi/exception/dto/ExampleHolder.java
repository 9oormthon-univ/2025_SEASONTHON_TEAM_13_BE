package cloud.emusic.emotionmusicapi.exception.dto;

import io.swagger.v3.oas.models.examples.Example;
import lombok.*;

@Getter
@Builder
public class ExampleHolder {
    private Example holder;
    private String name;
    private int code;
}
