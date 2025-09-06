package cloud.emusic.emotionmusicapi.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력 값입니다."),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    POST_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 게시글에 접근할 권한이 없습니다."),
    INVALID_PERMISSION(HttpStatus.FORBIDDEN, "게시글 수정 권한이 없습니다."),

    // 유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    // 감정 태그
    EMOTION_TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "감정 태그를 찾을 수 없습니다."),

    // 댓글 (추가된 부분)
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 댓글에 접근할 권한이 없습니다."),

    // 좋아요
    ALREADY_LIKED(HttpStatus.BAD_REQUEST, "이미 좋아요를 눌렀습니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글에 좋아요를 누르지 않았습니다."),
    SPOTIFY_API_ERROR(HttpStatus.BAD_REQUEST, "Spotify API 요청 중 오류가 발생했습니다."),

    SONG_NOT_FOUND(HttpStatus.NOT_FOUND, "노래를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}