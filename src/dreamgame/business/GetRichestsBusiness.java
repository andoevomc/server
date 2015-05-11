package dreamgame.business;

import dreamgame.config.DebugConfig;
import java.util.Vector;

import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.GetRichestsResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class GetRichestsBusiness extends AbstractBusiness
{

    private static final Logger mLog = 
    	LoggerContext.getLoggerFactory().getLogger(GetRichestsBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg)
    {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetRichestsBusiness - handleMessage");
	}
        mLog.debug("[GET RICHEST]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        GetRichestsResponse resGetRichestList = (GetRichestsResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try
        {
            long uid = aSession.getUID();
            mLog.debug("[GET RICHEST]: for" + uid);
            Vector<UserEntity> richests = DatabaseDriver.getRichests();
            resGetRichestList.setSuccess(ResponseCode.SUCCESS, richests);
        } catch (Throwable t){
            resGetRichestList.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cơ sở dữ liệu ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally{
            if ((resGetRichestList != null) ){
                aResPkg.addMessage(resGetRichestList);
            }
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
