package dreamgame.baucua.data;

import dreamgame.config.GameRuleConfig;
import dreamgame.data.MessagesID;
import dreamgame.data.SimpleTable;
import dreamgame.data.Timer;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.poker.data.Utils;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

public class BauCuaTable extends SimpleTable {

    public static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(BauCuaTable.class);
    public ArrayList<BauCuaPlayer> playings = new ArrayList<BauCuaPlayer>();
    private ArrayList<BauCuaPlayer> waitings = new ArrayList<BauCuaPlayer>();
    @SuppressWarnings("unused")
    public BauCuaPlayer owner;
    public BauCuaPlayer oldOwner;
    public static int timeBet = 20; //s
    public Timer timerAuto = new Timer(ZoneID.BAUCUA, 10000);
    int[] resultPiece;
    private boolean bettingTime; //đang trong thời gian đặt cược
    private Random r;
    public boolean needDestroy = false;

    public boolean roomIsFull() {
        return ((getPlayings().size() + getWaitings().size()) >= getMaximumPlayer());
    }

    public BauCuaPlayer getOwner() {
        return owner;
    }

    public ArrayList<BauCuaPlayer> getPlayings() {
        return playings;
    }

    public int getUserIndex(long id) {
        for (int i = 0; i
                < this.playings.size(); i++) {
            if (playings.get(i).id == id) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<BauCuaPlayer> copPlayerList() {
        ArrayList<BauCuaPlayer> list = new ArrayList<BauCuaPlayer>();
        for (BauCuaPlayer p : playings) {
            list.add(p);
        }
        return list;
    }

    public void setOrder(long[] order) {
        for (int i = 0; i < order.length; i++) {
            order[i] = 0;
        }
        int i = 0;
        for (BauCuaPlayer p : playings) {
            if (i < order.length) {
                order[i] = p.id;
            }

            i++;
        }
    }

    public boolean isAllReady() {
        for (int i = 0; i < playings.size(); i++) {
            BauCuaPlayer player = playings.get(i);
            if ((!player.isReady) && (player.id != this.owner.id)) {
                return false;
            }
        }
        return true;
    }

    public boolean containPlayer(long id) {
        for (int i = 0; i < playings.size(); i++) {
            if (playings.get(i).id == id) {
                return true;
            }
        }
        for (int i = 0; i < waitings.size(); i++) {
            if (waitings.get(i).id == id) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<BauCuaPlayer> getWaitings() {
        return waitings;
    }
    // Create table: owner, money, matchID, numberPlayer

    public BauCuaTable(BauCuaPlayer ow, long money, long match, int numPlayer) {

        startTime = System.currentTimeMillis();
        this.owner = ow;
        this.firstCashBet = money;
        this.matchID = match;
        this.maximumPlayer = numPlayer;
        this.playings.add(ow);
        timerAuto.setRuning(false);
        timerAuto.start();
        logdir = "baucua_log";
        initLogFile();
//        System.out.println("initlog !");
    }

    // Player join
    public void join(BauCuaPlayer player) throws BauCuaException {
        synchronized (playings) {
            for (BauCuaPlayer p : playings) {
                if (p.id == player.id) {
                    //user da ton tai!
                    throw new BauCuaException("User bau cua da ton tai.");
                }
            }
        }
//        if (this.isPlaying) {
//            if (this.playings.size() + this.waitings.size() < maximumPlayer) {
//                player.isObserve = true;
//                this.waitings.add(player);
//            } else {
//                throw new BauCuaException("Phong da thua nguoi roi ban");
//            }
//        } else 
        if (this.playings.size() < maximumPlayer) {
            this.playings.add(player);
        } else {
            throw new BauCuaException("Phong da thua nguoi roi ban");
        }
    }

    //Player removed
    public void removePlayer(long id) {

        try {
            for (BauCuaPlayer p : playings) {
                if (p.id == id) {
                    remove(p);
                    return;
                }
            }
            for (BauCuaPlayer p : waitings) {
                if (p.id == id) {
                    remove(p);
                    return;
                }
            }
        } catch (Exception e) {
            logCode("Error. Not found player : " + id);
        }
    }

    public void remove(BauCuaPlayer player) throws BauCuaException {
        try {
            synchronized (this.playings) {
//                System.out.println("out_code" + out_code.getClass());
                logCode("Remove player : " + player.id);
                for (BauCuaPlayer p : this.playings) {
                    if (p.id == player.id) {
                        playings.remove(player);
                        return;
                    }
                }
                for (BauCuaPlayer p : this.waitings) {
                    if (p.id == player.id) {
                        waitings.remove(p);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            mLog.error(e.getMessage() + " : " + player.id);
        }
    }

    public void startMatch() {
//        System.out.println(getValue(18));
        resetTable();
        if (this.playings.size() > 1) {
            this.isPlaying = true;
            for (BauCuaPlayer p : playings) {
                try {
                    DatabaseDriver.updateUserGameStatus(p.id, 1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            startBettingTime();
        } else {
            mLog.debug("Chua co nguoi choi nao!");
        }
    }

    @Override
    public BauCuaPlayer findPlayer(long uid) {
        for (BauCuaPlayer p : this.playings) {
            if (p.id == uid) {
                return p;


            }
        }
        for (BauCuaPlayer p : this.waitings) {
            if (p.id == uid) {
                return p;


            }
        }
        return null;


    }

    public int numRealPlaying() {
        int sum = 0;
        for (BauCuaPlayer p : playings) {
            if ((!p.isOutGame)) {
                sum++;
            }
        }
        return sum;
    }
    int numMaxBet = 10;

    public BauCuaPlayer ownerQuit() {
//        System.out.println("owner quit!");
        int numPlayer = playings.size();
        if (isPlaying) {
            numPlayer = numPlayer - 1;
        }
        for (int i = 0; i < playings.size(); i++) {
            if (playings.get(i).canOwner(numPlayer, firstCashBet, numMaxBet) && !playings.get(i).isOutGame) {
                ISession p = playings.get(i).currentSession;
                for (int j = 0; j
                        < playings.size(); j++) {
                    playings.get(j).currentOwner = p;
                }
                oldOwner = owner;
                owner = playings.get(i);
                ownerSession = p;
//                resetBlackList();
                return owner;
            }
        }
        return null;
    }

    public void resetTable() {
        oldOwner = null;
        for (BauCuaPlayer player : this.playings) {
            player.reset(firstCashBet);
        }

    }

    public void resetPlayer() {
//remove người chơi thoát giữa chừng để chuẩn bị ván mới
        ArrayList<BauCuaPlayer> needRemovePlayer = new ArrayList<BauCuaPlayer>();
        for (BauCuaPlayer p : playings) {
            if (p.isOutGame || p.notEnoughMoney()) {
                needRemovePlayer.add(p);
            }
        }
        if (needRemovePlayer.size() > 0) {
            for (int i = 0; i
                    < needRemovePlayer.size(); i++) {
                playings.remove(needRemovePlayer.get(i));
            }
        }
//        playings.addAll(waitings);
//        waitings.clear();
    }

    public void updateCashQuitPlayer(BauCuaPlayer p) {
//        updateGift();
    }

    public void startBettingTime() {
        System.out.println("start betting time!");
        timerAuto.setOwnerSession(ownerSession);
        timerAuto.setTimer(timeBet * 1000);
        timerAuto.setBauCuaTable(this);
        timerAuto.setRuning(true);
        timerAuto.reset();
        bettingTime = true;
    }

    public boolean bettingTime() {
        return bettingTime;
    }

//    public void stopBettingTime() {
//        System.out.println("stop betting time!");
//        bettingTime = false;
//        startSpinningTime();
//    }
//    public void startSpinningTime() {
//        System.out.println("start spinning time!");
//        timerAuto.setTimer(10000);
//        timerAuto.setTimeBauCua(this, false);
//        timerAuto.setRuning(true);
//        timerAuto.reset();
//    }
    public void endMatch() {
        System.out.println("send result!");
        bettingTime = false;
        resultPiece = new int[3];
        int[] fullPiece = new int[6];
        r = new Random();
        for (int i = 0; i < 3; i++) {
            int generalInt = r.nextInt(6);
            if (generalInt > 5) {
                generalInt = 5;
            }
            resultPiece[i] = generalInt;
            fullPiece[generalInt]++;
            System.out.println("result: " + resultPiece[i]);
        }

        //tính toán xiền 
        long ownerId = owner.id;
        if (oldOwner != null) {
            ownerId = oldOwner.id;
        }
        BauCuaPlayer aOwner = findPlayer(ownerId);

        for (BauCuaPlayer p : playings) {
            if (p.id != ownerId) {
                aOwner.money += p.calMoney(fullPiece, firstCashBet);
            }
        }

        aOwner.money = -aOwner.money;
        System.out.println("Owner: " + aOwner.money);
        isPlaying = false;
        updateCash();
        new Thread() {

            public void run() {
                sendEndMatch();
            }
        }.start();

    }

    public void sendEndMatch() {
        Zone zone = this.ownerSession.findZone(ZoneID.BAUCUA);
        Room room = zone.findRoom(this.getMatchID());
        MessageFactory msgFactory = this.ownerSession.getMessageFactory();
        EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
        endMatchRes.setBauCuaPlayer(copPlayerList());
        endMatchRes.setZoneID(ZoneID.BAUCUA);
        endMatchRes.setSuccessBauCua(1, getMatchID(), resultPiece);
        resetPlayer();
        room.broadcastMessage(endMatchRes, ownerSession, true);

        if (needDestroy) {
            Room r = zone.findRoom(getMatchID());
            destroy();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
//                java.util.logging.Logger.getLogger(BauCuaTable.class.getName()).log(Level.SEVERE, null, ex);
            }
            r.allLeft();
//            this = null;
        }
    }
    public final static int BET_SUCCESS = 0, NOT_ENOUGH_MONEY = 1, TIME_OUT = 2, MAX_LEVEL = 3, OWNER_BET = 4;

    public int onBet(long id, int piece, int numPiece) {
        if (!bettingTime) {
            return TIME_OUT;
        } else if (id == owner.id) {
            if (oldOwner == null) {
                return OWNER_BET;
            }
        }
//        else {
        return findPlayer(id).onBet(piece, numPiece, firstCashBet);
//        }
    }

    public int getNumPiece(int piece) {
        int num = 0;
        for (BauCuaPlayer p : playings) {
            num += p.betArr[piece];
        }
        return num;
    }

    public void updateCash() {
// startTime = System.currentTimeMillis();
        for (BauCuaPlayer p : playings) {
            try {
                long plus = p.money;
                String desc = "Choi game bau cua matchID : " + matchID + "-" + matchNum + " . (cash : " + p.cash + ")";
//                if (playings.get(i).id == winner.id) {
                if (plus > 0) {
                    plus = plus - plus * 10 / 100;
                    p.money = plus;
                }
                System.out.println(p.username + ": " + plus);
//                playings.get(i).cash = DatabaseDriver.getUserMoney(winner.id) + plus;
//                }
//                playings.get(i).cash = playings.get(i).cash + plus;

                logOut("End : " + p.username + " : " + plus + " -> " + p.cash);
                //người thoát giữa chừng đã bị trừ tiền lúc thoát rồi
//                if (!playings.get(i).isOutGame) {
                DatabaseDriver.updateUserMoney(plus, true, p.id, desc);
//                }
                p.cash = DatabaseDriver.getUserMoney(p.id);
                DatabaseDriver.updateUserGameStatus(p.id, 0);
            } catch (Exception ex) {
//                java.util.logging.Logger.getLogger(PhomTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//        updateGift();
        logOut("End Game DB Processing : " + (System.currentTimeMillis() - startTime) + "ms");

        if (DatabaseDriver.log_code) {
            out.flush();
            out_code.flush();
        }
    }

    public void destroy() {
        for (BauCuaPlayer p : playings) {
            try {
                DatabaseDriver.updateUserGameStatus(p.id, 0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            timerAuto.destroy();



        } catch (Exception e) {
            e.printStackTrace();


        }
        super.destroy();
//        System.out.println("Destroy : " + this.name);


        //timer.destroy();


    }

    public JSONArray getPlayerName() {
        JSONArray ja = new JSONArray();


        try {
            for (BauCuaPlayer p : playings) {
                JSONObject jo = new JSONObject();
                jo.put("name", p.username);
                jo.put("id", p.id);
                ja.put(jo);


            }
            for (BauCuaPlayer p : waitings) {
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

    public boolean canAddPlayer() {
        return owner.canOwner(playings.size() + 1, firstCashBet, numMaxBet);
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
