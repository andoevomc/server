/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.SimpleTable;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.GetPokerResponse;
import dreamgame.protocol.messages.StartRequest;
import dreamgame.protocol.messages.StartResponse;
import dreamgame.protocol.messages.StartedResponse;
import dreamgame.protocol.messages.TurnResponse;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.baucua.data.BauCuaTable;

//import dreamgame.chan.data.ChanPlayer;
//import dreamgame.chan.data.ChanTable;
import dreamgame.config.DebugConfig;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;

//import dreamgame.oantuti.data.OantutiTable;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.protocol.messages.OutResponse;
import dreamgame.xito.data.XiToTable;
import org.slf4j.Logger;

import phom.data.PhomPlayer;
import phom.data.PhomTable;

/**
 *
 * @author binh_lethanh
 */
public class StartBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(StartBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "StartBusiness - handleMessage");
	}
        boolean isFail = false;
        mLog.debug("[START] : Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        StartResponse resMatchStart = (StartResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        GetPokerResponse getPokerOwner = (GetPokerResponse) msgFactory.getResponseMessage(MessagesID.GET_POKER);

        try {
            StartRequest rqMatchStart = (StartRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room room = bacayZone.findRoom(rqMatchStart.mMatchId);
            TurnResponse resMatchTurn = (TurnResponse) msgFactory.getResponseMessage(MessagesID.MATCH_TURN);
            if (room != null) {
                mLog.debug("[START] : MatchID - " + rqMatchStart.mMatchId);
                StartedResponse broadcastMsg = (StartedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_STARTED);
                SimpleTable table = (SimpleTable) room.getAttactmentData();
                OutResponse rqsOut =
                        (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);

                switch (aSession.getCurrentZone()) {
                    case ZoneID.BACAY: {
                        // Start game
                        room.setPlaying(true);
                        ((BacayTable) table).startMatch();
                        resMatchStart.setCurrentPlayer(((BacayTable) table).getPlayers(),
                                (BacayPlayer) table.owner);
                        // Send poker to client
                        broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                ((BacayTable) table).getCurrentPlayer().id);

                        for (BacayPlayer player : ((BacayTable) table).getPlayers()) {
                            player.cash = DatabaseDriver.getUserMoney(player.id);
                            long playerID = player.id;
                            ISession playerSession = aSession.getManager().findSession(playerID);
                            System.out.println("Bacay p : " + playerSession.userInfo());

                            if (playerSession != null && player.cash > ((BacayTable) table).firstCashBet) {
                                GetPokerResponse getPoker = (GetPokerResponse) msgFactory.getResponseMessage(MessagesID.GET_POKER);
                                getPoker.setSuccess(ResponseCode.SUCCESS, player.id,
                                        player.username, player.playingCards[0],
                                        player.playingCards[1], player.playingCards[2]);
                                getPoker.first_id = ((BacayTable) table).getPlayers().get(0).id;
                                getPoker.matchId = rqMatchStart.mMatchId;

                                playerSession.write(getPoker);
                            } else {
                                rqsOut =
                                        (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);

                                ((BacayTable) table).removePlayer(playerID);
                                rqsOut.setSuccess(ResponseCode.SUCCESS, player.id,
                                        player.username + " không còn đủ tiền để chơi game này nữa.", player.username, 1);
                                Thread.sleep(400);
                                room.broadcastMessage(rqsOut, aSession, true);
                            }

                        }
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException ioe) {
                            ioe.printStackTrace();
                        }


                        // send broadcast msg  started to friends

                        /*
                         * if (aSession.getMobile()) {
                         * room.broadcastMessage(broadcastMsg, aSession, false);
                         * }
                         */

                        //Thread.sleep(400);
                        if (aSession.isAndroid()) {
                            BacayPlayer owner = ((BacayTable) table).getRoomOwner();
                            getPokerOwner.setSuccess(ResponseCode.SUCCESS, owner.id,
                                    owner.username, owner.playingCards[0],
                                    owner.playingCards[1], owner.playingCards[2]);
                            getPokerOwner.first_id = ((BacayTable) table).getPlayers().get(0).id;
                            getPokerOwner.matchId = rqMatchStart.mMatchId;

                        }
                        resMatchStart.setSuccess(ResponseCode.SUCCESS,
                                ((BacayTable) table).getRoomOwner().playingCards, aSession.getCurrentZone());
                        // response turn
                        //remove by tuanha ???

                        long id = ((BacayTable) table).getCurrentPlayer().id;
                        ISession n = aSession.getManager().findSession(id);
                        
                        /*if (n.getMobile() && n.getMobileVer().length() == 0) {
                            long moneyLeft = -1;
                            moneyLeft = ((BacayTable) table).moneyLeftOfPlayer(((BacayTable) table).getCurrentPlayer(),
                                    0, false, false);
                            resMatchTurn.setSTTTo(moneyLeft);
                            resMatchTurn.setPreID(table.owner.id);
                            resMatchTurn.setSuccess(ResponseCode.SUCCESS, table.getMinBet(),
                                    ((BacayTable) table).nextTurn(), 0, aSession.getCurrentZone());
                            n.write(resMatchTurn);
                        }*/
                        
                        /*
                         * long moneyLeft = -1; moneyLeft = ((BacayTable)
                         * table).moneyLeftOfPlayer(((BacayTable)
                         * table).getCurrentPlayer(), 0, false, false);
                         * resMatchTurn.setSTTTo(moneyLeft);
                         * resMatchTurn.setPreID(table.owner.id);
                         * resMatchTurn.setSuccess(ResponseCode.SUCCESS,
                         * table.getMinBet(), ((BacayTable) table).nextTurn(),
                         * 0, aSession.getCurrentZone());
                         * room.broadcastMessage(resMatchTurn, aSession, true);
                         */

                        break;

                    }
                    case ZoneID.OTT: {
                        //deleted
                        break;
                    }
                    case ZoneID.GAME_CHAN: {
//                        ChanTable pTable = (ChanTable) table;
//                        for (ChanPlayer p : pTable.getPlayings()) {
//                            p.cash = DatabaseDriver.getUserMoney(p.id);
//                            if (p.cash < pTable.firstCashBet * 3) {
//                                resMatchStart.setFailure(ResponseCode.FAILURE,
//                                        p.username + "Không đủ tiền để chơi!");
//                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//                                return 1;
//                            }
//                        }
//                        System.out.println("Num playing : " + pTable.getPlayings().size());
//                        if (pTable.getPlayings().size() < 2) {
//                            resMatchStart.setFailure(ResponseCode.FAILURE,
//                                    "Bàn chưa đủ người chơi!");
//                            isFail = true;
//                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//                            return 1;
//                        }
//                        for (ChanPlayer player : pTable.getPlayings()) {
//                            if (!player.isReady) {
//                                resMatchStart.setFailure(ResponseCode.FAILURE,
//                                        "Còn người chơi chưa sẵn sàng!");
//                                isFail = true;
//                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//                                return 1;
//                            }
//                        }
//
//                        pTable.start();
//                        broadcastMsg.setSuccess(ResponseCode.SUCCESS);
//                        broadcastMsg.setStarterID(pTable.getCurrentPlayer().id);
//                        resMatchStart.setSuccess(ResponseCode.SUCCESS, ZoneID.GAME_CHAN);
//
//                        for (ChanPlayer player : pTable.getPlayings()) {
//                            //System.out.println("id: " + player.id);
//                            long playerID = player.id;
//                            ISession playerSession = aSession.getManager().findSession(playerID);
//                            GetPokerResponse getPoker = (GetPokerResponse) msgFactory.getResponseMessage(MessagesID.GET_POKER);
//                            getPoker.setSuccess(ResponseCode.SUCCESS, player.id,
//                                    player.username);
//
//                            getPoker.setBeginID(pTable.getCurrentPlayer().id);
//                            getPoker.matchNum = pTable.matchNum;
//                            //getPoker.matchNum = pTable.ownerRoom.matchId;
//
//                            getPoker.chanCards = player.allCurrentCards;
//                            getPoker.matchId = rqMatchStart.mMatchId;
//                            pTable.setOrder(getPoker.order);
//
//
//                            if (playerSession == null) {
//                                mLog.error(pTable.turnInfo() + " : [" + player.username + "]");
//                                playerSession = player.currentSession;
//                            }
//
//                            if (playerID == aSession.getUID()) {
//                                aResPkg.addMessage(getPoker);
//                                // aSession.write(getPoker);
//                            } else {
//                                playerSession.write(getPoker);
//                            }
//                        }

                        break;
                    }

                    case ZoneID.PHOM: {
                        PhomTable pTable = (PhomTable) table;
                        for (PhomPlayer p : pTable.getPlayings()) {
                            p.cash = DatabaseDriver.getUserMoney(p.id);
                            if (p.cash < pTable.firstCashBet * 3) {
                                resMatchStart.setFailure(ResponseCode.FAILURE,
                                        p.username + "Không đủ tiền để chơi!");
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            }
                        }
                        if (pTable.getPlayings().size() < 2) {
                            isFail = true;
                            resMatchStart.setFailure(ResponseCode.FAILURE,
                                    "Không đủ người chơi!");
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }
                        mLog.debug(" is all ready = " + pTable.isAllReady());
                        if (!pTable.isAllReady()) {
                            resMatchStart.setFailure(ResponseCode.FAILURE, "Còn người chơi chưa sẵn sàng!");
                            aSession.write(resMatchStart);
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }
                        if (pTable.isPlaying) {
                            resMatchStart.setFailure(ResponseCode.FAILURE, "Ván chơi chưa kết thúc, bạn không thể bắt đầu ván mới!");
                            aSession.write(resMatchStart);
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }
                        pTable.start();
                        broadcastMsg.setSuccess(ResponseCode.SUCCESS);
                        broadcastMsg.setStarterID(pTable.getCurrentPlayer().id);
                        resMatchStart.setSuccess(ResponseCode.SUCCESS, ZoneID.PHOM);
                        // Send poker to client
                        for (PhomPlayer player : pTable.getPlayings()) {
                            //System.out.println("id: " + player.id);
                            long playerID = player.id;
                            ISession playerSession = aSession.getManager().findSession(playerID);
                            GetPokerResponse getPoker = (GetPokerResponse) msgFactory.getResponseMessage(MessagesID.GET_POKER);
                            getPoker.setSuccess(ResponseCode.SUCCESS, player.id,
                                    player.username);

                            getPoker.setBeginID(pTable.getCurrentPlayer().id);
                            getPoker.matchNum = pTable.matchNum;
                            getPoker.setPhomCards(player.allCurrentCards);
                            getPoker.matchId = rqMatchStart.mMatchId;
                            pTable.setOrder(getPoker.order);



                            if (playerSession == null) {
                                mLog.error(pTable.turnInfo() + " : [" + player.username + "]");
                                playerSession = player.currentSession;
                            }

                            if (playerID == aSession.getUID()) {
                                aResPkg.addMessage(getPoker);
                                // aSession.write(getPoker);
                            } else {
                                playerSession.write(getPoker);
                            }
                        }

                        if (aSession.getMobile()) {
                            room.broadcastMessage(broadcastMsg, aSession, true);
                        }

                        pTable.processAuto();

                        resMatchStart = null;
                        //check UKhan

                        /*
                         * if (pTable.getUKhan()) { PhomPlayer pUKhan =
                         * pTable.checkUKhan(); if (pUKhan != null) {
                         * EndMatchResponse endMatchRes = (EndMatchResponse)
                         * msgFactory.getResponseMessage(MessagesID.MATCH_END);
                         * // set the result endMatchRes.setZoneID(ZoneID.PHOM);
                         * endMatchRes.setSuccess(ResponseCode.SUCCESS,
                         * pTable.getPlayings(), pTable.getWinner());
                         * room.broadcastMessage(endMatchRes, aSession, true);
                         * room.setPlaying(false); } }
                         */

                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                        return 1;
                    }
                    case ZoneID.TIENLEN_MB:
                    case ZoneID.TIENLEN_DEMLA:
                    case ZoneID.TIENLEN: {
                        TienLenTable tTable = (TienLenTable) table;
                        for (TienLenPlayer p : tTable.getPlayings()) {
                            p.cash = DatabaseDriver.getUserMoney(p.id);
                            if (p.cash < tTable.firstCashBet * 10) {
                                resMatchStart.setFailure(ResponseCode.FAILURE,
                                        p.username + "Không đủ tiền để chơi!");
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            }
                        }
                        if (tTable.getPlayings().size() < 2) {
                            isFail = true;
                            resMatchStart.setFailure(ResponseCode.FAILURE,
                                    "Không đủ người chơi!");
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }

                        if (aSession.getUID() == tTable.owner.id) {
                            if (tTable.isAllReady()) {
                                long[] L = tTable.startMatch();
                                long idPerfectWin = L[0];
                                if (idPerfectWin > 0) {//Tới trắng khi chia bài!!!!
                                    table.isPlaying = false;
                                    EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
                                    // set the result
                                    endMatchRes.setZoneID(ZoneID.TIENLEN);
                                    endMatchRes.setTLPlayer(tTable.copPlayerList());
                                    endMatchRes.setSuccessTienLen(ResponseCode.SUCCESS, tTable.GetEndGamePerfect(idPerfectWin), idPerfectWin, tTable.matchID);
                                    endMatchRes.perfectType = L[1];
                                    endMatchRes.matchId = tTable.matchID;

                                    room.broadcastMessage(endMatchRes, aSession, true);
                                } else {
//                            tTable.isNewMatch = false;
//                        broadcastMsg.setSuccess(ResponseCode.SUCCESS);
//                        broadcastMsg.setStarterID(pTable.getCurrPlayer().id);
//                        resMatchStart.setSuccess(ResponseCode.SUCCESS, ZoneID.PHOM);
                                    // Send poker to client
                                    for (TienLenPlayer player : tTable.getPlayings()) {
                                        long playerID = player.id;
                                        ISession playerSession = aSession.getManager().findSession(playerID);
                                        GetPokerResponse getPoker = (GetPokerResponse) msgFactory.getResponseMessage(MessagesID.GET_POKER);
                                        getPoker.setSuccess(ResponseCode.SUCCESS, player.id,
                                                player.username);

                                        getPoker.tienlenCards_new = new byte[13];
                                        getPoker.matchNum = tTable.matchNum;
                                        getPoker.setBeginID(tTable.getCurrentTurnID());
                                        getPoker.isNewMatch = tTable.isNewMatch;
                                        getPoker.setTienLenCards(player.myHand);
                                        getPoker.matchId = rqMatchStart.mMatchId;
                                        tTable.setOrder(getPoker.order);

                                        if (playerSession == null) {
//                                    mLog.error(pTable.turnInfo() + " : [" + player.username + "]");
                                            //playerSession = player.currentSession;
                                            tTable.remove(player);
                                        } else if (playerID == aSession.getUID()) {
                                            aResPkg.addMessage(getPoker);
                                        } else {
                                            //for (int i=0;i<5;i++)
                                            playerSession.write(getPoker);
                                        }

                                    }
                                    tTable.isNewMatch = false;
//                               tTable.startTime();
                                }
//                        room.broadcastMessage(broadcastMsg, aSession, true);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            } else {
                                isFail = true;
                                resMatchStart.setFailure(ResponseCode.FAILURE,
                                        "Còn người chơi chưa sẵn sàng!");
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            }
                        } else {
                            isFail = true;
                            resMatchStart.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn, không được quyền bắt đầu!");
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }
                    }
                    case ZoneID.BAUCUA: {
                        BauCuaTable tTable = (BauCuaTable) table;
                        for (BauCuaPlayer p : tTable.getPlayings()) {
                            p.cash = DatabaseDriver.getUserMoney(p.id);
                            if (p.cash < tTable.firstCashBet) {
                                resMatchStart.setFailure(ResponseCode.FAILURE,
                                        p.username + "Không đủ tiền để chơi!");
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            }
                        }
                        if (tTable.getPlayings().size() < 2) {
                            isFail = true;
                            resMatchStart.setFailure(ResponseCode.FAILURE,
                                    "Không đủ người chơi!");
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }

                        if (aSession.getUID() == tTable.owner.id) {
//                            if (tTable.isAllReady()) {
                            tTable.startMatch();
                            // Send poker to client
                            for (BauCuaPlayer player : tTable.getPlayings()) {
                                long playerID = player.id;
                                ISession playerSession = aSession.getManager().findSession(playerID);
                                GetPokerResponse getPoker = (GetPokerResponse) msgFactory.getResponseMessage(MessagesID.GET_POKER);
                                getPoker.zoneID = ZoneID.BAUCUA;
                                getPoker.setSuccess(ResponseCode.SUCCESS, player.id,
                                        player.username);
                                getPoker.timeBet = tTable.timeBet;
//                                        getPoker.tienlenCards_new = new byte[13];
                                getPoker.matchNum = tTable.matchNum;
//                                        getPoker.setBeginID(tTable.getCurrentTurnID());
//                                        getPoker.isNewMatch = tTable.isNewMatch;
//                                        getPoker.setTienLenCards(player.myHand);
                                getPoker.matchId = rqMatchStart.mMatchId;
//                                        tTable.setOrder(getPoker.order);

                                if (playerSession == null) {
//                                    mLog.error(pTable.turnInfo() + " : [" + player.username + "]");
                                    //playerSession = player.currentSession;
                                    tTable.remove(player);
                                } else if (playerID == aSession.getUID()) {
                                    aResPkg.addMessage(getPoker);
                                } else {
                                    //for (int i=0;i<5;i++)
                                    playerSession.write(getPoker);
                                }

                            }


//                                room.broadcastMessage(broadcastMsg, aSession, true);
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
//                            } else {
//                                isFail = true;
//                                resMatchStart.setFailure(ResponseCode.FAILURE,
//                                        "Còn người chơi chưa sẵn sàng!");
//                                return 1;
//                            }
                        } else {
                            isFail = true;
                            resMatchStart.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn, không được quyền bắt đầu!");
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }
                    }
                    case ZoneID.POKER: {
                        PokerTable tTable = (PokerTable) table;
                        if (tTable.getPlayings().size() < 2) {
                            isFail = true;
                            resMatchStart.setFailure(ResponseCode.FAILURE,
                                    "Bàn chưa đủ người chơi!");
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }

                        if (aSession.getUID() == tTable.owner.id) {
                            if (tTable.isAllReady()) {
                                tTable.startMatch();
                                // Send poker to client
                                for (PokerPlayer player : tTable.getPlayings()) {
                                    long playerID = player.id;
                                    ISession playerSession = aSession.getManager().findSession(playerID);
                                    GetPokerResponse getPoker = (GetPokerResponse) msgFactory.getResponseMessage(MessagesID.GET_POKER);
                                    getPoker.setSuccess(ResponseCode.SUCCESS, player.id,
                                            player.username);

                                    getPoker.potMoney = tTable.getPotMoney();
                                    getPoker.matchId = tTable.matchID;
                                    getPoker.matchNum = tTable.matchNum;

                                    getPoker.setBeginID(tTable.getCurrentTurnID());
                                    getPoker.setMinMaxBet(tTable.minBet, tTable.maxBet);
                                    getPoker.setPokerCards(player.myHand, ZoneID.POKER);
                                    getPoker.matchId = rqMatchStart.mMatchId;
                                    tTable.setOrder(getPoker.order);

                                    if (playerSession == null) {
//                                    mLog.error(pTable.turnInfo() + " : [" + player.username + "]");
                                        //playerSession = player.currentSession;
                                        tTable.remove(player);
                                    } else if (playerID == aSession.getUID()) {
                                        aResPkg.addMessage(getPoker);
                                    } else {
                                        //for (int i=0;i<5;i++)
                                        playerSession.write(getPoker);
                                    }

                                }
//                                tTable.startNewRound();

                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            } else {
                                isFail = true;
                                resMatchStart.setFailure(ResponseCode.FAILURE,
                                        "Còn người chơi chưa sẵn sàng!");
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            }
                        } else {
                            isFail = true;
                            resMatchStart.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn!");
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }
                    }
                    case ZoneID.XITO: {
                        System.out.println("Start vao xito!");
                        XiToTable tTable = (XiToTable) table;
                        if (tTable.getPlayings().size() < 2) {
                            resMatchStart.setFailure(ResponseCode.FAILURE,
                                    "Bàn chưa đủ người chơi!");
                            isFail = true;
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }

                        if (aSession.getUID() == tTable.owner.id) {
                            if (tTable.isAllReady()) {
                                tTable.startMatch();
                                // Send poker to client
                                for (PokerPlayer player : tTable.getPlayings()) {
                                    long playerID = player.id;
                                    ISession playerSession = aSession.getManager().findSession(playerID);
                                    GetPokerResponse getPoker = (GetPokerResponse) msgFactory.getResponseMessage(MessagesID.GET_POKER);
                                    getPoker.setSuccess(ResponseCode.SUCCESS, player.id,
                                            player.username);
                                    getPoker.potMoney = tTable.getPotMoney();
                                    getPoker.matchNum = tTable.matchNum;
                                    getPoker.matchId = tTable.matchID;

//                                    getPoker.setBeginID(tTable.getCurrentTurnID());
//                                    getPoker.setMinMaxBet(tTable.minBet, tTable.maxBet);
                                    byte[] poker = new byte[2];
                                    poker[0] = player.myHand[0];
                                    poker[1] = player.myHand[1];
                                    getPoker.setPokerCards(poker, ZoneID.XITO);
                                    getPoker.matchId = rqMatchStart.mMatchId;
                                    tTable.setOrder(getPoker.order);

                                    if (playerSession == null) {
//                                    mLog.error(pTable.turnInfo() + " : [" + player.username + "]");
                                        //playerSession = player.currentSession;
                                        tTable.remove(player);
                                    } else if (playerID == aSession.getUID()) {
                                        aResPkg.addMessage(getPoker);
                                    } else {
                                        //for (int i=0;i<5;i++)
                                        playerSession.write(getPoker);
                                    }

                                }
//                                tTable.startNewRound();
                                System.out.println("Start vao đến đây!");
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            } else {
                                isFail = true;
                                resMatchStart.setFailure(ResponseCode.FAILURE,
                                        "Còn người chơi chưa sẵn sàng!");
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            }
                        } else {
                            isFail = true;
                            resMatchStart.setFailure(ResponseCode.FAILURE,
                                    "Bạn không phải là chủ bàn!");
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }
                    }
                    case ZoneID.MAUBINH: {
                            return 1;
                    }
                    //TODO: Add more here
                    default:
                        break;
                }

            } else {
                resMatchStart.setFailure(ResponseCode.FAILURE,
                        "Chủ bàn đã nghỉ. Bạn vui lòng chọn trận khác hoặc tạo ra bàn riêng của bạn.");
                isFail = true;
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }
        } catch (Throwable t) {
            resMatchStart.setFailure(ResponseCode.FAILURE, "Bị lỗi "
                    + t.toString());
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
            isFail = true;

        } finally {
            //System.out.println("Co vao day khong ban oi");
            mLog.debug("respond = "+resMatchStart);
            if (resMatchStart != null) {
                if (!isFail && (aSession.getCurrentZone() == ZoneID.BACAY)) {
                    aResPkg.addMessage(resMatchStart);
                }
                if(isFail){
                    aResPkg.addMessage(resMatchStart);
                }
            }            
            if (aSession.isAndroid()) {
                aResPkg.addMessage(getPokerOwner);
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return -1;
            }

        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
