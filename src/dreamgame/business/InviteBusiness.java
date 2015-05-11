package dreamgame.business;

import dreamgame.baucua.data.BauCuaPlayer;
import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.SimpleTable;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.InviteRequest;
import dreamgame.protocol.messages.InviteResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.room.fake.ZoneConfigManager;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Vector;

public class InviteBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(InviteBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "InviteBusiness - handleMessage");
	}
	
        int rtn = PROCESS_FAILURE;
        MessageFactory msgFactory = aSession.getMessageFactory();
        InviteResponse resInvite = (InviteResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        mLog.debug("[INVITE]: Catch");
        try {
            InviteRequest rqInvite = (InviteRequest) aReqMsg;
            long sourceID = rqInvite.sourceUid;
            long destID = rqInvite.destUid;
            long roomID = rqInvite.roomID;
	    Zone zone = aSession.findZone(aSession.getCurrentZone());
            Room room = zone.findRoom(roomID);
	    
            if (room == null) {
                mLog.error("OMG room is null : " + roomID);
                resInvite.setFailure(ResponseCode.FAILURE,
                        "Bàn chơi của bạn đã bị hủy. Không mời được.");
                aSession.write(resInvite);
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }
            SimpleTable table = (SimpleTable) room.getAttactmentData();
	    
	    // invite to a fake user -> tra ve success
	    boolean buddyIsFakeUser = DatabaseDriver.isFakeUser(destID);
	    if (buddyIsFakeUser) {
		int chance = ZoneConfigManager.generateRandomNumber(2, 4);
		// tung đồng xu xem nên response kiểu gì, success, failure?
		if ((chance % 2) == 0) {
		    resInvite.setSuccess(ResponseCode.SUCCESS,
				    sourceID, roomID, room.getName(), aSession.getUserName(), table.getMinBet(),
				    table.getLevel());
		    resInvite.currentZone = aSession.getCurrentZone();
		    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
		    return 1;
		}
		// failure
		else {
		    // chọn message trả về
		    chance = ZoneConfigManager.generateRandomNumber(1, 3);
		    String message = chance % 2 == 0 ? "Không mời được. Bạn ấy đang chơi rồi." : "Bạn ấy từ chối tham gia bàn của bạn";
		    resInvite.setFailure(ResponseCode.FAILURE, message);
                    aSession.write(resInvite);
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 1;
		}
	    }
		
            /*
             * switch (aSession.getCurrentZone()) { case ZoneID.BACAY: {
             */
            ISession buddySession = aSession.getManager().findSession(destID);
            if (buddySession != null) {
                Vector<Room> joinedRoom = buddySession.getJoinedRooms();

                //if ( buddySession.getCurrentZone() > 0 && buddySession.getCurrentZone() != aSession.getCurrentZone()) {

                long status = DatabaseDriver.getUserGameStatus(buddySession.getUID());
                if (status == 1) {
                    resInvite.setFailure(ResponseCode.FAILURE,
                            "Không mời được. Bạn ấy đang chơi rồi.");
                    aSession.write(resInvite);
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 1;
                }
                if (buddySession.getCurrentZone() != aSession.getCurrentZone()) {
                    resInvite.setFailure(ResponseCode.FAILURE,
                            "Không mời được. Bạn ấy đang ở Game khác rồi.");
                    aSession.write(resInvite);

                } else if (joinedRoom.size() > 0) {
		    boolean isTrongRoomDo = false;
		    for (int i = 0; i < joinedRoom.size(); i++) {
			if (roomID == joinedRoom.get(i).getRoomId()) {
			    isTrongRoomDo = true;
			    break;
			}
		    }
		    if (isTrongRoomDo)
			resInvite.setFailure(ResponseCode.FAILURE, "Bạn ý đang chơi với bạn nên không cần mời nữa.");
		    else
			resInvite.setFailure(ResponseCode.FAILURE, "Bạn ý đang chơi ở bàn khác mất rồi");
                    aSession.write(resInvite);
                } else {
                    // Get user information
                    UserEntity newUser = DatabaseDriver.getUserInfo(destID);
                    if (newUser != null) {
                        switch (aSession.getCurrentZone()) {
                            case ZoneID.BACAY:
                                if (newUser.money < table.getMinBet()) {
                                    resInvite.setFailure(ResponseCode.FAILURE,
                                            "Người bạn mời không đủ tiền chơi bàn này");
                                    aSession.write(resInvite);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                break;
                            case ZoneID.PHOM:
                                if (newUser.money < 5 * table.getMinBet()) {
                                    resInvite.setFailure(ResponseCode.FAILURE,
                                            "Người bạn mời không đủ tiền chơi bàn này");
                                    aSession.write(resInvite);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                break;
                            case ZoneID.COTUONG:
                                if (newUser.money < table.getMinBet()) {
                                    resInvite.setFailure(ResponseCode.FAILURE,
                                            "Người bạn mời không đủ tiền chơi bàn này");
                                    aSession.write(resInvite);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                break;
                            case ZoneID.TIENLEN_MB:
                            case ZoneID.TIENLEN_DEMLA:
                            case ZoneID.TIENLEN:
                                if (newUser.money < 10 * table.getMinBet()) {
                                    resInvite.setFailure(ResponseCode.FAILURE,
                                            "Người bạn mời không đủ tiền chơi bàn này");
                                    aSession.write(resInvite);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                break;
                            case ZoneID.BAUCUA:
                                if (newUser.money < table.getMinBet()) {
                                    resInvite.setFailure(ResponseCode.FAILURE,
                                            "Người bạn mời không đủ tiền chơi bàn này");
                                    aSession.write(resInvite);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                if (newUser.money < table.getMinBet()) {
                                    resInvite.setFailure(ResponseCode.FAILURE,
                                            "Người bạn mời không đủ tiền chơi bàn này");
                                    aSession.write(resInvite);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                break;
                            case ZoneID.POKER:
                            case ZoneID.XITO:
                                if (newUser.money < table.getMinBet()) {
                                    resInvite.setFailure(ResponseCode.FAILURE,
                                            newUser.mUsername + "Người bạn mời không đủ tiền chơi bàn này");
                                    aSession.write(resInvite);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                break;
                            case ZoneID.MAUBINH:
                                if (newUser.money < table.getMinBet() * 5) {
                                    resInvite.setFailure(ResponseCode.FAILURE,
                                            newUser.mUsername + "Người bạn mời không đủ tiền chơi bàn này");
                                    aSession.write(resInvite);
                                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                    return 1;
                                }
                                break;
                            default:
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return 1;
                        }
                        room.addWaitingSessionByID(buddySession);
                        resInvite.setSuccess(ResponseCode.SUCCESS,
                                sourceID, roomID, room.getName(), aSession.getUserName(), table.getMinBet(),
                                table.getLevel());
                        resInvite.currentZone = aSession.getCurrentZone();

                        if (!buddySession.realDead()) {
                            buddySession.write(resInvite);
                        }

                    } else {
                        resInvite.setFailure(ResponseCode.FAILURE,
                                "Không tìm thấy người bạn này");
                        aSession.write(resInvite);
                    }
                }
            } else {
                resInvite.setFailure(ResponseCode.FAILURE,
                        "Không tìm thấy tên bạn");
                aSession.write(resInvite);
            }
        } catch (Throwable t) {
            resInvite.setFailure(ResponseCode.FAILURE,
                    "Không thực hiện mời được!");
            aSession.setLoggedIn(false);
            aResPkg.addMessage(resInvite);
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return rtn;
    }
}
