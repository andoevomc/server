package dreamgame.baucua.data;

import org.slf4j.Logger;
import dreamgame.data.SimplePlayer;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.session.ISession;

public class BauCuaPlayer extends SimplePlayer {

    public static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(BauCuaPlayer.class);
    public boolean isReady = false;
    public boolean isOwner;
    public boolean isObserve;
    public ISession currentSession;
    //Thomc
    public boolean isOutGame = false; //set true nếu thoát game
    public long money = 0;
    public int[] betArr;
    public final static int maxBet = 5;

    public int onBet(int aPiece, int numPiece, long firstCashBet) {
//        if (betArr[aPiece] + 1 > maxBet) {
//            return BauCuaTable.MAX_LEVEL;
//        } else 

//rebet
        long tempCash = cash;
        if (betArr[aPiece] > 0) {
            tempCash += betArr[aPiece] * firstCashBet;
        }
        if (tempCash < numPiece * firstCashBet) {
            return BauCuaTable.NOT_ENOUGH_MONEY;
        }
        cash = tempCash;
        betArr[aPiece] = numPiece;
        cash -= numPiece * firstCashBet;
        return BauCuaTable.BET_SUCCESS;
    }

    public long calMoney(int[] fullPiece, long firsCashBet) {
        for (int i = 0; i < 6; i++) {
            if (fullPiece[i] > 0) {
                money += betArr[i] * fullPiece[i] * firsCashBet;
            } else {
                money -= betArr[i] * firsCashBet;
            }
        }
        System.out.println(username + ": " + money);
        return money;
    }

    public void setCurrentOwner(ISession currentOwner) {
        this.currentOwner = currentOwner;
    }

    public void setCurrentMatchID(long currentMatchID) {
        this.currentMatchID = currentMatchID;
    }

//    public BauCuaPlayer() {
////        this.isGiveUp = false;
//        //this.timeReq = 0;
//    }
    public void reset(long money_) {
        money = 0;
        setReady(false);
        isObserve = false;
        betArr = new int[6];
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

//    public void setAvatarID(int avatarID) {
//        this.avatarID = avatarID;
//    }
//    public void setLevel(int level) {
//        this.level = level;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public void setCash(long c) {
//        this.cash = c;
//    }
    public BauCuaPlayer(long id) {
        this.id = id;
    }

    public boolean canOwner(int numPlayer, long firstBet, int numMaxBet) {
        int num = numPlayer - 1;
        if (num <= 0) {
            num = 1;
        }
        System.out.println(" owner cash:" + cash);
        System.out.println(" cash:" + (num * firstBet * numMaxBet));
        return cash >= (num * firstBet * numMaxBet);
    }
}
