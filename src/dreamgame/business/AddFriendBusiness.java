package dreamgame.business;

import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.AddFriendRequest;
import dreamgame.protocol.messages.AddFriendResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class AddFriendBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(AddFriendBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "AddFriendBusiness - handleMessage");
	}
	
        mLog.debug("[Add Friend] : Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        AddFriendResponse resAddFriend =
                (AddFriendResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            AddFriendRequest rqAddFriend = (AddFriendRequest) aReqMsg;
	    
	    if (rqAddFriend.currID != aSession.getUID()) {
		resAddFriend.setFailure(ResponseCode.FAILURE, "Wrong request !!!");
		aResPkg.addMessage(resAddFriend);
		return 1; 
	    }
	    
            if(!DatabaseDriver.isFriend(rqAddFriend.currID, rqAddFriend.friendID)){
	            DatabaseDriver.addFriend(rqAddFriend.currID, rqAddFriend.friendID);
//	            DatabaseDriver.addFriend(rqAddFriend.friendID, rqAddFriend.currID);
	            resAddFriend.setSuccess(ResponseCode.SUCCESS);
            } else {
            	resAddFriend.setFailure(ResponseCode.FAILURE, "Hai bạn đã là bạn của nhau rồi");
            }
        } catch (Exception e) {
            resAddFriend.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cư sở dữ liệu");
        } finally {
            if ((resAddFriend != null)) {
                aResPkg.addMessage(resAddFriend);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
