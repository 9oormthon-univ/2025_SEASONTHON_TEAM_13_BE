package cloud.emusic.emotionmusicapi.dto.response.song;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackResponse {
    private String trackId;
    private String name;
    private String artist;
    private String spotifyUrl;
    private String imageUrl;

    private String album;
    private String releaseDate;
}