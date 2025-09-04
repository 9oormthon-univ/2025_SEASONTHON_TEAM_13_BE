package cloud.emusic.emotionmusicapi.dto.response;

import cloud.emusic.emotionmusicapi.domain.Comment;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CommentResponse {
  private final Long id;
  private final String content;
  private final String authorNickname;
  private final String authorProfileImageUrl;
  private final LocalDateTime createdAt;

  public CommentResponse(Comment comment) {
    this.id = comment.getId();
    this.content = comment.getContent();
    this.authorNickname = comment.getUser().getNickname();
    this.createdAt = comment.getCreatedAt();
    this.authorProfileImageUrl = comment.getUser().getProfileImage();
  }
}