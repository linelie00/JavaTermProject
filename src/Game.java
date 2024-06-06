import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Game {
    private JFrame frame;
    private JLabel moneyLabel;
    private JButton upgradeButton;
    private JButton earnButton;
    private int money;
    private int earningsPerClick;
    private int upgradeCost;

    public Game() {
        frame = new JFrame("Tycoon Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 500);
        frame.setLayout(new GridLayout(3, 1));

        money = 0;
        earningsPerClick = 1;
        upgradeCost = 10;

        moneyLabel = new JLabel("Money: $" + money);
        moneyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(moneyLabel);

        earnButton = new JButton("Earn Money");
        earnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                money += earningsPerClick;
                updateMoneyLabel();
            }
        });
        frame.add(earnButton);

        upgradeButton = new JButton("Upgrade (Cost: $10)");
        upgradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (money >= upgradeCost) {
                    money -= upgradeCost;
                    earningsPerClick++;
                    upgradeCost *= 2;
                    upgradeButton.setText("Upgrade (Cost: $" + upgradeCost + ")");
                    updateMoneyLabel();
                } else {
                    JOptionPane.showMessageDialog(frame, "Not enough money to upgrade!");
                }
            }
        });
        frame.add(upgradeButton);
    }

    public void start() {
        frame.setVisible(true);
    }

    private void updateMoneyLabel() {
        moneyLabel.setText("Money: $" + money);
    }
}
