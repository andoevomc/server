package dreamgame.poker.data;

import org.slf4j.Logger;
import dreamgame.data.SimplePlayer;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Vector;

public class PokerPlayer extends SimplePlayer {

    public static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(PokerPlayer.class);
    public boolean isReady = false;
    public boolean isOwner;
    public boolean isObserve;
    public ISession currentSession;
    //Thomc
    public boolean isOutGame = false; //set true nếu thoát game
    public long money = 0;
    public int cardsType = 52;
    public byte[] focusCards;
    public int allInRound;//tố hết
    public long allInMoney;
    public long currentBetMoney; //tiền cược mỗi vòng
    public boolean isFold = false;//úp bỏ
    public long firstCash = 0; //tiền bắt đầu mỗi ván

    public long[] potMoney;
    boolean isAllin;
    public boolean isWinner;
    public boolean addedMoney;
    public boolean turned;
    
    public void setCardsType(int type, byte[] cards) {
        cardsType = type;
//        if (type > 52) {
//            cardsType = type;
//
//        } else {
//            cardsType = 52;
//        }
        focusCards = cards;
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
        if (cash < moneyForBet) {
            return true;
        }
        try {
            if (cash <= 0) {
                return true;
            }
        } catch (Exception ex) {
//            java.util.logging.Logger.getLogger(TienLenPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean isAllIn(long money) {
        return money == cash;
    }
//    public boolean canEnterRoom() {
//        return (cash > moneyForBet);
//    }

    public void setCurrentOwner(ISession currentOwner) {
        this.currentOwner = currentOwner;
    }

    public void setCurrentMatchID(long currentMatchID) {
        this.currentMatchID = currentMatchID;
    }

    public PokerPlayer() {
//        this.isGiveUp = false;
        //this.timeReq = 0;
    }

    public void reset(long money_) {
        moneyForBet = money_;
        //timeReq = 0;
//        isGiveUp = false;
        isFold = false;
        allInRound = -1;
        money = 0;
        setReady(false);
        isObserve = false;
        allInMoney = 0;
        currentBetMoney = 0;
        firstCash = cash;
        cardsType = 52;
        focusCards = null;
        mVisibleCard = 0;
        currentCard = 0;
        potMoney = new long[4];
        isAllin = false;
        isWinner = false;
        addedMoney = false;
        turned = false;
//        isShow = false;
    }

    public boolean isShow() {
        return mVisibleCard != 0;
    }

    public boolean isAllIn() {
        return allInRound >= 0;
    }
    
    public boolean allIn() {
        return allInRound >= 0;
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
        this.firstCash = cash;
    }

    public PokerPlayer(long id) {
        this.id = id;
//        this.isGiveUp = false;
        //this.timeReq = 0;
    }
    //Thomc
    public byte[] myHand = new byte[13];  //các lá bài trên tay người chơi
    public int numHand = 13;//Số bài trên tay hiện tại;

    //Nhận quân bài lúc chia bài
    public void setMyCards(byte[] cards) {
//        System.out.println("Card của: " + this.username + Utils.bytesToString(cards));
        numHand = cards.length;
//        System.out.println(" cards.length lúc nhận bài" + cards.length);
//        System.out.println("cards.length: " + cards.length);
        this.myHand = cards;
//        System.out.println("numHand sau khi xếp: " + numHand);
    }

    public String byteToString(byte[] d) {
        String s = "";
        for (int i = 0; i < d.length; i++) {
            s = s + " " + d[i];
        }
        s = s + " ; len : " + d.length;
        return s;
    }
    public int mVisibleCard; //vị trí 1 trong 2 quân bài đầu tiên bị ẩn
    public byte currentCard;
//    public boolean isShow;

    public byte getCurrentCard(int numRound) {
        return myHand[numRound + 1];
    }

    public int getNumHand(int numRound) {
        return numRound + 2;
    }

    public byte[] getMyHandForMe(int numRound, boolean isEndMatch) {
        byte[] currCards = null;
        int numCards = getNumHand(numRound);
        if (isEndMatch) {
            return myHand;
        }
//        else {
//            numCards = numRound + 2;
////            numCards = 3;
//        }
//        else {
//            numCards = numRound + 1;
//        }
        Vector temp = new Vector();
        for (int i = 0; i < numCards; i++) {
//            if (i != inVisibleCard) {
            temp.add(myHand[i]);

//            }
        }
        currCards = new byte[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            currCards[i] = Byte.parseByte(temp.elementAt(i).toString());
        }
        return currCards;

    }

    public byte[] getMyHandForOther(int numRound) {
        byte[] currCards = null;
        if (numRound == 0 && isShow()) {
            currCards = new byte[1];
            currCards[0] = (byte) mVisibleCard;
        } else if (numRound == 1) {
            currCards = new byte[2];
            currCards[0] = (byte) mVisibleCard;
            currCards[1] = myHand[2];
        } else if (numRound == 2 || numRound == 3) {
            currCards = new byte[3];
            currCards[0] = (byte) mVisibleCard;
            currCards[1] = myHand[2];
            currCards[2] = myHand[3];
        }
//            numCards = 3;
        return currCards;

    }

    public boolean isContainsCards(byte card) {
        for (int i = 0; i < 2; i++) {
            if (myHand[i] == card) {
                return true;
            }
        }
        return false;
    }
}
