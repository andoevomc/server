/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.AcceptJoinResponse;
import dreamgame.protocol.messages.JoinResponse;
import dreamgame.protocol.messages.JoinedResponse;
import dreamgame.protocol.messages.ReplyRequest;
import dreamgame.protocol.messages.ReplyResponse;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.baucua.data.BauCuaTable;
import dreamgame.config.DebugConfig;
import dreamgame.config.GameRuleConfig;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.RoomStatus;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.CoTuongTable;
import dreamgame.poker.data.PokerPlayer;
import dreamgame.poker.data.PokerTable;
import dreamgame.tienlen.data.TienLenPlayer;
import dreamgame.tienlen.data.TienLenTable;
import dreamgame.xito.data.XiToTable;
import java.util.ArrayList;
import java.util.Vector;
import org.slf4j.Logger;
import phom.data.PhomPlayer;
import phom.data.PhomTable;

/**
 *
 * @author binh_lethanh
 */
public class ReplyBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(ReplyBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "ReplyBusiness - handleMessage");
	}
	
        MessageFactory msgFactory = aSession.getMessageFactory();
        ReplyResponse resMatchReply = (ReplyResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            mLog.debug("[REPLY]: Catch");
            long uid = aSession.getUID();
            ReplyRequest rqMatchReply = (ReplyRequest) aReqMsg;
            long buddy_uid = rqMatchReply.buddy_uid;
            ISession buddy_session = aSession.getManager().findSession(
                    buddy_uid);

            if (buddy_session != null) {

                Zone caroZone = aSession.findZone(buddy_session.getCurrentZone());
                // Khong can
                Room currentRoom = caroZone.findRoom(rqMatchReply.mMatchId);
                if (currentRoom != null) {
                    if (!currentRoom.isPlaying()) {
                        if (rqMatchReply.mIsAccept) {
                            aSession.setCurrentZone(caroZone.getZoneId());
			    // trungnm -> them vao de biet duoc nguoi choi dang o ban voi so tien bao nhieu
			    aSession.setCurrentMoneyMatch(currentRoom.minBet);
                            System.out.println("Zone:" + aSession.getCurrentZone() + "   ID:" + aSession.getID());
                            make(buddy_session, rqMatchReply.mMatchId, uid, buddy_uid);
                            resMatchReply.setSuccess(ResponseCode.SUCCESS,
                                    rqMatchReply.mIsAccept, aSession.getUID(),
                                    aSession.getUserName());
                            buddy_session.write(resMatchReply);
                        } else {
                            resMatchReply.setFailure(ResponseCode.FAILURE, aSession.getUserName()
                                    + " không đồng ý");
                            buddy_session.write(resMatchReply);
                        }
                    } else {
                        resMatchReply.setFailure(ResponseCode.FAILURE, "Bàn đang chơi mất rồi. Bạn chờ nhé!");
                        aSession.write(resMatchReply);
                    }
                } else {
                    if (rqMatchReply.mIsAccept) {
                        resMatchReply.setFailure(ResponseCode.FAILURE,
                                "Bạn kia đã đặt lệnh hủy trận này rồi. Bạn chờ trận khác nhé!");
                        aSession.write(resMatchReply);
                    }
                }
            } else {
                if (rqMatchReply.mIsAccept) {
                    resMatchReply.setFailure(ResponseCode.FAILURE,
                            "Không tìm lại được người mời");
                    aSession.write(resMatchReply);
                }
            }

        } catch (Throwable t) {
            resMatchReply.setFailure(ResponseCode.FAILURE, "Bị lỗi "
                    + t.toString());
            aResPkg.addMessage(resMatchReply);
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }

    public void make(ISession aSession, long matchID, long playerID, long ownerID) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "ReplyBusiness - make: aSession.getUserName() = " + aSession.getUserName() + "; matchID = " + matchID + "; playerID = " + playerID + "; ownerID = " + ownerID);
	}
	
        String username = null;
        MessageFactory msgFactory = aSession.getMessageFactory();
        // Feedback to Player
        JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);
        resMatchJoin.isInvite = true;
        // Feedback to Room's Owner
        AcceptJoinResponse resAcceptJoin = (AcceptJoinResponse) msgFactory.getResponseMessage(MessagesID.ACCEPT_JOINT);
        try {
            mLog.debug("[ACCEPT INVITE]: Catch");
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room currentRoom = bacayZone.findRoom(matchID);
            UserEntity newUser = DatabaseDriver.getUserInfo(playerID);
            if (newUser != null) {
                long moneyOfPlayer = newUser.money;
                long uid = newUser.mUid;
                username = newUser.mUsername;
                if (currentRoom != null) {
                    switch (aSession.getCurrentZone()) {
                        case ZoneID.BACAY: {
                            ISession playerSession = currentRoom.getWaitingSessionByID(uid);
                            JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                            BacayTable currentTable = (BacayTable) currentRoom.getAttactmentData();

                            if (currentTable.isPlaying) {
                                resMatchJoin.setFailure(
                                        ResponseCode.FAILURE,
                                        "Bàn chơi đã bắt đầu bạn không thể tham gia!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            } else if ((currentTable.getPlayers().size() + 1) >= currentTable.getMaximumPlayer()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Chủ Room không còn đủ trình hoặc không muốn chiều thêm người nữa!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            // Check money of player
                            if (moneyOfPlayer < currentTable.getJoinMoney()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để tham gia room này!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            int joinedCode = currentRoom.join(playerSession);
                            currentRoom.removeWaitingSessionByID(playerSession);
                            // in cases
                            if (joinedCode == RoomStatus.JOIN_PLAYER) {
                                BacayPlayer newPlayer = new BacayPlayer(uid);
                                newPlayer.setAvatarID(newUser.avatarID);
                                newPlayer.setLevel(newUser.level);
                                newPlayer.setCash(newUser.money);
                                newPlayer.setUsername(newUser.mUsername);
                                newPlayer.moneyForBet = currentTable.getMinBet();
                                newPlayer.setCurrentMatchID(matchID);
                                currentTable.addPlayer(newPlayer);

                                // broadcast's values
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS, uid,
                                        newUser.mUsername, newUser.level,
                                        newUser.avatarID, newUser.money, ZoneID.BACAY);

                                // join's values
                                resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                        currentRoom.getName(), currentTable.getMinBet(), aSession.getCurrentZone());
                                resMatchJoin.setRoomID(matchID);
                                resMatchJoin.setCurrentPlayersBacay(currentTable.getPlayers(), currentTable.getRoomOwner());
				resMatchJoin.setCapacity(currentTable.getMaximumPlayer());
                                // send broadcast msg to friends
                                currentRoom.broadcastMessage(broadcastMsg,
                                        aSession, true);
                            } 
			    else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                                // broadcast's values
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS, uid,
                                        newUser.mUsername, newUser.level,
                                        newUser.avatarID, newUser.money, ZoneID.BACAY);
                                // send broadcast msg to friends
                                currentRoom.broadcastMessage(broadcastMsg,
                                        aSession, true);
                                // reply response message
                                resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                        currentRoom.getName(), currentTable.getMinBet(), aSession.getCurrentZone());
                                resMatchJoin.setRoomID(matchID);
                                resMatchJoin.setCurrentPlayersBacay(currentTable.getPlayers(), currentTable.getRoomOwner());
				resMatchJoin.setCapacity(currentTable.getMaximumPlayer());
                            } 
			    else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                                BacayPlayer newPlayer = new BacayPlayer(uid);
                                newPlayer.setAvatarID(newUser.avatarID);
                                newPlayer.setLevel(newUser.level);
                                newPlayer.setCash(newUser.money);
                                newPlayer.setUsername(newUser.mUsername);
                                currentTable.addPlayerToWaitingList(newPlayer);
                                resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                        currentRoom.getName(), currentTable.getMinBet(), aSession.getCurrentZone());
                                resMatchJoin.setRoomID(matchID);
                                resMatchJoin.setCurrentPlayersBacay(currentTable.getPlayers(), currentTable.getRoomOwner());
				resMatchJoin.setCapacity(currentTable.getMaximumPlayer());
                            } 
			    else if (joinedCode == RoomStatus.JOIN_FULL) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bàn này đã đầy rồi, chọn bàn khác đi bạn!");
                            }
                            resAcceptJoin.setSuccess(ResponseCode.SUCCESS);
                            // Feedback to Player
                            if (username != null) {
                                playerSession.write(resMatchJoin);
                            }
                            break;
                        }
                        // TODO Add more here
                        case ZoneID.PHOM: {
                            ISession playerSession = aSession.getManager().findSession(uid);
                            JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                            PhomTable currentTable = (PhomTable) currentRoom.getAttactmentData();

                            if ((currentTable.getPlayings().size() + currentTable.getWaitings().size()) >= currentTable.getMaximumPlayer()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bàn đã đầy!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            // Check money of player
                            if (moneyOfPlayer < currentTable.getJoinMoney()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để tham gia room này!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            int joinedCode = currentRoom.join(playerSession);
                            currentRoom.removeWaitingSessionByID(playerSession);
                            // in cases
                            if (joinedCode == RoomStatus.JOIN_PLAYER) {
                                boolean isResume = false;
                                String cards = "";
                                if (currentTable.containPlayer(uid)) {
                                    if (currentTable.isPlaying) {
                                        isResume = true;
                                    }

                                    PhomPlayer newPlayer = currentTable.findPlayer(uid);
                                    cards = newPlayer.allPokersToString();
                                    newPlayer.isAutoPlay = false;
                                } else {
                                    PhomPlayer newPlayer = new PhomPlayer(uid);
                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = currentTable.firstCashBet;
                                    newPlayer.currentMatchID = matchID;
                                    newPlayer.currentSession = aSession;

                                    currentTable.join(newPlayer);

                                }

                                // broadcast's values
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        uid, newUser.mUsername,
                                        newUser.level,
                                        newUser.avatarID,
                                        newUser.money,
                                        ZoneID.PHOM);

                                //broadcastMsg.setPhomInfo(table.anCayMatTien, table.taiGuiUDen);

                                // join's values
                                if (currentTable.isPlaying) {
                                    resMatchJoin.isObserve = true;
                                }

                                resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                        currentRoom.getName(),
                                        currentTable.firstCashBet, aSession.getCurrentZone());
                                resMatchJoin.setRoomID(matchID);
                                resMatchJoin.setCurrentPlayersPhom(
                                        currentTable.getPlayings(), currentTable.getWaitings(),
                                        currentTable.owner);

                                resMatchJoin.setPhomInfo(currentTable.anCayMatTien, currentTable.taiGuiUDen, currentTable.isPlaying,
                                        isResume, currentTable.currentPlayer.id, cards, currentTable.restCards.size());
                                resMatchJoin.setCapacity(currentTable.getMaximumPlayer());
                                // send broadcast msg to friends
                                resMatchJoin.zoneID = playerSession.getCurrentZone();
                                playerSession.write(resMatchJoin);

                                currentRoom.broadcastMessage(broadcastMsg, aSession, true);

                            } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                            } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                            } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bàn này đã đầy rồi, chọn bàn khác đi bạn!");
                            }
                            // Feedback to Player
                            //aSession.write(resMatchJoin);
                            //   playerSession.write(resMatchJoin);
                            break;
                        }
                        case ZoneID.TIENLEN_MB:
                        case ZoneID.TIENLEN_DEMLA:
                        case ZoneID.TIENLEN: {
                            ISession playerSession = aSession.getManager().findSession(uid);
                            JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                            TienLenTable currentTable = (TienLenTable) currentRoom.getAttactmentData();

                            if ((currentTable.getPlayings().size() + currentTable.getWaitings().size()) >= currentTable.getMaximumPlayer()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Phòng đã đầy!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            // Check money of player
                            if (moneyOfPlayer < currentTable.getJoinMoney()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để tham gia room này!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            if (currentTable.containPlayer(uid)) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bạn đang trong bàn chơi rồi, vui lòng chờ hết ván nhé!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            int joinedCode = currentRoom.join(playerSession);
                            currentRoom.removeWaitingSessionByID(playerSession);
                            // in cases
                            if (joinedCode == RoomStatus.JOIN_PLAYER) {
//                                boolean isResume = false;
//                                String cards = "";
                                if (currentTable.containPlayer(uid)) {
                                    break;
//                                    if (currentTable.isPlaying) {
//                                        isResume = true;
//                                    }
//
//                                    PhomPlayer newPlayer = currentTable.findPlayer(uid);
//                                    cards = newPlayer.allPokersToString();
//                                    newPlayer.isAutoPlay = false;
                                } else {
                                    TienLenPlayer newPlayer = new TienLenPlayer(uid);
                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = currentTable.firstCashBet;
                                    newPlayer.currentMatchID = matchID;
                                    newPlayer.currentSession = aSession;
                                    currentTable.join(newPlayer);

                                }

                                // broadcast's values
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        uid, newUser.mUsername,
                                        newUser.level,
                                        newUser.avatarID,
                                        newUser.money,
                                        ZoneID.TIENLEN);

                                //broadcastMsg.setPhomInfo(table.anCayMatTien, table.taiGuiUDen);

                                // join's values
                                if (currentTable.isPlaying) {
                                    resMatchJoin.isObserve = true;
                                }

                                resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                        currentRoom.getName(),
                                        currentTable.firstCashBet, aSession.getCurrentZone());
                                resMatchJoin.setRoomID(matchID);
                                resMatchJoin.setCurrentTienLenPlayers(currentTable.getPlayings(), currentTable.getWaitings(),
                                        currentTable.getOwner());
//                                resMatchJoin.setCurrentTienLenPlayers(
//                                        currentTable.getPlayings(), currentTable.getWaitings(),
//                                        currentTable.owner);

//                                resMatchJoin.setPhomInfo(currentTable.anCayMatTien, currentTable.taiGuiUDen, currentTable.isPlaying,
//                                        isResume, currentTable.currentPlayer.id, cards, currentTable.restCards.size());
                                resMatchJoin.setCapacity(currentTable.getMaximumPlayer());
                                // send broadcast msg to friends
                                resMatchJoin.zoneID = playerSession.getCurrentZone();
                                playerSession.write(resMatchJoin);

                                currentRoom.broadcastMessage(broadcastMsg, aSession, true);

                            } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                            } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                            } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bàn này đã đầy rồi, chọn bàn khác đi bạn!");
                            }
                            // Feedback to Player
                            //aSession.write(resMatchJoin);
                            //   playerSession.write(resMatchJoin);
                            break;
                        }
                        case ZoneID.MAUBINH: {
                            break;
                        }
                        case ZoneID.POKER: {
                            ISession playerSession = aSession.getManager().findSession(uid);
                            JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                            PokerTable currentTable = (PokerTable) currentRoom.getAttactmentData();

                            if ((currentTable.getPlayings().size() + currentTable.getWaitings().size()) >= currentTable.getMaximumPlayer()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bàn chơi đã đủ người!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            // Check money of player
                            if (moneyOfPlayer < currentTable.getJoinMoney()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để vào bàn này!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            if (currentTable.containPlayer(uid)) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bạn vẫn còn trong bàn chơi, vui lòng chờ hết ván!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            int joinedCode = currentRoom.join(playerSession);
                            currentRoom.removeWaitingSessionByID(playerSession);
                            // in cases
                            if (joinedCode == RoomStatus.JOIN_PLAYER) {
//                                boolean isResume = false;
//                                String cards = "";
                                if (currentTable.containPlayer(uid)) {
                                    break;
//                                    if (currentTable.isPlaying) {
//                                        isResume = true;
//                                    }
//
//                                    PhomPlayer newPlayer = currentTable.findPlayer(uid);
//                                    cards = newPlayer.allPokersToString();
//                                    newPlayer.isAutoPlay = false;
                                } else {
                                    PokerPlayer newPlayer = new PokerPlayer(uid);
                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = currentTable.firstCashBet;
                                    newPlayer.currentMatchID = matchID;
                                    newPlayer.currentSession = aSession;
                                    currentTable.join(newPlayer);

                                }

                                // broadcast's values
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        uid, newUser.mUsername,
                                        newUser.level,
                                        newUser.avatarID,
                                        newUser.money,
                                        ZoneID.POKER);

                                //broadcastMsg.setPhomInfo(table.anCayMatTien, table.taiGuiUDen);

                                // join's values
                                if (currentTable.isPlaying) {
                                    resMatchJoin.isObserve = true;
                                }

                                resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                        currentRoom.getName(),
                                        currentTable.firstCashBet, aSession.getCurrentZone());
                                resMatchJoin.setRoomID(matchID);
                                resMatchJoin.setCurrentPlayersPoker(currentTable.getPlayings(), currentTable.getWaitings(),
                                        currentTable.getOwner());
//                                resMatchJoin.setCurrentPlayersTienLen(
//                                        currentTable.getPlayings(), currentTable.getWaitings(),
//                                        currentTable.owner);

//                                resMatchJoin.setPhomInfo(currentTable.anCayMatTien, currentTable.taiGuiUDen, currentTable.isPlaying,
//                                        isResume, currentTable.currentPlayer.id, cards, currentTable.restCards.size());
                                resMatchJoin.setCapacity(currentTable.getMaximumPlayer());
                                // send broadcast msg to friends
                                resMatchJoin.zoneID = playerSession.getCurrentZone();
                                playerSession.write(resMatchJoin);

                                currentRoom.broadcastMessage(broadcastMsg, aSession, true);

                            } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                            } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                            } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bàn này đã đủ người chơi rồi, bạn vui lòng chọn bàn khác!");
                            }
                            // Feedback to Player
                            //aSession.write(resMatchJoin);
                            //   playerSession.write(resMatchJoin);
                            break;
                        }
                        case ZoneID.XITO: {
                            ISession playerSession = aSession.getManager().findSession(uid);
                            JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                            XiToTable currentTable = (XiToTable) currentRoom.getAttactmentData();

                            if ((currentTable.getPlayings().size() + currentTable.getWaitings().size()) >= currentTable.getMaximumPlayer()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bàn này đã đủ người chơi rồi, bạn vui lòng chọn bàn khác!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            // Check money of player
                            if (moneyOfPlayer < currentTable.getJoinMoney()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để vào bàn này!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            if (currentTable.containPlayer(uid)) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bạn vẫn còn trong bàn chơi, vui lòng chờ hết ván!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            int joinedCode = currentRoom.join(playerSession);
                            currentRoom.removeWaitingSessionByID(playerSession);
                            // in cases
                            if (joinedCode == RoomStatus.JOIN_PLAYER) {
//                                boolean isResume = false;
//                                String cards = "";
                                if (currentTable.containPlayer(uid)) {
                                    break;
//                                    if (currentTable.isPlaying) {
//                                        isResume = true;
//                                    }
//
//                                    PhomPlayer newPlayer = currentTable.findPlayer(uid);
//                                    cards = newPlayer.allPokersToString();
//                                    newPlayer.isAutoPlay = false;
                                } else {
                                    PokerPlayer newPlayer = new PokerPlayer(uid);
                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = currentTable.firstCashBet;
                                    newPlayer.currentMatchID = matchID;
                                    newPlayer.currentSession = aSession;
                                    currentTable.join(newPlayer);

                                }

                                // broadcast's values
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        uid, newUser.mUsername,
                                        newUser.level,
                                        newUser.avatarID,
                                        newUser.money,
                                        ZoneID.POKER);

                                //broadcastMsg.setPhomInfo(table.anCayMatTien, table.taiGuiUDen);

                                // join's values
                                if (currentTable.isPlaying) {
                                    resMatchJoin.isObserve = true;
                                }

                                resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                        currentRoom.getName(),
                                        currentTable.firstCashBet, aSession.getCurrentZone());
                                resMatchJoin.setRoomID(matchID);
                                resMatchJoin.setCurrentPlayersPoker(currentTable.getPlayings(), currentTable.getWaitings(),
                                        currentTable.getOwner());
//                                resMatchJoin.setCurrentPlayersTienLen(
//                                        currentTable.getPlayings(), currentTable.getWaitings(),
//                                        currentTable.owner);

//                                resMatchJoin.setPhomInfo(currentTable.anCayMatTien, currentTable.taiGuiUDen, currentTable.isPlaying,
//                                        isResume, currentTable.currentPlayer.id, cards, currentTable.restCards.size());
                                resMatchJoin.setCapacity(currentTable.getMaximumPlayer());
                                // send broadcast msg to friends
                                resMatchJoin.zoneID = playerSession.getCurrentZone();
                                playerSession.write(resMatchJoin);

                                currentRoom.broadcastMessage(broadcastMsg, aSession, true);

                            } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                            } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                            } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bàn này đã đủ người chơi rồi, bạn vui lòng chọn bàn khác!");
                            }
                            // Feedback to Player
                            //aSession.write(resMatchJoin);
                            //   playerSession.write(resMatchJoin);
                            break;
                        }
                        case ZoneID.BAUCUA: {
                            ISession playerSession = aSession.getManager().findSession(uid);
                            JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                            BauCuaTable currentTable = (BauCuaTable) currentRoom.getAttactmentData();

                            if ((currentTable.getPlayings().size() + currentTable.getWaitings().size()) >= currentTable.getMaximumPlayer()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Phòng đã đầy!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            // Check money of player
                            if (moneyOfPlayer < currentTable.getJoinMoney()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để tham gia room này!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            } else if (!currentTable.canAddPlayer()) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Chủ bàn không đủ tiền để thêm người nữa!");
                                aSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            if (currentTable.containPlayer(uid)) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bạn đang trong bàn chơi rồi, vui lòng chờ hết ván nhé!");
                                playerSession.write(resMatchJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            int joinedCode = currentRoom.join(playerSession);
                            currentRoom.removeWaitingSessionByID(playerSession);
                            // in cases
                            if (joinedCode == RoomStatus.JOIN_PLAYER) {
//                                boolean isResume = false;
//                                String cards = "";
                                if (currentTable.containPlayer(uid)) {
                                    break;
//                                    if (currentTable.isPlaying) {
//                                        isResume = true;
//                                    }
//
//                                    PhomPlayer newPlayer = currentTable.findPlayer(uid);
//                                    cards = newPlayer.allPokersToString();
//                                    newPlayer.isAutoPlay = false;
                                } else {
                                    BauCuaPlayer newPlayer = new BauCuaPlayer(uid);
                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = currentTable.firstCashBet;
                                    newPlayer.currentMatchID = matchID;
                                    newPlayer.currentSession = aSession;
                                    currentTable.join(newPlayer);

                                }

                                // broadcast's values
                                broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                        uid, newUser.mUsername,
                                        newUser.level,
                                        newUser.avatarID,
                                        newUser.money,
                                        ZoneID.BAUCUA);

                                //broadcastMsg.setPhomInfo(table.anCayMatTien, table.taiGuiUDen);

                                // join's values
                                if (currentTable.isPlaying) {
                                    resMatchJoin.isObserve = true;
                                }

                                resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                        currentRoom.getName(),
                                        currentTable.firstCashBet, aSession.getCurrentZone());
                                resMatchJoin.setRoomID(matchID);
                                resMatchJoin.setCurrentBauCuaPlayers(currentTable.getPlayings(), currentTable.getWaitings(),
                                        currentTable.getOwner());

//                                resMatchJoin.setCurrentTienLenPlayers(
//                                        currentTable.getPlayings(), currentTable.getWaitings(),
//                                        currentTable.owner);

//                                resMatchJoin.setPhomInfo(currentTable.anCayMatTien, currentTable.taiGuiUDen, currentTable.isPlaying,
//                                        isResume, currentTable.currentPlayer.id, cards, currentTable.restCards.size());
                                resMatchJoin.setCapacity(currentTable.getMaximumPlayer());
                                // send broadcast msg to friends
//                                resMatchJoin.zoneID = playerSession.getCurrentZone();
                                playerSession.write(resMatchJoin);

                                currentRoom.broadcastMessage(broadcastMsg, aSession, true);

                            } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                            } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                            } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                resMatchJoin.setFailure(ResponseCode.FAILURE,
                                        "Bàn này đã đầy rồi, chọn bàn khác đi bạn!");
                            }
                            // Feedback to Player
                            //aSession.write(resMatchJoin);
                            //   playerSession.write(resMatchJoin);
                            break;
                        }
                        case ZoneID.COTUONG: {
                            ISession playerSession = aSession.getManager().findSession(uid);
                            CoTuongTable currentTable = (CoTuongTable) currentRoom.getAttactmentData();
                            if (currentTable.isPlaying) {
                                resAcceptJoin.setFailure(
                                        ResponseCode.FAILURE,
                                        "Bàn chơi đã có người chơi rồi!");
                                playerSession.write(resAcceptJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            // Check money of player
                            if (moneyOfPlayer < currentTable.getJoinMoney()) {
                                resAcceptJoin.setFailure(
                                        ResponseCode.FAILURE,
                                        "Bạn không đủ tiền để tham gia bàn này!");
                                playerSession.write(resAcceptJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return;
                            }
                            int joinedCode = currentRoom.join(playerSession);
//                            JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                            // broadcast's values
//Tho
                            currentTable.removeAllPlayer();
                            CoTuongPlayer newPlayer = new CoTuongPlayer(uid);
                            newPlayer.setAvatarID(newUser.avatarID);
                            newPlayer.setLevel(newUser.level);
                            newPlayer.setCash(newUser.money);
                            newPlayer.setUsername(newUser.mUsername);
                            newPlayer.moneyForBet = currentTable.getMinBet();
                            newPlayer.setCurrentMatchID(matchID);
                            currentTable.addPlayer(newPlayer);
                            currentTable.setPlayer(newPlayer);
                            resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                    currentRoom.getName(),
                                    currentTable.getMinBet(), aSession.getCurrentZone());
                            resMatchJoin.setRoomID(matchID);
                            resMatchJoin.available = currentTable.available;
                            resMatchJoin.totalTime = currentTable.totalTime;
                            resMatchJoin.setCurrentPlayersCoTuong(
                                    currentTable.getPlayers(),
                                    currentTable.getOwner());
                            currentTable.player_list.add(newPlayer);
                            resMatchJoin.setPlayerList(currentTable.getPlayersList());

                            resMatchJoin.uid = newPlayer.id;
                            // broadcast's values
                            resAcceptJoin.setSuccess(
                                    ResponseCode.SUCCESS);
                            currentRoom.broadcastMessage(
                                    resMatchJoin, aSession, true);
                            //for android
                            JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                            broadcastMsg.setSuccess(ResponseCode.SUCCESS,
                                    uid, newUser.mUsername,
                                    newUser.level,
                                    newUser.avatarID,
                                    newUser.money,
                                    ZoneID.TIENLEN);
                            ArrayList<ISession> list = currentRoom.getListSession();
                            for (ISession i : list) {
                                if (i.isAndroid()) {
                                    i.write(broadcastMsg);
                                }
                            }

                            currentTable.startMatch();
                            try { // Write to room's owner
                                aSession.write(resAcceptJoin);
                            } catch (ServerException e) {
                                mLog.error("Process error.", e.getCause());
                            }
//                            broadcastMsg.setSuccess(ResponseCode.SUCCESS, uid,
//                                    newUser.mUsername, newUser.level,
//                                    newUser.avatarID, newUser.money, ZoneID.COTUONG);
//                            currentRoom.broadcastMessage(broadcastMsg,
//                                    aSession, true);
                            break;
                        }
                        default:
                            break;
                    }

                } else {
                    resAcceptJoin.setFailure(ResponseCode.FAILURE,
                            "Bạn đã rời khỏi bàn chơi");
                }
            } else {
                resAcceptJoin.setFailure(ResponseCode.FAILURE,
                        "Không tìm thấy bạn trong cơ sở dữ liệu");
            }
        } catch (Throwable t) {
            resAcceptJoin.setFailure(ResponseCode.FAILURE, "Bị lỗi ");
            mLog.error("Process error.", t);
        }


    }
}
