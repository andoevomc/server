package dreamgame.cotuong.data;

import dreamgame.data.SimplePlayer;
import dreamgame.gameserver.framework.session.ISession;

/**
 *
 * @author Dinhpv
 */
public class CoTuongPlayer extends SimplePlayer {

    public int remainTime = 0;

    public CoTuongPlayer(long id) {
        this.id = id;
    }

    public CoTuongPlayer(long id, long minBet, long cash, int level,
            int avatar, String name, long matchID) {
        this.id = id;
        this.moneyForBet = minBet;
        this.cash = cash;
        this.level = level;
        this.avatarID = avatar;
        this.username = name;
        this.currentMatchID = matchID;
    }

    public int getAvatarID() {
        return avatarID;
    }

    public void setAvatarID(int avatarID) {
        this.avatarID = avatarID;
    }

    public long getCash() {
        return cash;
    }

    public void setCash(long cash) {
        this.cash = cash;
    }

    public long getCurrentMatchID() {
        return currentMatchID;
    }

    public void setCurrentMatchID(long currentMatchID) {
        this.currentMatchID = currentMatchID;
    }

    public ISession getCurrentOwner() {
        return currentOwner;
    }

    public void setCurrentOwner(ISession currentOwner) {
        this.currentOwner = currentOwner;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isIsGiveUp() {
        return isGiveUp;
    }

    public void setIsGiveUp(boolean isGiveUp) {
        this.isGiveUp = isGiveUp;
    }

    public boolean isIsStop() {
        return isStop;
    }

    public void setIsStop(boolean isStop) {
        this.isStop = isStop;
    }

    public boolean isIsWin() {
        return isWin;
    }

    public void setIsWin(boolean isWin) {
        this.isWin = isWin;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getMoneyForBet() {
        return moneyForBet;
    }

    public void setMoneyForBet(long moneyForBet) {
        this.moneyForBet = moneyForBet;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
