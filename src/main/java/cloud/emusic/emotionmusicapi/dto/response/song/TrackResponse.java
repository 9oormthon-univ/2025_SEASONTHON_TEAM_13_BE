package cloud.emusic.emotionmusicapi.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackResponse {
    private String id;
    private String name;
    private String artist;
    private String spotifyUrl;
    private String imageUrl;

    // ✅ 아래 두 필드를 추가해주세요.
    private String album;
    private String releaseDate;

    private double valence;
    private double energy;
}