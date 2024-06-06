public class Store {
    private int money;
    private int earningsPerClick;
    private int upgradeCost;

    public Store() {
        money = 0;
        earningsPerClick = 1;
        upgradeCost = 10;
    }

    public int getMoney() {
        return money;
    }

    public int getEarningsPerClick() {
        return earningsPerClick;
    }

    public int getUpgradeCost() {
        return upgradeCost;
    }

    public void earnMoney() {
        money += earningsPerClick;
    }

    public boolean upgrade() {
        if (money >= upgradeCost) {
            money -= upgradeCost;
            earningsPerClick++;
            upgradeCost *= 2;
            return true;
        }
        return false;
    }
}
