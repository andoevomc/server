package dreamgame.business;

import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.PrivateChatRequest;
import dreamgame.protocol.messages.PrivateChatResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class PrivateChatBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(PrivateChatBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PrivateChatBusiness - handleMessage");
	}
        int rtn = PROCESS_FAILURE;
        MessageFactory msgFactory = aSession.getMessageFactory();
        PrivateChatResponse resChat =
                (PrivateChatResponse) msgFactory.getResponseMessage(aReqMsg.getID());

        mLog.debug("[PRIVATE CHAT]: Catch");
        try {
            PrivateChatRequest rqChat = (PrivateChatRequest) aReqMsg;
            String message = rqChat.mMessage;
            long sourceID = rqChat.sourceUid;
            long destID = rqChat.destUid;

            ISession buddySession = aSession.getManager().findSession(destID);
            ISession sourceSesstion = aSession.getManager().findSession(sourceID);
            if (buddySession != null) {
                resChat.setSuccess(ResponseCode.SUCCESS, sourceID, message, sourceSesstion.getUserName());
                // Send message to buddy
                buddySession.write(resChat);
            } else {
                DatabaseDriver.insertOfflineMess(sourceID, destID, message);
                resChat.setFailure(ResponseCode.FAILURE, DatabaseDriver.getUserInfo(destID).mUsername + " hiện tại không online");
                // Send back if error
                aSession.write(resChat);
            }

            rtn = PROCESS_OK;

        } catch (Throwable t) {

            resChat.setFailure(ResponseCode.FAILURE, "Process Error!");
            aSession.setLoggedIn(false);
            rtn = PROCESS_OK;
            // log this error
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return rtn;
    }
}
