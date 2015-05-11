package dreamgame.data;

import dreamgame.gameserver.framework.session.ISession;

public class SimplePlayer {

    public int level;
    public int avatarID;
    public String username;
    public boolean isStop;
    public long id;
    public long moneyForBet;
    public boolean isWin;
    public boolean isGiveUp;
    public long cash;
    public long currentMatchID;
    public ISession currentOwner;
    public ISession currentSession;
    public boolean isReady = false;

    public boolean notEnoughMoney() {
        if (cash < moneyForBet) {
            return true;
        }
        return false;
    }

    public void setCash(long c) {
        this.cash = c;
    }

    public void setAvatarID(int avatarID) {
        this.avatarID = avatarID;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
