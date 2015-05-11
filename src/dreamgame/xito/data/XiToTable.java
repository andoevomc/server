package dreamgame.xito.data;

import dreamgame.data.MessagesID;
import java.util.ArrayList;
import java.util.logging.Level;
import org.slf4j.Logger;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.data.Timer;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.protocol.messages.TurnRequest;
import dreamgame.protocol.messages.TurnResponse;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IBusiness;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Vector;

public class XiToTable extends PokerTable {

    public static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(XiToTable.class);

    public XiToTable(PokerPlayer ow, long money, long match, int numPlayer) {
        super(ow, money, match, numPlayer);
        timerAuto = new Timer(ZoneID.XITO, 10000);
        logdir = "xito_log";
        timerAuto.setRuning(false);
        timerAuto.start();
        pokerTable = false;
    }

    @Override
    public ArrayList<Integer> getRandomList() {
//Xì tố chỉ lấy từ quân 7 đến Át
        ArrayList<Integer> res = new ArrayList<Integer>();
        ArrayList<Integer> currList = new ArrayList<Integer>();

        for (int i = 0; i
                < 52; i++) {
            if (getValue(i + 1) > 4) {
                currList.add(i + 1);
            }
        }
        System.out.println("ramdom list size:" + currList.size());
        int len = currList.size();
        for (int i = 0; i
                < len; i++) {
            int index = getRandomNumber(currList, res);
            currList.remove(index);
        }
        return res;


    }

    private void chia() {
        matchNum++;
        if (out == null) {
            initLogFile();


        }
        if (DatabaseDriver.log_code ) {
           logOut();
           logOut("*******************" + matchID + "-" + matchNum + " : (" + owner.username + ")***************************");
           logOut("Minbet : " + firstCashBet);
            if (DatabaseDriver.log_code ) {
                out_code.println();
                out_code.println("*******************" + matchID + "-" + matchNum + " : (" + owner.username + ")***************************");
            }
        }
        ArrayList<Integer> currList = getRandomList();
//        System.out.println("ramdom list size:" + currList.size());
        if ((this.playings.size() <= 4) && (this.playings.size() > 1)) {
            for (int i = 0; i
                    < playings.size(); i++) {
                PokerPlayer p = this.playings.get(i);
                byte[] cards = new byte[5];
                for (int j = 5 * i; j
                        < 5 * (i + 1); j++) {
                    cards[j - (5 * i)] = currList.get(j).byteValue();
                }
                p.setMyCards(cards);
                String s = p.username + ":(Cash " + p.cash + "):";
                for (int k = 0; k
                        < cards.length; k++) {
                    s = s + " " + cards[k];


                }

                s = s + "   ;(" + cardToString(cards) + ")";
                if (DatabaseDriver.log_code ) {
                   logOut(s);
                }

            }
            if (DatabaseDriver.log_code ) {
                out.flush();
            }
            //cheat
//            playings.get(0).setMyCards(new byte[]{7, 4});
//            playings.get(1).setMyCards(new byte[]{10, 17});

        } else {
            mLog.debug("Sai ne!");


        }
    }

    @Override
    public void startNewRound() {
        resetAllInPlayer();
        resetPlayerTurn();
        if (!checkEndMatch() && numRound < 3) {
            resetBetMoney();
            numStart();
//            if (numRound == 0) {
                startTime();
//            }
            lastTurnID = getCurrentTurnID();
            {
                new Thread() {

                    @Override
                    public void run() {
                        try {
                            sleep(2000);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(XiToTable.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        sendPoker();
                    }
                }.start();


            }
        } else {
            sendEndMatch(-1);


        }

    }

    @Override
    public void sendEndMatch(long idCancel) {
        endMatchProcess();
        isPlaying = false;
        Zone zone = this.ownerSession.findZone(ZoneID.XITO);
        Room room = zone.findRoom(this.getMatchID());
        MessageFactory msgFactory = this.ownerSession.getMessageFactory();
        EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
        endMatchRes.money = getPotMoney();
        endMatchRes.setPokerPlayer(copPlayerList());
        endMatchRes.setZoneID(ZoneID.XITO);
        endMatchRes.remainPoker = Utils.bytesToString(getRemainPoker());

        if (idCancel != 0) {
            endMatchRes.uid = idCancel;
        }
        endMatchRes.setSuccessPoker(1, getMatchID());
        if (owner.notEnoughMoney()) {
            PokerPlayer p1 = ownerQuit();
            room.setOwnerName(p1.username);
            owner = p1;
            System.out.println(p1.id + " " + p1.username + " p1");

            endMatchRes.newOwner = p1.id;
        }
        resetPlayer();
        room.broadcastMessage(endMatchRes, ownerSession, true);



    }

    public void sendPoker() {
        if (DatabaseDriver.log_code ) {
           logOut("-------------bat dau chia vong: " + numRound + "-----------");
        }
        Zone zone = this.ownerSession.findZone(ZoneID.XITO);
        Room room = zone.findRoom(this.getMatchID());
        MessageFactory msgFactory = this.ownerSession.getMessageFactory();
        TurnResponse res = (TurnResponse) msgFactory.getResponseMessage(MessagesID.MATCH_TURN);
        numRound++;
        resetBetLevel(true);
        currentBet = minBet;
        res.setMinMaxBet(minBet, maxBet);
        res.isNewRound = true;
        res.nextID = getCurrentTurnID();
        System.out.println("getCurrentTurnID(); " + getCurrentTurnID());
        res.potMoney = getPotMoney();
        for (PokerPlayer p : playings) {
            if (!p.isOutGame && !p.isFold && !p.isAllIn()) {
                int cardsType = checkCardsType(p.getMyHandForMe(numRound, false));
                if (cardsType > 0) {
                    p.setCardsType(cardsType, getCards(p.getMyHandForMe(numRound, false), cardsType));
                }
            }
            p.currentCard = p.getCurrentCard(numRound);
            if (DatabaseDriver.log_code ) {
               logOut("- chia (" + p.username + "): " + cardToString(p.currentCard) + " (" + p.currentCard + ")");
            }
//            if (!p.isOutGame) {
//                ISession aSession = ownerSession.getManager().findSession(p.id);
//                res.xitoPlayers = coppyXiToPlayerList();
//               byte[] poker=new byte[1];
//               poker[0]=p.myHand
//                res.poker = Utils.bytesToString(poker);
//                res.matchId = this.getMatchID();
//                res.setSuccessPoker(1, ZoneID.POKER);
//                aSession.write(res);
//
//            }
        }
        res.isVisible = true;
        if (numRound == 3) {
            res.isVisible = false;
        }
        res.matchId = this.getMatchID();
        res.pokerPlayers = copPlayerList();
        res.setSuccessPoker(1, ZoneID.XITO);
        room.broadcastMessage(res, this.ownerSession, true);
    }

    @Override
    public void endMatchProcess() {
        resetAllInPlayer();
        for (int i = 3; i
                >= 0; i--) {
            if (pot[i] > 0) {
                ArrayList<PokerPlayer> arr = new ArrayList<PokerPlayer>();
                for (PokerPlayer p : playings) {
//                    System.out.println(p.username + ": allinround: " + p.allInRound);
//                    if ((!p.isAllIn() || p.allInRound >= i) && !p.isOutGame && !p.isFold) {
                    if ((!p.allIn() || p.allInRound >= i) && !p.isOutGame && !p.isFold) {
                        System.out.println("added: " + p.username);
                        arr.add(p);
                    }
                }
                //người thắng từng vòng
                if (arr.size() > 0) {
                    ArrayList<PokerPlayer> winners = checkWinner(arr);
//                    int num = winners.size();
                    int numWinner = winners.size();
                    int numLost = 0;
                    long realWinMoney = 0;
                    long totalWinMoney = 0;
                    Vector<Long> winMoneys = new Vector<Long>();
                    
                    for (PokerPlayer p : winners) {
                        realWinMoney += p.potMoney[i];
                        p.isWinner = true;
                    }
                    System.out.println("số người thắng vòng " + i + ": " + numWinner);
                    logOut("số người thắng vòng " + i + ": " + numWinner);
                    for (PokerPlayer p : playings) {
                        if (!p.isWinner) {
                            System.out.println(p.username + " bỏ ra: " + p.potMoney[i] + " ở vòng " + i);
                            logOut(p.username + " bỏ ra: " + p.potMoney[i] + " ở vòng " + i);
                            long win = 0;
                            if (realWinMoney > p.potMoney[i]) {
                                totalWinMoney += p.potMoney[i];
                                win = p.potMoney[i];
                                System.out.println(p.username + " thua  " + p.potMoney[i] + " ở vòng " + i);
                                logOut(p.username + " thua  " + p.potMoney[i] + " ở vòng " + i);
                            } else {
                                p.cash += p.potMoney[i] - realWinMoney;
                                win = realWinMoney;
                                totalWinMoney += realWinMoney;
                                System.out.println(p.username + " thua  " + realWinMoney + " ở vòng " + i);
                                logOut(p.username + " thua  " + realWinMoney + " ở vòng " + i);
                            }
                            if (win > 0) {
                                numLost++;
                                winMoneys.add(Long.valueOf(win));
                            }
                        } else {
                            
                            p.cash += p.potMoney[i];
                        }
                    }
//                    System.out.println("số người thua vòng " + i + ": " + numLost);
                    System.out.println("Tổng tiền thắng ở vòng " + i + ": " + totalWinMoney);
                    logOut("Tổng tiền thắng ở vòng " + i + ": " + totalWinMoney);
                    if (numWinner >= 1 && totalWinMoney > 0) {
                        for (long aWin : winMoneys) {
                            int currNumWinner = numWinner;
                            do {
                                
                                long winMoney = aWin / currNumWinner;
                                boolean b = false;
                                for (PokerPlayer p : winners) {
                                    if (!p.addedMoney) {
                                        System.out.println(p.username + " bỏ ra: " + p.potMoney[i] + " ở vòng " + i);
                                        logOut(p.username + " bỏ ra: " + p.potMoney[i] + " ở vòng " + i);
                                        if (p.potMoney[i] < winMoney) {
                                            p.money += p.potMoney[i];
                                            
                                            System.out.println(p.username + " thắng  " + p.potMoney[i] * numLost + " ờ vòng " + i);
                                            logOut(p.username + " thắng  " + p.potMoney[i] * numLost + " ờ vòng " + i);
                                            currNumWinner--;
                                            aWin -= p.potMoney[i];
                                            p.addedMoney = true;
                                            b = true;
                                        }
                                    }
                                }
                                if (!b) {
                                    for (PokerPlayer p : winners) {
                                        if (!p.addedMoney) {
                                            p.money += winMoney;
                                            System.out.println(p.username + " thắng  " + winMoney + " ờ vòng " + i);
                                            logOut(p.username + " thắng  " + winMoney + " ờ vòng " + i);
                                            currNumWinner--;
                                            p.addedMoney = true;
                                        }
                                    }
                                }
                            } while (currNumWinner > 0);
                            for (PokerPlayer p : winners) {
                                p.addedMoney = false;
                            }
                        }
                    }
                    for (PokerPlayer p : winners) {
                        p.addedMoney = false;
                        p.isWinner = false;
                        byte[] biggestCard = p.getMyHandForMe(numRound, true);
                        int t = checkCardsType(biggestCard);
                        if (t > 0) {
                            System.out.println(cardToString(getCards(biggestCard, t)));
                        }
                        if (DatabaseDriver.log_code ) {
//                            out.println("nguoi thang vong" + i + ": " + p.username + ": +" + p.money + "US");
//                            out.flush();
                        }
                    }
                }

//                if (i == 3 || winner == null) {
//                    winner = aWinner;
//               }
//                /*for debug*/
//                byte[] biggestCard = coppyHand(aWinner.myHand);
//                int t = checkCardsType(biggestCard);
//                if (t> 52) {
//                    System.out.println(cardToString(getCards(biggestCard, t)));
//                } else {
//                    System.out.println("maxcard:" + maxCard(biggestCard));
//                }
//                /*for debug*/
            }
        }
        for (PokerPlayer p : playings) {
            if (!p.isOutGame && !p.isFold) {
                int cardsType = checkCardsType(p.getMyHandForMe(numRound, true));
                if (cardsType > 0) {
                    p.setCardsType(cardsType, getCards(p.getMyHandForMe(numRound, true), cardsType));
                }
            }
            if (p.money == 0) {
                System.out.println("người thua: " + p.username + " : " + (p.cash - p.firstCash));
                logOut("người thua: " + p.username + " : " + (p.cash - p.firstCash));
                p.money = p.cash - p.firstCash;
            } else {
                p.cash = p.firstCash + p.money;
                System.out.println("người thắng: " + p.username + " : " + (p.cash - p.firstCash));
                logOut("người thắng: " + p.username + " : " + (p.cash - p.firstCash));
//                p.money = p.cash - p.firstCash;
            }
        }
        updateCash();
    }

    public void autoShow(long uid) {
        System.out.println("ownerSession" + ownerSession);
//                System.out.println("tienlenPlayer.id" + tienlenPlayer.id);
        ISession session = ownerSession.getManager().findSession(uid);
        IResponsePackage responsePkg = session.getDirectMessages();//new SimpleResponsePackage();
        MessageFactory msgFactory = session.getMessageFactory();
        TurnRequest reqMatchTurn = (TurnRequest) msgFactory.getRequestMessage(MessagesID.MATCH_TURN);
        reqMatchTurn.isShow = true;
//        reqMatchTurn.money = 0;
        reqMatchTurn.uid = uid;
//        reqMatchTurn.isTimeout = true;
        reqMatchTurn.mMatchId = getMatchID();
        reqMatchTurn.visibleCard = findPlayer(uid).myHand[0];
//                reqMatchTurn.isTimeoutTL = true;

        IBusiness business = null;
        // Check if timeout
        if (reqMatchTurn.uid != -1) {
            try {
                business = msgFactory.getBusiness(MessagesID.MATCH_TURN);
                business.handleMessage(session, reqMatchTurn, responsePkg);
            } catch (ServerException se) {
            }
        }
    }

    @Override
    public ArrayList<PokerPlayer> checkWinner(ArrayList<PokerPlayer> arr) {
        ArrayList<PokerPlayer> resultArr = new ArrayList<PokerPlayer>();
        if (arr.size() == 1) {
            resultArr.add(findPlayer(arr.get(0).id));
            return resultArr;
        }
        int winIndex = 0;
        for (int i = 1; i < arr.size(); i++) {
            if (compareCards2(arr.get(i).getMyHandForMe(numRound, true), arr.get(winIndex).getMyHandForMe(numRound, true)) == 1) {
                winIndex = i;
            }
        }
        PokerPlayer p = arr.get(winIndex);
        resultArr.add(p);
        for (int i = 1; i < arr.size(); i++) {
            if (compareCards2(p.getMyHandForMe(numRound, true), arr.get(i).getMyHandForMe(numRound, true)) == 0) {
                PokerPlayer p1 = arr.get(i);
                if (p1.id != p.id) {
                    resultArr.add(p1);
                }
            }
        }
//        arr.remove(0);        
        return resultArr;
    }

    public int compareCards2(byte[] cards1, byte[] cards2) {
        int v1 = checkCardsType(cards1);
        int v2 = checkCardsType(cards2);
//        System.out.println("v1: " + v1 + ",   v2: " + v2);
        if (v1 == v2) {
            if (v1 == 0) {
                System.out.println("2 bộ bài mậu thầu -> xét quân bài lớn nhất");
                int maxSub1 = maxCard(cards1);
                int maxSub2 = maxCard(cards2);
                if (maxSub1 == maxSub2) {
                    return 0;
                } else if (maxSub1 > maxSub2) {
                    return 1;
                }
                return -1;
            } else {
                byte[] b1 = getCards(cards1, v1);
                byte[] b2 = getCards(cards2, v2);
                return compareCards(b1, b2);
            }
        } else if (v1 > v2) {
            return 1;
        }
        return -1;
    }

    @Override
    public void resetTable() {
        super.resetTable();

    }

    public boolean showCard(long uid, int card) {
        PokerPlayer p = findPlayer(uid);
        if (p.isContainsCards((byte) card) && !p.isShow()) {
            p.mVisibleCard = card;
            return true;
        }
        return false;
    }

    @Override
    public void startMatch() {
        long time=System.currentTimeMillis();
//        System.out.println(getValue(18));
        resetTable();

        for (PokerPlayer p : playings) {
            try {
                DatabaseDriver.updateUserGameStatus(p.id, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (this.playings.size() > 1) {
            this.isPlaying = true;
            chia();
//            numStart();
            startBet();
//            resetBetLevel(true);
            this.timerAuto.setOwnerSession(ownerSession);
            startShowTime();
//            try {
//                this.timerAuto.start();
//            } catch (Exception e) {
//                this.timerAuto.reset();
//            }


        } else {
            mLog.debug("Chua co nguoi choi nao!");
        }
        
        if (DatabaseDriver.log_code && out!=null ) {
            out.println("Start Game DB Processing : " + (System.currentTimeMillis() - time) + "ms");
            out.flush();
        }

    }

    public void startShowTime() {
        timerAuto.setTimer(7000);
        timerAuto.setShowTime(this);
//        timerAuto.setTienLenTable(this);
        timerAuto.setRuning(true);
        timerAuto.reset();
    }

    @Override
    public void startTime() {
        timerAuto.setTimer(20000);
        timerAuto.setXiTo(currPlayer, this);
//        timerAuto.setTienLenTable(this);
        timerAuto.setRuning(true);
        timerAuto.reset();
    }

    public void showAll() {
        for (PokerPlayer p : playings) {
            if (!p.isOutGame & !p.isShow()) {
                final long uid = p.id;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(XiToTable.class.getName()).log(Level.SEVERE, null, ex);
                }
                new Thread() {

                    @Override
                    public void run() {
                        autoShow(uid);
                    }
                }.start();
            }
        }
    }

    public boolean isAllShow() {
        for (PokerPlayer p : playings) {
            if (!p.isOutGame & !p.isShow()) {
                return false;
            }
        }
        return true;
    }
//    private void updateCash() {
//        startTime = System.currentTimeMillis();
//        //TODO:
//        for (int i = 0; i < playings.size(); i++) {
//            try {
//                long plus = playings.get(i).money;
//                String desc = "Choi game Xito matchID : " + matchID + "-" + matchNum + " . (cash : " + playings.get(i).cash + ")";
////                if (playings.get(i).id == winner.id) {
////                if (plus > 0) {
////                    long realPlus = plus - plus * DatabaseDriver.taxPlayGame / 100;
////
////                }
////                System.out.println("Tiền người thắng được cộng: " + plus);
////                playings.get(i).cash = DatabaseDriver.getUserMoney(winner.id) + plus;
//
////                }
//                //                playings.get(i).cash = playings.get(i).cash + plus;
//
//               logOut("End : " + playings.get(i).username + " : " + plus + " -> " + playings.get(i).cash);
//                //người thoát giữa chừng đã bị trừ tiền lúc thoát rồi
//
//
////                if (!playings.get(i).isOutGame) {
//                DatabaseDriver.updateUserGameMoney(plus, true, playings.get(i).id, desc, playings.get(i).currentOwner.getCurrentZone());
//                //                }
//
//                DatabaseDriver.updateExp(playings.get(i).id, 1, playings.get(i).currentSession);
//                playings.get(i).cash = DatabaseDriver.getUserMoney(playings.get(i).id);
//            } catch (Exception ex) {
////                java.util.logging.Logger.getLogger(PhomTable.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
////        updateGift();
//       logOut("End Game DB Processing : " + (System.currentTimeMillis() - startTime) + "ms");
//        out.flush();
//    }
}
