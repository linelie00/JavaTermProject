import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class KoreanDictionaryAPI {
    private static final String API_URL = "https://opendict.korean.go.kr/api/search";
    private static final String API_KEY = "C956A2407AF0A3C67401DA0B27201261";

    public static String search(String query) {
        try {
            // URL 구성 시 쿼리 파라미터들을 '&'로 연결하는 것이 아니라 '?'로 시작하고 '&'로 구분합니다.
            String urlString = API_URL + "?advanced=y&key=" + API_KEY + "&q=" + query + "&req_type=json&method=exact&type=word&pos=1";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            connection.disconnect();

            return parseJSON(content.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private static String parseJSON(String json) {
        StringBuilder result = new StringBuilder();

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray itemArray = jsonObject.getJSONObject("channel").getJSONArray("item");

            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject item = itemArray.getJSONObject(i);
                String word = item.getString("word");
                JSONArray senseArray = item.getJSONArray("sense");
                JSONObject sense = senseArray.getJSONObject(0); // Assuming there is only one sense for each word
                String definition = sense.getString("definition");
                result.append("Word: ").append(word).append("\n");
                result.append("Definition: ").append(definition).append("\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.append("Error parsing JSON: ").append(e.getMessage());
        }

        return result.toString();
    }
}
