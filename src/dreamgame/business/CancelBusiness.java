/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import java.util.Hashtable;

import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.CancelRequest;
import dreamgame.protocol.messages.CancelResponse;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.protocol.messages.TurnResponse;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.bacay.data.Poker;
import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.baucua.data.BauCuaTable;

//import dreamgame.chan.data.ChanPlayer;
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
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.CoTuongTable;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.xito.data.XiToTable;
import com.mysql.jdbc.Driver;

import org.slf4j.Logger;
import phom.data.PhomPlayer;
import phom.data.PhomTable;

/**
 *
 * @author binh_lethanh
 */
public class CancelBusiness extends AbstractBusiness {
    
    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(CancelBusiness.class);
    
    @SuppressWarnings("deprecation")
    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
        
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "CancelBusiness - handleMessage");
	}
        
        MessageFactory msgFactory = aSession.getMessageFactory();
        CancelResponse resMatchCancel = (CancelResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            CancelRequest rqMatchCancel = (CancelRequest) aReqMsg;
            mLog.debug("[CANCEL]: Catch   ; isLogOut : " + rqMatchCancel.isLogout);
            
            long matchId = rqMatchCancel.mMatchId;
            long uid = rqMatchCancel.uid;
            mLog.debug("[CANCEL]: ID - " + uid + ", match id - " + matchId);
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            
            if (matchId >= 0) {
                Room room = bacayZone.findRoom(matchId);
                if (room != null) {
                    switch (aSession.getCurrentZone()) {
                        case ZoneID.BACAY: {
                            resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
                            resMatchCancel.zone_id = aSession.getCurrentZone();
                            
                            BacayTable currentTable = (BacayTable) room.getAttactmentData();
                            BacayPlayer currPlayer = currentTable.findPlayer(uid);
                            currPlayer.isOutGame = true;
                            
                            if (currentTable.getIsPlaying()) { // is playing
                                resMatchCancel.setGamePlaying(true);
                                long pre_id = currentTable.getCurrentPlayer().id;
                                mLog.debug("cur id : " + pre_id + " cancel id : " + uid);
                                currentTable.setLoserForOutConnection(currPlayer);

                                // Two condition to quit game: table is finished or room's owner is quit
                                if ((uid == currentTable.getRoomOwner().id) || (currentTable.checkFinish())) {
                                    currentTable.postProcess();
                                    EndMatchResponse endMatchRes =
                                            (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
                                    // set the result
                                    endMatchRes.setZoneID(ZoneID.BACAY);
                                    endMatchRes.setSuccess(ResponseCode.SUCCESS, matchId,
                                            currentTable.getPlayers(), currentTable.getRoomOwner().cash, currentTable.getRoomOwner());
                                    
                                    Hashtable<Long, Poker[]> pokers = new Hashtable<Long, Poker[]>();
                                    //roomOwner
                                    pokers.put(currentTable.getRoomOwner().id, currentTable.getRoomOwner().playingCards);

                                    // Players
                                    for (BacayPlayer player : currentTable.getPlayers()) {
                                        pokers.put(player.id, player.playingCards);
                                    }
                                    endMatchRes.setBacayPokers(pokers);
                                    endMatchRes.setNewRoomOwner(currentTable.getRoomOwner().id);
                                    
                                    
                                    resMatchCancel.setSuccess(1, uid);
//                                    if (uid == currentTable.getRoomOwner().id)                                                                              
//                                        resMatchCancel.setFailure(0, "Chủ room đã out khỏi match. Room bị hủy");                                                                                
                                    
                                    resMatchCancel.money = aSession.getUserEntity().money;
                                    aSession.write(resMatchCancel);
//                                    room.broadcastMessage(resMatchCancel, aSession, true);
                                    
                                    if (uid == currentTable.getRoomOwner().id) {
                                        room.broadcastMessage(endMatchRes, aSession, false);
                                        
                                        
                                        CancelResponse resMatchCancelFail = (CancelResponse) msgFactory.getResponseMessage(aReqMsg.getID());
                                        resMatchCancelFail.uid = aSession.getUID();
                                        resMatchCancelFail.zone_id = aSession.getCurrentZone();
                                        resMatchCancelFail.setFailure(0, "Chủ room đã out khỏi match. Room bị hủy");                                        
                                        room.broadcastMessage(resMatchCancelFail, aSession, false);
                                        room.allLeft();
                                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                        return 1;
                                    } else { // End player is out
					Thread.sleep(2000);
                                        room.broadcastMessage(endMatchRes, aSession, true);
                                        room.setPlaying(false);
                                        currentTable.setIsPlaying(false);                                        
                                    }
                                    
                                    System.out.println("Stop timer here!");
                                    //Reset timer
                                    currentTable.getTimer().reset();
                                    //currentTable.getTimer().stop();
                                    currentTable.getTimer().setRuning(false);
                                    currentTable.resetForNewMatch();
                                } else if (uid == pre_id) { // is current player -- send broadcast TURN response message
                                    resMatchCancel.setUserPlaying(true);
                                    TurnResponse resMatchTurn =
                                            (TurnResponse) msgFactory.getResponseMessage(MessagesID.MATCH_TURN);
                                    long moneyLeft = currentTable.moneyLeftOfPlayer(currentTable.getCurrentPlayer(),
                                            0, false, false);
                                    mLog.debug("moneyLeft = "+moneyLeft);
				    resMatchTurn.matchId = currentTable.matchID;
                                    resMatchTurn.setSTTTo(moneyLeft);
                                    resMatchTurn.setSuccess(ResponseCode.SUCCESS, 0,
                                            currentTable.getCurrentPlayer().id, 0, aSession.getCurrentZone());
                                    room.broadcastMessage(resMatchTurn, aSession, false);
                                } else { // another player
                                    resMatchCancel.setUserPlaying(false);
                                }
                                
                            } else { // is not playing
                                resMatchCancel.setGamePlaying(false);
                                if (uid == currentTable.getRoomOwner().id) { // delete room
                                    resMatchCancel.setFailure(0, "Chủ bàn đã thoát khỏi bàn chơi.");
                                    room.broadcastMessage(resMatchCancel, aSession, false);
                                    currentTable.getTimer().reset();
                                    currentTable.getTimer().stop();
                                    room.allLeft();
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                } else { // Set playing false
                                    currentTable.removePlayer(currPlayer);
                                    room.setPlaying(false);
                                    currentTable.setIsPlaying(false);
                                }
                            }
                            // Remove Player from room
//                            try {
//                                currentTable.removePlayer(currPlayer);
//                            } catch (Exception e) {
//                                currentTable.removePlayerToWaitingList(currentTable.findPlayer(uid));
//                            }
                            
                            break;
                        }
                        case ZoneID.GAME_CHAN: {
                            
//                            ChanTable table = (ChanTable) room.getAttactmentData();
//                            uid = aSession.getUID();
//                            
//                            System.out.println("Came here auto play: " + table.isPlaying);
//                            resMatchCancel.setUid(uid);
//                            if (table == null) {
//                                mLog.error("Table is null ! uid : " + uid);
//                            } else {
//                                try {
//                                    table.logCode("Player Out : " + uid + " ; " + aSession.getUserName());
//                                } catch (Exception e) {
//                                }
//                            }
//                            
//                            ChanPlayer player = table.findPlayer(uid);
//                            
//                            if (table.isPlaying) {
//                            } else {
//                                {
//                                    System.out.println("Came here Cancel : " + uid + "  ; " + table.currentPlayer.id);
//                                    
//                                    if (player != null) {
//                                        table.remove(player);
//                                    }
//                                    
//                                    if (table.playings.size() == 0) {
//                                        room.broadcastMessage(resMatchCancel, aSession, false);
//                                        table.destroy();
//                                        room.allLeft();
//                                        table = null;
//                                        break;
//                                    }
//                                    
//                                    if (uid == table.owner.id) {
//                                        ChanPlayer p1 = table.ownerQuit();
//                                        if (p1 != null) {
//                                            room.setOwnerName(p1.username);
//                                            resMatchCancel.newOwner = p1.id;
//                                            table.owner = p1;
//                                        }
//                                    }
//                                    if (uid == table.currentPlayer.id) {
//                                        table.setNewStarter(table.playings.get(0));
//                                    }
//                                    resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
//                                }
//                            }
                            break;
                        }
                        
                        case ZoneID.PHOM: {
                            
                            PhomTable table = (PhomTable) room.getAttactmentData();
                            uid = aSession.getUID();
                            
                            System.out.println("Came here auto play: " + table.isPlaying);
                            resMatchCancel.setUid(uid);
                            if (table == null) {
                                mLog.error("Table is null ! uid : " + uid);
                            } else {
                                try {
                                    table.logCode("Player Out : " + uid + " ; " + aSession.getUserName());
                                } catch (Exception e) {
                                }
                            }
                            
                            PhomPlayer player = table.findPlayer(uid);
                            
                            if (table.isPlaying) {
                                resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                
                                if (player != null && !player.isObserve) {
                                    player.isAutoPlay = true;
                                    if (uid == table.owner.id) {
                                        PhomPlayer p1 = table.ownerQuit();
                                        if (p1 != null) {
                                            room.setOwnerName(p1.username);
                                            resMatchCancel.newOwner = p1.id;
                                            table.owner = p1;
                                        }
                                    }
                                    
                                    if (table.playings.size() == 0 || table.numRealPlaying() == 0) {
                                        room.broadcastMessage(resMatchCancel, aSession, false);
                                        table.destroy();
                                        room.allLeft();
                                        table = null;
                                        break;
                                    } else if (table.getCurrentPlayer().id == player.id) {
                                        table.processAuto();
                                    }
                                }
                                
                                if (player != null && player.isObserve) {
                                    table.remove(player);
                                }
                                
                            } else {
                                /*
                                 * if (uid == table.roomOwnerId) {
                                 * System.out.append("owner id : "+uid);
                                 * resMatchCancel.setFailure(ResponseCode.FAILURE,
                                 * "Chá»§ phÃ²ng Ä‘Ã£ thoÃ¡t khá»i bÃ n
                                 * chÆ¡i.");
                                 * room.broadcastMessage(resMatchCancel,
                                 * aSession, false);
                                 *
                                 * room.allLeft(); return 1; }else
                                 */
                                {
                                    System.out.println("Came here Cancel : " + uid + "  ; " + table.currentPlayer.id);
                                    
                                    if (player != null) {
                                        table.remove(player);
                                        room.left(aSession);
                                    }
                                    mLog.debug(" so nguoi con lai = " + table.playings.size());
                                    if (table.playings.size() == 0) {
                                        room.broadcastMessage(resMatchCancel, aSession, false);
                                        table.destroy();
                                        room.allLeft();
                                        table = null;
                                        break;
                                    }
                                    
                                    
                                    if (uid == table.owner.id) {
                                        PhomPlayer p1 = table.ownerQuit();
                                        if (p1 != null) {
                                            room.setOwnerName(p1.username);
                                            resMatchCancel.newOwner = p1.id;
                                            table.owner = p1;
                                        }
                                    }
                                    if (uid == table.currentPlayer.id) {
                                        table.setNewStarter(table.playings.get(0));
                                    }
                                    resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                }
                            }
                            break;
                        }
                        case ZoneID.TIENLEN_MB:
                        case ZoneID.TIENLEN_DEMLA:
                        case ZoneID.TIENLEN: {
                            resMatchCancel.setZone(ZoneID.TIENLEN);
                            TienLenTable table = (TienLenTable) room.getAttactmentData();
                            uid = aSession.getUID();

//                            System.out.println("Came here auto play: " + table.isPlaying);
                            resMatchCancel.setUid(uid);
                            if (table == null) {
                                mLog.error("Table is null ! uid : " + uid);
                            } else {
                                TienLenPlayer player = table.findPlayer(uid);
                                if (player != null && !player.isObserve) {
                                    player.isOutGame = true;
                                }
                                if (table.isPlaying) {
                                    EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);

//                                resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                    if (player != null && !player.isObserve) {
                                        CancelResponse resCancel = (CancelResponse) msgFactory.getResponseMessage(MessagesID.MATCH_CANCEL);
                                        resCancel.setZone(ZoneID.TIENLEN);
                                        table.updateCashQuitPlayer(table.findPlayer(uid));
                                        resCancel.money = DatabaseDriver.getUserMoney(uid);
                                        resCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                        aSession.write(resCancel);
                                        aResPkg.addMessage(resCancel);
                                        if (uid == table.owner.id) {
                                            TienLenPlayer p1 = table.ownerQuit();
                                            if (p1 != null) {
                                                System.out.println("Có null không vậy???");
                                                
                                                room.setOwnerName(p1.username);
                                                resMatchCancel.newOwner = p1.id;
                                                endMatchRes.newOwner = p1.id;
                                                System.out.println("chủ room mới là: " + p1.id);
                                                table.owner = p1;
                                            }
                                        }
                                        System.out.println("Chạy đến đây không vậy???");
//                                    if (table.getPlayings().size() == 0 || table.numRealPlaying() == 0) {

                                        System.out.println("Thằng ở vị trí: " + table.getUserIndex(player.id) + " mới bằng " + player.isOutGame);
                                        if (table.getPlayings().size() == 0) {
                                            resMatchCancel.mCode = 1;
                                            room.broadcastMessage(resMatchCancel, aSession, false);
                                            table.destroy();
                                            room.allLeft();
                                            table = null;
                                            
                                            break;
                                        } else if (table.numRealPlaying2() == 1 && !table.choiDemLa) {
                                            System.out.println("Thoát ngang chạy vào !đếm lá!");
                                            //Nếu còn 2 người chơi mà 1 người thoát --> kết thúc ván
                                            table.isPlaying = false;
                                            table.isNewMatch = true;
                                            endMatchRes.setZoneID(ZoneID.TIENLEN);
                                            endMatchRes.uid = uid;
//                                            long idWin = 0;
                                            for (TienLenPlayer p : table.getPlayings()) {
                                                if (p.id != uid && !p.isOutGame && !p.isCong() && !p.isToi()) {
                                                    System.out.println("tìm thấy thằng còn lại nè: " + p.username + ": " + table.sttFirst);
                                                    p.sttToi = table.sttFirst + 1;
                                                    break;
                                                }
                                            }
//                                            table.winner = table.findPlayer(idWin);
                                            endMatchRes.setTLPlayer(table.copPlayerList());
                                            endMatchRes.setSuccessTienLen(ResponseCode.SUCCESS, table.GetEndGame2(), 0, table.matchID);
                                            endMatchRes.idWin = table.winner.id;
                                            room.broadcastMessage(endMatchRes, aSession, false);
                                            room.left(aSession);
                                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                            return 1;
                                        } else if (table.numRealPlaying() == 1) {
                                            //Nếu còn 2 người chơi mà 1 người thoát --> kết thúc ván
                                            table.isPlaying = false;
                                            table.isNewMatch = true;
                                            endMatchRes.setZoneID(ZoneID.TIENLEN);
                                            endMatchRes.uid = uid;
                                            long idWin = 0;
                                            for (TienLenPlayer p : table.getPlayings()) {
                                                if (p.id != uid && !p.isOutGame) {
                                                    idWin = p.id;
                                                    break;
                                                }
                                            }
                                            table.winner = table.findPlayer(idWin);
                                            endMatchRes.setTLPlayer(table.copPlayerList());
                                            endMatchRes.setSuccessTienLen(ResponseCode.SUCCESS, table.GetEndGame(idWin), idWin, table.matchID);
                                            
                                            room.broadcastMessage(endMatchRes, aSession, false);
                                            room.left(aSession);
                                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                            return 1;
                                            
                                        } /*
                                         * Bỏ lượt khi thoát game - Nếu đang là
                                         * lượt của nó: + Nếu được đánh lượt mới
                                         * : chuyển lượt cho đứa bên cạnh + Nếu
                                         * chặt: chuyển lượt bình thường - Nếu
                                         * không phải lượt nó + Mà người đánh
                                         * vừa xong là nó (tức là đánh xong rồi
                                         * quit)à chuyển lastTurnID cho đứa bên
                                         * cạnh +Nếu không :set isgiveup=true;
                                         */ else if (table.getCurrentTurnID() == player.id) {
//                                        player.isOutGame = true;
                                            if (table.isNewRound) {
                                                table.nextUser(table.getUserIndex(uid));
                                                System.out.println("table.getUserIndex(uid)" + table.getUserIndex(uid));
                                                table.isNewRound = true;
                                                resMatchCancel.setNextPlayer(table.getCurrentTurnID(), true);
                                            } else {
                                                table.nextUser(table.getUserIndex(uid));
                                                resMatchCancel.setNextPlayer(table.getCurrentTurnID(), table.isNewRound);
                                            }
                                        } else {

////                                        player.isOutGame = true;
                                            if (table.lastTurnID == uid) {
                                                System.out.println("đánh xong rùi thoát nè!");
//                                                int preIndex = -1;
                                                table.lastTurnID = table.getPlayings().get(table.findNext(table.getUserIndex(uid))).id;
//                                                System.out.println("lastTurnID " + table.lastTurnID);
                                            }
                                        }
                                        //chơi nhất-bét gửi thông tin cóng/ về thứ mấy
                                        if (!table.choiDemLa) {
                                            resMatchCancel.stt = player.sttToi;
                                        }
                                        resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
//                                        room.broadcastMessage(resMatchCancel, aSession, false);

                                    }
                                    if (player != null && player.isObserve) {
                                        System.out.println("Player bị remove tại đây!!híc híc");
                                        table.remove(player);
                                    }
                                    
                                } else {
                                    /*
                                     * if (uid == table.roomOwnerId) {
                                     * System.out.append("owner id : "+uid);
                                     * resMatchCancel.setFailure(ResponseCode.FAILURE,
                                     * "Chá»§ phÃ²ng Ä‘Ã£ thoÃ¡t khá»i bÃ n
                                     * chÆ¡i.");
                                     * room.broadcastMessage(resMatchCancel,
                                     * aSession, false);
                                     *
                                     * room.allLeft(); return 1; }else
                                     */

//                                    System.out.println("Came here Cancel : " + uid + "  ; " + table.currentPlayer.id);

                                    if (player != null) {
                                        table.remove(player);
                                    }
                                    System.out.println("so nguoi con lai : " + table.numRealPlaying());
                                    
                                    if (table.numRealPlaying() == 0) {
                                        resMatchCancel.mCode = 1;
                                        room.broadcastMessage(resMatchCancel, aSession, false);
                                        table.destroy();
                                        room.allLeft();
                                        table = null;
                                        break;
                                    } //Trường hợp khi người chơi thoát hết, chỉ còn chủ room thì lần sau sẽ bắt đầu ván mới
                                    else if (table.getPlayings().size() == 1 && player != null && !player.isObserve) {
                                        table.isNewMatch = true;
                                    }
                                    
                                    if (uid == table.owner.id) {
                                        TienLenPlayer p1 = table.ownerQuit();
                                        if (p1 != null) {
                                            room.setOwnerName(p1.username);
                                            resMatchCancel.newOwner = p1.id;
                                            table.owner = p1;
                                        }
                                        resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
//                                        room.broadcastMessage(resMatchCancel, aSession, false);

                                    }
//                                    if (uid == table.currentPlayer.id) {
//                                        table.setNewStarter(table.playings.get(0));
//                                    }

                                }
                            }
                            break;
                        }
                        
                        case ZoneID.BAUCUA: {
                            resMatchCancel.setZone(ZoneID.BAUCUA);
                            BauCuaTable table = (BauCuaTable) room.getAttactmentData();
                            uid = aSession.getUID();

//                            System.out.println("Came here auto play: " + table.isPlaying);
                            resMatchCancel.setUid(uid);
                            if (table == null) {
                                mLog.error("Table is null ! uid : " + uid);
                            } else {
                                BauCuaPlayer player = table.findPlayer(uid);
                                if (player != null && !player.isObserve) {
                                    player.isOutGame = true;
                                }
                                if (table.isPlaying) {
//                                    EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);

//                                resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                    if (player != null && !player.isObserve) {
                                        CancelResponse resCancel = (CancelResponse) msgFactory.getResponseMessage(MessagesID.MATCH_CANCEL);
                                        resCancel.setZone(ZoneID.BAUCUA);
//                                        table.updateCashQuitPlayer(table.findPlayer(uid));
                                        resCancel.money = DatabaseDriver.getUserMoney(uid);
                                        resCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                        aSession.write(resCancel);
                                        aResPkg.addMessage(resCancel);
                                        if (uid == table.owner.id) {
                                            BauCuaPlayer p1 = table.ownerQuit();
                                            if (p1 != null) {
                                                System.out.println("Có null không vậy???");
                                                
                                                room.setOwnerName(p1.username);
                                                resMatchCancel.newOwner = p1.id;
//                                                endMatchRes.newOwner = p1.id;
                                                System.out.println("chủ room mới là: " + p1.id);
                                                table.owner = p1;
                                            } else {
                                                table.needDestroy = true;
                                                resMatchCancel.setMessage("Chủ bàn vừa thoát, bàn chơi sẽ bị hủy cuối ván do không có người đủ tiền làm chủ!");
                                            }
                                        }
//                                        System.out.println("Chạy đến đây không vậy???");
//                                    if (table.getPlayings().size() == 0 || table.numRealPlaying() == 0) {

//                                        System.out.println("Thằng ở vị trí: " + table.getUserIndex(player.id) + " mới bằng " + player.isOutGame);
                                        if (table.getPlayings().size() == 0) {
                                            resMatchCancel.mCode = 1;
                                            room.broadcastMessage(resMatchCancel, aSession, false);
                                            table.destroy();
                                            room.allLeft();
                                            table = null;
                                            
                                            break;
                                        }
//                                        else if (table.numRealPlaying() == 1) {
////                                            //Nếu còn 2 người chơi mà 1 người thoát --> kết thúc ván
////                                            table.isPlaying = false;
//////                                            table.isNewMatch = true;
////                                            endMatchRes.setZoneID(ZoneID.TIENLEN);
////                                            endMatchRes.uid = uid;
////                                            long idWin = 0;
////                                            for (PokerPlayer p : table.getPlayings()) {
////                                                if (p.id != uid && !p.isOutGame) {
////                                                    idWin = p.id;
////                                                    break;
////                                                }
////                                            }
////                                            table.winner = table.findPlayer(idWin);
//////                                            endMatchRes.setTLPlayer(table.copPlayerList());
//////                                            endMatchRes.setSuccessTienLen(ResponseCode.SUCCESS, table.GetEndGame(idWin), idWin, table.matchID);
//
////                                            room.broadcastMessage(endMatchRes, aSession, false);
//                                            table.sendEndMatch(uid);
//                                            room.left(aSession);
//                                            return 1;
//
//                                        } /*
//                                         * Bỏ lượt khi thoát game - Nếu đang là
//                                         * lượt của nó: + Nếu được đánh lượt mới
//                                         * : chuyển lượt cho đứa bên cạnh + Nếu
//                                         * chặt: chuyển lượt bình thường - Nếu
//                                         * không phải lượt nó + Mà người đánh
//                                         * vừa xong là nó (tức là đánh xong rồi
//                                         * quit)à chuyển lastTurnID cho đứa bên
//                                         * cạnh +Nếu không :set isgiveup=true;
//                                         * //
//                                         */ else if (table.getCurrentTurnID() == player.id) {
//                                            table.onBet(uid, 0, true);
//////                                        player.isOutGame = true;
////                                            if (table.isNewRound) {
////                                                table.nextUser(table.getUserIndex(uid));
////                                                System.out.println("table.getUserIndex(uid)" + table.getUserIndex(uid));
////                                                table.isNewRound = true;
////                                                resMatchCancel.setNextPlayer(table.getCurrentTurnID(), true);
////                                            } else {
////                                                table.nextUser(table.getUserIndex(uid));
////                                                resMatchCancel.setNextPlayer(table.getCurrentTurnID(), table.isNewRound);
////                                            }
//                                        } else {
//                                            player.isOutGame = true;
////                                            if (table.lastTurnID == uid) {
////                                                System.out.println("đánh xong rùi thoát nè!");
//////                                                int preIndex = -1;
////                                                table.lastTurnID = table.getPlayings().get(table.findNext(table.getUserIndex(uid))).id;
//////                                                System.out.println("lastTurnID " + table.lastTurnID);
////                                            }
//                                        }

                                        resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
//                                        room.broadcastMessage(resMatchCancel, aSession, false);

                                    }
//                                    if (player != null && player.isObserve) {
//                                        System.out.println("Player bị remove tại đây!!híc híc");
//                                        table.remove(player);
//                                    }

                                } else {
                                    /*
                                     * if (uid == table.roomOwnerId) {
                                     * System.out.append("owner id : "+uid);
                                     * resMatchCancel.setFailure(ResponseCode.FAILURE,
                                     * "Chá»§ phÃ²ng Ä‘Ã£ thoÃ¡t khá»i bÃ n
                                     * chÆ¡i.");
                                     * room.broadcastMessage(resMatchCancel,
                                     * aSession, false);
                                     *
                                     * room.allLeft(); return 1; }else
                                     */

//                                    System.out.println("Came here Cancel : " + uid + "  ; " + table.currentPlayer.id);
//                                    System.out.println("vao case 2!");
                                    if (player != null) {
//                                        System.out.println("vao case 2!");
                                        table.remove(player);
                                    }
                                    System.out.println("so nguoi con lai : " + table.numRealPlaying());
                                    
                                    if (table.numRealPlaying() == 0) {
                                        resMatchCancel.mCode = 1;
                                        room.broadcastMessage(resMatchCancel, aSession, false);
                                        table.destroy();
                                        room.allLeft();
                                        table = null;
                                        break;
                                    } //Trường hợp khi người chơi thoát hết, chỉ còn chủ room thì lần sau sẽ bắt đầu ván mới
//                                    else if (table.getPlayings().size() == 1 && player != null && !player.isObserve) {
////                                        table.isNewMatch = true;
//                                    }

                                    if (uid == table.owner.id) {
                                        BauCuaPlayer p1 = table.ownerQuit();
                                        if (p1 != null) {
                                            room.setOwnerName(p1.username);
                                            resMatchCancel.newOwner = p1.id;
                                            table.owner = p1;
                                        } else {
                                            resMatchCancel.mCode = 1;
                                            resMatchCancel.setMessage("Bàn bị hủy do không có người đủ tiền làm chủ!");
                                            room.broadcastMessage(resMatchCancel, aSession, false);
                                            table.destroy();
                                            room.allLeft();
                                            table = null;
                                            break;
                                        }
                                        resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
//                                        room.broadcastMessage(resMatchCancel, aSession, false);

                                    }
//                                    if (uid == table.currentPlayer.id) {
//                                        table.setNewStarter(table.playings.get(0));
//                                    }

                                }
                            }
                            break;
                        }
                        case ZoneID.POKER: {
                            resMatchCancel.setZone(ZoneID.POKER);
                            PokerTable table = (PokerTable) room.getAttactmentData();
                            uid = aSession.getUID();

//                            System.out.println("Came here auto play: " + table.isPlaying);
                            resMatchCancel.setUid(uid);
                            if (table == null) {
                                mLog.error("Table is null ! uid : " + uid);
                            } else {
                                PokerPlayer player = table.findPlayer(uid);
                                if (player != null && !player.isObserve) {
                                    player.isOutGame = true;
                                }
                                if (table.isPlaying) {
                                    EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);

//                                resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                    if (player != null && !player.isObserve) {
                                        CancelResponse resCancel = (CancelResponse) msgFactory.getResponseMessage(MessagesID.MATCH_CANCEL);
                                        resCancel.setZone(ZoneID.POKER);
//                                        table.updateCashQuitPlayer(table.findPlayer(uid));
                                        resCancel.money = DatabaseDriver.getUserMoney(uid);
                                        resCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                        aSession.write(resCancel);
                                        aResPkg.addMessage(resCancel);
                                        if (uid == table.owner.id) {
                                            PokerPlayer p1 = table.ownerQuit();
                                            if (p1 != null) {
                                                System.out.println("Có null không vậy???");
                                                
                                                room.setOwnerName(p1.username);
                                                resMatchCancel.newOwner = p1.id;
                                                endMatchRes.newOwner = p1.id;
                                                System.out.println("chủ room mới là: " + p1.id);
                                                table.owner = p1;
                                            }
                                        }
                                        System.out.println("Chạy đến đây không vậy???");
//                                    if (table.getPlayings().size() == 0 || table.numRealPlaying() == 0) {

                                        System.out.println("Thằng ở vị trí: " + table.getUserIndex(player.id) + " mới bằng " + player.isOutGame);
                                        if (table.getPlayings().isEmpty()) {
                                            resMatchCancel.mCode = 1;
                                            room.broadcastMessage(resMatchCancel, aSession, false);
                                            table.destroy();
                                            room.allLeft();
                                            table = null;
                                            
                                            break;
                                        } else if (table.numRealPlaying() == 1) {
//                                            //Nếu còn 2 người chơi mà 1 người thoát --> kết thúc ván
//                                            table.isPlaying = false;
////                                            table.isNewMatch = true;
//                                            endMatchRes.setZoneID(ZoneID.TIENLEN);
//                                            endMatchRes.uid = uid;
//                                            long idWin = 0;
//                                            for (PokerPlayer p : table.getPlayings()) {
//                                                if (p.id != uid && !p.isOutGame) {
//                                                    idWin = p.id;
//                                                    break;
//                                                }
//                                            }
//                                            table.winner = table.findPlayer(idWin);
////                                            endMatchRes.setTLPlayer(table.copPlayerList());
////                                            endMatchRes.setSuccessTienLen(ResponseCode.SUCCESS, table.GetEndGame(idWin), idWin, table.matchID);

//                                            room.broadcastMessage(endMatchRes, aSession, false);
                                            resMatchCancel.mCode = 1;
                                            room.broadcastMessage(resMatchCancel, aSession, false);
                                            table.sendEndMatch(uid);
                                            room.left(aSession);
                                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                            return 1;
                                            
                                        } /*
                                         * Bỏ lượt khi thoát game - Nếu đang là
                                         * lượt của nó: + Nếu được đánh lượt mới
                                         * : chuyển lượt cho đứa bên cạnh + Nếu
                                         * chặt: chuyển lượt bình thường - Nếu
                                         * không phải lượt nó + Mà người đánh
                                         * vừa xong là nó (tức là đánh xong rồi
                                         * quit)à chuyển lastTurnID cho đứa bên
                                         * cạnh +Nếu không :set isgiveup=true;
                                         * //
                                         */ else if (table.getCurrentTurnID() == uid) {
                                            System.out.println("đến lượt đánh thì thoát!");
                                            table.autoFold(uid);
//                                            table.onBet(uid, 0, true);
////                                        player.isOutGame = true;
//                                            if (table.isNewRound) {
//                                                table.nextUser(table.getUserIndex(uid));
//                                                System.out.println("table.getUserIndex(uid)" + table.getUserIndex(uid));
//                                                table.isNewRound = true;
//                                                resMatchCancel.setNextPlayer(table.getCurrentTurnID(), true);
//                                            } else {
//                                                table.nextUser(table.getUserIndex(uid));
//                                                resMatchCancel.setNextPlayer(table.getCurrentTurnID(), table.isNewRound);
//                                            }
                                        } else {
                                            player.isOutGame = true;
//                                            if (table.lastTurnID == uid) {
//                                                System.out.println("đánh xong rùi thoát nè!");
////                                                int preIndex = -1;
//                                                table.lastTurnID = table.getPlayings().get(table.findNext(table.getUserIndex(uid))).id;
////                                                System.out.println("lastTurnID " + table.lastTurnID);
//                                            }
                                        }
                                        
                                        resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
//                                        room.broadcastMessage(resMatchCancel, aSession, false);

                                    }
                                    if (player != null && player.isObserve) {
                                        System.out.println("Player bị remove tại đây!!híc híc");
                                        table.remove(player);
                                    }
                                    
                                } else {
                                    /*
                                     * if (uid == table.roomOwnerId) {
                                     * System.out.append("owner id : "+uid);
                                     * resMatchCancel.setFailure(ResponseCode.FAILURE,
                                     * "Chá»§ phÃ²ng Ä‘Ã£ thoÃ¡t khá»i bÃ n
                                     * chÆ¡i.");
                                     * room.broadcastMessage(resMatchCancel,
                                     * aSession, false);
                                     *
                                     * room.allLeft(); return 1; }else
                                     */

//                                    System.out.println("Came here Cancel : " + uid + "  ; " + table.currentPlayer.id);

                                    if (player != null) {
                                        table.remove(player);
                                    }
                                    System.out.println("so nguoi con lai : " + table.numRealPlaying());
                                    
                                    if (table.numRealPlaying() == 0) {
                                        resMatchCancel.mCode = 1;
                                        room.broadcastMessage(resMatchCancel, aSession, false);
                                        table.destroy();
                                        room.allLeft();
                                        table = null;
                                        break;
                                    } //Trường hợp khi người chơi thoát hết, chỉ còn chủ room thì lần sau sẽ bắt đầu ván mới
                                    else if (table.getPlayings().size() == 1 && player != null && !player.isObserve) {
//                                        table.isNewMatch = true;
                                    }
                                    
                                    if (uid == table.owner.id) {
                                        PokerPlayer p1 = table.ownerQuit();
                                        if (p1 != null) {
                                            room.setOwnerName(p1.username);
                                            resMatchCancel.newOwner = p1.id;
                                            table.owner = p1;
                                        }
                                        resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
//                                        room.broadcastMessage(resMatchCancel, aSession, false);

                                    }
//                                    if (uid == table.currentPlayer.id) {
//                                        table.setNewStarter(table.playings.get(0));
//                                    }

                                }
                            }
                            break;
                        }
                        case ZoneID.XITO: {
                            resMatchCancel.setZone(ZoneID.XITO);
                            XiToTable table = (XiToTable) room.getAttactmentData();
                            uid = aSession.getUID();

//                            System.out.println("Came here auto play: " + table.isPlaying);
                            resMatchCancel.setUid(uid);
                            if (table == null) {
                                mLog.error("Table is null ! uid : " + uid);
                            } else {
                                PokerPlayer player = table.findPlayer(uid);
                                if (player != null && !player.isObserve) {
                                    player.isOutGame = true;
                                }
                                if (table.isPlaying) {
                                    EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);

//                                resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                    if (player != null && !player.isObserve) {
                                        CancelResponse resCancel = (CancelResponse) msgFactory.getResponseMessage(MessagesID.MATCH_CANCEL);
                                        resCancel.setZone(ZoneID.XITO);
//                                        table.updateCashQuitPlayer(table.findPlayer(uid));
                                        resCancel.money = DatabaseDriver.getUserMoney(uid);
                                        resCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                        aSession.write(resCancel);
                                        aResPkg.addMessage(resCancel);
                                        if (uid == table.owner.id) {
                                            PokerPlayer p1 = table.ownerQuit();
                                            if (p1 != null) {
                                                System.out.println("Có null không vậy???");
                                                
                                                room.setOwnerName(p1.username);
                                                resMatchCancel.newOwner = p1.id;
                                                endMatchRes.newOwner = p1.id;
                                                System.out.println("chủ room mới là: " + p1.id);
                                                table.owner = p1;
                                            }
                                        }
                                        System.out.println("Chạy đến đây không vậy???");
//                                    if (table.getPlayings().size() == 0 || table.numRealPlaying() == 0) {

                                        System.out.println("Thằng ở vị trí: " + table.getUserIndex(player.id) + " mới bằng " + player.isOutGame);
                                        if (table.getPlayings().isEmpty()) {
                                            resMatchCancel.mCode = 1;
                                            room.broadcastMessage(resMatchCancel, aSession, false);
                                            table.destroy();
                                            room.allLeft();
                                            table = null;
                                            
                                            break;
                                        } else if (table.numRealPlaying() == 1) {
//                                            //Nếu còn 2 người chơi mà 1 người thoát --> kết thúc ván
//                                            table.isPlaying = false;
////                                            table.isNewMatch = true;
//                                            endMatchRes.setZoneID(ZoneID.TIENLEN);
//                                            endMatchRes.uid = uid;
//                                            long idWin = 0;
//                                            for (PokerPlayer p : table.getPlayings()) {
//                                                if (p.id != uid && !p.isOutGame) {
//                                                    idWin = p.id;
//                                                    break;
//                                                }
//                                            }
//                                            table.winner = table.findPlayer(idWin);
////                                            endMatchRes.setTLPlayer(table.copPlayerList());
////                                            endMatchRes.setSuccessTienLen(ResponseCode.SUCCESS, table.GetEndGame(idWin), idWin, table.matchID);

//                                            room.broadcastMessage(endMatchRes, aSession, false);
                                            resMatchCancel.mCode = 1;
                                            room.broadcastMessage(resMatchCancel, aSession, false);
                                            table.sendEndMatch(uid);
                                            room.left(aSession);
                                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                            return 1;
                                            
                                        } /*
                                         * Bỏ lượt khi thoát game - Nếu đang là
                                         * lượt của nó: + Nếu được đánh lượt mới
                                         * : chuyển lượt cho đứa bên cạnh + Nếu
                                         * chặt: chuyển lượt bình thường - Nếu
                                         * không phải lượt nó + Mà người đánh
                                         * vừa xong là nó (tức là đánh xong rồi
                                         * quit)à chuyển lastTurnID cho đứa bên
                                         * cạnh +Nếu không :set isgiveup=true;
                                         * //
                                         */ else if (table.getCurrentTurnID() == uid) {
                                            System.out.println("đến lượt đánh thì thoát!");
                                            table.autoFold(uid);
//                                            table.onBet(uid, 0, true);
////                                        player.isOutGame = true;
//                                            if (table.isNewRound) {
//                                                table.nextUser(table.getUserIndex(uid));
//                                                System.out.println("table.getUserIndex(uid)" + table.getUserIndex(uid));
//                                                table.isNewRound = true;
//                                                resMatchCancel.setNextPlayer(table.getCurrentTurnID(), true);
//                                            } else {
//                                                table.nextUser(table.getUserIndex(uid));
//                                                resMatchCancel.setNextPlayer(table.getCurrentTurnID(), table.isNewRound);
//                                            }
                                        } else {
                                            player.isOutGame = true;
//                                            if (table.lastTurnID == uid) {
//                                                System.out.println("đánh xong rùi thoát nè!");
////                                                int preIndex = -1;
//                                                table.lastTurnID = table.getPlayings().get(table.findNext(table.getUserIndex(uid))).id;
////                                                System.out.println("lastTurnID " + table.lastTurnID);
//                                            }
                                        }
                                        
                                        resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
//                                        room.broadcastMessage(resMatchCancel, aSession, false);

                                    }
                                    if (player != null && player.isObserve) {
                                        System.out.println("Player bị remove tại đây!!híc híc");
                                        table.remove(player);
                                    }
                                    
                                } else {
                                    /*
                                     * if (uid == table.roomOwnerId) {
                                     * System.out.append("owner id : "+uid);
                                     * resMatchCancel.setFailure(ResponseCode.FAILURE,
                                     * "Chá»§ phÃ²ng Ä‘Ã£ thoÃ¡t khá»i bÃ n
                                     * chÆ¡i.");
                                     * room.broadcastMessage(resMatchCancel,
                                     * aSession, false);
                                     *
                                     * room.allLeft(); return 1; }else
                                     */

//                                    System.out.println("Came here Cancel : " + uid + "  ; " + table.currentPlayer.id);

                                    if (player != null) {
                                        table.remove(player);
                                    }
                                    System.out.println("so nguoi con lai : " + table.numRealPlaying());
                                    
                                    if (table.numRealPlaying() == 0) {
                                        resMatchCancel.mCode = 1;
                                        room.broadcastMessage(resMatchCancel, aSession, false);
                                        table.destroy();
                                        room.allLeft();
                                        table = null;
                                        break;
                                    } //Trường hợp khi người chơi thoát hết, chỉ còn chủ room thì lần sau sẽ bắt đầu ván mới
                                    else if (table.getPlayings().size() == 1 && player != null && !player.isObserve) {
//                                        table.isNewMatch = true;
                                    }
                                    
                                    if (uid == table.owner.id) {
                                        PokerPlayer p1 = table.ownerQuit();
                                        if (p1 != null) {
                                            room.setOwnerName(p1.username);
                                            resMatchCancel.newOwner = p1.id;
                                            table.owner = p1;
                                        }
                                        resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
//                                        room.broadcastMessage(resMatchCancel, aSession, false);

                                    }
//                                    if (uid == table.currentPlayer.id) {
//                                        table.setNewStarter(table.playings.get(0));
//                                    }

                                }
                            }
                            break;
                        }
                        case ZoneID.CARO: {
                            //TODO: co caro o day
                            break;
                        }
                        //Tho
                        case ZoneID.COTUONG: {
                            resMatchCancel.setUid(uid);
                            resMatchCancel.setZone(ZoneID.COTUONG);
                            //TODO:
//                            CoTuongTable table = (CoTuongTable) room.getAttactmentData();
//                            uid = aSession.getUID();
                            CoTuongTable table = (CoTuongTable) room.getAttactmentData();
                            CoTuongPlayer leftPlayer = new CoTuongPlayer(uid);
                            if (table.isPlaying) {
//                                CoTuongTable table = (CoTuongTable) room.getAttactmentData();
                                try {
                                    if (table.owner.getId() == uid || table.player.getId() == uid) {
                                        if (!table.isEnd()) {
                                            if (table.owner.getId() == uid) {
                                                table.updateCash(false);
                                            } else {
                                                table.updateCash(true);
                                            }
                                        }
                                        System.out.println("match playing!");
                                        resMatchCancel.money = DatabaseDriver.getUserMoney(uid);
                                        resMatchCancel.setMoneyEndMatch(DatabaseDriver.getUserMoney(table.owner.id), DatabaseDriver.getUserMoney(table.player.id));
                                        resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                        room.broadcastMessage(resMatchCancel, aSession, false);
                                        aResPkg.addMessage(resMatchCancel);
                                        if (table.owner.getId() == uid) {
                                            room.allLeft();
                                        } else {
                                            room.left(aSession);
                                            aSession.leftRoom(matchId);
                                            table.removeAllPlayer();
                                            table.player_list.remove(leftPlayer);
                                            table.resetBoard();
                                            table.isFull = false;
                                            table.isFullPlayer = false;
                                            table.isPlaying = false;
                                        }
                                        table.destroy();
                                        if (rqMatchCancel.isOutOfGame) {
                                            aSession.setCurrentZone(ZoneID.GLOBAL);
					    aSession.setChannel(0);
                                        } else if (rqMatchCancel.isLogout) { // if logout - kill session
                                            if (aSession != null) {
                                                aSession.close();
                                            }
                                        }
                                        table.mIsEnd = true;
                                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                        return 1;
                                    }
                                    
                                } catch (NullPointerException ex) {
//                                ex.printStackTrace();
//                                 Room currentRoom = aSession.leftRoom(matchId);
                                    room.left(aSession);
                                    if (leftPlayer != null) {
                                        table.player_list.remove(leftPlayer);
                                    }
                                    if (rqMatchCancel.isOutOfGame) {
                                        aSession.setCurrentZone(ZoneID.GLOBAL);
					aSession.setChannel(0);
                                    } else if (rqMatchCancel.isLogout) { // if logout - kill session
                                        if (aSession != null) {
                                            aSession.close();
                                        }
                                    }
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 0;
                                }
                            } else {
                                if (table.owner.getId() == uid) {
                                    resMatchCancel.setSuccess(ResponseCode.SUCCESS, uid);
                                    room.broadcastMessage(resMatchCancel, aSession, false);
                                    room.allLeft();
                                    table.destroy();
                                }
                            }
                            break;
                            
                        }
                        case ZoneID.MAUBINH: {
                            break;
                        }
                        //TODO: Add more here
                        default:
                            break;
                    } // end switch 
		    
                    // Finally
                    Room currentRoom = aSession.leftRoom(matchId);
                    if (currentRoom == null) {
                        System.out.println("Current Room is Null!");
                    }
                    if (currentRoom != null) {
                        currentRoom.left(aSession);
                        System.out.println("chạy qua finally!");
//                        if (aSession.getCurrentZone() == ZoneID.TIENLEN) {
                        resMatchCancel.mCode = 1;
//                        }
                        Thread.sleep(500);
                        room.broadcastMessage(resMatchCancel, aSession, false);
                    }
                    
                    if (rqMatchCancel.isOutOfGame) {
                        aSession.setCurrentZone(ZoneID.GLOBAL);
			aSession.setChannel(0);
                    } 
		    else if (rqMatchCancel.isLogout) { // if logout - kill session
                        if (aSession != null) {
                            aSession.close();
                        }
                    }
		    
                } // end if room != null
            } // end if matchID >= 0
	    // if Player is not in any match - do nothing
	    else { 
                if (rqMatchCancel.isLogout) { // if logout - kill session
                    if (aSession != null) {
                        aSession.close();
                    }
                }
            }
        } catch (Throwable t) {
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
