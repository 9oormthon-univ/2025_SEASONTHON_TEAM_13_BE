package cloud.emusic.emotionmusicapi.dto.response;

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
    private double valence;
    private double energy;
}