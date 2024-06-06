import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ClientUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel nicknameLabel;
    private JLabel scoreLabel;
    private JButton backButton;

    public ClientUI() {
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("끝말잇기 게임");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new BorderLayout());

        // 헤더 패널 생성
        JPanel headerPanel = new JPanel(new BorderLayout());
        nicknameLabel = new JLabel("닉네임: ");
        scoreLabel = new JLabel("점수: 0");
        backButton = new JButton("돌아가기");
        headerPanel.add(nicknameLabel, BorderLayout.WEST);
        headerPanel.add(scoreLabel, BorderLayout.CENTER);
        headerPanel.add(backButton, BorderLayout.EAST);

        // 대화창 패널 생성
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // 입력창 패널 생성
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("보내기");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // 프레임에 패널들 추가
        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(chatPanel, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public void setNickname(String nickname) {
        nicknameLabel.setText("닉네임: " + nickname);
    }

    public void setScore(int score) {
        scoreLabel.setText("점수: " + score);
    }

    public void setBackButtonListener(ActionListener listener) {
        backButton.addActionListener(listener);
    }

    public void setSendButtonListener(ActionListener listener) {
        sendButton.addActionListener(listener);
    }

    public String getInputText() {
        return inputField.getText().trim();
    }

    public void clearInputText() {
        inputField.setText("");
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }
}
