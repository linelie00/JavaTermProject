import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class RoomUIManager {

    private boolean isPlayerTurn = true;
    private JFrame frame;
    private JPanel chatPanel;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter out;
    private BufferedReader in;
    private Client client;
    private JPanel previousUI;

    private JLabel turnLabel;
    private String lastWord = null;

    private void switchTurn() {
        isPlayerTurn = !isPlayerTurn;
        updateTurnLabel();
        if (!isPlayerTurn) {
            // 상대의 턴이 되면 2초 후 단어를 서버에 요청하여 출력
            Timer timer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    requestWordBot();
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void updateTurnLabel() {
        if (isPlayerTurn) {
            turnLabel.setText("플레이어의 턴");
        } else {
            turnLabel.setText("상대의 턴");
        }
    }

    public RoomUIManager(JFrame frame, PrintWriter out, BufferedReader in, Client client, JPanel previousUI) {
        this.frame = frame;
        this.out = out;
        this.in = in;
        this.client = client;
        this.previousUI = previousUI; // 이전 UI를 저장
        initUI();
    }

    private void initUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel mainHeaderPanel = createMainHeaderPanel();
        frame.add(mainHeaderPanel, BorderLayout.NORTH);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setFont(UIManager.getFont("TextField.font"));
        sendButton = createStyledButton("보내기");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendWord();
            }
        });

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendWord();
            }
        });

        frame.revalidate();
        frame.repaint();
    }

    private JPanel createMainHeaderPanel() {
        JPanel mainHeaderPanel = new JPanel();
        mainHeaderPanel.setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        mainHeaderPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel turnPanel = createTurnPanel();
        mainHeaderPanel.add(turnPanel, BorderLayout.SOUTH);

        return mainHeaderPanel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.LIGHT_GRAY);

        // 닉네임과 점수 표시 패널
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.X_AXIS));
        JLabel nicknameLabel = new JLabel("닉네임: ");
        JLabel scoreLabel = new JLabel("점수: ");
        userInfoPanel.add(nicknameLabel);
        userInfoPanel.add(Box.createHorizontalStrut(5));
        userInfoPanel.add(new JLabel("User")); // 닉네임은 임시로 "User"로 설정
        userInfoPanel.add(Box.createHorizontalStrut(20));
        userInfoPanel.add(scoreLabel);
        userInfoPanel.add(Box.createHorizontalStrut(5));
        userInfoPanel.add(new JLabel("0")); // 점수는 임시로 0으로 설정
        headerPanel.add(userInfoPanel, BorderLayout.WEST);

        // 방 이름(또는 봇전) 표시 레이블
        JLabel roomLabel = new JLabel("Room Name"); // 방 이름은 임시로 "Room Name"으로 설정
        roomLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(roomLabel, BorderLayout.CENTER);

        // 나가기 버튼
        JButton exitButton = new JButton("나가기");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitRoom();
            }
        });
        headerPanel.add(exitButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createTurnPanel() {
        JPanel turnPanel = new JPanel(new BorderLayout());
        turnLabel = new JLabel("플레이어의 턴");
        turnLabel.setHorizontalAlignment(SwingConstants.CENTER);
        turnPanel.add(turnLabel, BorderLayout.CENTER);
        return turnPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIManager.getFont("Button.font"));
        button.setBackground(new Color(100, 149, 237));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private boolean isKoreanWord(String word) {
        // 한글 자음과 모음 유니코드 범위: 0xAC00 ~ 0xD7A3
        for (char c : word.toCharArray()) {
            if (c < 0xAC00 || c > 0xD7A3) {
                return false; // 한글이 아닌 문자가 포함되어 있으면 false 반환
            }
        }
        return true;
    }

    private void sendWord() {
        String word = inputField.getText().trim();
        word = DuEumLaw.applyDuEumLaw(word); // 두음법칙 적용

        if (word.length() >= 2 && isKoreanWord(word)) {
            if (lastWord == null || word.startsWith(lastWord.substring(lastWord.length() - 1))) {
                if (out != null) {
                    out.println(word);
                    inputField.setText("");
                    addMessage("플레이어", true);
                } else {
                    System.err.println("Error: Output stream is not initialized.");
                }
            } else {
                showWarningMessage("단어는 '" + lastWord.substring(lastWord.length() - 1) + "'로 시작해야 합니다.");
            }
        } else {
            showWarningMessage("유효하지 않은 단어입니다.");
        }
    }

    private void requestWordBot() {
        addMessage("봇", false);
        try {
            out.println("/requestword");
            String response = in.readLine();
            if (response != null) {
                String[] parts = response.split("\n", 2);
                if (parts.length > 0) {
                    String word = parts[0].replace("Word: ", "").trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMessage(String message, boolean isPlayer) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // 마진 추가

        JLabel messageLabel = new JLabel("<html><body style='width: 200px;'>" + message + "</body></html>");
        messageLabel.setOpaque(true);
        messageLabel.setBackground(Color.WHITE);

        // Increase font size for "플레이어" and "봇"
        if (message.equals("플레이어") || message.equals("봇")) {
            messageLabel.setFont(new Font(messageLabel.getFont().getName(), Font.BOLD, 16));
        } else if (message.startsWith("Word:")){
            switchTurn();
            System.out.println("말:"+ message);
            String[] parts = message.split("\n", 2);
            String word = parts[0].replace("Word: ", "").trim();
            lastWord = word;
        }

        if (isPlayer) {
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.add(messageLabel, BorderLayout.WEST);
            wrapper.setBackground(Color.WHITE);
            messagePanel.add(wrapper, BorderLayout.WEST);
        } else {
            messagePanel.add(messageLabel, BorderLayout.WEST);
        }

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        // Scroll to bottom
        JScrollPane scrollPane = (JScrollPane) chatPanel.getParent().getParent();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
            }
        });
    }

    public void receiveMessage(String message) {
        addMessage(message, false);
    }

    public void showWarningMessage(String message) {
        final JDialog dialog = new JDialog(frame, "경고", true);
        dialog.setLayout(new BorderLayout());
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        dialog.add(label, BorderLayout.CENTER);
        dialog.setSize(200, 100);
        dialog.setLocationRelativeTo(frame);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        timer.setRepeats(false);
        timer.start();

        dialog.setVisible(true);
    }

    private void exitRoom() {
        try {
            if (out != null) {
                out.println("/exit");
            }
            // 기존의 UI로 변경
            frame.getContentPane().removeAll();
            frame.add(previousUI, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class IncomingReader implements Runnable {
        private BufferedReader in;
        private RoomUIManager roomUIManager;

        public IncomingReader(BufferedReader in, RoomUIManager roomUIManager) {
            this.in = in;
            this.roomUIManager = roomUIManager;
        }

        @Override
        public void run() {
            String response;
            try {
                while ((response = in.readLine()) != null) {
                    if (response.equals("해당하는 단어가 없습니다.")) {
                        roomUIManager.receiveMessage(response);
                    } else {
                        roomUIManager.receiveMessage(response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
