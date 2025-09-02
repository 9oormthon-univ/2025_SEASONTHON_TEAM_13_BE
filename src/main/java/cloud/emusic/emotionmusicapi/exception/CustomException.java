package cloud.emusic.emotionmusicapi.exception;

import cloud.emusic.emotionmusicapi.exception.dto.ErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // RuntimeException에 메시지 전달
        this.errorCode = errorCode;
    }
}
