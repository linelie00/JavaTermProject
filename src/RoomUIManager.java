import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;

public class RoomUIManager {

    private boolean isPlayerTurn = true;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter out;
    private BufferedReader in;
    private Client client;
    private JPanel previousUI;

    private JLabel turnLabel;

    private void switchTurn() {
        isPlayerTurn = !isPlayerTurn;
        updateTurnLabel();
        if (!isPlayerTurn) {
            // 상대의 턴이 되면 단어를 서버에 요청하여 출력
            requestWordBot();
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

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setFont(UIManager.getFont("TextField.font"));
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

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
        if (word.length() >= 2 && isKoreanWord(word)) {
            if (out != null) {
                out.println(word);
                inputField.setText("");
                receiveMessage("플레이어: " + word);
                switchTurn(); // 단어를 보내면 턴을 변경
            } else {
                System.err.println("Error: Output stream is not initialized.");
            }
        } else {
            showWarningMessage("유효하지 않은 단어입니다.");
        }
    }

    private void requestWordFromServer() {
        try {
            out.println("/requestword");
            String response = in.readLine();
            if (response != null) {
                receiveMessage(response);
                switchTurn(); // 단어를 받으면 턴을 변경
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestWordBot() {
        try {
            out.println("/requestword");
            String response = in.readLine();
            if (response != null) {
                receiveMessage("봇: " + response);
                switchTurn(); // 봇의 단어를 받으면 턴을 변경
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage(String message) {
        chatArea.append(message + "\n");
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
}