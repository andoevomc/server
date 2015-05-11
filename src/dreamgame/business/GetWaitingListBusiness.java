package dreamgame.business;

import dreamgame.config.DebugConfig;
import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.SimpleTable;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.CancelRequest;
import dreamgame.protocol.messages.GetUserInfoResponse;
import dreamgame.protocol.messages.GetWaitingListRequest;
import dreamgame.protocol.messages.GetWaitingListResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.RoomEntity;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Vector;
import org.slf4j.Logger;

public class GetWaitingListBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(GetWaitingListBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetWaitingListBusiness - handleMessage");
	}
        mLog.debug("[GET WAITING ROOM LIST]: Catch : Zone = " + aSession.getCurrentZone());
        MessageFactory msgFactory = aSession.getMessageFactory();
        GetWaitingListResponse resGetWaitingList = (GetWaitingListResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            GetWaitingListRequest rqGetWaitingList = (GetWaitingListRequest) aReqMsg;
            Zone zone = aSession.findZone(aSession.getCurrentZone());

            if (zone == null) {
                mLog.error("Error : Cant get zone ! " + aSession.getCurrentZone() + " : [" + aSession.getUserName() + "]");
                if (aSession.getCurrentZone() == 0) {
                    aSession.setCurrentZone(ZoneID.BACAY);
                    DatabaseDriver.updateUserZone(aSession.getUID(), ZoneID.BACAY);
                    zone = aSession.findZone(aSession.getCurrentZone());
                }
            }

            if (rqGetWaitingList == null) {
                mLog.error("Error : rqGetWaitingList null ! " + aSession.getCurrentZone());
            }

            Vector<Room> joinedRoom = aSession.getJoinedRooms();
            if (joinedRoom.size() > 0) {
                for (Room r : joinedRoom) {
                    //Room r = joinedRoom.firstElement();
                    System.out.println("User is in room : " + r.getName());
                    SimpleTable p = (SimpleTable) r.getAttactmentData();
                    System.out.println("room : p.isPlaying : " + p.isPlaying);
                    if (!p.isPlaying || (aSession.getCurrentZone() == ZoneID.TIENLEN || aSession.getCurrentZone() == ZoneID.TIENLEN_DEMLA || aSession.getCurrentZone() == ZoneID.TIENLEN_MB)) {
                        //remove player from room!
                        CancelRequest rqBoc = (CancelRequest) msgFactory.getRequestMessage(MessagesID.MATCH_CANCEL);
                        rqBoc.mMatchId = r.getRoomId();
                        rqBoc.uid = aSession.getUID();
                        IBusiness business = msgFactory.getBusiness(MessagesID.MATCH_CANCEL);
                        business.handleMessage(aSession, rqBoc, aResPkg);
                    }
                }
            }

            //switch (aSession.getCurrentZone()) {
            //case ZoneIDConstant.ID_ZONE_BACAY: {
            //int numWaitingRoom = zone.getNumWaitingRoom();
            //zone.create10room(aSession,aSession.getCurrentZone());

            int mLength = rqGetWaitingList.mLength;

            if (aSession.getMobile()) {

                mLength = 200;

            }

            System.out.println("Current zone : " + aSession.getCurrentZone());

            //rqGetWaitingList.mOffset=3;

            Vector<RoomEntity> waitingRooms = zone.dumpWaitingRooms(rqGetWaitingList.mOffset,
                    mLength, rqGetWaitingList.level, rqGetWaitingList.minLevel, aSession.getCurrentZone(), aSession.getChannel());

            mLog.debug("[GET WAITING ROOM LIST]: size - " + waitingRooms.size());


            resGetWaitingList.totalRoom = zone.getTotalRoom();
            if (aSession.getChannel() > 0) {
                resGetWaitingList.totalRoom = zone.getTotalRoom(aSession.getChannel());
            }

            resGetWaitingList.setSuccess(ResponseCode.SUCCESS, waitingRooms.size(), waitingRooms, aSession.getCurrentZone());
            resGetWaitingList.isMobile = aSession.getMobile();
            resGetWaitingList.compress = rqGetWaitingList.compress;

            /*
             * LoginResponse resLogin = (LoginResponse)
             * msgFactory.getResponseMessage(MessagesID.LOGIN);
             * resLogin.setFailure(ResponseCode.FAILURE, "no matter what");
            aSession.write(resLogin);
             */

            if (aSession.getReceiveGift() == 1 && aSession.getRemainGift() > 0) {
//                if (DatabaseDriver.getUserMoney(aSession.getUID()) == 0) {
//                    aSession.setGiftInfo(0, aSession.getRemainGift() - 1);
//
//                    aSession.writeMessage("Do bạn đã chơi hết tiền. Bạn nhận được khuyến mại " + aSession.getCashGift()
//                            + "$. Số lần khuyến mại còn lại trong ngày : " + aSession.getRemainGift());
//
//                    long uid = aSession.getUID();
//                    DatabaseDriver.updateGiftInfo(uid, 0, aSession.getRemainGift());
//                    DatabaseDriver.updateCashOnly(uid, aSession.getCashGift());
//
//                    UserEntity user = DatabaseDriver.getUserInfo(uid);
//                    if (user != null) {
//                        boolean isFriend = false;
//                        GetUserInfoResponse resGetUserInfo = (GetUserInfoResponse) msgFactory.getResponseMessage(MessagesID.GET_USER_INFO);
//                        resGetUserInfo.setSuccess(ResponseCode.SUCCESS, user.mUid, user.mUsername,
//                                user.mAge, user.mIsMale, user.money, user.playsNumber, user.avatarID, isFriend, user.level);
//                        aSession.write(resGetUserInfo);
//                    }
//                }
            }

            //      break;
            //  }
            //TODO: add more here
            //  default:
            //      break;
            // }

            //      break;
            //  }
            //TODO: add more here
            //  default:
            //      break;
            // }

        } catch (Throwable t) {
            resGetWaitingList.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cơ sở dữ liệu ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resGetWaitingList != null)) {
                aResPkg.addMessage(resGetWaitingList);
            }
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
