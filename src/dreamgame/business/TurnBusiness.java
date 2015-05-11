/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.gameserver.framework.common.ServerException;
import java.util.Hashtable;

import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.protocol.messages.OutResponse;
import dreamgame.protocol.messages.TurnRequest;
import dreamgame.protocol.messages.TurnResponse;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.bacay.data.Poker;
import dreamgame.baucua.data.BauCuaTable;

//import dreamgame.caro.data.CaroTable;

//import dreamgame.chan.data.ChanPoker;
//import dreamgame.chan.data.ChanTable;
import dreamgame.config.DebugConfig;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.cotuong.data.CoTuongTable;
import dreamgame.cotuong.data.moveFromTo;
import dreamgame.databaseDriven.DatabaseDriver;
import java.util.logging.Level;
//import dreamgame.oantuti.data.OantutiTable;
import dreamgame.poker.data.PokerTable;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.xito.data.XiToTable;
import org.slf4j.Logger;
import phom.data.PhomPlayer;

import phom.data.PhomTable;
import phom.data.Utils;

/**
 *
 * @author binh_lethanh
 */
public class TurnBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(TurnBusiness.class);

    @SuppressWarnings("deprecation")
    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "TurnBusiness - handleMessage");
	}
        // process's status
        mLog.debug("[TURN] : Catch  ; " + aSession.getUserName());
        MessageFactory msgFactory = aSession.getMessageFactory();
        TurnResponse resMatchTurn = (TurnResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        boolean isFinish = false;
        try {
            TurnRequest rqMatchTurn = (TurnRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room room = bacayZone.findRoom(rqMatchTurn.mMatchId);
            resMatchTurn.matchId = rqMatchTurn.mMatchId;

            //Thread.sleep(10000);

            long currID = rqMatchTurn.uid;
            if (room != null) {
                mLog.debug("[TURN] Current room = " + room.getName());
                mLog.debug("[TURN] Current player = " + currID + " - name : "
                        + aSession.getUserName() + " ; zone = " + aSession.getCurrentZone());
                switch (aSession.getCurrentZone()) {
                    case ZoneID.BACAY: {
                        BacayTable currentTable = (BacayTable) room.getAttactmentData();
                        // set check type
                        BacayPlayer currPlayer = currentTable.getCurrentPlayer();
                        boolean isAllowPlay;
                        long moneyLeft = -1;
                        if (currentTable.getIsOwner()) {
                            isAllowPlay = currentTable.isAllowPlay(currentTable.getRoomOwner(), rqMatchTurn.money, true);
                        } else {

                            if (currentTable.getCurrentPlayer() == null) {
                                mLog.error("[" + aSession.getUserName() + "] OMG CurrentPlayer is null. roomName : " + currentTable.roomInfo());
                                isAllowPlay = false;
                            } else {
                                isAllowPlay = currentTable.isAllowPlay(currentTable.getCurrentPlayer(), rqMatchTurn.money, false);
                            }
                        }
                        if (isAllowPlay) {
                            long moneyLeftPre = currentTable.moneyLeftOfPlayer(
                                    currentTable.getCurrentPlayer(),
                                    rqMatchTurn.money, false, true);

                            System.out.println("MoneyLeftPre : " + moneyLeftPre);

                            if (currentTable.play(rqMatchTurn.money, currID)) { // if
                                moneyLeft = currentTable.moneyLeftOfPlayer(
                                        currentTable.getCurrentPlayer(),
                                        rqMatchTurn.money, false, true);
                                System.out.println("moneyLeft : " + moneyLeft);

                                if (moneyLeftPre < moneyLeft) {
                                    moneyLeft = moneyLeftPre;
                                }

                                resMatchTurn.setSTTTo(moneyLeft);

                                if (currentTable.getCurrentPlayer() != null) {
                                    currentTable.getCurrentPlayer().setState(false);
                                    currentTable.getTimer().setCurrentPlayer(
                                            currentTable.getCurrentPlayer());
                                    currentTable.getTimer().reset();
                                }
                                currentTable.getRoomOwner().setState(false);
                            } else { // Not change player
                                if (!currentTable.getIsOwner()) {
                                    moneyLeft = currentTable.moneyLeftOfPlayer(
                                            currentTable.getCurrentPlayer(),
                                            rqMatchTurn.money, false, false);
                                } else {
                                    moneyLeft = currentTable.moneyLeftOfPlayer(
                                            currentTable.getRoomOwner(),
                                            rqMatchTurn.money, true, false);
                                }

                                if (moneyLeftPre < moneyLeft) {
                                    moneyLeft = moneyLeftPre;
                                }

                                resMatchTurn.setSTTTo(moneyLeft);
                                currentTable.getCurrentPlayer().setState(false);
                                currentTable.getRoomOwner().setState(false);
                                if (currentTable.getIsOwner()) { // Player were
                                    // played
                                    currentTable.getTimer().setCurrentPlayer(
                                            currentTable.getRoomOwner());
                                    currentTable.getTimer().reset();
                                } else { // Owner were played
                                    currentTable.getTimer().setCurrentPlayer(
                                            currentTable.getCurrentPlayer());
                                    currentTable.getTimer().reset();
                                }
                            }

                            if (currentTable.checkFinish()) {
                                currentTable.postProcess();
                                isFinish = true;
                                EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
                                // set the result
                                endMatchRes.setZoneID(ZoneID.BACAY);
                                endMatchRes.setSuccess(ResponseCode.SUCCESS,
                                        rqMatchTurn.mMatchId, currentTable.getPlayers(), currentTable.getRoomOwner().cash, currentTable.getRoomOwner());

                                Hashtable<Long, Poker[]> pokers = new Hashtable<Long, Poker[]>();
                                // roomOwner
                                pokers.put(currentTable.getRoomOwner().id,
                                        currentTable.getRoomOwner().playingCards);
                                // Players
                                for (BacayPlayer player : currentTable.getPlayers()) {
                                    pokers.put(player.id, player.playingCards);
                                }
                                endMatchRes.setBacayPokers(pokers);
                                // currentTable.changeOwnerInNextMatch();
                                endMatchRes.setNewRoomOwner(currentTable.getRoomOwner().id);

                                room.broadcastMessage(endMatchRes, aSession,
                                        true);
                                room.setPlaying(false);
                                currentTable.setIsPlaying(false);
                                currentTable.getTimer().setRuning(false);
                                currentTable.resetForNewMatch();
                            } else {
                                // response turn
                                resMatchTurn.setPreID(currID);

                                resMatchTurn.setSuccess(ResponseCode.SUCCESS,
                                        rqMatchTurn.money, currentTable.nextTurn(),
                                        currPlayer.timeReq, aSession.getCurrentZone());


                                room.broadcastMessage(resMatchTurn,
                                        aSession, true);
                            }
                            // If Turn for timeout
                            if (rqMatchTurn.isTimeout == true) {
                                OutResponse resOut = (OutResponse) msgFactory.getResponseMessage(MessagesID.OUT);
                                resOut.setSuccess(
                                        ResponseCode.SUCCESS,
                                        currID,
                                        aSession.getUserName()
                                        + " bị timeout và không tiếp tục tố nữa.",
                                        aSession.getUserName(), 0);
                                // Send to clients
                                room.broadcastMessage(resOut, aSession,
                                        false);
                            }
                        } else {
                            System.out.println("rqMatchTurn.money : "+rqMatchTurn.money);
                            System.out.println("currPlayer.cash : "+currPlayer.cash);
                            
                            resMatchTurn.setFailure(ResponseCode.FAILURE,
                                    "Bạn tố số tiền vượt quá khả năng của bạn hoặc người chơi khác!");                            
                        }
                        break;
                    }
                    case ZoneID.OTT: {
//                        int type = rqMatchTurn.ottObject;
//                        OantutiTable table = (OantutiTable) room.getAttactmentData();
//                        table.play(currID, type);
                        break;
                    }

                    case ZoneID.GAME_CHAN: {

//                        ChanTable table = (ChanTable) room.getAttactmentData();
//
//                        mLog.debug("Process Turn : " + table.turnInfo() + " : card : " + rqMatchTurn.phomCard);
//
//                        long predID = table.getCurrentPlayer().id;
//
//                        if (!table.isPlaying) {
//                            mLog.error("Error Danh bai. Khi da ket thuc van! : " + table.turnInfo());
//                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
//                            return 1;
//                        }
//
//                        // play
//                        if (rqMatchTurn.isDuoi) {
//                            table.play(currID, table.lastDeck);
//                        } else {
//                            table.play(currID, ChanPoker.numToChanPoker(rqMatchTurn.phomCard));
//                        }
//
//                        resMatchTurn.setSuccess(ResponseCode.SUCCESS, rqMatchTurn.phomCard,
//                                table.getCurrentPlayer().id, ZoneID.PHOM);
//
//                        //if (!resMatchTurn.auto)
//                        resMatchTurn.setPreID(predID);
//                        resMatchTurn.zoneID = ZoneID.GAME_CHAN;
//                        resMatchTurn.isDuoi = rqMatchTurn.isDuoi;
//                        resMatchTurn.isChiu = rqMatchTurn.isChiu;
//
//                        //resMatchTurn.deck=table.restCards.size();
//                        // notify to other players
//                        room.broadcastMessage(resMatchTurn, aSession, true);
//                        resMatchTurn = null;
                        break;
                    }
                    case ZoneID.PHOM: {

                        PhomTable table = (PhomTable) room.getAttactmentData();

                        mLog.debug("Process Turn : " + table.turnInfo() + " : card : " + rqMatchTurn.phomCard);

                        long predID = table.getCurrentPlayer().id;

                        if (!table.isPlaying) {
                            mLog.error("Error Danh bai. Khi da ket thuc van! : " + table.turnInfo());
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }

                        // play
                        if (!table.checkPlayCard(Utils.numToPoker(rqMatchTurn.phomCard))) {
                            resMatchTurn.setFailure(0, "Bài này đánh sẽ phá phỏm!");
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }

                        table.play(currID, Utils.numToPoker(rqMatchTurn.phomCard));

                        if (table.checkEatable(table.currentPlayer, (byte) rqMatchTurn.phomCard)) {
                            resMatchTurn.canEat = true;
                        }

                        String phom1 = table.getCurrentPhom(table.getPrePlayer());
                        resMatchTurn.phom = phom1;
                        if (table.per_u_status) {
                            resMatchTurn.u = true;
                        }

                        resMatchTurn.setSuccess(ResponseCode.SUCCESS, rqMatchTurn.phomCard,
                                table.getCurrentPlayer().id, ZoneID.PHOM);

                        resMatchTurn.deck = table.restCards.size();
                        //if (!resMatchTurn.auto)
                        resMatchTurn.setPreID(predID);
                        // notify to other players
                        room.broadcastMessage(resMatchTurn, aSession, true);

                        resMatchTurn = null;

                        if (table.getWinner() != null && !table.isPlaying) { //Stop game
                            System.out.println("Came here!");
                            Thread.sleep(50);
                            EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
                            // set the result
                            endMatchRes.setZoneID(ZoneID.PHOM);
                            endMatchRes.matchId = rqMatchTurn.mMatchId;

                            if (table.forScores.size() > 0) {
                                endMatchRes.setSuccess(ResponseCode.SUCCESS,
                                        table.forScores, table.getWinner());
                            } else {
                                endMatchRes.setSuccess(ResponseCode.SUCCESS,
                                        table.getPlayings(), table.getWinner());
                            }

                            if (table.owner.notEnoughMoney()) {
                                PhomPlayer p1 = table.ownerQuit();
                                room.setOwnerName(p1.username);
                                table.owner = p1;
                                endMatchRes.newOwner = p1.id;
                            }

                            Thread.sleep(300);
                            room.broadcastMessage(endMatchRes, aSession,
                                    true);
                            room.setPlaying(false);
                            
                            if (table.winquit) {
                                table.getWinner().currentSession.writeMessage("Bạn đã chiến thắng vì mọi người đã thoát game!");
                            }
                            
                            for (PhomPlayer player : table.getPlayings()) {
                                if (player.notEnoughMoney()) {
                                    ISession playerSession = aSession.getManager().findSession(player.id);
                                    room.left(playerSession);
                                }
                            }
                            //Thread.yield();
                            table.resetPlayers();

                            /*
                             * try { for (int
                             * i=0;i<table.getPlayings().size();i++) for
                             * (PhomPlayer player : table.getPlayings()) { if
                             * (player.cash <= 5 * table.firstCashBet) {
                             * OutResponse rqsOut = (OutResponse)
                             * msgFactory.getResponseMessage(MessagesID.OUT);
                             *
                             * rqsOut.setSuccess(ResponseCode.SUCCESS,
                             * player.id, "Bạn không còn đủ tiền để chơi game
                             * này nữa.", player.username, 1); rqsOut.mCode=0;
                             *
                             * mLog.debug("Khong du tien roi: " +
                             * player.username + ":" + player.id); ISession
                             * playerSession =
                             * aSession.getManager().findSession(player.id);
                             * playerSession.write(rqsOut); if (playerSession !=
                             * null) {
                             * playerSession.leftRoom(endMatchRes.mMatchId);
                             * room.left(playerSession); }
                             *
                             * rqsOut.setSuccess(ResponseCode.SUCCESS,
                             * player.id,"Người chơi "+ player.username + "
                             * không còn đủ tiền để chơi game này nữa.",
                             * player.username, 1);
                             *
                             * table.remove(player); if
                             * (table.owner.id==player.id) { PhomPlayer
                             * p1=table.ownerQuit();
                             * room.setOwnerName(p1.username);
                             * rqsOut.setNewRoomOwner(p1.id); table.owner=p1; }
                             *
                             * Thread.sleep(300); room.broadcastMessage(rqsOut,
                             * aSession, true); break; }
                             *
                             * }
                             *
                             * } catch (Exception eas) { eas.printStackTrace();
                             * }
                             */

                        }
                        break;
                    }

//                    case ZoneID.PHOM: {
//
//                        PhomTable table = (PhomTable) room.getAttactmentData();
//
//                        mLog.debug("Process Turn : " + table.turnInfo() + " : card : " + rqMatchTurn.phomCard);
//
//                        long predID = table.getCurrentPlayer().id;
//
//                        if (!table.isPlaying) {
//                            mLog.error("Error Danh bai. Khi da ket thuc van! : " + table.turnInfo());
//                            return 1;
//                        }
//                        // play
//                        table.play(currID, Utils.numToPoker(rqMatchTurn.phomCard));
//
//                        resMatchTurn.setSuccess(ResponseCode.SUCCESS, rqMatchTurn.phomCard,
//                                table.getCurrentPlayer().id, ZoneID.PHOM);
//
//                        //if (!resMatchTurn.auto)
//                        resMatchTurn.setPreID(predID);
//                        // notify to other players
//                        room.broadcastMessage(resMatchTurn, aSession, true);
//
//                        resMatchTurn = null;
//
//                        if (table.getWinner() != null && !table.isPlaying) { //Stop game
//                            System.out.println("Came here!");
//                            Thread.sleep(50);
//                            EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
//                            // set the result
//                            endMatchRes.setZoneID(ZoneID.PHOM);
//                            endMatchRes.matchId = rqMatchTurn.mMatchId;
//                            if (table.forScores.size() > 0) {
//                                endMatchRes.setSuccess(ResponseCode.SUCCESS,
//                                        table.forScores, table.getWinner());
//                            } else {
//                                endMatchRes.setSuccess(ResponseCode.SUCCESS,
//                                        table.getPlayings(), table.getWinner());
//                            }
//
//                            if (table.owner.notEnoughMoney()) {
//                                PhomPlayer p1 = table.ownerQuit();
//                                room.setOwnerName(p1.username);
//                                table.owner = p1;
//                                endMatchRes.newOwner = p1.id;
//                            }
//
//                            Thread.sleep(300);
//                            room.broadcastMessage(endMatchRes, aSession,
//                                    true);
//                            room.setPlaying(false);
//
//                            for (PhomPlayer player : table.getPlayings()) {
//                                if (player.notEnoughMoney()) {
//                                    ISession playerSession = aSession.getManager().findSession(player.id);
//                                    room.left(playerSession);
//                                }
//                            }
//                            //Thread.yield();
//                            table.resetPlayers();
//
//                            /*
//                             * try { for (int
//                             * i=0;i<table.getPlayings().size();i++) for
//                             * (PhomPlayer player : table.getPlayings()) { if
//                             * (player.cash <= 5 * table.firstCashBet) {
//                             * OutResponse rqsOut = (OutResponse)
//                             * msgFactory.getResponseMessage(MessagesID.OUT);
//                             *
//                             * rqsOut.setSuccess(ResponseCode.SUCCESS,
//                             * player.id, "Bạn không còn đủ tiền để chơi game
//                             * này nữa.", player.username, 1); rqsOut.mCode=0;
//                             *
//                             * mLog.debug("Khong du tien roi: " +
//                             * player.username + ":" + player.id); ISession
//                             * playerSession =
//                             * aSession.getManager().findSession(player.id);
//                             * playerSession.write(rqsOut); if (playerSession !=
//                             * null) {
//                             * playerSession.leftRoom(endMatchRes.mMatchId);
//                             * room.left(playerSession); }
//                             *
//                             * rqsOut.setSuccess(ResponseCode.SUCCESS,
//                             * player.id,"Người chơi "+ player.username + "
//                             * không còn đủ tiền để chơi game này nữa.",
//                             * player.username, 1);
//                             *
//                             * table.remove(player); if
//                             * (table.owner.id==player.id) { PhomPlayer
//                             * p1=table.ownerQuit();
//                             * room.setOwnerName(p1.username);
//                             * rqsOut.setNewRoomOwner(p1.id); table.owner=p1; }
//                             *
//                             * Thread.sleep(300); room.broadcastMessage(rqsOut,
//                             * aSession, true); break; }
//                             *
//                             * }
//                             *
//                             * } catch (Exception eas) { eas.printStackTrace();
//                             * }
//                             */
//
//                        }
//                        break;
//                    }
                    case ZoneID.CARO: {
                        // get attached table
			//deleted
                        break;
                    }
                    case ZoneID.COTUONG: {
                        // get attached table
                        CoTuongTable currentTable = (CoTuongTable) room.getAttactmentData();
                        currentTable.timer.setRuning(false);
                        System.out.println("currentTable: " + currentTable);
                        // set check type
                        long nID = 0;
                        moveFromTo amove = new moveFromTo();
                        amove.fromCol = 8 - rqMatchTurn.mv.fromCol;
                        amove.toCol = 8 - rqMatchTurn.mv.toCol;
                        amove.fromRow = 9 - rqMatchTurn.mv.fromRow;
                        amove.toRow = 9 - rqMatchTurn.mv.toRow;

                        if (currentTable.owner.id == rqMatchTurn.uid) {
                            nID = currentTable.player.id;
                            currentTable.move(rqMatchTurn.mv);
                            currentTable.setCurrPlayer(true);
                        } else {
                            nID = currentTable.owner.id;
                            currentTable.setCurrPlayer(false);
                            currentTable.move(amove);

                        }
                        currentTable.startTime();
//                        //Tho
//                        if (true) {
                        // response turn
                        resMatchTurn.setSuccess(ResponseCode.SUCCESS, amove, nID, ZoneID.COTUONG, currentTable.currPlayer.remainTime);
                        room.broadcastMessage(resMatchTurn, aSession, false);

                        // check if match is end
                        long idWin = currentTable.checkGameStatus();
                        EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);

                        if (idWin >= 0) {
                            //Thomc
                            if (idWin > 0) {
                                if (idWin == currentTable.owner.id) {
                                    currentTable.updateCash(true);
                                    currentTable.setCurrPlayer(true);
                                } else {
                                    currentTable.updateCash(false);
                                    currentTable.setCurrPlayer(false);
                                }
                                endMatchRes.isPeace = false;
                            } else {
                                currentTable.updatePeaceCash();
                                endMatchRes.isPeace = true;
                                currentTable.setCurrPlayer(true);
                            }
                            // set the result
                            endMatchRes.setZoneID(ZoneID.COTUONG);
                            endMatchRes.setMoneyEndMatch(DatabaseDriver.getUserMoney(currentTable.owner.id), DatabaseDriver.getUserMoney(currentTable.player.id));
                            endMatchRes.setSuccess(ResponseCode.SUCCESS, idWin, rqMatchTurn.mMatchId);
                            room.broadcastMessage(endMatchRes, aSession, true);
                            room.setPlaying(false);
                            currentTable.mIsEnd = true;
                            currentTable.destroy();
                        }
//                            return 1;
//                        }
                        isFinish = true;
                        break;
                    }
                    case ZoneID.TIENLEN_MB:
                    case ZoneID.TIENLEN_DEMLA:
                    case ZoneID.TIENLEN: {
                        TienLenTable table = (TienLenTable) room.getAttactmentData();
//                        long predID = table.predID;
                        // play
//                        Thomc
//                        switch (table.play(currID, rqMatchTurn.tienlenCards, rqMatchTurn.isGiveup, rqMatchTurn.isTimeoutTL)) {
                        int playReturn = table.play(currID, rqMatchTurn.tienlenCards, rqMatchTurn.isGiveup, rqMatchTurn.isTimeoutTL);
                        System.out.println("PLAY: " + playReturn);
                        switch (playReturn) {
                            case TienLenTable.SUCCESS:
                                resMatchTurn.setSuccessTienLen(ResponseCode.SUCCESS, rqMatchTurn.tienlenCards,
                                        table.getCurrentTurnID(), table.isNewRound, ZoneID.TIENLEN);
                                resMatchTurn.setcurrID(currID);
                                resMatchTurn.setIsGiveup(rqMatchTurn.isGiveup);
                                if (!table.choiDemLa && table.toiList.size() > 0) {
                                    resMatchTurn.setToiPlayer(table.toiList);
                                }
                                //Thông tin chặt chém!
                                if (table.fightOccur) {
                                    resMatchTurn.setFightInfo(table.fightInfo);
                                }

                                // notify to other players
                                room.broadcastMessage(resMatchTurn, aSession, true);
                                isFinish = true;
                                break;

                            case TienLenTable.END_MATCH:
                                table.isPlaying = false;
                                EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
                                // set the result
                                endMatchRes.setZoneID(ZoneID.TIENLEN);
                                endMatchRes.setTLPlayer(table.copPlayerList());
                                endMatchRes.matchId = rqMatchTurn.mMatchId;
                                if (table.choiDemLa) {
                                    System.out.println("choi dem la");
                                    endMatchRes.setSuccessTienLen(ResponseCode.SUCCESS, table.GetEndGame(currID), currID, rqMatchTurn.mMatchId);
                                } else {
                                    endMatchRes.setSuccessTienLen(ResponseCode.SUCCESS, table.GetEndGame2(), currID, rqMatchTurn.mMatchId);
                                    endMatchRes.idWin = table.winner.id;
                                    endMatchRes.uidTurn = currID;
                                }
                                endMatchRes.setLastCards(rqMatchTurn.tienlenCards);
//                                Thông tin chặt chém!
                                if (table.fightOccur) {
                                    endMatchRes.setFightInfo(table.fightInfo);
                                }
                                if (table.owner.notEnoughMoney()) {
                                    TienLenPlayer p1 = table.ownerQuit();
                                    room.setOwnerName(p1.username);
                                    table.owner = p1;
                                    endMatchRes.newOwner = p1.id;
                                }
                                room.broadcastMessage(endMatchRes, aSession, true);

                                isFinish = true;
                                break;
                            case TienLenTable.INVALID_TURN:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Không phải lượt đi của bạn!");
                                break;
                            case TienLenTable.INVALID_PLAY:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Đi không hợp lệ!");
                                break;
                            case TienLenTable.INVALID_FIGTH:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Chặt không hợp lệ!");
                                break;
                            case TienLenTable.INVALID_GIVEUP:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Bạn không được bỏ lượt!");
                                break;
                            case TienLenTable.CARDS_NOT_FOUND:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Không tìm thấy quân bài bạn đánh!");
                                break;
                            case 999:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Bạn bị thối 2");
                                break;
                            case 111:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Quân bài đi đầu phải là quân bài nhỏ nhất!");
                                break;
                            default:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Bị lỗi!");
                                break;

                        }

                        break;
                    }
                    case ZoneID.BAUCUA: {
                        BauCuaTable table = (BauCuaTable) room.getAttactmentData();
//                        long predID = table.predID;
                        // play
//                        Thomc
                        switch (table.onBet(currID, rqMatchTurn.piece, rqMatchTurn.num)) {
                            //thành công
                            case BauCuaTable.BET_SUCCESS:
                                resMatchTurn.setSuccessBauCua(
                                        ResponseCode.SUCCESS,
                                        currID,
                                        rqMatchTurn.piece,
                                        rqMatchTurn.num,
                                        table.getNumPiece(rqMatchTurn.piece),
                                        table.findPlayer(currID).cash);

//                                resMatchTurn.setcurrID(currID);
                                room.broadcastMessage(resMatchTurn, aSession, true);
                                isFinish = true;
                                break;

                            case BauCuaTable.NOT_ENOUGH_MONEY:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Bạn không đủ tiền để đặt!");
                                break;
                            case BauCuaTable.TIME_OUT:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Đã hết thời gian đặt cược!");
                                break;
                            case BauCuaTable.OWNER_BET:
                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Chủ bàn không được đặt cược!");
                                break;
                            default:
                                resMatchTurn = null;
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return -1;
//                                resMatchTurn.setFailure(ResponseCode.FAILURE, "Bị lỗi!");

                        }

                        break;
                    }
                    case ZoneID.POKER: {
                        PokerTable table = (PokerTable) room.getAttactmentData();
                        if (!table.isPlaying) {
                            isFinish = true;
                            break;
                        }
                        int betType = table.onBet(rqMatchTurn.uid, rqMatchTurn.money, rqMatchTurn.isFold);
                        resMatchTurn.betTypeDes = table.betTypeDes;
                        switch (betType) {
                            case PokerTable.TURN_NEXT:
                                System.out.println("PokerTable.TURN_NEXT!");
                                resMatchTurn.setSuccessPoker(ResponseCode.SUCCESS, ZoneID.POKER);
                                resMatchTurn.currID = rqMatchTurn.uid;
                                resMatchTurn.money = rqMatchTurn.money;
                                resMatchTurn.isGiveup = rqMatchTurn.isFold;
                                if (table.getCurrPlayer().isAllIn()) {
                                    table.startNewRound();
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                } else {
                                    resMatchTurn.nextID = table.getCurrentTurnID();
                                    resMatchTurn.setMinMaxBet(table.minBet, table.maxBet);

                                }
                                resMatchTurn.potMoney = table.getPotMoney();
                                room.broadcastMessage(resMatchTurn, aSession, true);

                                isFinish = true;
                                break;
                            case PokerTable.TURN_NEW_ROUND:
                                System.out.println("PokerTable.TURN_NEW_ROUND!");
                                resMatchTurn.setSuccessPoker(ResponseCode.SUCCESS, ZoneID.POKER);
//                                resMatchTurn.setMinMaxBet(table.minBet, table.maxBet);
                                resMatchTurn.money = rqMatchTurn.money;
                                resMatchTurn.currID = rqMatchTurn.uid;
                                resMatchTurn.nextID = 0;
                                resMatchTurn.isGiveup = rqMatchTurn.isFold;
                                resMatchTurn.potMoney = table.getPotMoney();
                                room.broadcastMessage(resMatchTurn, aSession, true);
                                table.startNewRound();
                                isFinish = true;
                                break;
                            case PokerTable.TURN_END_MATCH:
                                System.out.println("PokerTable.TURN_END_MATCH!");
                                table.isPlaying = false;
                                table.endMatchProcess();
                                EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
                                // set the result
				endMatchRes.remainPoker = dreamgame.tienlen.data.Utils.bytesToString(table.getRemainPoker());
                                endMatchRes.money = table.getPotMoney();
                                endMatchRes.setPokerPlayer(table.copPlayerList());
                                endMatchRes.setZoneID(ZoneID.POKER);
                                endMatchRes.setSuccessPoker(ResponseCode.SUCCESS, rqMatchTurn.mMatchId);
                                
                                if (table.owner.notEnoughMoney()) {
                                    PokerPlayer p1 = table.ownerQuit();
                                    room.setOwnerName(p1.username);
                                    table.owner = p1;
                                    endMatchRes.newOwner = p1.id;
                                }                                
                                table.resetPlayer();
                                
                                room.broadcastMessage(endMatchRes, aSession, true);
                                isFinish = true;
                                break;

                            case PokerTable.TURN_INVALID:
                                System.out.println("khong phai luot cua ban!");
                                break;
                            default:
                                System.out.println("loi!");
                                break;
                        }

                        break;
                    }

                    case ZoneID.XITO: {

                        XiToTable table = (XiToTable) room.getAttactmentData();
                        if (!table.isPlaying) {
                            isFinish = true;
                            break;
                        }
                        System.out.println("rqMatchTurn.isShow:" + rqMatchTurn.isShow + "----isplaying: " + table.isPlaying);
                        if (rqMatchTurn.isShow) {
                            if (!table.isPlaying) {
                                isFinish = true;
                                break;
                            }
//                            System.out.println("dddaaaaayyyyy!!!!");
                            if (table.showCard(currID, rqMatchTurn.visibleCard)) {
//                                System.out.println("heeeeee");
                                resMatchTurn.setSuccessPoker(ResponseCode.SUCCESS, ZoneID.XITO);
                                resMatchTurn.currID = rqMatchTurn.uid;
                                resMatchTurn.isShow = true;
                                resMatchTurn.visibleCard = rqMatchTurn.visibleCard;
                                room.broadcastMessage(resMatchTurn, aSession, true);
                                isFinish = true;
//                                table.findPlayer(currID).isShow = true;
                                if (table.isAllShow()) {
                                    table.startNewRound();
                                }
                                break;
                            } else {
                                isFinish = true;
                                break;
                            }
                        } else {

                            int betType = table.onBet(rqMatchTurn.uid, rqMatchTurn.money, rqMatchTurn.isFold);
                            resMatchTurn.betTypeDes = table.betTypeDes;
                            switch (betType) {
                                case PokerTable.TURN_NEXT:
                                    System.out.println("next!");
                                    resMatchTurn.setSuccessPoker(ResponseCode.SUCCESS, ZoneID.XITO);
                                    resMatchTurn.currID = rqMatchTurn.uid;
                                    resMatchTurn.money = rqMatchTurn.money;
                                    resMatchTurn.isGiveup = rqMatchTurn.isFold;

                                    if (table.getCurrPlayer().isAllIn()) {
                                        table.startNewRound();
                                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                        return 1;
                                    } else {
                                        resMatchTurn.nextID = table.getCurrentTurnID();
                                        resMatchTurn.setMinMaxBet(table.minBet, table.maxBet);

                                    }
                                    resMatchTurn.potMoney = table.getPotMoney();
                                    room.broadcastMessage(resMatchTurn, aSession, true);

                                    isFinish = true;
                                    break;
                                case PokerTable.TURN_NEW_ROUND:
                                    System.out.println("chia vong moi!");
                                    resMatchTurn.setSuccessPoker(ResponseCode.SUCCESS, ZoneID.XITO);
//                                resMatchTurn.setMinMaxBet(table.minBet, table.maxBet);
                                    resMatchTurn.money = rqMatchTurn.money;
                                    resMatchTurn.currID = rqMatchTurn.uid;
                                    resMatchTurn.nextID = 0;
                                    resMatchTurn.isGiveup = rqMatchTurn.isFold;
                                    resMatchTurn.potMoney = table.getPotMoney();
                                    room.broadcastMessage(resMatchTurn, aSession, true);
                                    table.startNewRound();
                                    isFinish = true;
                                    break;
                                case PokerTable.TURN_END_MATCH:
                                    System.out.println("end!");
                                    table.isPlaying = false;
                                    table.endMatchProcess();
                                    EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
                                    // set the result
                                    endMatchRes.money = table.getPotMoney();
                                    endMatchRes.setPokerPlayer(table.copPlayerList());
                                    endMatchRes.setZoneID(ZoneID.POKER);
                                    endMatchRes.setSuccessPoker(ResponseCode.SUCCESS, rqMatchTurn.mMatchId);
                                    
                                    System.out.println(table.owner.notEnoughMoney() + "table.owner.notEnoughMoney()");
                                    if (table.owner.notEnoughMoney()) {
                                        PokerPlayer p1 = table.ownerQuit();
                                        room.setOwnerName(p1.username);
                                        table.owner = p1;
                                        System.out.println(p1.id + " " + p1.username + " p1");

                                        endMatchRes.newOwner = p1.id;
                                    }
                                    table.resetPlayer();
                                    
                                    room.broadcastMessage(endMatchRes, aSession, true);
                                    isFinish = true;
                                    break;

                                case PokerTable.TURN_INVALID:
                                    System.out.println("khong phai luot cua ban!");
                                    break;
                                case 999:
                                    resMatchTurn.setFailure(ResponseCode.FAILURE,
                                            "Số tiền bạn tố vượt quá số tiền cho phép!");
                                    aSession.write(resMatchTurn);
                                    break;
                                default:
                                    System.out.println("loi!");
                                    break;
                            }

                            break;
                        }
                    }
                    case ZoneID.MAUBINH: {
                        break;
                    }
                    //TODO: Add more here
                    default:
                        break;
                }

            } else {
                mLog.error("Room is null ; matchID : " + rqMatchTurn.mMatchId + " ; " + aSession.getUserName() + " ; zone = " + aSession.getCurrentZone());

                resMatchTurn.setFailure(ResponseCode.FAILURE,
                        "Bạn cần tham gia vào một trận trước khi chơi.");
                aSession.write(resMatchTurn);
            }
        } catch (Throwable t) {
            System.out.println(t.toString());
            resMatchTurn.setFailure(ResponseCode.FAILURE, "Bị lỗi : " + t.getMessage());
            mLog.error("Process message " + aReqMsg.getID() + " error: " + t.getMessage(), t);
            /*try {
                aSession.write(resMatchTurn);
            } catch (ServerException ex) {
                java.util.logging.Logger.getLogger(TurnBusiness.class.getName()).log(Level.SEVERE, null, ex);
            }*/

        } finally {
            if ((resMatchTurn != null) && (!isFinish)) {
                aResPkg.addMessage(resMatchTurn);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
