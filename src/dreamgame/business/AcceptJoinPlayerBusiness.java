package dreamgame.business;

import dreamgame.data.MessagesID;
import java.util.Vector;

import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.AcceptJoinPlayerRequest;
import dreamgame.protocol.messages.AcceptJoinPlayerResponse;
import dreamgame.protocol.messages.JoinResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.CoTuongTable;

import dreamgame.config.DebugConfig;
/**
 *
 * @author Dinhpv
 */
public class AcceptJoinPlayerBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(AcceptJoinPlayerBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "AcceptJoinPlayerBusiness - handleMessage");
	}
        String username = null;
        boolean isSuccess = false;
        MessageFactory msgFactory = aSession.getMessageFactory();
        AcceptJoinPlayerResponse resAcceptJoin = (AcceptJoinPlayerResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            mLog.debug("[ACCEPT JOIN PLAYER ROOM]: Catch");
            AcceptJoinPlayerRequest rqAcceptJoin = (AcceptJoinPlayerRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room currentRoom = bacayZone.findRoom(rqAcceptJoin.mMatchId);
            UserEntity newUser = DatabaseDriver.getUserInfo(rqAcceptJoin.uid);
            if (newUser != null) {
                long moneyOfPlayer = newUser.money;
                long uid = newUser.mUid;
                username = newUser.mUsername;
                if (currentRoom != null) {
                    switch (aSession.getCurrentZone()) {
                        case ZoneID.COTUONG: {
                            //Tho
                            ISession playerSession = currentRoom.getSessionByID(uid);
//                            ISession playerSession = aSession.getManager().findSession(uid);
                            if (playerSession == null) {
                                resAcceptJoin.setFailure(ResponseCode.FAILURE,
                                        username + " đã thoát.");
                                aSession.write(resAcceptJoin);
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                            }
                            Vector<Room> joinedRoom = playerSession.getJoinedRooms();
//                            Tho
                            if (!joinedRoom.contains(currentRoom)) {
//                            if (joinedRoom.size() > 0) {
                                resAcceptJoin.setFailure(ResponseCode.FAILURE,
                                        username + " đã tham gia bàn khác.");
                            } else {
                                if (rqAcceptJoin.isAccept) {
                                    CoTuongTable currentTable = (CoTuongTable) currentRoom.getAttactmentData();
                                    if (currentTable.isPlaying) {
                                        resAcceptJoin.setFailure(
                                                ResponseCode.FAILURE,
                                                "Bàn chơi đã có người chơi rồi!");
                                        playerSession.write(resAcceptJoin);
                                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                        return 1;
                                    }
                                    // Check money of player
                                    if (moneyOfPlayer < currentTable.getJoinMoney()) {
                                        resAcceptJoin.setFailure(
                                                ResponseCode.FAILURE,
                                                "Bạn không đủ tiền để tham gia bàn này!");
                                        playerSession.write(resAcceptJoin);
                                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                        return 1;
                                    }
                                    isSuccess = true;
                                    JoinResponse resMatchJoin = (JoinResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOIN);
                                    //Tho
                                    currentRoom.join(playerSession);
                                    currentTable.removeAllPlayer();
                                    CoTuongPlayer newPlayer = new CoTuongPlayer(uid);
                                    newPlayer.setAvatarID(newUser.avatarID);
                                    newPlayer.setLevel(newUser.level);
                                    newPlayer.setCash(newUser.money);
                                    newPlayer.setUsername(newUser.mUsername);
                                    newPlayer.moneyForBet = currentTable.getMinBet();
                                    newPlayer.setCurrentMatchID(rqAcceptJoin.mMatchId);
                                    currentTable.setPlayer(newPlayer);
                                    currentTable.addPlayer(newPlayer);

                                    // broadcast's values
                                    resAcceptJoin.available = currentTable.available;
                                    resAcceptJoin.totalTime = currentTable.totalTime;
                                    resAcceptJoin.avatar = newPlayer.avatarID;
                                    resAcceptJoin.level = newPlayer.level;
                                    resAcceptJoin.username = newPlayer.username;
                                      resAcceptJoin.cash = newPlayer.cash;
                                    resAcceptJoin.setSuccess(
                                            ResponseCode.SUCCESS, newUser.mUid);
//                                    currentRoom.broadcastMessage(
//                                            resAcceptJoin, aSession, true);
//                                    playerSession.write(resAcceptJoin);
//                                    aSession.write(resAcceptJoin);


                                    for (CoTuongPlayer player : currentTable.getPlayersList()) {
                                        long playerID = player.id;
                                        ISession playerSessions = aSession.getManager().findSession(playerID);
                                        if (playerSessions.getMobile()) {
                                            if (playerID == aSession.getUID()) {
                                                aResPkg.addMessage(resAcceptJoin);
                                            } else {
                                                //for (int i=0;i<5;i++)
                                                playerSessions.write(resAcceptJoin);
                                            }
                                        }

                                    }

//                                    currentRoom.broadcastMessage(
//                                            resAcceptJoin, aSession, true);

                                    resMatchJoin.available = currentTable.available;
                                    resMatchJoin.totalTime = currentTable.totalTime;
                                    resMatchJoin.setSuccess(ResponseCode.SUCCESS,
                                            currentRoom.getName(),
                                            currentTable.getMinBet(), aSession.getCurrentZone());
                                    resMatchJoin.setRoomID(rqAcceptJoin.mMatchId);

                                    resMatchJoin.setCurrentPlayersCoTuong(
                                            currentTable.getPlayers(),
                                            currentTable.getOwner());
                                    currentRoom.broadcastMessage(
                                            resMatchJoin, aSession, true);
                                    currentTable.startMatch();
                                } else {
                                    resAcceptJoin.setFailure(
                                            ResponseCode.FAILURE,
                                            "Bạn bị từ chối!");
                                    playerSession.write(resAcceptJoin);
                                    isSuccess = true;
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
        } catch (Throwable t) {
            resAcceptJoin.setFailure(ResponseCode.FAILURE, "Bị lỗi ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if (resAcceptJoin != null) {
                if (!isSuccess) {
                    aResPkg.addMessage(resAcceptJoin);
                }
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
