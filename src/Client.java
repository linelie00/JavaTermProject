import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private GameLogic gameLogic;
    private JFrame frame;
    private JPanel nicknamePanel;
    private JLabel nicknameLabel;
    private JTextField nicknameField;
    private JButton botGameButton;
    private JButton createRoomButton;
    private JButton joinRoomButton;
    private JPanel roomPanel;
    private JTextField roomNameField;
    private JLabel roomNameLabel;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String nickname;
    private RoomUIManager roomUIManager;

    public Client() {
        gameLogic = new GameLogic();
        initStartUI();
    }

    public void updateUI(Component component) {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(component);
        frame.revalidate();
        frame.repaint();
    }

    public void initStartUI() {
        frame = new JFrame("끝말잇기 게임");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(230, 230, 250));

        nicknamePanel = new JPanel(new BorderLayout());
        nicknameLabel = new JLabel("닉네임:   ");
        nicknameLabel.setFont(UIManager.getFont("Label.font"));
        nicknameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nicknamePanel.setBackground(panel.getBackground());
        nicknameField = new JTextField();
        nicknameField.setFont(UIManager.getFont("TextField.font"));
        nicknamePanel.add(nicknameLabel, BorderLayout.WEST);
        nicknamePanel.add(nicknameField, BorderLayout.CENTER);

        botGameButton = createStyledButton("봇과 대결");
        createRoomButton = createStyledButton("방 생성");
        joinRoomButton = createStyledButton("방 입장");

        roomPanel = new JPanel(new BorderLayout());
        roomNameLabel = new JLabel("방 이름:   ");
        roomNameLabel.setFont(UIManager.getFont("Label.font"));
        roomNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roomPanel.setBackground(panel.getBackground());
        roomNameField = new JTextField();
        roomNameField.setFont(UIManager.getFont("TextField.font"));
        roomPanel.add(roomNameLabel, BorderLayout.WEST);
        roomPanel.add(roomNameField, BorderLayout.CENTER);

        panel.add(nicknamePanel);
        panel.add(botGameButton);
        panel.add(createRoomButton);
        panel.add(joinRoomButton);
        panel.add(roomPanel);

        frame.add(panel, BorderLayout.CENTER);

        botGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nickname = nicknameField.getText().trim();
                if (!nickname.isEmpty()) {
                    connectToServer();
                    startBotGame();
                } else {
                    JOptionPane.showMessageDialog(frame, "닉네임을 입력하세요.");
                }
            }
        });

        createRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nickname = nicknameField.getText().trim();
                if (!nickname.isEmpty()) {
                    connectToServer();
                    createRoom();
                } else {
                    JOptionPane.showMessageDialog(frame, "닉네임을 입력하세요.");
                }
            }
        });

        joinRoomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nickname = nicknameField.getText().trim();
                if (!nickname.isEmpty()) {
                    connectToServer();
                    joinRoom();
                } else {
                    JOptionPane.showMessageDialog(frame, "닉네임을 입력하세요.");
                }
            }
        });

        frame.setVisible(true);
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

    private void startBotGame() {
        JOptionPane.showMessageDialog(frame, "봇과의 게임을 시작합니다.");
        initGameUI();
    }

    private void createRoom() {
        String roomName = roomNameField.getText().trim();
        if (!roomName.isEmpty()) {
            connectToServer();
            out.println("/create " + roomName.trim());
            initGameUI();
        } else {
            JOptionPane.showMessageDialog(frame, "방 이름을 입력하세요.");
        }
    }

    private void joinRoom() {
        String roomName = roomNameField.getText().trim();
        if (!roomName.isEmpty()) {
            connectToServer();
            out.println("/join " + roomName.trim());
            initGameUI();
        } else {
            JOptionPane.showMessageDialog(frame, "방 이름을 입력하세요.");
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new IncomingReader()).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initGameUI() {
        JPanel previousUI = (JPanel) frame.getContentPane().getComponent(0); // 현재 UI를 저장

        roomUIManager = new RoomUIManager(frame, out, in, this, previousUI);
        frame.revalidate();
        frame.repaint();
    }

    private class IncomingReader implements Runnable {
        public void run() {
            String response;
            try {
                while ((response = in.readLine()) != null) {
                    if (response.equals("해당하는 단어가 없습니다.")) {
                        roomUIManager.showWarningMessage(response);
                    } else {
                        roomUIManager.receiveMessage(response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        new Client();
    }
}
