import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class Server {
    private static final int PORT = 8282;
    private static final String API_URL = "https://opendict.korean.go.kr/api/search";
    private static final String API_KEY = "C956A2407AF0A3C67401DA0B27201261";

    private static String lastWord = null; // 마지막 단어를 저장하기 위한 변수

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        System.out.println("서버가 시작되었습니다.");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.equals("/exit")) {
                        break;
                    } else if (input.equals("/requestword")) {
                        if (lastWord != null) {
                            System.out.println(lastWord);
                            String result = generateWordFromAPIBot(lastWord);
                            out.println(result);
                        } else {
                            out.println("No previous word found.");
                        }
                    } else {
                        // Handle player's input word (for example, validation)
                        String result = validateWord(input);
                        out.println(result);
                        lastWord = input; // 마지막 단어를 저장
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private String validateWord(String word) {
            try {
                String urlString = API_URL + "?advanced=y&key=" + API_KEY + "&q=" + word + "&req_type=json&method=exact&type=word&pos=1";
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return parseJSON(response.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return "Error validating word.";
            }
        }

        private String parseJSON(String json) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONArray itemArray = jsonObject.getJSONObject("channel").getJSONArray("item");
                if (itemArray.length() > 0) {
                    JSONObject item = itemArray.getJSONObject(0);
                    String word = item.getString("word");
                    JSONArray senseArray = item.getJSONArray("sense");
                    JSONObject sense = senseArray.getJSONObject(0);
                    String definition = sense.getString("definition");
                    return "Word: " + word + "\nDefinition: " + definition;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "해당하는 단어가 없습니다.";
        }

        private String fetchWordDefinitionBot(String word) {
            try {
                String urlString = API_URL + "?advanced=y&key=" + API_KEY + "&q=" + word + "&req_type=json&method=start&num=100&type=word&pos=1";
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray itemArray = jsonObject.getJSONObject("channel").getJSONArray("item");
                if (itemArray.length() > 0) {
                    JSONObject item = itemArray.getJSONObject(0);
                    JSONArray senseArray = item.getJSONArray("sense");
                    JSONObject sense = senseArray.getJSONObject(0);
                    String definition = sense.getString("definition");
                    return "Word: " + word + "\nDefinition: " + definition;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Definition not found.";
        }

        private String generateWordFromAPIBot(String lastWord) {
            try {
                WordGeneratorBot wordGeneratorBot = new WordGeneratorBot();
                return wordGeneratorBot.selectRandomWordBot(lastWord);
            } catch (Exception e) {
                e.printStackTrace();
                return "Error generating word.";
            }
        }

        public class WordGeneratorBot {
            private List<String> wordListFromAPI;

            public WordGeneratorBot() {
                wordListFromAPI = new ArrayList<>();
            }

            private void fetchWordListFromAPIBot(String wordStart) {
                try {
                    String urlString = API_URL + "?advanced=y&key=" + API_KEY + "&q=" + wordStart + "&req_type=json&method=start&num=100&type=word&pos=1";
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // 호출한 JSON을 바로 출력
                    System.out.println("Fetched JSON for word starting with '" + wordStart + "': " + response.toString());

                    JSONObject jsonObject = new JSONObject(response.toString());
                    JSONArray itemArray = jsonObject.getJSONObject("channel").getJSONArray("item");
                    for (int i = 0; i < itemArray.length(); i++) {
                        JSONObject item = itemArray.getJSONObject(i);
                        String word = item.getString("word");
                        if (isKoreanWord(word) && word.length() >= 2) {
                            wordListFromAPI.add(word);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private boolean isKoreanWord(String word) {
                for (char c : word.toCharArray()) {
                    if (c < 0xAC00 || c > 0xD7A3) {
                        return false;
                    }
                }
                return true;
            }

            public String selectRandomWordBot(String playerWord) {
                if (playerWord == null || playerWord.isEmpty()) {
                    return "Invalid player word.";
                }

                String wordEnd = playerWord.substring(playerWord.length() - 1);
                fetchWordListFromAPIBot(wordEnd);

                if (wordListFromAPI.isEmpty()) {
                    return "No words found starting with: " + wordEnd;
                }

                Random random = new Random();
                int index = random.nextInt(wordListFromAPI.size());
                String selectedWord = wordListFromAPI.get(index);

                return "Word: " + selectedWord + "\nDefinition: " + fetchWordDefinitionBot(selectedWord);
            }
        }
    }
}
