package dreamgame.business;


import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.FindRoomByOwnerRequest;
import dreamgame.protocol.messages.FindRoomByOwnerResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;

public class FindRoomByOwnerBusiness extends AbstractBusiness
{

    private static final Logger mLog = 
    	LoggerContext.getLoggerFactory().getLogger(FindRoomByOwnerBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg)
    {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "FindRoomByOwnerBusiness - handleMessage");
	}
        mLog.debug("[FIND ROOM]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        FindRoomByOwnerResponse resFindRoom = (FindRoomByOwnerResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try
        {
            FindRoomByOwnerRequest rqFind = (FindRoomByOwnerRequest) aReqMsg;
            String username = rqFind.roomOwner;
            Zone zone = aSession.findZone(aSession.getCurrentZone());
            Room room = zone.findRoomByOwner(username);
            if(room != null){
            	resFindRoom.setSuccess(ResponseCode.SUCCESS, room.dumpRoom());
            } else {
            	resFindRoom.setFailure(ResponseCode.FAILURE, "Không tìm được bàn");
            }
        } catch (Throwable t){
            resFindRoom.setFailure(ResponseCode.FAILURE, "Không tìm được bàn");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally{
            if ((resFindRoom != null) ){
                aResPkg.addMessage(resFindRoom);
            }
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
