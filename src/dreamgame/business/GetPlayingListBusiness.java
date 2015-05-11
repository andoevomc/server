package dreamgame.business;
import dreamgame.config.DebugConfig;
import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.GetPlayingListRequest;
import dreamgame.protocol.messages.GetPlayingListResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.RoomEntity;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import java.util.Vector;
import org.slf4j.Logger;

public class GetPlayingListBusiness extends AbstractBusiness
{

    private static final Logger mLog = 
    	LoggerContext.getLoggerFactory().getLogger(GetPlayingListBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg)
    {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetPlayingListBusiness - handleMessage");
	}
        MessageFactory msgFactory = aSession.getMessageFactory();
        GetPlayingListResponse resGetPlayingList = (GetPlayingListResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        mLog.debug("[GET PLAYING ROOM LIST]: Catch");
        try
        {
            GetPlayingListRequest rqGetWaitingList = (GetPlayingListRequest) aReqMsg;
            Zone zone = aSession.findZone(aSession.getCurrentZone());

            /*int numPlayingRoom = zone.getNumPlaylingRoom();

            Vector<RoomEntity> playingRooms = zone.dumpPlayingRooms(rqGetWaitingList.mOffset, rqGetWaitingList.mLength, aSession.getCurrentZone());
            resGetPlayingList.setSuccess(ResponseCode.SUCCESS, numPlayingRoom, playingRooms);
             * 
             */
        } catch (Throwable t) {
            resGetPlayingList.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cơ sở dữ liệu ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resGetPlayingList != null)) {
                aResPkg.addMessage(resGetPlayingList);
            }
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }

}
