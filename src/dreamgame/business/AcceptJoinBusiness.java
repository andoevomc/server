package dreamgame.business;

import java.util.Vector;

import org.slf4j.Logger;

import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.AcceptJoinRequest;
import dreamgame.protocol.messages.AcceptJoinResponse;
import dreamgame.protocol.messages.JoinResponse;
import dreamgame.protocol.messages.JoinedResponse;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
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

import dreamgame.config.DebugConfig;

public class AcceptJoinBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(AcceptJoinBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "AcceptJoinBusiness - handleMessage");
	}
	
        String username = null;
        MessageFactory msgFactory = aSession.getMessageFactory();
        // Feedback to Player
        JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);

        // Feedback to Room's Owner
        AcceptJoinResponse resAcceptJoin = (AcceptJoinResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            mLog.debug("[ACCEPT JOIN ROOM]: Catch");
            AcceptJoinRequest rqAcceptJoin = (AcceptJoinRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room currentRoom = bacayZone.findRoom(rqAcceptJoin.mMatchId);

            if (currentRoom==null)
            {
                mLog.error("CurrentRoom is null : " + rqAcceptJoin.mMatchId);
                resMatchJoin.setFailure(
                        ResponseCode.FAILURE,
                        "Phòng đã bị hủy!");
                aSession.write(resMatchJoin);
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }

            boolean isPassSecure = (currentRoom.checkPassword(rqAcceptJoin.password));
            if (isPassSecure) {
                // Get user information
                UserEntity newUser = DatabaseDriver.getUserInfo(rqAcceptJoin.uid);
                if (newUser != null) {
                    long moneyOfPlayer = newUser.money;
                    long uid = newUser.mUid;
                    username = newUser.mUsername;
                    if (currentRoom != null) {
                        switch (aSession.getCurrentZone()) {
                            case ZoneID.BACAY: {
                                ISession playerSession = currentRoom.getWaitingSessionByID(uid);
                                Vector<Room> joinedRoom = playerSession.getJoinedRooms();
                                if (joinedRoom.size() > 0) {
                                    resAcceptJoin.setFailure(ResponseCode.FAILURE,
                                            username + " đã tham gia bàn khác.");
                                } else {
                                    currentRoom.removeWaitingSessionByID(playerSession);
                                    if (rqAcceptJoin.isAccept) {
                                        JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                                        BacayTable currentTable = (BacayTable) currentRoom.getAttactmentData();
                                        if ((currentTable.getPlayers().size() + 1) >= currentTable.getMaximumPlayer()) {
                                            resMatchJoin.setFailure(
                                                    ResponseCode.FAILURE,
                                                    "Chủ Room không còn đủ trình hoặc không muốn thêm người nữa!");
                                            playerSession.write(resMatchJoin);
                                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                            return 1;
                                        }
                                        // Check money of player
                                        if (moneyOfPlayer < currentTable.getJoinMoney()) {
                                            resMatchJoin.setFailure(
                                                    ResponseCode.FAILURE,
                                                    "Bạn không đủ tiền để tham gia bàn này!");
                                            playerSession.write(resMatchJoin);
                                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                            return 1;
                                        }
                                        int joinedCode = currentRoom.join(playerSession);

                                        // in cases
                                        if (joinedCode == RoomStatus.JOIN_PLAYER) {
                                            BacayPlayer newPlayer = new BacayPlayer(
                                                    uid);
                                            newPlayer.setAvatarID(newUser.avatarID);
                                            newPlayer.setLevel(newUser.level);
                                            newPlayer.setCash(newUser.money);
                                            newPlayer.setUsername(newUser.mUsername);
                                            newPlayer.moneyForBet = currentTable.getMinBet();
                                            newPlayer.setCurrentMatchID(rqAcceptJoin.mMatchId);
                                            currentTable.addPlayer(newPlayer);

                                            // broadcast's values
                                            broadcastMsg.setSuccess(
                                                    ResponseCode.SUCCESS,
                                                    uid, newUser.mUsername,
                                                    newUser.level,
                                                    newUser.avatarID,
                                                    newUser.money, ZoneID.BACAY);

                                            // join's values
                                            resMatchJoin.setSuccess(
                                                    ResponseCode.SUCCESS,
                                                    currentRoom.getName(),
                                                    currentTable.getMinBet(), aSession.getCurrentZone());
                                            resMatchJoin.setRoomID(rqAcceptJoin.mMatchId);
                                            resMatchJoin.setCurrentPlayersBacay(
                                                    currentTable.getPlayers(),
                                                    currentTable.getRoomOwner());
                                            // send broadcast msg to friends
                                            currentRoom.broadcastMessage(
                                                    broadcastMsg, aSession, true);
                                        } else if (joinedCode == RoomStatus.JOIN_OBSERVER) {
                                            // broadcast's values
                                            broadcastMsg.setSuccess(
                                                    ResponseCode.SUCCESS,
                                                    uid, newUser.mUsername,
                                                    newUser.level,
                                                    newUser.avatarID,
                                                    newUser.money, ZoneID.BACAY);
                                            // send broadcast msg to friends
                                            currentRoom.broadcastMessage(
                                                    broadcastMsg, aSession, true);
                                            // reply response message

                                            resMatchJoin.setSuccess(
                                                    ResponseCode.SUCCESS,
                                                    currentRoom.getName(),
                                                    currentTable.getMinBet(), aSession.getCurrentZone());
                                            resMatchJoin.setRoomID(rqAcceptJoin.mMatchId);
                                            resMatchJoin.setCurrentPlayersBacay(
                                                    currentTable.getPlayers(),
                                                    currentTable.getRoomOwner());

                                        } else if (joinedCode == RoomStatus.JOIN_ALREADY) {
                                            BacayPlayer newPlayer = new BacayPlayer(
                                                    uid);
                                            newPlayer.setAvatarID(newUser.avatarID);
                                            newPlayer.setLevel(newUser.level);
                                            newPlayer.setCash(newUser.money);
                                            newPlayer.setUsername(newUser.mUsername);
                                            currentTable.addPlayerToWaitingList(newPlayer);
                                            resMatchJoin.setSuccess(
                                                    ResponseCode.SUCCESS,
                                                    currentRoom.getName(),
                                                    currentTable.getMinBet(), aSession.getCurrentZone());
                                            resMatchJoin.setRoomID(rqAcceptJoin.mMatchId);

                                            resMatchJoin.setCurrentPlayersBacay(
                                                    currentTable.getPlayers(),
                                                    currentTable.getRoomOwner());
                                        } else if (joinedCode == RoomStatus.JOIN_FULL) {
                                            resMatchJoin.setFailure(
                                                    ResponseCode.FAILURE,
                                                    "Bàn chơi này đã đầy rồi, chọn bàn khác đi bạn!");
                                        }
                                    } else {
                                        resMatchJoin.setFailure(
                                                ResponseCode.FAILURE,
                                                "Chủ bàn không đồng ý");
                                    }
                                    resAcceptJoin.setSuccess(ResponseCode.SUCCESS);
                                    // Feedback to Player
                                    if (playerSession != null) {
                                        playerSession.write(resMatchJoin);
                                    }
                                }
                                break;
                            }
                            case ZoneID.COTUONG: {
                                ISession playerSession = currentRoom.getSessionByID(uid);
                                Vector<Room> joinedRoom = playerSession.getJoinedRooms();

                                if (!joinedRoom.contains(currentRoom)) {
                                    resAcceptJoin.setFailure(ResponseCode.FAILURE,
                                            username + " đã tham gia bàn khác.");
                                } else {
                                    if (rqAcceptJoin.isAccept) {
                                        JoinedResponse broadcastMsg = (JoinedResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                                        CoTuongTable currentTable = (CoTuongTable) currentRoom.getAttactmentData();
                                        if (currentTable.isPlaying) {
                                            resMatchJoin.setFailure(
                                                    ResponseCode.FAILURE,
                                                    "Bàn chơi đã có người chơi rồi!");
                                            playerSession.write(resMatchJoin);
                                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                            return 1;
                                        }
                                        // Check money of player
                                        if (moneyOfPlayer < currentTable.getJoinMoney()) {
                                            resMatchJoin.setFailure(
                                                    ResponseCode.FAILURE,
                                                    "Bạn không đủ tiền để tham gia bàn này!");
                                            playerSession.write(resMatchJoin);
                                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                            return 1;
                                        }
                                        CoTuongPlayer newPlayer = new CoTuongPlayer(uid);
                                        newPlayer.setAvatarID(newUser.avatarID);
                                        newPlayer.setLevel(newUser.level);
                                        newPlayer.setCash(newUser.money);
                                        newPlayer.setUsername(newUser.mUsername);
                                        newPlayer.moneyForBet = currentTable.getMinBet();
                                        newPlayer.setCurrentMatchID(rqAcceptJoin.mMatchId);
                                        currentTable.setPlayer(newPlayer);
                                        // broadcast's values
                                        broadcastMsg.setSuccess(
                                                ResponseCode.SUCCESS,
                                                uid, newUser.mUsername,
                                                newUser.level,
                                                newUser.avatarID,
                                                newUser.money, ZoneID.COTUONG);
                                        currentRoom.broadcastMessage(
                                                broadcastMsg, aSession, true);
                                    }
                                }
                                break;
                            }
                            default:
                                break;
                        }

                    } else {
                        resAcceptJoin.setFailure(ResponseCode.FAILURE,
                                "Bạn đã thoát khỏi room");
                    }
                } else {
                    resAcceptJoin.setFailure(ResponseCode.FAILURE, username
                            + " không tồn tại");
                }
            } else {
                resAcceptJoin.setFailure(ResponseCode.FAILURE,
                        "Mật khẩu vào bàn không đúng!");
            }
        } catch (Throwable t) {
            resAcceptJoin.setFailure(ResponseCode.FAILURE, "Bị lỗi ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if (resAcceptJoin != null) {
                aResPkg.addMessage(resAcceptJoin);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
