package cloud.emusic.emotionmusicapi.dto.request;

import java.util.List;
import java.util.Map;

public class EmotionMapper {

    // 한글 감정 → 영어 감정
    private static final Map<String, String> emotionEnglish = Map.ofEntries(
            Map.entry("기쁨", "happy"),
            Map.entry("행복", "happy"),
            Map.entry("즐거움", "happy"),

            Map.entry("설렘", "excited"),
            Map.entry("두근거림", "excited"),

            Map.entry("놀람", "surprised"),

            Map.entry("감동", "emotional"),

            Map.entry("화남", "angry"),
            Map.entry("짜증", "angry"),

            Map.entry("슬픔", "sad"),
            Map.entry("우울", "sad"),
            Map.entry("외로움", "sad"),

            Map.entry("피곤함", "tired"),
            Map.entry("졸림", "sleep"),
            Map.entry("힘듦", "tired"),

            Map.entry("복잡함", "confused"),
            Map.entry("어지러운", "confused")
    );

    // 영어 감정 변환
    public static List<String> getEnglishEmotions(List<String> emotions) {
        return emotions.stream()
                .map(e -> emotionEnglish.getOrDefault(e, "General"))
                .toList();
    }
}