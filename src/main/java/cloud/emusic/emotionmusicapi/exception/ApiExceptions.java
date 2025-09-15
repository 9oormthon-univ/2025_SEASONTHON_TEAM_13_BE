package cloud.emusic.emotionmusicapi.exception;

import cloud.emusic.emotionmusicapi.exception.dto.ErrorCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiExceptions {
    ErrorCode[] values(); // API에서 발생할 수 있는 에러 코드 목록
}