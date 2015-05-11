/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.bacay.data;

import dreamgame.config.GameRuleConfig;
import java.util.ArrayList;

import org.slf4j.Logger;

import dreamgame.data.SimpleException;
import dreamgame.data.SimpleTable;
import dreamgame.data.Timer;
import dreamgame.data.Transform;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;


import dreamgame.gameserver.framework.common.LoggerContext;
import org.json.JSONArray;
import org.json.JSONObject;
import phom.data.PhomPlayer;

/**
 *
 * @author binh_lethanh
 */
public class BacayTable extends SimpleTable {

    private boolean isStop;
    private ArrayList<BacayPlayer> players = new ArrayList<BacayPlayer>();
    private ArrayList<BacayPlayer> waitingPlayers = new ArrayList<BacayPlayer>();
    //private BacayPlayer roomOwner;
    private BacayPlayer currentPlayer;
    private BacayPlayer prePlayer;
    private int currentIndexOfPlayer;
    private boolean isOwner = false; // who is requesting?
    private long preMoneyBet;
    // Check to 3 times request
    private int timeReqForOwner;
    private boolean roomOwnerGiveUp = false;
    boolean testing = false;

    public BacayPlayer getPrePlayer() {
        return prePlayer;
    }
    public static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(BacayTable.class);
    public Timer timer = new Timer(ZoneID.BACAY, 30000);

    public Timer getTimer() {
        return timer;
    }

    public ArrayList<BacayPlayer> getWaitingPlayers() {
        return waitingPlayers;
    }

    public void addPlayerToWaitingList(BacayPlayer p) {
        this.waitingPlayers.add(p);
    }

    public void removePlayerToWaitingList(BacayPlayer p) {
        this.waitingPlayers.remove(p);
    }

    public long nextTurn() {


        if (this.isOwner) {
            if (owner == null) {
                mLog.error(roomInfo() + " . OMG owner is null");
            }

            return this.owner.id;
        } else {
            if (currentPlayer == null) {
                mLog.error(roomInfo() + " . nextTurn : OMG currentPlayer is null");
            }

            return this.currentPlayer.id;
        }
    }

    public boolean isReady() {
        for (BacayPlayer p : this.players) {
            if (!p.isReady) {
                return false;
            }
        }
        return true;
    }

    public BacayTable() {
        logdir = "bacay_log";

        initLogFile();
    }

    public BacayPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getIsOwner() {
        return this.isOwner;
    }

    public BacayPlayer getRoomOwner() {
        return (BacayPlayer) owner;
    }

    public String roomInfo() {
        String s = "";
        try {
            s = s + "[" + this.matchID + "-" + matchNum + " : " + isPlaying + "-" + (players.size() + 1) + "]";
            s = s + "[id : " + (currentPlayer.id) + "]";
            s = s + "[" + (currentPlayer.username) + "]";
        } catch (Exception e) {
        }
        return s;
    }

    public ArrayList<BacayPlayer> getPlayers() {
        synchronized (this.players) {
            return players;
        }
    }

    public BacayTable(BacayPlayer owner, long inputMinBet, int size) {
        this.owner = owner;
        this.firstCashBet = inputMinBet;

        this.maximumPlayer = (int) (owner.cash / this.firstCashBet);
        if (this.maximumPlayer > size) {
            this.maximumPlayer = size;
        }
        this.isStop = false;

        logdir = "bacay_log";
        initLogFile();
    }

    @Override
    public BacayPlayer findPlayer(long uid) throws SimpleException {
        if (uid == this.owner.id) {
            return (BacayPlayer) this.owner;
        }
        for (BacayPlayer p : this.players) {
            if (p.id == uid) {
                return p;
            }
        }
        for (BacayPlayer p : this.waitingPlayers) {
            if (p.id == uid) {
                return p;
            }
        }

        mLog.error(roomInfo() + " : Cant find user : " + uid);

        throw new SimpleException(ZoneID.BACAY, "Khong tim thay");
    }

    public boolean isPlayerInPlayingList(long uid) {
        for (BacayPlayer p : this.players) {
            if (p.id == uid) {
                return true;
            }
        }
        return false;
    }

    public void addPlayer(BacayPlayer p) throws SimpleException {
        if (out_code == null) {
            initLogFile();
        }

        logCode("addPlayer : " + p.username);
        synchronized (this.players) {

            for (BacayPlayer bp : this.players) {
                if (bp.id == p.id) {
                    //user da ton tai!
                    throw new SimpleException(ZoneID.BACAY, "User da ton tai.");
                }
            }

            p.setCurrentOwner(this.ownerSession);
            this.players.add(p);
        }
    }

    public void removePlayer(BacayPlayer player) {
        logCode("Remove Player : " + player.username);

        synchronized (this.players) {
            this.players.remove(player);
        }
    }

    public void removePlayer(long uid) {
        synchronized (this.players) {
            try {
                this.players.remove(findPlayer(uid));
            } catch (SimpleException e) {
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void setLoserForOutConnection(BacayPlayer currPlayer) {
        System.out.println("setLoserForOutConnection!");
        this.timer.reset();
        //this.timer.stop();
        long uid = currPlayer.id;
        if (uid == this.getRoomOwner().id) {
            roomOwnerGiveUp = true;
            for (BacayPlayer p : this.players) {
                p.isWin = true;
                p.isStop = true;
            }
            this.isStop = true;
        } else if (uid == this.currentPlayer.id) {
            //setup loser
            mLog.debug("thoat");
            this.currentPlayer.isWin = false;
            this.currentPlayer.isGiveUp = true;
            this.currentPlayer.isStop = true;
            // switch player
            try {
                this.currentPlayer = nextPlayer();
		timer.setCurrentPlayer(currentPlayer);
//                currentIndexOfPlayer--;                
            } catch (Exception ex) {
            }
        } else {
            if (!currPlayer.isStop) {
                currPlayer.isStop = true;
                currPlayer.isWin = false;
                currPlayer.isGiveUp = true;
            }
        }
    }
//	public void setLoserForOutConnection(BacayPlayer currPlayer) {
//    	this.timer.reset();
//    	this.timer.stop();
//    	long uid = currPlayer.id;
//        if (uid == this.owner.id) {
//            roomOwnerGiveUp = true;
//            for (BacayPlayer p : this.players) {
//                p.isWin = true;
//                p.isStop = true;
//            }
//            this.isStop = true;
//        } else if (uid == this.currentPlayer.id) {
//            //setup loser
//            this.currentPlayer.isWin = false;
//            this.currentPlayer.isGiveUp = true;
//            this.currentPlayer.isStop = true;
//            // switch player
//            try {
//                this.currentPlayer = nextPlayer();
//            } catch (Exception ex) {
//            }
//        } else {
//        	if(!currPlayer.isStop){
//	        	currPlayer.isStop = true;
//	        	currPlayer.isWin = false;
//	        	currPlayer.isGiveUp = true;
//        	}
//        }
//    }

    public BacayPlayer getPlayerInRoom(long uid) {
        synchronized (this.players) {
            for (BacayPlayer p : this.players) {
                if (p.id == uid) {
                    return p;
                }
            }
            return null;
        }
    }

    public void resetPlayer() {
        mLog.debug("reset player");
        ArrayList<BacayPlayer> needRemovePlayer = new ArrayList<BacayPlayer>();
        for (BacayPlayer p : players) {
            if (p.isOutGame || p.notEnoughMoney()) {
                needRemovePlayer.add(p);
            }
        }
        if (needRemovePlayer.size() > 0) {
            for (int i = 0; i < needRemovePlayer.size(); i++) {
                players.remove(needRemovePlayer.get(i));
            }
        }
//        players.addAll(waitings);
//        waitings.clear();
    }
    
    /**
     * Start match
     */
    public void startMatch() {
        System.out.println("Start match!");
        for (BacayPlayer p : this.players) {
            p.reset(this.firstCashBet);
//            p.isOutGame = false;
            try {
                DatabaseDriver.updateUserGameStatus(p.id, 1);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        try {
            DatabaseDriver.updateUserGameStatus(owner.id, 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ((BacayPlayer) this.owner).reset(this.firstCashBet);

        this.roomOwnerGiveUp = false;
        this.isPlaying = true;
        this.isStop = false;
        this.timeReqForOwner = 0;
        this.currentPlayer = this.players.get(0);
        chiabai();
        this.currentIndexOfPlayer = 0;
        this.preMoneyBet = 0;


        this.timer.setOwnerSession(ownerSession);
        this.timer.setCurrentPlayer(currentPlayer);
        this.timer.setRuning(true);
        this.timer.reset();
        try {
            System.out.println("Timer start : ");
            this.timer.start();
            
        } catch (Exception e) {
            this.timer.reset();
        }
    }
    public int matchNum = 0;

    public String showCards(Poker[] allCurrentCards) {
        String s = "";
        for (int i = 0; i < 3; i++) {
            s = s + (allCurrentCards[i].toString() + "|");
        }

        s = s + ("(");
        for (int i = 0; i < 3; i++) {
            if (i == 3 - 1) {
                s = s + (allCurrentCards[i].toInt());
            } else {
                s = s + (allCurrentCards[i].toInt() + " ");
            }
        }
        s = s + (")");

        return s;
    }

    public void createLogFile() {
        System.out.println("Create log files : " + matchID);
        if (testing) {
            System.out.println("Testing :");
            BacayPlayer bo = (BacayPlayer) this.owner;
            bo.setPokers("22 5 3");
            players.get(0).setPokers("22 35 3");

            players.get(0).isWin = players.get(0).isWin((BacayPlayer) this.owner);
            System.out.println("isWin : " + players.get(0).isWin);
        }

        logCode();
        logCode();
        logCode("*******************" + matchID + "-" + matchNum + " : " + owner.username + "***************************");

        logOut();
        logOut("*******************" + matchID + "-" + matchNum + " : " + owner.username + "***************************");

        BacayPlayer bo = (BacayPlayer) this.owner;

        logOut("Owner : (cash " + owner.cash + ") " + owner.username + " : id " + owner.id + " :  (" + showCards(bo.playingCards) + "   ; point = " + bo.myCompute());
        for (int j = 0; j < players.size(); j++) {
            BacayPlayer player = this.players.get(j);
            //player.setPokers(res.get(j));
            logOut("Player (cash " + player.cash + ") " + j + " : " + player.username + " : id " + player.id + " :  (" + showCards(player.playingCards) + "   ; point = " + player.myCompute());
        }
        //out.flush();
        //out_code.flush();
    }

    private void chiabai() {
        matchNum++;

        int number = this.players.size() + 1;
        if (number > 12) {
            System.out.println("Imposible!");
            return;
        } else {

            //while(true){

            ArrayList<Poker[]> res = chia(number);
            ((BacayPlayer) this.owner).setPokers(res.get(0));
            for (int j = 1; j < number; j++) {
                BacayPlayer player = this.players.get(j - 1);
                player.setPokers(res.get(j));
                player.isWin = player.isWin((BacayPlayer) this.owner);
            }

            //    if (players.get(0).point==((BacayPlayer) this.owner).point)
            //        break;
            //}

            createLogFile();

        }
    }

    private ArrayList<Poker[]> chia(int number) {
        ArrayList<Poker[]> res = new ArrayList<Poker[]>();
        ArrayList<Integer> currList = getRandomList();
        for (int i = 0; i < number; i++) {
            Poker[] p = new Poker[3];
            for (int j = 0; j < 3; j++) {
                p[j] = Transform.numberToPoker(currList.get(3 * i + j));
            }
            res.add(p);
        }
        return res;
    }

    private ArrayList<Integer> getRandomList() {
        ArrayList<Integer> res = new ArrayList<Integer>();
        ArrayList<Integer> currList = new ArrayList<Integer>();
        for (int i = 0; i < 36; i++) {
            currList.add(i, i + 1);
        }
        for (int i = 0; i < 36; i++) {
            int index = getRandomNumber(currList, res);
            currList.remove(index);
        }
        return res;
    }

    private int getRandomNumber(ArrayList<Integer> input, ArrayList<Integer> result) {
        int lengh = input.size() - 1;
        int index = (int) Math.round(Math.random() * lengh);
        result.add(input.get(index));
        return index;
    }

    /**
     * Playing: id to play money : money to bet -1 : reject 0 : accepted and
     * stop betting >0 : accepted and continue betting Return : 'true' if
     * changed player 'false' if not change player yet or finished game
     */
    public synchronized boolean play(long money, long id) {
        if (((this.currentPlayer.id == id) && (!this.isOwner))
                || ((this.owner.id == id) && (this.isOwner))) {
            this.currentPlayer.setState(true);

            if (!isOwner) {
                prePlayer = currentPlayer;
            }
            // Case
            if ((money == 0)) { // Stop betting

                if (isOwner) {
                    logOut("Owner " + owner.username + " : Stop betting");
                } else {
                    logOut(currentPlayer.username + " : Stop betting");
                }

                //out.flush();

                this.currentPlayer.isStop = true;
                this.currentPlayer = nextPlayer();
                if (this.isOwner) {
                    switchPlyer();
                } else {
                    // Do nothing
                }
                this.preMoneyBet = 0;
                return true; // changed player
            } else if (money > 0) { // continue betting

                if (isOwner) {
                    logOut("Owner " + owner.username + " : Betting " + money);
                } else {
                    logOut(currentPlayer.username + " : Betting " + money);
                }
                //out.flush();

                this.currentPlayer.moneyForBet += money;
                this.preMoneyBet = money;
                if (this.isOwner) {
                    if (this.timeReqForOwner == 3) {
                        this.currentPlayer = nextPlayer();
                        return true; // changed player
                    } else {
                        switchPlyer();
                    }
                } else {
                    switchPlyer();
                }
                return false; // not change player yet

            } else { // Reject case

                if (isOwner) {
                    logOut("Owner " + owner.username + " : Reject ");
                } else {
                    logOut(currentPlayer.username + " : Reject ");
                }
                //out.flush();

                this.currentPlayer.moneyForBet -= this.preMoneyBet;
                this.currentPlayer.isStop = true;
                this.currentPlayer = nextPlayer();
                if (this.isOwner) {
                    switchPlyer();
                } else {
                    // Do nothing
                }
                this.preMoneyBet = 0;
                return true; // changed player
            }
        } else { // Not current player
            System.out.println("thang co id:" + id + "dang oanh bua!s");
            return false;//not change player yet
        }
    }
    /*
     * public void changeOwnerInNextMatch() { long money = 0; PlayerInMatch res
     * = null; for (PlayerInMatch p : this.players) { if (p.moneyForBet > money)
     * { money = p.moneyForBet; if (p.isWin) { res = p; } } } if (res == null) {
     * // Do nothing } else if (res.id != this.roomOwner.id) {
     * this.players.remove(res); this.players.add(this.roomOwner);
     * this.roomOwner = res; } }
     */

    private void switchPlyer() {
        if (this.isOwner) {
            this.isOwner = false;
        } else {
            this.isOwner = true;
        }
    }

    public BacayPlayer nextPlayer() {
//        System.out.println("Next player : " + currentPlayer.id);
        mLog.debug("curr player : " + currentPlayer.username+" is out game = "+currentPlayer.isOutGame);

        // Increment index of current player --> get next player
        this.currentIndexOfPlayer++;
        // Restart time of request
        this.timeReqForOwner = 0;
        this.prePlayer = this.currentPlayer;
        //Get next player (if it exits)
        try {
//            if (this.players.get(this.currentIndexOfPlayer).isStop) {
            if (this.players.get(this.currentIndexOfPlayer).isOutGame) {
                mLog.debug("vao day ");
                return nextPlayer();
            } else {                
                if (currentPlayer.isOutGame == true) {
                    this.currentIndexOfPlayer--;
                    mLog.debug(" out game next player : " + this.players.get(this.currentIndexOfPlayer).username + " is out game = " + this.players.get(this.currentIndexOfPlayer).isOutGame);
                    return this.players.get(this.currentIndexOfPlayer);
                }
                mLog.debug("next player : " + this.players.get(this.currentIndexOfPlayer).username + " is out game = " + this.players.get(this.currentIndexOfPlayer).isOutGame);
                return this.players.get(this.currentIndexOfPlayer);
            }
        } catch (Exception e) {
	    mLog.debug("End game, no more player to fight");
	    System.out.println("Sau khi nguoi choi cuoi cung ket thuc to");
//            e.printStackTrace();
            this.isStop = true;
            return null;
        }
    }

    //Use for room-owner
    public long allMoneyToBet() {
        long res = 0;
        for (BacayPlayer p : this.players) {
            res += p.moneyForBet;
        }
        return res;
    }

    /**
     * @Desc check money for play
     *
     * @param player
     * @param isOwner
     * @return
     */
    public boolean isAllowPlay(BacayPlayer player, long m, boolean isOwner) {
        if (m == -1) {
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).id == player.id) {
                    if (!isOwner) {
                        players.get(i).isWin = false;
                    }
                }
            }
            if (isOwner) {
                if (prePlayer == null) {
                    mLog.error("Preplayer is null !");
                }

                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).id == getPrePlayer().id) {
                        players.get(i).isWin = true;
                    }
                }
            }
            return true;
        }
        if (isOwner) {
            long allMoney = allMoneyToBet() + m;
            return (player.cash >= allMoney);
        } else {
            return (player.cash >= (player.moneyForBet + m));
        }

    }

    public long moneyLeftOfPlayer(BacayPlayer player, long m, boolean isOwner, boolean isChangePlayer) {
        if (((m == -1) && (!isChangePlayer)) || (this.isStop)) {
            return 0;
        } else {
            if (isOwner) {
                long allMoney = allMoneyToBet() + m;
                System.out.println("Bet : " + allMoney + ": " + allMoneyToBet());
                return (player.cash - allMoney);
            } else {
                return (player.cash - (player.moneyForBet + m));
            }
        }
    }

    /**
     * Post processing while match is finished
     */
    public void postProcess() {

        logOut();
        logOut("roomOwnerGiveUp : " + roomOwnerGiveUp);

        this.isPlaying = false;

        for (BacayPlayer p : this.players) {
            try {
                DatabaseDriver.updateUserGameStatus(p.id, 0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        try {
            DatabaseDriver.updateUserGameStatus(owner.id, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        for (int i = 0; i < this.players.size(); i++) {
            BacayPlayer player = this.players.get(i);
            if (roomOwnerGiveUp) {
                player.isWin = true;
            } else if (player.isGiveUp) {
                player.isWin = false;
            }

            try {
                long moneyBet = player.moneyForBet;

                /*
                 * thomc fix:
                 *
                 */
                if (owner.cash < moneyBet) {
                    moneyBet = owner.cash;
                }
                // Log User-match for player                
                /*
                 * DatabaseDriver.logUserMatch(player.id, matchID, "ban la nguoi
                 * choi", moneyBet, player.isWin, this.matchIDAuto);
                 * DatabaseDriver.logUserMatch(this.owner.id, matchID, "ban la
                 * chu room", moneyBet, !player.isWin, this.matchIDAuto);
                 */
                String desc = "Choi game 3cay : " + matchID + "-" + matchNum;

                if (player.isWin) {
                    player.moneyForBet -= player.moneyForBet * 5 / 100;
                    owner.cash = DatabaseDriver.updateUserMoney(moneyBet, !player.isWin, this.owner.id, desc);
                    player.cash = DatabaseDriver.updateUserMoney(moneyBet * 95 / 100, player.isWin, player.id, desc);
                } else {
                    owner.cash = DatabaseDriver.updateUserMoney(moneyBet * 95 / 100, !player.isWin, this.owner.id, desc);
                    player.cash = DatabaseDriver.updateUserMoney(moneyBet, player.isWin, player.id, desc);
                }

                // return money of player
                player.cash = DatabaseDriver.getUserMoney(player.id);
                if (DatabaseDriver.eventAwardEnable) {
                    DatabaseDriver.updatePlayAward(player.id);
                }

                logOut("End : " + player.username + " : " + player.isWin + " : " + player.moneyForBet + " ; cash : " + player.cash);
                //out.flush();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Loi 1 roi: " + e.getMessage());
            }
        }
        try {
            this.owner.cash = DatabaseDriver.getUserMoney(this.owner.id);

            if (DatabaseDriver.eventAwardEnable) {
                DatabaseDriver.updatePlayAward(owner.id);
            }

            logOut("End : " + owner.username + " : " + owner.isWin + " : " + owner.moneyForBet + " ; cash : " + owner.cash);
            if (DatabaseDriver.log_code) {
                out.flush();
                out_code.flush();
            }

        } catch (Exception e) {
            System.out.println("Loi 2 roi: " + e.getMessage());
        }
    }

    public boolean checkFinish() {
        return this.isStop;
    }

    /**
     * Reset if catch a return match request
     */
    public void resetForNewMatch() {
        System.out.println("resetForNewMatch !");
        
        isStop = false;
        this.players.addAll(this.waitingPlayers);
        this.waitingPlayers = new ArrayList<BacayPlayer>();
        
        ArrayList<BacayPlayer> needRemovePlayer = new ArrayList<BacayPlayer>();
        for (BacayPlayer p : players) {
            if (p.isOutGame || p.notEnoughMoney()) {
                needRemovePlayer.add(p);
            }
        }
        if (needRemovePlayer.size() > 0) {
            for (int i = 0; i < needRemovePlayer.size(); i++) {
                players.remove(needRemovePlayer.get(i));
            }
        }
        
        int d = players.size() ; 
        for (int i =d-1; i>=0; i-- ){
            if ( players.get(i).isOutGame || players.get(i).notEnoughMoney() )
                players.remove(i);
        }
        
        try {
            currentPlayer = players.get(0);
        } catch (Exception ex) {
        }
        currentIndexOfPlayer = 0;
        isOwner = false;
        this.isPlaying = false;
        this.preMoneyBet = 0;
        timeReqForOwner = 0;
        for (BacayPlayer p : this.players) {
            p.reset(this.firstCashBet);
            p.isOutGame = false;
        }
        ((BacayPlayer) this.owner).reset(this.firstCashBet);
    }

    // Check while restart match - if room's owner has not enough money for all player
    public boolean canOwnerContinue() {

        long expectMoney = this.firstCashBet * this.players.size();
        long actualMoney = this.owner.cash;

        return (actualMoney >= expectMoney);
    }

    public void destroy() {
        for (BacayPlayer p : this.players) {
            try {
                DatabaseDriver.updateUserGameStatus(p.id, 0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        try {
            DatabaseDriver.updateUserGameStatus(owner.id, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (timer != null) {
            System.out.println("Timer destroyed!");
            timer.destroy();
        }
        super.destroy();
    }

    public JSONArray getPlayerName() {
        JSONArray ja = new JSONArray();

        try {

            for (BacayPlayer p : this.players) {
                JSONObject jo = new JSONObject();
                jo.put("name", p.username);
                jo.put("id", p.id);
                ja.put(jo);
            }
            for (BacayPlayer p : waitingPlayers) {
                JSONObject jo = new JSONObject();
                jo.put("name", p.username);
                jo.put("id", p.id);
                ja.put(jo);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return ja;
    }

    @Override
    public long getJoinMoney() {
	return GameRuleConfig.MONEY_TIMES_BET_TO_JOIN * firstCashBet;
    }
    
    @Override
    public String getJoinMoneyErrorMessage() {
	return "Bạn cần có ít nhất " + GameRuleConfig.MONEY_TIMES_BET_TO_JOIN + " lần tiền cược của bàn để tham gia!";
    }
}
