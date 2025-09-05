package cloud.emusic.emotionmusicapi.dto.request;

import java.util.List;
import java.util.Map;

public class EmotionMapper {
    private static final Map<String, List<String>> emotionGenres = Map.ofEntries(
            Map.entry("기쁨", List.of("k-pop", "dance", "idol")),
            Map.entry("행복", List.of("k-pop", "dance")),
            Map.entry("즐거움", List.of("k-pop", "dance")),
            Map.entry("설렘", List.of("k-pop", "j-pop", "idol")),
            Map.entry("두근거림", List.of("k-pop", "dance")),
            Map.entry("놀람", List.of("k-pop", "dance")),
            Map.entry("감동", List.of("ballad", "acoustic", "k-indie")),
            Map.entry("화남", List.of("k-hip-hop", "rock")),
            Map.entry("짜증", List.of("k-hip-hop", "rock")),
            Map.entry("슬픔", List.of("ballad", "acoustic")),
            Map.entry("우울", List.of("ballad", "acoustic")),
            Map.entry("외로움", List.of("ballad", "acoustic")),
            Map.entry("피곤함", List.of("lo-fi", "chill", "k-indie")),
            Map.entry("졸림", List.of("lo-fi", "chill")),
            Map.entry("힘듦", List.of("acoustic", "lo-fi")),
            Map.entry("복잡함", List.of("indie", "alternative", "k-indie")),
            Map.entry("어지러운", List.of("indie", "alternative"))
    );

    public static List<String> getGenres(List<String> emotions) {
        return emotions.stream()
                .flatMap(e -> emotionGenres.getOrDefault(e, List.of("pop")).stream())
                .distinct()
                .toList();
    }
}