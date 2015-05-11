package dreamgame.business;

import dreamgame.config.DebugConfig;
import java.util.Vector;

import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.GetBestPlayerResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class GetBestPlayerBusiness extends AbstractBusiness
{

    private static final Logger mLog = 
    	LoggerContext.getLoggerFactory().getLogger(GetBestPlayerBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg)
    {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetBestPlayerBusiness - handleMessage");
	}
        // process's status
        mLog.debug("[GET BEST PLAYER]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        GetBestPlayerResponse resGetBestPlayer = (GetBestPlayerResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try
        {
            long uid = aSession.getUID();
            mLog.debug("[GET BEST PLAYER]: for" + uid);
            Vector<UserEntity> richests = DatabaseDriver.getBestPlayer();
            resGetBestPlayer.setSuccess(ResponseCode.SUCCESS, richests);
        } catch (Throwable t) {
            resGetBestPlayer.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cơ sở dữ liệu ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resGetBestPlayer != null)) {
                aResPkg.addMessage(resGetBestPlayer);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
