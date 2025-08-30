package cloud.emusic.emotionmusicapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelloRequest {

    @Schema(description = "메시지에서 반환할 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED) //필수 입력
    @NotBlank //NULL을 허용하지 않음 @NotNull은 String형식 제외 사용
    private String name;
}
