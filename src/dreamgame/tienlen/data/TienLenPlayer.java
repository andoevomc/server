package dreamgame.tienlen.data;

import dreamgame.data.Couple;
import java.util.ArrayList;
import java.util.logging.Level;
import org.slf4j.Logger;
import dreamgame.data.SimplePlayer;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.session.ISession;

public class TienLenPlayer extends SimplePlayer {

    public static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(TienLenPlayer.class);
    public ArrayList<Poker> playingCards = new ArrayList<Poker>();
    public int point;
    //public int timeReq;
    public boolean isReady = false;
    public boolean isOwner;
    public boolean isObserve;
    public boolean isAcive = true; // For each round while playing    
    //Thomc
    public boolean isOutGame = false; //set true nếu thoát game
//    public ArrayList<Couple<Long, Long>> cashLost = new ArrayList<Couple<Long, Long>>();
//    public ArrayList<Couple<Long, Long>> cashWin = new ArrayList<Couple<Long, Long>>();
    public long money = 0;
    //Kiểu chơi nhất-nhì- ba-bét
    public int sttToi = 0; //Tới nhất, nhì, ba, bét;
//    public boolean isCong = false; //Bị cóng khi có 1 người tới mà chưa đánh được cây nào

    public void setCong() {
        sttToi = 5;
    }

    public boolean isToi() {
        return sttToi > 0;
    }
//TLMB thối 2

    public boolean hasLast2() {
        if (myHand.length == 0) {
            return false;
        }
        for (int i = 0; i < myHand.length; i++) {
            if (Utils.getValue(myHand[i]) != 12) {
                return false;
            }
        }
        return true;
    }
//TLMB check đánh quân bài nhỏ nhất phải đánh đầu tiên

    public boolean hasMinCard(byte[] cards) {
        return Utils.sortCards(cards)[0] == myHand[0];
    }

    public boolean isCong() {
        return sttToi == 5;
    }

    public boolean checkCong() {
        return numHand == 13;
    }

    public long moneyLost(long money_) {
        if (this.cash <= 0) {
            return 0;
        } else if (this.cash < money_) {
            return this.cash;
        } else {
            return money_;
        }
    }

    public boolean notEnoughMoney() {
        if (cash < moneyForBet * 10) {
            return true;
        }
        try {
            if (DatabaseDriver.getUserMoney(this.id) < 0) {
                return true;
            }
        } catch (Exception ex) {
//            java.util.logging.Logger.getLogger(TienLenPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean canEnterRoom() {
        return (cash > moneyForBet * 10);
    }

    public void setCurrentOwner(ISession currentOwner) {
        this.currentOwner = currentOwner;
    }
    /**
     * For Time Out
     */
    public boolean isGetData = false;

    public void setState(boolean is) {
        isGetData = is;
    }

    public void setCurrentMatchID(long currentMatchID) {
        this.currentMatchID = currentMatchID;
    }

    public TienLenPlayer() {
        this.isGiveUp = false;
        //this.timeReq = 0;
    }

    public void reset(long money_) {
        point = 0;
        isStop = false;
        playingCards = new ArrayList<Poker>();
        moneyForBet = money_;
        //timeReq = 0;
        isGiveUp = false;
        isGetData = false;
        money = 0;
        setReady(false);
        isObserve = false;
        sttToi = 0;
//        isCong = false;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
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

    public void setCash(long c) {
        this.cash = c;
    }

    public TienLenPlayer(long id) {
        this.id = id;
        this.isGiveUp = false;
        //this.timeReq = 0;
    }

    public TienLenPlayer(ArrayList<Poker> inputPoker, long id, long minBet) {
        this.playingCards = inputPoker;
        this.id = id;
        this.moneyForBet = minBet;
        this.isGiveUp = false;
        //this.timeReq = 0;

    }

    public void setPokers(Poker[] inputPoker) {
        for (Poker p : inputPoker) {
            this.playingCards.add(p);
            System.out.print("; " + p.getNum() + " " + p.getType());
        }
    }

    public int minPoker() {
        int res = 100;
        for (Poker p : this.playingCards) {
            if (res > p.toInt()) {
                res = p.toInt();
            }
        }
        return res;
    }

    public void removeCards(Poker[] cards) {
        for (Poker p : cards) {
            this.playingCards.remove(p);
        }
    }
    //Thomc
    public byte[] myHand = new byte[13];  //các lá bài trên tay người chơi
    public int numHand = 13;//Số bài trên tay hiện tại;

    //Nhận quân bài lúc chia bài
    public void setMyCards(byte[] cards) {
//        System.out.println("Card của: " + this.username + Utils.bytesToString(cards));
        numHand = cards.length;
        System.out.println(" cards.length lúc nhận bài" + cards.length);
        System.out.println("cards.length: " + cards.length);
        this.myHand = Utils.sortCards(cards);
        System.out.println("numHand sau khi xếp: " + numHand);
//        this.myHand = cards;
    }
    // Kiểm tra các quân bài gửi lên có đúng bài của người chơi hay không trước khi remove

    public boolean isContainsCards(byte[] revCards) {
        if (numHand <= 0) {
            return false;
        }
        for (int i = 0; i < revCards.length; i++) {
            boolean isContainsCard = false;
            for (int j = 0; j < myHand.length; j++) {
                if (revCards[i] == myHand[j]) {
                    isContainsCard = true;
                    break;
                }
            }
            if (!isContainsCard) {
                return false;
            }
        }
        return true;
    }

    public String byteToString(byte[] d) {
        String s = "";
        for (int i = 0; i < d.length; i++) {
            s = s + " " + d[i];
        }
        s = s + " ; len : " + d.length;
        return s;
    }
    //remove (những) quân bài vừa đánh

    public void removeCards(byte[] revCards) {
        /*
         * System.out.println("revCards: " + revCards[0]);
         * System.out.println("numHand: " + numHand);
         * System.out.println("revCards.length: " + revCards.length);
         */

        this.numHand = numHand - revCards.length;
        System.out.println("numhand: " + numHand);
        if (numHand < 0) {
            return;
        }

        byte[] newHand = new byte[numHand];

        try {
            if (numHand > 0) {
                int newIndex = 0;
                for (int j = 0; j < myHand.length; j++) {
                    boolean needRemove = false;
                    for (int i = 0; i < revCards.length; i++) {
                        //System.out.println("myHand[j]: " + myHand[j]);
                        if (myHand[j] == revCards[i]) {
                            needRemove = true;
                            break;
                        }
                    }
                    if (!needRemove) {
                        //System.out.println("newHand.length" + newHand.length);
                        //System.out.println("newIndex" + newIndex);
                        newHand[newIndex] = myHand[j];
                        newIndex++;
                    }
                }

            }
            myHand = newHand;
        } catch (Throwable t) {
            mLog.error("numHand : " + numHand + " ;; matchID : " + this.currentMatchID);
            mLog.error("revCards : " + byteToString(revCards));
            mLog.error("myHand : " + byteToString(myHand));
        }
    }
//Trả về quân bài nhỏ nhất của người chơi

    public byte minCard() {
        byte min = myHand[0];
        for (int i = 1; i < numHand; i++) {
            if (Utils.isBigger(min, myHand[i])) {
                min = myHand[i];
            }
        }
        return min;
    }
}
