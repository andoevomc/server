package dreamgame.business;


import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.SuggestRequest;
import dreamgame.protocol.messages.SuggestResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class SuggestBusiness extends AbstractBusiness
{

    private static final Logger mLog = 
    	LoggerContext.getLoggerFactory().getLogger(SuggestBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg)
    {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "SuggestBusiness - handleMessage");
	}
        mLog.debug("[ SUGGEST ]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        SuggestResponse resSuggest = (SuggestResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try
        {
            SuggestRequest rqSuggest = (SuggestRequest) aReqMsg;
            long uid = rqSuggest.uid;
            String note = rqSuggest.note;
            mLog.debug("[ SUGGEST ]: of" + uid);
            DatabaseDriver.insertSuggestion(uid, note);
            resSuggest.setSuccess(ResponseCode.SUCCESS);
        } catch (Throwable t){
        	resSuggest.setFailure(ResponseCode.FAILURE, "Quá trình góp ý bị lỗi");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
        	if(resSuggest != null){
        		aResPkg.addMessage(resSuggest);
        	}
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
