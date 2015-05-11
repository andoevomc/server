package dreamgame.business;

import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.RemoveFriendRequest;
import dreamgame.protocol.messages.RemoveFriendResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class RemoveFriendBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(RemoveFriendBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "RemoveFriendBusiness - handleMessage");
	}
        mLog.debug("[Remove Friend] : Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        RemoveFriendResponse resRemoveFriend =
                (RemoveFriendResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            RemoveFriendRequest rqRemoveFriend = (RemoveFriendRequest) aReqMsg;
	    
	    if (rqRemoveFriend.currID != aSession.getUID()) {
		resRemoveFriend.setFailure(ResponseCode.FAILURE, "Wrong request !!!");
		aResPkg.addMessage(resRemoveFriend);
		return 1; 
	    }
	    
            if(DatabaseDriver.isFriend(rqRemoveFriend.currID, rqRemoveFriend.friendID)) {
	            DatabaseDriver.removeFriend(rqRemoveFriend.currID, rqRemoveFriend.friendID);
//	            DatabaseDriver.removeFriend(rqRemoveFriend.friendID, rqRemoveFriend.currID);
	            resRemoveFriend.setSuccess(ResponseCode.SUCCESS);
            } 
	    else {
            	resRemoveFriend.setFailure(ResponseCode.FAILURE, "Bạn đã thêm bạn ý vào danh sách bạn chưa nhỉ?");
            }

        } catch (Exception e) {
            resRemoveFriend.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cơ sở dữ liệu");
            mLog.debug("Loi: "+e.getCause());
        } finally {
            if ((resRemoveFriend != null)) {
                aResPkg.addMessage(resRemoveFriend);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
