package dreamgame.business;
import dreamgame.config.DebugConfig;
import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.ChatRequest;
import dreamgame.protocol.messages.ChatResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import org.slf4j.Logger;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;

public class ChatBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(ChatBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "ChatBusiness - handleMessage");
	}
        int rtn = PROCESS_FAILURE;
        MessageFactory msgFactory = aSession.getMessageFactory();
        ChatResponse resChat = (ChatResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        mLog.debug("[CHAT]: Catch");
        try {
            // request message and its values
        	ChatRequest rqChat = (ChatRequest) aReqMsg;
            String message = rqChat.mMessage;
            Room currentRoom;
            /*
            int messageid = rqChat.type;
            if (messageid == ZoneIDConstant.ID_ZONE_PRIVATE_CHAT) {
                // chat zone
                Zone chatZone = aSession.findZone(ZoneIDConstant.ID_ZONE_PRIVATE_CHAT);
                currentRoom = chatZone.findRoom(rqChat.mRoomId);
                if (currentRoom == null && !aSession.isJoinedFull(ZoneIDConstant.ID_ZONE_PRIVATE_CHAT)) {
                    currentRoom = chatZone.createRoom();
                    rqChat.mRoomId = currentRoom.getRoomId();
                }
            } else {
                Zone chatZone = aSession.findZone(ZoneIDConstant.ID_ZONE_BACAY);
                currentRoom = chatZone.findRoom(rqChat.mRoomId);
                if (currentRoom == null && !aSession.isJoinedFull(ZoneIDConstant.ID_ZONE_BACAY)) {
                    currentRoom = chatZone.createRoom();
                    rqChat.mRoomId = currentRoom.getRoomId();
                }
            }
            */
            // broadcast
            Zone chatZone = aSession.findZone(aSession.getCurrentZone());
            currentRoom = chatZone.findRoom(rqChat.mRoomId);
            resChat.matchId = rqChat.mRoomId;
            
            if(currentRoom != null){
	            // broadcast
	            ChatResponse broadcastMsg = (ChatResponse) msgFactory.getResponseMessage(aReqMsg.getID());
	            broadcastMsg.setMessage(message);
	            broadcastMsg.setUsername(aSession.getUserName());
	            broadcastMsg.setRoomID(rqChat.mRoomId);
	            broadcastMsg.setType(rqChat.type);
	            broadcastMsg.setSuccess(ResponseCode.SUCCESS);
	            currentRoom.broadcastMessage(broadcastMsg, aSession, false);
            }else {
            	resChat.setFailure(ResponseCode.FAILURE, "Bàn chơi đã bị hủy, bạn không thể chat trong bàn này được.!");
            }
            rtn = PROCESS_OK;
        } catch (Throwable t) {
            // response failure
            resChat.setFailure(ResponseCode.FAILURE, "Phần Chat đang bị lỗi!");
            aSession.setLoggedIn(false);
            rtn = PROCESS_OK;
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
            aResPkg.addMessage(resChat);
        } 

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return rtn;
    }

}
