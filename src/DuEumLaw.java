public class DuEumLaw {

    // 두음법칙을 적용하여 변환된 단어를 반환하는 메서드
    public static String applyDuEumLaw(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }

        char firstChar = word.charAt(0);
        if (firstChar == 'ㄹ') {
            return 'ㄴ' + word.substring(1);
        } else if (firstChar == 'ㄴ') {
            return 'ㅇ' + word.substring(1);
        }

        return word;
    }
}
