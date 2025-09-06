package cloud.emusic.emotionmusicapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "song")
public class Song {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "song_id")
  private Long id;

  @Column(name = "track_id", nullable = false, unique = true)
  private String trackId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "artist", nullable = false)
  private String artist;

  @Column(name = "album_art_url")
  private String albumArtUrl;

  @Column(name = "play_count", nullable = false)
  private Integer playCount = 0;

  @Column(name = "spotify_play_url")
  private String spotifyPlayUrl;

  @Builder
  public Song(String trackId, String title, String artist, String albumArtUrl, String spotifyPlayUrl) {
    this.trackId = trackId;
    this.title = title;
    this.artist = artist;
    this.albumArtUrl = albumArtUrl;
    this.spotifyPlayUrl = spotifyPlayUrl;
    this.playCount = 0;
  }

  public void plusPlayCount() {
    this.playCount += 1;
  }

}