package dreamgame.business;

import java.util.Vector;

import org.slf4j.Logger;

import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.AcceptJoinPlayerResponse;
import dreamgame.protocol.messages.AcceptJoinRequest;
import dreamgame.protocol.messages.AcceptJoinResponse;
import dreamgame.protocol.messages.JoinPlayerRequest;
import dreamgame.protocol.messages.JoinPlayerResponse;
import dreamgame.protocol.messages.JoinResponse;
import dreamgame.protocol.messages.JoinedResponse;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.config.DebugConfig;

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

/**
 *
 * @author Dinhpv
 */
public class JoinPlayerBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(AcceptJoinBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "JoinPlayerBusiness - handleMessage");
	}
        String username = null;
        boolean isFail = true;
        MessageFactory msgFactory = aSession.getMessageFactory();
        // Feedback to Player
        JoinPlayerResponse joinPlayerResponse = (JoinPlayerResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            mLog.debug("[JOIN PLAYER]: Catch");
            JoinPlayerRequest rqAcceptJoin = (JoinPlayerRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room currentRoom = bacayZone.findRoom(rqAcceptJoin.mMatchId);
            if (true) {
                // Get user information
                UserEntity newUser = DatabaseDriver.getUserInfo(aSession.getUID());
                if (newUser != null) {
                    long uid = newUser.mUid;
                    username = newUser.mUsername;
                    if (currentRoom != null) {
                        switch (aSession.getCurrentZone()) {
                            case ZoneID.COTUONG: {
                                //Tho
                                ISession playerSession = currentRoom.getSessionByID(uid);
//                                ISession playerSession = aSession.getManager().findSession(uid);
                                Vector<Room> joinedRoom = playerSession.getJoinedRooms();
//                                Tho
                                if (!joinedRoom.contains(currentRoom)) {
//                                if (joinedRoom.size() > 0) {
                                    joinPlayerResponse.setFailure(ResponseCode.FAILURE,
                                            username + " đã tham gia bàn khác.");
                                } else {


                                    //JoinPlayerResponse broadcastMsg = (JoinPlayerResponse) msgFactory.getResponseMessage(MessagesID.MATCH_JOINED);
                                    CoTuongTable currentTable = (CoTuongTable) currentRoom.getAttactmentData();
                                    long ownerID = currentTable.getOwner().id;
                                    ISession ownerSession = aSession.getManager().findSession(ownerID);
                                    if (newUser != null) {
                                        long cash = newUser.money;
                                        int avatar = newUser.avatarID;
                                        int level = newUser.level;
                                        if (cash >= currentTable.getJoinMoney()) {
                                            if (currentTable.isPlaying) {
                                                joinPlayerResponse.setFailure(
                                                        ResponseCode.FAILURE,
                                                        "Đã có người vào chơi!");
                                                aResPkg.addMessage(joinPlayerResponse);
                                            } else {
                                                if (level >= currentTable.getLevel()) {
                                                    //send request for room owner
                                                    joinPlayerResponse.setSuccess(
                                                            ResponseCode.SUCCESS, uid,
                                                            cash, avatar, level, username);
                                                    ownerSession.write(joinPlayerResponse);
                                                    isFail = false;
                                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                                    return 1;
                                                } else { // not enough level
                                                    joinPlayerResponse.setFailure(
                                                            ResponseCode.FAILURE,
                                                            "Bạn đang tham gia một bàn quá sức của mình!");
                                                    aResPkg.addMessage(joinPlayerResponse);
                                                }
                                            }
                                        } else { // send back only player
                                            joinPlayerResponse.setFailure(ResponseCode.FAILURE,
                                                    "Bạn không đủ tiền để tham gia bàn này!");
                                            aResPkg.addMessage(joinPlayerResponse);
                                        }
                                    } else {
                                        joinPlayerResponse.setFailure(
                                                ResponseCode.FAILURE,
                                                "Không tìm thấy bạn trong cơ sở dữ liệu!");
                                        aResPkg.addMessage(joinPlayerResponse);
                                    }

                                }
                                break;
                            }


                            default:
                                break;
                        }

                    } else {
                        joinPlayerResponse.setFailure(ResponseCode.FAILURE,
                                "Bạn đã thoát khỏi room");
                    }
                } else {
                    joinPlayerResponse.setFailure(ResponseCode.FAILURE, username
                            + " không tồn tại");
                }
            } else {
                joinPlayerResponse.setFailure(ResponseCode.FAILURE,
                        "Mật khẩu vào bàn không đúng!");
            }
        } catch (Throwable t) {
            joinPlayerResponse.setFailure(ResponseCode.FAILURE, "Bị lỗi ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);


        } finally {
            if (joinPlayerResponse != null && isFail) {
                aResPkg.addMessage(joinPlayerResponse);


            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;

    }
}
