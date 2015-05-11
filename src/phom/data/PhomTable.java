package phom.data;

import dreamgame.config.GameRuleConfig;
import java.util.ArrayList;
import java.util.Vector;

import dreamgame.data.Couple;
import dreamgame.data.SimpleException;
import dreamgame.data.SimpleTable;
import dreamgame.data.Timer;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Random;

import dreamgame.gameserver.framework.common.LoggerContext;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;


import java.util.ArrayList;
import java.util.ArrayList;

public class PhomTable extends SimpleTable {

    public long swap1,swap2;
    public boolean testing = false;
    public boolean winquit = false ;
    
    public int testCode = 0;
    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(PhomTable.class);
    public ArrayList<PhomPlayer> playings = new ArrayList<PhomPlayer>();
    private ArrayList<PhomPlayer> waitings = new ArrayList<PhomPlayer>();
    public ArrayList<PhomPlayer> forScores = new ArrayList<PhomPlayer>();
    //maximal number of players    
    //list off rest cards
    public ArrayList<Poker> restCards = new ArrayList<Poker>();
    public PhomPlayer currentPlayer;
    private PhomPlayer prePlayer;
    public PhomPlayer winner;
    private int currentIndexOfPlayer;
    private int firstRoundIndex;
    public boolean isUKhan = false;
    public boolean anCayMatTien = true; // default
    public boolean taiGuiUDen = false;
    public Poker currPoker = null;
    private boolean chot = false;
    private boolean haBai = false;
    public int turn = 0;
    public PhomPlayer firstChot = null;
    public int haTurn = 0;
    public int matchNum = 0;
    int turnNumber = 0;

    public boolean roomIsFull() {
        return ((getPlayings().size() + getWaitings().size()) >= getMaximumPlayer());
    }
    //private Timer timer = new Timer(ZoneID.PHOM, 60000);
    private Timer timerAuto = new Timer(ZoneID.PHOM, 5000);

    //private Timer timer = new Timer(ZoneID.PHOM, 5000);
    

    public void setOrder(long[] order) {

        for (int i = 0; i < order.length; i++) {
            order[i] = 0;
        }

        int i = 0;
        for (PhomPlayer p : playings) {
            if (i < order.length) {
                order[i] = p.id;
            }

            i++;
        }
    }

    public int numRealPlaying() {
        int sum = 0;
        for (PhomPlayer p : playings) {
            if (!p.isAutoPlay) {
                sum++;
            }
        }
        return sum;
    }
    public boolean isAllReady() {
        for (int i = 0; i < playings.size(); i++) {
            PhomPlayer player = playings.get(i);
           mLog.debug(player.username +" is ready = "+player.isReady);
            if (!player.isReady) {
                return false;
            }
        }
        return true;
    }
    public void destroy() {
        for (PhomPlayer p : playings) {
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

        System.out.println("Destroy : " + this.name);
        super.destroy();

        //timer.destroy();
    }

    public PhomPlayer getWinner() {
        return winner;
    }

    public PhomPlayer getPrePlayer() {
        return prePlayer;
    }

    public PhomPlayer getCurrentPlayer() {
        return currentPlayer;
    }

    public int getCurrentIndexOfPlayer() {
        return currentIndexOfPlayer;
    }

    public Poker getCurrPoker() {
        return currPoker;
    }

    public void setNewStarter(PhomPlayer player) {
        int index;
        try {
            index = indexOfPlayer(player);

            if (index >= 0) {
                setNewStarter(index, player);
            } else {
                setNewStarter(0, playings.get(0));
            }
        } catch (PhomException ex) {
            java.util.logging.Logger.getLogger(PhomTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setNewStarter(int index, PhomPlayer player) {
        this.currentIndexOfPlayer = index;
        this.currentPlayer = playings.get(index);
        this.firstRoundIndex = index;
        this.prePlayer = this.currentPlayer;
    }

    public ArrayList<PhomPlayer> getPlayings() {
        return playings;
    }

    public ArrayList<PhomPlayer> getWaitings() {
        return waitings;
    }

    public int getMaximumPlayer() {
        return maximumPlayer;
    }

    public void setMaximumPlayer(int max) {
        maximumPlayer = max;
    }

    public long getMoneyBet() {
        return firstCashBet;
    }

    public boolean allReady() {
        System.out.println("owner.id : " + owner.id);
        System.out.println("playings.size() : " + playings.size());

        for (PhomPlayer p : playings) {
            if (p.id != owner.id && !p.isReady) {
                return false;
            }
        }
        return true;
    }
    //Create table

    public PhomTable(PhomPlayer ow, String na, long money) {

        startTime = System.currentTimeMillis();

        ow.isReady = true;
        this.owner = ow;
        this.name = na;
        this.firstCashBet = money;
        this.playings.add((PhomPlayer) this.owner);
        this.currentIndexOfPlayer = 0;
        this.firstRoundIndex = 0;
        this.currentPlayer = (PhomPlayer) this.owner;
        currentPlayer.setCurrentOwner(this.ownerSession);
        this.prePlayer = this.currentPlayer;

        ow.currentMatchID = matchID;

        timerAuto.setRuning(false);
        timerAuto.start();
        logdir = "phom_log";

        System.out.println("Playing Size : " + playings.size());

        initLogFile();
    }

    private void resetFirstRoundIndex() {
        if (this.firstRoundIndex == this.playings.size()) {
            this.firstRoundIndex = 0;
        } else {
            this.firstRoundIndex++;
        }
    }

    public void setUKhan(boolean isUK) {
        this.isUKhan = isUK;
    }

    public boolean getUKhan() {
        return this.isUKhan;
    }

    public void setAnCayMatTien(boolean isAN) {
        this.anCayMatTien = isAN;
    }

    public void setTai(boolean isTai) {
        this.taiGuiUDen = isTai;
    }

    public void setNumber(int numberPalyer) {
        if ((numberPalyer < 4) && (numberPalyer > 1)) {
            this.maximumPlayer = numberPalyer;
        }
    }

    public void setupBotTimer() {
        timerAuto.setPhomTable(this);
        timerAuto.setTimer(5000);
        timerAuto.setRuning(true);
    }

    /**
     * *************************************************
     */
    //Player joined
    public void join(PhomPlayer player) throws PhomException {
        System.out.println("Join player : " + player.username + " ; size = " + playings.size());
        synchronized (playings) {
            for (PhomPlayer p : playings) {
                if (p.id == player.id) {
                    //user da ton tai!
                    throw new PhomException("User da ton tai.");
                }
            }
        }
        if (out_code == null) {
            initLogFile();
        }


        try {
            synchronized (this.playings) {

                System.out.println("last Acess : " + ownerSession.getLastAccessTime());
                //System.out.println("last Real Acess : "+ownerSession.);                
                if (isPlaying) {
                    player.setCurrentOwner(this.ownerSession);
                    player.currentMatchID = this.matchID;
                    player.isObserve = true;
                    waitings.add(player);
                    out_code.println("Player view : " + player.id + " " + player.username + turnInfo());
                } else if (playings.size() < 4) {
                    player.setCurrentOwner(this.ownerSession);
                    player.currentMatchID = this.matchID;
                    this.playings.add(player);
                    out_code.println("Player joined : " + player.id + " " + player.username + turnInfo());
                }
            }

//            if (owner.botPlayer) {
//                //    owner=player;
//
//                timerAuto.setPhomPlayer(player);
//                setupBotTimer();
//
//            }
//
//            if (!isPlaying) {
//                for (PhomPlayer p : playings) {
//                    if (p.currentSession == null && !p.botPlayer) {
//                        remove(p);
//                        break;
//                    }
//                }
//            }

        } catch (Exception e) {
            e.printStackTrace();

            throw new PhomException(e.getMessage());
        }
    }

    /**
     * *************************************************
     */
    public void removePlayer(long id) {

        try {
            for (PhomPlayer p : playings) {
                if (p.id == id) {
                    remove(p);
                    return;
                }
            }
            for (PhomPlayer p : waitings) {
                if (p.id == id) {
                    remove(p);
                    return;
                }
            }
        } catch (Exception e) {
            out_code.println("Error. Not found player : " + id);
        }
    }

    //Player removed
    public void remove(PhomPlayer player) throws PhomException {
        try {
            synchronized (this.playings) {

                if (winner!=null && player.id == winner.id && !isPlaying ){
                    if (owner!=null && player.id!=owner.id){
                        setNewStarter((PhomPlayer)owner);
                    }
                }
                
                out_code.println("Remove player : " + player.id);
                for (PhomPlayer p : this.playings) {
                    if (p.id == player.id) {
                        playings.remove(player);
                        return;
                    }
                }
                for (PhomPlayer p : this.waitings) {
                    if (p.id == player.id) {
                        waitings.remove(p);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            if (player != null) {
                mLog.error(turnInfo() + " : " + player.id);
            } else {
                mLog.error(turnInfo() + " : remove Null");
            }

            throw new PhomException(e.getMessage());
        }
    }

    /**
     * *************************************************
     */
    /*
     * Start game
     */
    public void start() throws PhomException {
        haTurn = 0;
        System.out.println("An cay mat tien : " + anCayMatTien);

        for (PhomPlayer p:playings){
            p.reset();
        }
        
        if (turn > 0) {
            reset();
        }
        
        turn = 0;
        matchNum++;

        if (waitings.size() > 0) {
            resetPlayers();
        }

        if (this.playings.size() > 1) {
            this.isPlaying = true;
            for (PhomPlayer p : playings) {
                try {
                    DatabaseDriver.updateUserGameStatus(p.id, 1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
            chiabai();
        } else {
            throw new PhomException("Chua co nguoi choi cung!");
        }
    }

    public void changeMoney(PhomPlayer p1, PhomPlayer p2, long money) {
        long t = p1.moneyCompute();
        if (p1.cash + t < money) {
            money = p1.cash + t;
        }

        p1.cashLost.add(new Couple<Long, Long>(p1.id, money));
        p2.cashWin.add(new Couple<Long, Long>(p2.id, money));
    }

    public PhomPlayer checkUKhan() {
        int temp = this.currentIndexOfPlayer;
        int terminalID;
        if (temp == 0) {
            terminalID = this.playings.size() - 1;
        } else {
            terminalID = temp - 1;
        }
        while (temp != terminalID) {
            PhomPlayer p = this.playings.get(temp);
            if (p.isUkhan()) {
                for (PhomPlayer player : this.playings) {
                    if (player.id != p.id) {

                        changeMoney(player, p, p.moneyForBet * 5);
                        //player.cashLost.add(new Couple<Long, Long>(this.currentPlayer.id, p.moneyForBet * 5));
                        //p.cashWin.add(new Couple<Long, Long>(p.id, p.moneyForBet * 5));

                        p.isWin = true;
                    }
                }
                this.winner = p;
                updateCash();
                return p;
            } else {
                if (temp == this.playings.size() - 1) {
                    temp = 0;
                } else {
                    temp++;
                }
            }
        }
        return null;
    }

    public boolean isHaBaiTurn() {
        int leftCard = restCards.size();
        if (leftCard < this.playings.size()) {
            return true;
        }

        return false;
    }

    public boolean lastTurn() {
        int number = this.playings.size();
        int leftCard = restCards.size();
        System.out.println("lastTurn : " + restCards.size() + " : " + leftCard);
        return (leftCard <= 0);
    }

    public void createLogFile() {
        try {

            if (out == null) {
                initLogFile();
            }

            logCode();
            logCode();
            logCode("*******************" + matchID + "-" + matchNum + " : " + owner.username + "***************************");

            logOut();
            logOut("*******************" + this.matchID + "- room" + matchID + " : " + owner.username + "***************************");

            logOut("taiGuiUDen : " + taiGuiUDen);
            logOut("anCayMatTien : " + anCayMatTien);

            logOut("Deck : " + turnInfo() + " : " + currentPlayer.showCards(restCards));
            for (int j = 0; j < playings.size(); j++) {
                logOut("[" + this.matchID + "]" + "Player [" + playings.get(j).id + "][" + playings.get(j).username
                        + "](" + playings.get(j).allCurrentCards.size() + "):" + playings.get(j).showCards(playings.get(j).allCurrentCards));
            }
            for (int j = 0; j < playings.size(); j++) {
                if (j > 0) {
                    logOut(",\"" + playings.get(j).showPureCards(playings.get(j).allCurrentCards) + "\"");
                } else {
                    logOut("\"" + playings.get(j).showPureCards(playings.get(j).allCurrentCards) + "\"");
                }
            }
            logOut("{0");
//            out.flush();

            //"23 29 4 36 14 24 48 15 28"
            //,"1 7 34 49 40 30 6 12 22 43"
            //,"35 37 10 45 46 26 41 25 47"
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chiabai() {
        if (currentIndexOfPlayer >= playings.size()) {
            setNewStarter((PhomPlayer) owner);
        }
        currentPlayer = playings.get(currentIndexOfPlayer);
        firstRound = currentIndexOfPlayer;

        /*
         * for (PhomPlayer p : playings) { try {
         * DatabaseDriver.updateUserLastMatch(p.id, p.currentMatchID); } catch
         * (Exception ex) {
         * java.util.logging.Logger.getLogger(PhomTable.class.getName()).log(Level.SEVERE,
         * null, ex); } }
         */

        /*System.out.println("Test test GetPhom : "+currentPlayer.getPhom()) ;
        currentPlayer.eatingCards.add(Utils.numToPoker(13));
        String[] hands = {
            "11#14#2#43#10#42#5#31#12#13" //,"5 6 32"
            , "6 7 31 12 29 30 24 47 46"
        };
        currentPlayer.setPoker(currentPlayer.frontCards, hands[0].replace("#", " "));
        System.out.println("eatingCards : "+currentPlayer.eatingCards);
        System.out.println("allCurrentCards : "+currentPlayer.allCurrentCards);
        System.out.println("Test test GetPhom : "+currentPlayer.getPhom()) ;
        System.out.println("new techno GetPhom : "+getCurrentPhom()) ;*/        
        
        int number = this.playings.size();
        if (number > 4) {
            System.out.println("Imposible!");
            return;
        } else {
            ArrayList<ArrayList<Poker>> res = chia(number);
            //((PhomPlayer) this.owner).setPokers(res.get(0));
            for (int j = 0; j < number; j++) {
                playings.get(j).setMoney(this.firstCashBet);
            }

            if (!testing) {
                for (int j = 0; j < number; j++) {
                    this.playings.get(j).setPokers(res.get(j));
                }


                this.currentPlayer.takeTenthPoker(this.restCards.get(restCards.size() - 1));
                this.restCards.remove(this.restCards.size() - 1);

                //mLog.info("Start match : " + turnInfo());
                //mLog.info("Start player : " + currentPlayer.username);
                //currentPlayer.showCards();
                //mLog.info("Deck : " + turnInfo() + " : " + currentPlayer.showCards(restCards));

                for (int j = 0; j < number; j++) {
                    this.playings.get(j).showCards();
                }
                //processNormalTimeOut();

            } else {
                Testing();
            }
            createLogFile();
        }
    }

    public void doTestCode() {
    }

    public void Testing2() {
        System.out.println("Start Testing2");
        String deck = "45 4 3 32 43 29 14";

        String[] hands = {
            "19 17 47 21 44 37 6 52 1 33", "7 39 40 42 48 46 18 10 8"
        };

        restCards.remove(restCards.size() - 1);
        if (deck.length() > 0) {
            String[] cards = deck.split(" ");
            for (int i = 0; i < cards.length; i++) {
                restCards.set(i, Utils.numToPoker(Integer.parseInt(cards[i])));
            }
        }
        for (int j = 0; j < playings.size(); j++) {

            PhomPlayer p = playings.get(j);
            p.setPoker(hands[j]);
            if (p.allCurrentCards.size() == 10) {
                setNewStarter(j, p);
            }
        }

        //mLog.info("Deck : " + turnInfo() + " : " + currentPlayer.showCards(restCards));
        for (int j = 0; j < playings.size(); j++) {
            this.playings.get(j).showCards();
        }
    }

    public void Testing() {

        ArrayList<Poker> res = new ArrayList<Poker>();
        for (int i = 1; i < 53; i++) {
            Poker p = Utils.numToPoker(i);
            res.add(p);
        }

        String[] hands = {
            "3 4 5 25 20 33 23 9 10 22" //,"5 6 32"
            , "6 7 31 12 29 30 24 47 46"
        };

        Random r = new Random();

        for (int j = 0; j < playings.size(); j++) {
            PhomPlayer p = playings.get(j);
            if (j >= hands.length) {

                for (int i = 0; i < 9; i++) {
                    int km = r.nextInt(restCards.size());
                    p.allCurrentCards.add(res.get(km));
                    res.remove(km);
                }

            } else {
                p.setPoker(res, hands[j]);
                if (p.allCurrentCards.size() == 10) {
                    setNewStarter(j, p);
                }
            }
        }

        if (false) {
            for (int j = 0; j < playings.size(); j++) {
                PhomPlayer p = playings.get(j);
                if (j == 0) {
                    /*
                     * allCurrentCards.set(0, new Poker(4,PokerType.Pic));
                     * allCurrentCards.set(1, new Poker(3,PokerType.Tep));
                     * allCurrentCards.set(2, new Poker(3,PokerType.Co));
                     * allCurrentCards.set(3, new Poker(3,PokerType.Ro));
                     */

                    //setPoker("17 18");

                    p.setPoker(res, "2 3 4 15 16 17 30 33 34 26");

                    //p.setPoker(res, "1 14 2 15 3 16 7 21 35 10");


                    //p.takeTenthPoker(this.restCards.lastElement());
                    //setPoker("3 20");

                    this.setNewStarter(0, p);

                } else {
                    /*
                     * allCurrentCards.set(0, new Poker(4,PokerType.Ro));
                     * allCurrentCards.set(1, new Poker(4,PokerType.Tep));
                     * allCurrentCards.set(2, new Poker(4,PokerType.Co));
                     *
                     * allCurrentCards.set(3, new Poker(3,PokerType.Pic));
                     */
                    //setPoker("4 5 6 7 8 9 10 11 25");
                    //p.setPoker(res,"17 18 3");
                    //p.setPoker(res,"32 8 9");
                    p.setPoker(res, "5 18 29 35");
                    //setPoker("4 5 33 34 21 22");
                }
            }
        }



        restCards.remove(restCards.size() - 1);
        for (int i = 0; i < playings.size() * 3 + 1; i++) {
            int k = r.nextInt(res.size());
            restCards.set(i, res.get(k));
            res.remove(k);
        }

        System.out.println("restCardsLeft : " + restCards.size());

        //mLog.info("Deck : " + turnInfo() + " : " + currentPlayer.showCards(restCards));
        for (int j = 0; j < playings.size(); j++) {
            this.playings.get(j).showCards();
        }

    }

    private ArrayList<ArrayList<Poker>> chia(int number) {
        ArrayList<ArrayList<Poker>> res = new ArrayList<ArrayList<Poker>>();
        ArrayList<Integer> currList = Utils.getRandomList();

        for (int i = 0; i < number; i++) {
            ArrayList<Poker> p = new ArrayList<Poker>();
            for (int j = 0; j < 9; j++) {
                Poker temp = Utils.numToPoker(currList.get(9 * i + j));
                p.add(temp);
            }
            res.add(p);
        }
        /**
         * all rest cards 2 players --> 8 cards 3 players --> 12 cards 4 players
         * --> 16 cards
         */
        for (int j = 0; j < 4 * number; j++) {
            Poker p = Utils.numToPoker(currList.get(9 * number + j));
            this.restCards.add(p);
        }
        return res;
    }

    public void processU() {
        try {
            this.winner = this.currentPlayer;
            this.isPlaying = false;
            timerAuto.setRuning(false);
            currentPlayer.uStatus = true;

            System.out.println("Process U: " + this.currentPlayer.uType);
            // U bt
            if ((this.currentPlayer.uType == 11)
                    || (this.currentPlayer.uType == 12 || currentPlayer.eatingCards.size() == 3)) {
                PhomPlayer preP = getPrePlayer(this.currentPlayer);
                changeMoney(preP, currentPlayer, preP.moneyForBet * 5 * (this.playings.size() - 1));

                //preP.cashLost.add(new Couple<Long, Long>(this.currentPlayer.id,
                //        preP.moneyForBet * 5 * (this.playings.size() - 1)));
                // this.currentPlayer.cashWin.add(new Couple<Long, Long>(preP.id,
                //       preP.moneyForBet * 5 * (this.playings.size() - 1)));

                currentPlayer.isWin = true;
            } else if (firstChot != null && currentPlayer.id != firstChot.id) {
                System.out.println("Tru tien thang u` de`n!");

                changeMoney(firstChot, currentPlayer, firstChot.moneyForBet * 5 * (this.playings.size() - 1));

                //firstChot.cashLost.add(new Couple<Long, Long>(this.currentPlayer.id,
                //        firstChot.moneyForBet * 5 * (this.playings.size() - 1)));
                //this.currentPlayer.cashWin.add(new Couple<Long, Long>(firstChot.id,
                //       firstChot.moneyForBet * 5 * (this.playings.size() - 1)));

                currentPlayer.isWin = true;
            } else if (this.currentPlayer.uType == 1) {
                for (PhomPlayer p : this.playings) {
                    if (p.id != this.currentPlayer.id) {
                        changeMoney(p, currentPlayer, p.moneyForBet * 5);
                        //p.cashLost.add(new Couple<Long, Long>(this.currentPlayer.id, p.moneyForBet * 5));
                        //this.currentPlayer.cashWin.add(new Couple<Long, Long>(p.id, p.moneyForBet * 5));
                        currentPlayer.isWin = true;
                    }
                }
            } // U den  || Tai gui u den

            updateCash();

            forScores = new ArrayList<PhomPlayer>();
            for (PhomPlayer p : playings) {
                forScores.add(p);
            }

            logOut();
            logOut("//EndGame :  winner U`: " + winner.username);
            logOut("//------------------------------------");
//            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkPlayCard(Poker card) {
        byte[] myHand = new byte[10];
        byte[] takenCard = new byte[4];
        int i = 0;
        for (Poker p : currentPlayer.allCurrentCards) {
            if (!p.isEqual(card)) {
                myHand[i] = (byte) p.toInt();
                i++;
            }
        }
        i = 0;
        for (Poker p : currentPlayer.eatingCards) {
            takenCard[i] = (byte) p.toInt();
            i++;
        }
        int numHand = currentPlayer.allCurrentCards.size();

        byte takenNum = (byte) currentPlayer.eatingCards.size();
        boolean haPhomFlag = currentPlayer.haPhom;

        getPhom(myHand, numHand, takenCard, takenNum, haPhomFlag);

        System.out.println("per_score1 : " + per_score1);
        if (per_score1 > 5000) {
            return false;
        }

        return true;
    }

    /**
     * *************************************************
     */
    //Play
    public void play(long uid, Poker card) throws PhomException {
        if (uid == this.currentPlayer.id) {
            this.currPoker = card;

            if (card == null) {
                mLog.error(turnInfo() + ": play : card null ! ; uid=" + uid);
            } else {
                out_code.println(turnInfo() + ": play : " + card.toString() + " ; left : " + currentPlayer.allCurrentCards.size());
            }

            this.currentPlayer.play(card);
            //if (!testing)
            {
                logOut("," + card.toInt() + "} // " + card.toString() + "   ;  " + currentPlayer.username);
//                out.flush();
            }

            if (currentPlayer.allCurrentCards.size() == 0) {
                currentPlayer.uType = 1;
                processU();
                return;
            } else {
                setChot();
                next();
            }

            if (this.isPlaying == false) {
                postProcess();
            }
        } else {
            mLog.error(turnInfo() + ": play : " + card.toString() + " ; uid=" + uid + " ; Current:");
            try {
                ISession s = findPlayer(uid).currentSession;
                mLog.error(": Fail user : " + s.userInfo() + "   ;  Current : " + currentPlayer.currentSession.userInfo());
            } catch (SimpleException ex) {
                java.util.logging.Logger.getLogger(PhomTable.class.getName()).log(Level.SEVERE, null, ex);
            }

            throw new PhomException(uid + " dang oanh bua ne!");
        }
    }

    public String turnInfo() {
        String s = "";
        try {
            s = s + "[" + this.matchID + "-" + matchNum + " : " + isPlaying + "-" + numRealPlaying() + "-" + restCards.size() + "]";
            s = s + "[turn : " + (turn / playings.size() + 1) + "]";
            s = s + "[id : " + (currentPlayer.id) + "]";
            if (currentPlayer.isAutoPlay) {
                s = s + "[Auto]";
            }

            s = s + "[" + (currentPlayer.username) + "]";
            s = s + "[";
            for (PhomPlayer p : playings) {
                if (p.currentSession == null) {
                    s = s + "0";
                } else {
                    s = s + "1";
                }
            }
            s = s + "]";
        } catch (Exception e) {
        }
        return s;
    }

    /**
     * *************************************************
     */
    public boolean checkU() {
        boolean u = false;
        String phom = getCurrentPhom();

        return u;
    }

    public String getCurrentPhom(PhomPlayer currentPlayer) {
        byte[] myHand = new byte[10];
        byte[] takenCard = new byte[4];
        int i = 0;
        for (Poker p : currentPlayer.allCurrentCards) {
            myHand[i] = (byte) p.toInt();
            i++;
        }
        i = 0;
        for (Poker p : currentPlayer.eatingCards) {
            takenCard[i] = (byte) p.toInt();
            i++;
        }
        int numHand = currentPlayer.allCurrentCards.size();

        byte takenNum = (byte) currentPlayer.eatingCards.size();
        boolean haPhomFlag = currentPlayer.haPhom;

        getPhom(myHand, numHand, takenCard, takenNum, haPhomFlag);
        return this.phomStr1;
    }

    public String getCurrentPhom() {
        return getCurrentPhom(currentPlayer);
    }

    //Boc
    public Poker getCard(long uid) throws PhomException {

        System.out.println("[getCard]: uid : " + uid + " ; current : " + currentPlayer.id + " ; rescard : " + restCards.size());

        if (uid == this.currentPlayer.id && !currentPlayer.doneBocBai) {
            //if (true){
            //System.out.println("1: "+this.restCards.size());

            Poker res = restCards.get(restCards.size() - 1);

            //System.out.println("2: "+this.restCards.size());
            this.restCards.remove(this.restCards.size() - 1);
            //System.out.println("3:"+this.restCards.size());
            this.currentPlayer.take(res);

            out_code.println(turnInfo() + "Boc : " + res.toInt() + ":" + res.toString() + " : size : " + restCards.size());

            //if (!testing)
            {
                logOut(",{0");
//                out.flush();
            }

            return res;
        } else {
            throw new PhomException(uid + " dang oanh bua");
        }

    }
    /**
     * *************************************************
     */
    public String phomStr2, phomStr1;
    boolean[] per_flag2, per_flag1;
    int per_score2 = 0;
    int per_score1 = 0;
    public boolean per_u_status;

    public int getType(int b) {
        return (b - 1) / 13;
    }

    public int getValue(int b) {
        return (b - 1) % 13;
    }

    public boolean checkEatable(PhomPlayer currentPlayer, byte lastCard) {
        //if (currentTurn!=0) return false;

//        System.out.println("**************************************");
//        System.out.println("Start checkEat : "+this.currentTurn);

        byte[] myHand = new byte[10];
        byte[] takenCard = new byte[4];
        int i = 0;
        for (Poker p : currentPlayer.allCurrentCards) {
            myHand[i] = (byte) p.toInt();
            i++;
        }
        i = 0;
        for (Poker p : currentPlayer.eatingCards) {
            takenCard[i] = (byte) p.toInt();
            i++;
        }

        int numHand = currentPlayer.allCurrentCards.size();
        System.out.println("MyHand** : ");
        for (int j = 0; j < numHand; j++) {
            System.out.print(myHand[j] + " ");
        }
        System.out.println();

        byte takenNum = (byte) currentPlayer.eatingCards.size();
        boolean haPhomFlag = currentPlayer.haPhom;

        System.out.println("LastCard : " + lastCard);
        myHand[numHand] = lastCard;

        byte lastNum = takenNum;
        byte lastV = takenCard[0];

        takenCard[takenNum] = lastCard;
        takenNum++;

        if (haPhomFlag) {
            takenCard[0] = lastCard;
            takenNum = 1;
        }

        getPhom(myHand, numHand + 1, takenCard, takenNum, haPhomFlag);

        takenNum = lastNum;
        takenCard[0] = lastV;
        if (per_score1 > 1000) {
            return false;
        }

        return true;
    }

    public void getPhom(byte[] h, int numHand, byte[] takenCard, byte takenNum, boolean haPhomFlag) {
        boolean b1 = checkPerfect1(h, numHand, takenCard, takenNum, haPhomFlag);
        boolean b2 = checkPerfect2(h, numHand, takenCard, takenNum, haPhomFlag);
        //u_status = b1 || b2;
        per_u_status = b1 || b2;
        if (per_score1 > per_score2) {
            per_flag1 = per_flag2;
            per_score1 = per_score2;
            phomStr1 = phomStr2;
        }
    }

    public boolean checkPerfect2(byte[] h, int numHand, byte[] eat, int eatnum, boolean haPhomFlag) {

        boolean[] eatflag = new boolean[53];
        for (int i = 0; i < eatflag.length; i++) {
            eatflag[i] = false;
        }
        for (int i = 0; i < eatnum; i++) {
            eatflag[eat[i]] = true;
        }

        boolean[] flag = new boolean[numHand];
        boolean[] sortedFlag = new boolean[numHand];

        byte[] sortedHand = new byte[numHand];
        int[] sortedType = new int[numHand];
        for (int i = 0; i < numHand; i++) {
            sortedHand[i] = h[i];
        }

        phomStr2 = "";

        for (int i = 0; i < numHand; i++) {
            for (int j = 0; j < numHand - i - 1; j++) {
                if (sortedHand[j] > sortedHand[j + 1]) {
                    byte tmp = sortedHand[j];
                    sortedHand[j] = sortedHand[j + 1];
                    sortedHand[j + 1] = tmp;
                }
            }
        }

        for (int i = 0; i < numHand; i++) {
            flag[i] = true;
            sortedFlag[i] = true;
            sortedType[i] = getType(sortedHand[i]);
        }

        int k = 0;

        //Tách ra các phỏm ngang trước
        for (int i = 0; i < 13; i++) {
            int count = 0;
            int counteat = 0;
            for (int j = 0; j < numHand; j++) {
                if (flag[j]) {
                    int num = (h[j] - 1) % 13;
                    if (num == i) {
                        count++;
                        if (eatflag[h[j]]) {
                            counteat++;
                        }
                    }
                }
            }
            if (count >= 3 && counteat < 2) {
                for (int j = 0; j < numHand; j++) {
                    int num = (h[j] - 1) % 13;
                    if (num == i) {
                        flag[j] = false;

                        if (phomStr2.length() > 0 && phomStr2.charAt(phomStr2.length() - 1) != ';') {
                            phomStr2 = phomStr2 + "#" + h[j];
                        } else {
                            phomStr2 = phomStr2 + h[j];
                        }
                    }
                }
                phomStr2 = phomStr2 + ";";
                k = k + count;
            }
        }
        for (int i = 0; i < numHand; i++) {
            for (int j = 0; j < numHand; j++) {
                if (h[j] == sortedHand[i]) {
                    sortedFlag[i] = flag[j];
                    break;
                }
            }
        }

        byte[] eatc = {0, 0, 0};
        //Tách tiếp các phỏm dọc
        for (int i = 0; i < numHand - 2; i++) {
            if (sortedFlag[i]) {
                int count = 1;
                int counteat = 0;
                if (eatflag[sortedHand[i]]) {
                    eatc[counteat] = sortedHand[i];
                    counteat++;
                }
                for (int j = i; j < numHand - 1; j++) {
                    if (sortedType[j] == sortedType[j + 1] && Math.abs(sortedHand[j] - sortedHand[j + 1]) == 1 && sortedFlag[j] && sortedFlag[j + 1]) {
                        count++;
                        if (eatflag[sortedHand[j + 1]]) {
                            eatc[counteat] = sortedHand[j + 1];
                            counteat++;
                        }
                    } else {
                        break;
                    }
                }
                if ((count >= 3 && counteat < 2) || (count >= 6 && counteat == 2)) {
                    if (counteat == 2) {
                        int e0 = eatc[0];
                        int e1 = eatc[1];
                        if (eatc[0] > eatc[1]) {
                            e0 = eat[1];
                            e1 = eat[0];
                        }
                        int c0 = sortedHand[i];
                        int c1 = sortedHand[i + count - 1];

                        if (c0 == e0 && e1 - e0 < 3) {
                            break;
                        }
                        if (c0 + 1 == e0 && e1 == e0 + 1) {
                            break;
                        }
                        if (c1 == e1 && e1 - e0 < 3) {
                            break;
                        }
                        if (c1 + 1 == e1 && e1 == e0 + 1) {
                            break;
                        }
                    }

                    for (int j = 0; j < count; j++) {
                        sortedFlag[i + j] = false;

                        if (phomStr2.length() > 0 && phomStr2.charAt(phomStr2.length() - 1) != ';') {
                            phomStr2 = phomStr2 + "#" + sortedHand[i + j];
                        } else {
                            phomStr2 = phomStr2 + sortedHand[i + j];
                        }
                    }
                    phomStr2 = phomStr2 + ";";
                }
            }
        }

        if (phomStr2.length() > 0) {
            if (phomStr2.charAt(phomStr2.length() - 1) == ';') {
                phomStr2 = phomStr2.substring(0, phomStr2.length() - 1);
            }
            if (phomStr2.charAt(0) == '#') {
                phomStr2 = phomStr2.substring(1);
            }
        }

        int counteat = 0;
        per_flag2 = sortedFlag;
        per_score2 = 0;
        int count = 0;
        for (int i = 0; i < numHand; i++) {
            if (sortedFlag[i] == false) {
                count++;
                if (eatflag[sortedHand[i]]) {
                    counteat++;
                }
            } else {
                per_score2 = per_score2 + getValue(sortedHand[i]) + 1;
            }
        }


        if (counteat != eatnum && !haPhomFlag) {
            per_score2 = per_score2 + 5000;
        }

        if (eatnum == 1 && haPhomFlag && counteat == 0) {
            per_score2 = per_score2 + 1000;
        }

        if (count == 0) {
            per_score2 = per_score2 + 1000;
        }

        //System.out.println("Check Phom 2: phomStr2 : " + phomStr2);
        //System.out.println("Check Perfect 2 : " + per_score2 + " : " + count);
        //System.out.println("Count Eat : " + counteat + "  ; eatnum : " + eatnum);

        if (numHand - count <= 1 && per_score2 < 30) {
            return true;
        }


        return false;
    }

    public boolean checkPerfect1(byte[] h, int numHand, byte[] eat, int eatnum, boolean haPhomFlag) {

        System.out.println("Eat cards : ");
        /*
         * for (int i = 0; i < eatnum; i++) { System.out.print(eat[i] + " "); }
         * System.out.println();
         *
         * System.out.println("MyHand : "); for (int i = 0; i < numHand; i++) {
         * System.out.print(h[i] + " "); } System.out.println();
         */

        boolean[] flag = new boolean[numHand];
        boolean[] sortedFlag = new boolean[numHand];

        boolean[] eatflag = new boolean[53];
        for (int i = 0; i < eatflag.length; i++) {
            eatflag[i] = false;
        }

        for (int i = 0; i < eatnum; i++) {
            eatflag[eat[i]] = true;
        }

        byte[] sortedHand = new byte[numHand];
        int[] sortedType = new int[numHand];
        for (int i = 0; i < numHand; i++) {
            sortedHand[i] = h[i];
        }

        for (int i = 0; i < numHand; i++) {
            for (int j = 0; j < numHand - i - 1; j++) {
                if (sortedHand[j] > sortedHand[j + 1]) {
                    byte tmp = sortedHand[j];
                    sortedHand[j] = sortedHand[j + 1];
                    sortedHand[j + 1] = tmp;
                }
            }
        }

        for (int i = 0; i < numHand; i++) {
            flag[i] = true;
            sortedFlag[i] = true;
            sortedType[i] = getType(sortedHand[i]);
        }

        phomStr1 = "";

        byte[] eatc = {0, 0, 0};
        //Tách tiếp các phỏm dọc
        for (int i = 0; i < numHand - 2; i++) {
            if (sortedFlag[i]) {
                int count = 1;
                int counteat = 0;
                if (eatflag[sortedHand[i]]) {
                    eatc[counteat] = sortedHand[i];
                    counteat++;
                }
                for (int j = i; j < numHand - 1; j++) {
                    if (sortedType[j] == sortedType[j + 1] && Math.abs(sortedHand[j] - sortedHand[j + 1]) == 1 && sortedFlag[j] && sortedFlag[j + 1]) {
                        count++;
                        if (eatflag[sortedHand[j + 1]]) {
                            eatc[counteat] = sortedHand[j + 1];
                            counteat++;
                        }
                    } else {
                        break;
                    }
                }

                if ((count >= 3 && counteat < 2) || (counteat == 2 && count >= 6)) {
                    if (counteat == 2) {
                        int e0 = eatc[0];
                        int e1 = eat[1];
                        if (eatc[0] > eatc[1]) {
                            e0 = eat[1];
                            e1 = eat[0];
                        }
                        int c0 = sortedHand[i];
                        int c1 = sortedHand[i + count - 1];

                        if (c0 == e0 && e1 - e0 < 3) {
                            break;
                        }
                        if (c0 + 1 == e0 && e1 == e0 + 1) {
                            break;
                        }
                        if (c1 == e1 && e1 - e0 < 3) {
                            break;
                        }
                        if (c1 + 1 == e1 && e1 == e0 + 1) {
                            break;
                        }
                    }

                    for (int j = 0; j < count; j++) {
                        sortedFlag[i + j] = false;
                        if (phomStr1.length() > 0 && phomStr1.charAt(phomStr1.length() - 1) != ';') {
                            phomStr1 = phomStr1 + "#" + sortedHand[i + j];
                        } else {
                            phomStr1 = phomStr1 + sortedHand[i + j];
                        }
                    }
                    phomStr1 = phomStr1 + ";";
                }
            }
        }


        for (int i = 0; i < numHand; i++) {
            for (int j = 0; j < numHand; j++) {
                if (h[i] == sortedHand[j]) {
                    flag[i] = sortedFlag[j];
                    break;
                }
            }
        }

        int k = 0;

        //Tách ra các phỏm ngang sau
        for (int i = 0; i < 13; i++) {
            int count = 0;
            int counteat = 0;

            for (int j = 0; j < numHand; j++) {
                if (flag[j]) {
                    int num = getValue(h[j]);
                    if (num == i) {
                        count++;
                        if (eatflag[h[j]]) {
                            counteat++;
                        }
                    }

                }
            }

            if (count >= 3 && counteat < 2) {
                for (int j = 0; j < numHand; j++) {
                    int num = getValue(h[j]);
                    if (num == i && flag[j]) {
                        flag[j] = false;
                        if (phomStr1.length() > 0 && phomStr1.charAt(phomStr1.length() - 1) != ';') {
                            phomStr1 = phomStr1 + "#" + h[j];
                        } else {
                            phomStr1 = phomStr1 + h[j];
                        }
                    }
                }
                phomStr1 = phomStr1 + ";";
            }
        }

        if (phomStr1.length() > 0) {
            if (phomStr1.charAt(phomStr1.length() - 1) == ';') {
                phomStr1 = phomStr1.substring(0, phomStr1.length() - 1);
            }
            if (phomStr1.charAt(0) == '#') {
                phomStr1 = phomStr1.substring(1);
            }
        }

        //System.out.println("Check Phom 1: phomStr1 : " + phomStr1);


        per_flag1 = flag;
        per_score1 = 0;
        int count = 0;
        int counteat = 0;

        for (int i = 0; i < numHand; i++) {
            if (flag[i]) {
                per_score1 = per_score1 + getValue(h[i]) + 1;
            } else {
                count++;
                if (eatflag[h[i]]) {
                    counteat++;
                }
            }
        }

        if (counteat != eatnum && !haPhomFlag) {
            per_score1 = per_score1 + 5000;
        }

        if (eatnum == 1 && haPhomFlag && counteat == 0) {
            per_score1 = per_score1 + 1000;
        }

        //mo'm
        if (count == 0) {
            per_score1 = per_score1 + 1000;
        }


        //System.out.println("Check Perfect 1 : " + per_score1 + " : " + count);


        if (numHand - count <= 1 && per_score1 < 30) {
            return true;
        }

        return false;
    }

    //An    
    public long eat(long uid) throws PhomException {
        long res = 0;
        swap1=0;
        swap2=0;
        
        System.out.println("uid : " + uid);
        System.out.println("currentPlayer.id : " + currentPlayer.id);

        if (uid == this.currentPlayer.id && checkEatable(currentPlayer, (byte) this.currPoker.toInt())) {
            PhomPlayer prePlayer = getPrePlayer(this.currentPlayer);

            //currentPlayer.showCards();            
            //System.out.println("Eatable : " + eatable);

            if (prePlayer.haPhom && firstChot == null) {
                firstChot = currentPlayer;
            }


            if (restCards.size() <= playings.size()) {
                chot = true;
            } else {
                chot = false;
            }


            res = this.currentPlayer.eat(this.currPoker, prePlayer, this.chot, this.anCayMatTien);

            changeMoney(prePlayer, currentPlayer, res);

            swapCard();
            //currentPlayer.cashWin.add(new Couple<Long, Long>(currentPlayer.id, res));
            //prePlayer.cashLost.add(new Couple<Long, Long>(prePlayer.id, res));

            //mLog.info(turnInfo() + ": an quan : " + this.currPoker);
            //if (!testing)
            {
                logOut(",{1");
//                out.flush();
            }
            // Tinh lai so cay bai truoc mat

            /*
             * System.out.println("firstRoundIndex : "+firstRoundIndex);
             * PhomPlayer firstRoundPlayer =
             * this.playings.get(this.firstRoundIndex);
             * firstRoundPlayer.numberCardPlay--; if (this.taiGuiUDen) {
             * firstRoundPlayer.isStop = false; } resetFirstRoundIndex(); if
             * (this.currentPlayer.numberCardPlay == 3) {
             * this.currentPlayer.status = 3; } else { this.currentPlayer.status
             * = 2; }
             */

        } else {
            throw new PhomException(uid + " dang oanh bua");
        }
        return res;
    }

    public boolean containPlayer(long id) {
        for (int i = 0; i < playings.size(); i++) {
            if (playings.get(i).id == id) {
                return true;
            }
        }

        return false;
    }

    /**
     * *************************************************
     */
    /*
     * Ha phom 0: Khong U 1: U 3 phom 2: UKhan
     */
    public boolean checkHaPhom(long uid, ArrayList<ArrayList<Integer>> cards,
            int u, int card) throws PhomException {
        if (u == 0) {
            if (cards != null & cards.size() > 0) {
                byte[] myHand = new byte[10];
                int numHand = 0;


                for (ArrayList<Integer> a : cards) {
                    ArrayList<Poker> temp = new ArrayList<Poker>();
                    for (int i : a) {
                        if (!currentPlayer.hasPoker(Utils.numToPoker(i))) {
                            return false;
                        }
                        myHand[numHand] = (byte) i;
                        numHand++;
                    }

                }

                byte[] takenCard = new byte[4];
                int i = 0;
                for (Poker p : currentPlayer.eatingCards) {
                    takenCard[i] = (byte) p.toInt();
                    i++;
                }

                byte takenNum = (byte) currentPlayer.eatingCards.size();
                boolean haPhomFlag = currentPlayer.haPhom;

                getPhom(myHand, numHand, takenCard, takenNum, haPhomFlag);

                if (per_u_status && per_score1 == 0) {
                    return true;
                } else {
                    return false;
                }

            }




        }
        return true;
    }

    public JSONArray getGuiBai() {
        JSONArray ja = new JSONArray();

        boolean[] flag = new boolean[53];
        for (int i = 0; i < flag.length; i++) {
            flag[i] = true;
        }

        if (!currentPlayer.momStatus) //có phỏm
        {
            for (PhomPlayer p : playings) {
                if (p.id != currentPlayer.id) {

                    for (int k = 0; k < 2; k++) {

                        try {
                            for (Poker po : currentPlayer.allCurrentCards) {
                                int i = 0;
                                for (Phom ph : p.phoms) {
                                    //System.out.println(p.username+ ":"+ph.toString()+" : card "+po.toInt()+"_"+po.toString() );
                                    if (ph.okGui((byte) po.toInt()) && flag[po.toInt()]) {
                                        flag[po.toInt()] = false;

                                        ph.guis.add(po);

                                        JSONObject jo = new JSONObject();
                                        jo.put("card", po.toInt());
                                        jo.put("guiId", p.id);
                                        jo.put("phomId", i);
                                        ja.put(jo);
                                    }
                                    i++;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }
            }

            for (int k = 0; k < flag.length; k++) {
                if (flag[k] == false) {
                    for (PhomPlayer p : playings) {
                        if (p.id != currentPlayer.id) {
                            for (Phom ph : p.phoms) {
                                if (ph.guis.size() > 0) {
                                    for (Poker po : ph.guis) {
                                        if (po.toInt() == k) {
                                            //System.out.println("len "+ph.guis.size());
                                            ph.guis.remove(po);
                                            //System.out.println("len "+ph.guis.size());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        return ja;
    }

    public ArrayList<ArrayList<Integer>> haPhom(long uid, ArrayList<ArrayList<Integer>> cards,
            int u, int card) throws PhomException {
        if (cards == null) {
            cards = new ArrayList();
        }

        if (!currentPlayer.isAutoPlay && currentPlayer.outOfTime) {
            currentPlayer.waitMe(timerAuto, 10000);
        }

        currentPlayer.doneHaBai = true;

        System.out.println("Ha phom : " + cards.size() + "  ; u = " + u + "  ; phom : " + currentPlayer.phoms);
        System.out.println("CurrentID : " + currentPlayer.id + "  ; uid = " + uid + "  ; ha. ta'i : " + currentPlayer.haPhom);
        if (firstChot != null) {
            System.out.println("Chot dau : " + firstChot.username);
        }

        out_code.println("Ha phom : " + cards.size() + "  ; u = " + u);
        logOut();
        logOut("//HaPhom : " + cards.size() + "   ; " + currentPlayer.username + " ; Ha : " + cards);
//        out.flush();

        PhomPlayer pu;
        try {
            pu = this.findPlayer(uid);

            if (turn == 0 && pu.isUkhan() && u > 0) {
                pu.isWin = true;
                winner = pu;
                for (int i = 0; i < playings.size(); i++) {
                    PhomPlayer pp = playings.get(i);
                    if (pp.id != uid) {
                        //pp.cashLost.add(new Couple<Long, Long>(this.currentPlayer.id, pu.moneyForBet * 5));
                        changeMoney(pp, pu, pu.moneyForBet * 5);
                    }
                }
                pu.uType = 2;
                //pu.cashWin.add(new Couple<Long, Long>(pu.id, pu.moneyForBet * 5 * (playings.size() - 1)));
                processU();
                return null;
            }

        } catch (SimpleException ex) {
            java.util.logging.Logger.getLogger(PhomTable.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (uid == this.currentPlayer.id) {
            ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>();
            switch (u) {
                case 0: {// Khong U
                    //Check first
                    try {

                        if (cards.size() == 0) {
                            if (!currentPlayer.haPhom) {
                                currentPlayer.momStatus = true;
                            }
                        } else {
                            currentPlayer.momStatus = false;
                        }

                        currentPlayer.haPhom = true;

                        haTurn++;

                        if (currentPlayer.stoppingOrder == 0) {
                            currentPlayer.stoppingOrder = haTurn;
                        }

                        ArrayList<Phom> phoms = currentPlayer.phoms;//new ArrayList<Phom>();


                        if (cards != null & cards.size() > 0) {
                            for (ArrayList<Integer> a : cards) {
                                ArrayList<Poker> temp = new ArrayList<Poker>();
                                for (int i : a) {

                                    currentPlayer.removePoker(Utils.numToPoker(i));
                                    System.out.println("Remove : " + i + " card left : " + currentPlayer.allCurrentCards.size());

                                    temp.add(Utils.numToPoker(i));
                                }
                                //if (Utils.checkPhom(temp)) {
                                phoms.add(new Phom(temp));
                                System.out.println("Add phom! " + phoms);
                                //} else {
                                //    throw new PhomException("Khong dung phom");
                                //}
                            }
                        }
                        this.currentPlayer.setPhoms(phoms);
                        System.out.println("new phom! " + currentPlayer.phoms);

                        /*
                         * if (!this.currentPlayer.checkEatCardInPhoms()) {
                         * throw new PhomException("Có cây ăn mà không nằm trong
                         * phỏm"); }
                         */

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new PhomException("Phom ko dung");
                    }
                    res = cards;

                    if (currentPlayer.allCurrentCards.size() <= 1) {
                        currentPlayer.uType = 1;
                        processU();
                    }

                    break;
                }
                case 1: {// U
                    currentPlayer.uStatus = true;

                    if (this.currentPlayer.checkU(card, this.taiGuiUDen, this.haBai)) {
                        for (Phom phom : this.currentPlayer.phoms) {
                            ArrayList<Integer> temp = new ArrayList<Integer>();
                            for (Poker p : phom.cards) {
                                temp.add(p.toInt());
                            }
                            res.add(temp);
                        }
                        processU();
                    } else {
                        throw new PhomException("Khong phai U ban oi");
                    }
                    break;
                }
                case 2: {// UKhan
                    if (this.isUKhan) {
                    } else {
                        throw new PhomException("Khong choi U khan!");
                    }
                    break;
                }
                default:
                    break;
            }
            return res;
        } else {
            throw new PhomException(uid + " dang oanh bua");
        }

    }

    /**
     * *************************************************
     */
    // Gui
    public boolean checkGui(long sUid, ArrayList<Integer> cards) {
        for (int i : cards) {
            try {
                PhomPlayer sPlayer = findPlayer(sUid);
                if (!sPlayer.hasPoker(Utils.numToPoker(i))) {
                    System.out.println("Not include card : " + Utils.numToPoker(i));
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean gui(long sUid, ArrayList<Integer> cards, long dUid, int phomID)
            throws PhomException {
        ArrayList<Poker> temp = new ArrayList<Poker>();
        System.out.println("gui cards : " + cards.size());

        for (int i : cards) {
            System.out.println("cards-i : " + i);
            temp.add(Utils.numToPoker(i));

            logOut();
            logOut("//gui : " + Utils.numToPoker(i) + "    ; " + currentPlayer.username);
//            out.flush();

        }
        try {
            PhomPlayer dPlayer = findPlayer(dUid);
            PhomPlayer sPlayer = findPlayer(sUid);
            dPlayer.guiED(phomID, temp);
            sPlayer.gui(temp);
            sPlayer.isStop = true;

            System.out.println("sPlayer.allCurrentCards.size() : " + sPlayer.allCurrentCards.size());
            System.out.println("sPlayer.offeringCards.size() : " + sPlayer.offeringCards.size());
            //if (sPlayer.allCurrentCards.)
            if (sPlayer.point == 0
                    || sPlayer.allCurrentCards.size() <= 1) { // U gui : 0 diem
                sPlayer.uType = 3;

                currentPlayer.uType = 1;
                processU();

                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new PhomException("Khong tim dc player de gui");
        }
    }

    /**
     * *************************************************
     */
    // Het van - tinh tien
    public void postProcess() throws PhomException {
        //this.currentPlayer.setStoppingOrder(this.playings.size());

        PhomPlayer temp = this.currentPlayer;
        System.out.println("Current player : " + currentPlayer.username);

        for (PhomPlayer player : this.playings) {
            player.computeFinalPoint();
        }

        PhomPlayer[] players = new PhomPlayer[playings.size()];
        for (int i = 0; i < playings.size(); i++) {
            players[i] = playings.get(i);
        }

        //Utils.quicksortPhomPlayers(0, this.playings.size() - 1, players);

        //sort
        for (int i = 0; i < playings.size(); i++) {
            for (int j = 0; j < playings.size() - i - 1; j++) {
                //if (players[j].point > players[j + 1].point)
                if (!players[j].isWin(players[j + 1])) {
                    PhomPlayer tmp = players[j];
                    players[j] = players[j + 1];
                    players[j + 1] = tmp;
                }
            }
        }

        winner = players[0];
        int k = 0;
        for (PhomPlayer p : players) {
            k++;
            p.winOrder = k;
        }

        if (winner == null) {
            System.out.println("Winner is still null.");
        }

        winner.isWin = true;

        for (int i = 0; i < playings.size(); i++) {
            System.out.println(i + ": " + players[i].username + " : " + players[i].point + " : " + players[i].moneyCompute() + " : " + players[i].stoppingOrder);
        }
        for (int i = 1; i < players.length; i++) {
            PhomPlayer p = players[i];
            if (!p.momStatus) {
                changeMoney(p, winner, this.firstCashBet * i);
                //p.cashLost.add(new Couple<Long, Long>(this.winner.id, this.firstCashBet * i));
                //this.winner.cashWin.add(new Couple<Long, Long>(p.id, this.firstCashBet * i));
            } else {
                changeMoney(p, winner, this.firstCashBet * 4);
                // p.cashLost.add(new Couple<Long, Long>(this.winner.id, this.firstCashBet * 4));
                //  this.winner.cashWin.add(new Couple<Long, Long>(p.id, this.firstCashBet * 4));
            }
        }

        updateCash();
        forScores = new ArrayList<PhomPlayer>();
        for (PhomPlayer p : playings) {
            forScores.add(p);
        }

        logOut();
        logOut("//EndGame :  winner: " + winner.username);
        logOut("//------------------------------------");
//        out.flush();

    }

    public void updateGift() {
        for (PhomPlayer p : playings) {
            if (p.cash == 0) {
                ISession is = p.currentSession;
                if (is != null) {
                    if (is.getRemainGift() > 0) {
                        logOut("Gift to " + p.username + " -> " + is.getCashGift());
                        is.setGiftInfo(1, is.getRemainGift());
                        DatabaseDriver.updateGiftInfo(p.id, 1, is.getRemainGift());
                    }
                }
            }
        }
    }

    private void updateCash() {
        //TODO:
        startTime = System.currentTimeMillis();

        for (int i = 0; i < playings.size(); i++) {
            try {
                PhomPlayer p = playings.get(i);
                long plus = p.moneyCompute();

                if (p.id == winner.id && plus > 0) {
                    plus = plus - plus * 5 / 100;
                }
                p.money = plus;
                String desc = "Choi game Phom matchID : " + matchID + "-" + matchNum + " (Cash:" + playings.get(i).cash + ")";
                playings.get(i).cash = DatabaseDriver.updateUserMoney(p.money, true, p.id, desc);

                logOut("End : " + p.username + " : " + p.isWin + " : " + p.moneyForBet + " ; cash : " + p.cash);
                DatabaseDriver.updateUserGameStatus(p.id, 0);

                //out.flush();

            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(PhomTable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        updateGift();
        logOut("End Game DB Processing : " + (System.currentTimeMillis() - startTime) + "ms");

        if (DatabaseDriver.log_code) {
            out.flush();
            out_code.flush();
        }

    }

    /**
     * *************************************************
     */
    public void resetPlayers() {
        System.out.println("Reset players now!");
        int num=playings.size();
        
        for (int i = num-1; i >=0 ; i--) {
            if (playings.get(i).isAutoPlay || playings.get(i).notEnoughMoney()) {
                playings.remove(i);
            }
        }
        this.playings.addAll(this.waitings);
        this.waitings = new ArrayList<PhomPlayer>();

        for (PhomPlayer p : playings) {
            p.isReady = false;            
        }
        owner.isReady = true;
    }

    //reset game
    public void reset() throws PhomException {

        System.out.println("Reset Phom data!");
        out_code.println("Reset Phom data!");

        winquit=false;
//        resetPlayers();
        turn = 0;
        if (winner != null) {
            //mLog.info("Last Match winner : " + winner.username);
        } else {
            mLog.error("Last match winner is null!");
        }

        int index = indexOfPlayer(this.winner);

        //this.owner = this.winner;
        currentPlayer = winner;
        firstChot = null;
        this.chot = false;
        this.haBai = false;
        this.currPoker = null;
        this.restCards = new ArrayList<Poker>();

        if (index >= 0) {
            setNewStarter(index, winner);
        } else {
            setNewStarter(0, playings.get(0));
        }

//        for (PhomPlayer player : this.playings) {
//            player.reset();
//        }

    }

    /**
     * *************************************************
     */
    //find player
    @Override
    public PhomPlayer findPlayer(long uid) throws SimpleException {
        for (PhomPlayer p : this.playings) {
            if (p.id == uid) {
                return p;
            }
        }

        for (PhomPlayer p : this.waitings) {
            if (p.id == uid) {
                return p;
            }
        }
        mLog.error(turnInfo() + " : findPlayer : " + uid);
        return null;
    }

    public int indexOfPlayer(PhomPlayer p) throws PhomException {
        if (p == null) {
            return -1;
        }

        for (int i = 0; i < this.playings.size(); i++) {
            PhomPlayer player = this.playings.get(i);
            if (player.id == p.id) {
                return i;
            }
        }
        return -1;
    }

    private void setChot() {
        try {
            if (this.currentPlayer.playingCards.size() == 3) { //
                PhomPlayer next = getNextPlayer(this.currentPlayer);
                if (next.playingCards.size() == 2) { //
                    PhomPlayer n = getNextPlayer(next);
                    if (n.playingCards.size() == 3) { //
                        this.chot = true;
                    }
                } else if (next.playingCards.size() == 3) {
                    //this.haBai = true;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getPrePlayerID(long uid) throws PhomException {
        try {
            PhomPlayer player = findPlayer(uid);
            int index = indexOfPlayer(player);
            if (index == 0) {
                return (this.playings.size() - 1);
            } else {
                return (index - 1);
            }
        } catch (Exception e) {
            throw new PhomException("Khong tim thay nguoi choi!");
        }
    }

    public PhomPlayer getPrePlayer(PhomPlayer p) throws PhomException {
        int index = indexOfPlayer(p);
        if (index == 0) {
            return this.playings.get(this.playings.size() - 1);
        } else {
            return this.playings.get(index - 1);
        }
    }

    public PhomPlayer getNextPlayer(PhomPlayer p) throws PhomException {
        int index = indexOfPlayer(p);
        if (index == (this.playings.size() - 1)) {
            return this.playings.get(0);
        } else {
            return this.playings.get(index + 1);
        }
    }

    public PhomPlayer ownerQuit() {
        System.out.println("owner quit!");
        for (int i = 0; i < playings.size(); i++) {
            if (!playings.get(i).isAutoPlay && !playings.get(i).notEnoughMoney()) {
                ISession p = playings.get(i).currentSession;
                for (int j = 0; j < playings.size(); j++) {
                    playings.get(j).currentOwner = p;
                }
                ownerSession = p;
                return playings.get(i);
            }
        }
        return null;
    }

    /*
     * public void processNormalTimeOut() { if (ownerSession==null)
     * System.out.println("ownerSession null :( Problems :(("); if
     * (currentPlayer.currentOwner==null) { System.out.println("Problems :((");
     * currentPlayer.currentOwner=ownerSession; }
     *
     * timerAuto.setRuning(false); timer.setPhomPlayer(currentPlayer);
     * timer.setPhomTable(this); timer.setRuning(true); timer.reset(); }
     */
    public void processAuto() {

        if (ownerSession == null) {
            System.out.println("ownerSession null :( Problems :((");
        }
        if (currentPlayer.currentOwner == null) {
            System.out.println("Problems :((");
            currentPlayer.currentOwner = ownerSession;
        }

        System.out.println("processAuto : " + currentPlayer.id + " : " + currentPlayer.username);
        System.out.println("ownerSession.uid : " + this.ownerSession.getUID());


        if (currentPlayer.isAutoPlay) {
            timerAuto.setTimer(5000);
        } else {
            timerAuto.setTimer(30000);
        }

        if (testing) {
            timerAuto.setTimer(300000);
        }

//        if (currentPlayer.botPlayer) {
//            timerAuto.setTimer(4000);
//        }

        timerAuto.setPhomPlayer(currentPlayer);
        timerAuto.setPhomTable(this);
        timerAuto.setRuning(true);
        timerAuto.reset();

    }

    public boolean allSamePlayed() {

        int a = playings.get(0).frontCards.size();
        for (int i = 0; i < playings.size(); i++) {
            if (playings.get(i).frontCards.size() != a) {
                return false;
            }
        }
        return true;
    }
    public int firstRound = 0;

    public void swapCard() {
        try {

            int currentTurn = currentIndexOfPlayer;
            int numUser = playings.size();
            ArrayList<Poker>[] users = new ArrayList[numUser];
            for (int i = 0; i < users.length; i++) {
                users[i] = playings.get(i).frontCards;
            }

            if (allSamePlayed()) {
                firstRound = currentTurn;
                return;
            }

            int lastTurn = currentTurn - 1;
            if (lastTurn < 0) {
                lastTurn = numUser - 1;
            }

            /*
             * System.out.println("currentTurn :
             * "+users[currentTurn].userInfo.name+" :
             * "+users[currentTurn].playedNum); System.out.println("firstRound :
             * "+users[firstRound].userInfo.name+" :
             * "+users[firstRound].playedNum); System.out.println("lastTurn :
             * "+users[lastTurn].userInfo.name+" : "+users[lastTurn].playedNum);
             */

            if (lastTurn != firstRound && users[currentTurn].size() >= users[lastTurn].size()) {

                if (users[currentTurn].size() == users[lastTurn].size() && numUser == 2) {
                    return;
                }
                if (users[firstRound].size() == 0) {
                    return;
                }

                if (users[currentTurn].size() == users[lastTurn].size()
                        && users[currentTurn].size() == users[firstRound].size() && numUser == 3) {
                    firstRound = currentTurn;
                    return;
                }

                int last = users[firstRound].size() - 1;
                Poker p = users[firstRound].get(last);
                users[lastTurn].add(p);
                users[firstRound].remove(last);
                
                swap1=playings.get(firstRound).id;
                swap2=playings.get(lastTurn).id;
                
                //users[firstRound].playedNum--;
                //users[lastTurn].playedCard[users[lastTurn].playedNum] = users[firstRound].playedCard[users[firstRound].playedNum];
                //users[lastTurn].playedNum++;

                firstRound++;
                if (firstRound >= numUser) {
                    firstRound = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Next player
    public void gameStop() {
        this.isPlaying = false;
        this.timerAuto.setRuning(false);
        System.out.println("Game end!");
    }

    public void next() {

        this.prePlayer = this.playings.get(this.currentIndexOfPlayer);

        if (this.currentIndexOfPlayer == this.playings.size() - 1) {
            if (!this.playings.get(0).isStop) {
                this.currentIndexOfPlayer = 0;
                this.currentPlayer = this.playings.get(0);
            } else {
                this.isPlaying = false;
            }
        } else {
            if (!this.playings.get(this.currentIndexOfPlayer + 1).isStop) {
                this.currentIndexOfPlayer++;
                this.currentPlayer = this.playings.get(this.currentIndexOfPlayer);
            } else {
                this.isPlaying = false;
            }
        }

        if (currentPlayer.haPhom && !taiGuiUDen) {
            isPlaying = false;
            System.out.println("Game end!");
            return;
        }

        currentPlayer.doneBocBai = false;
        currentPlayer.doneHaBai = false;
        currentPlayer.outOfTime = false;

        System.out.println("Next : " + currentPlayer.id + " ; auto= " + currentPlayer.isAutoPlay + " ; isPlaying: " + isPlaying);

        if (isPlaying) {
            processAuto();
        }

        turn++;

        if (lastTurn()) {
            gameStop();
        }
        
        int botPlayers=0;
        int nPlayers=0;
        PhomPlayer winner1=null;
        
        for (PhomPlayer p :playings){            
            if (!p.isAutoPlay ) {
                nPlayers++;
                winner1=p;
            }
        }
        
        if ( nPlayers==1 && botPlayers==0 ) {            
            winner=winner1;            
            for (PhomPlayer p :playings){
                if (p.id!=winner.id)
                    p.momStatus=true;
            }
            winquit=true;
            gameStop();
        }       
        
    }

    public JSONArray getPlayerName() {
        JSONArray ja = new JSONArray();

        try {

            for (PhomPlayer p : playings) {
                JSONObject jo = new JSONObject();
                jo.put("name", p.username);
                jo.put("id", p.id);
                ja.put(jo);
            }
            for (PhomPlayer p : waitings) {
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
