import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class KoreanDictionaryApp {
    private JFrame frame;
    private JTextField searchField;
    private JButton searchButton;
    private JTextArea resultArea;

    public KoreanDictionaryApp() {
        frame = new JFrame("Korean Dictionary");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        searchField = new JTextField();
        searchButton = new JButton("Search");
        resultArea = new JTextArea();
        resultArea.setEditable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(searchField, BorderLayout.CENTER);
        panel.add(searchButton, BorderLayout.EAST);

        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = searchField.getText();
                if (!query.isEmpty()) {
                    String result = KoreanDictionaryAPI.search(query);
                    resultArea.setText(result);
                }
            }
        });
    }

    public void start() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        KoreanDictionaryApp app = new KoreanDictionaryApp();
        app.start();
    }
}
