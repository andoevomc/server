package dreamgame.business;

import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.EnterZoneRequest;
import dreamgame.protocol.messages.EnterZoneResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class EnterZoneBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(ChatBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "EnterZoneBusiness - handleMessage");
	}
	
        MessageFactory msgFactory = aSession.getMessageFactory();
        EnterZoneResponse resEnter = (EnterZoneResponse) msgFactory.getResponseMessage(aReqMsg.getID());

        mLog.debug("[ENTER ZONE]: Catch");
        String zoneName = "#";
        try {
            EnterZoneRequest rqEnter = (EnterZoneRequest) aReqMsg;
            int zoneID = rqEnter.zoneID;

            if (zoneID>0)
            {
                resEnter.zoneId=zoneID;
            }
            
            if (rqEnter.zoneLevel==2 && rqEnter.channelId>0)
            {
                
                resEnter.setSuccess(ResponseCode.SUCCESS);
                resEnter.channelId = rqEnter.channelId;
                aSession.setChannel(rqEnter.channelId);
                resEnter.maxRoom = 50;
                //aSession.write(resEnter);
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }

            zoneName = ZoneID.getZoneName(zoneID);
            mLog.debug("Zone name = " + zoneName);
            aSession.setCurrentZone(zoneID);
            resEnter.setSuccess(ResponseCode.SUCCESS);
            DatabaseDriver.updateUserZone(aSession.getUID(), zoneID);
                      
        } catch (Throwable t) {
            resEnter.setFailure(ResponseCode.FAILURE, "Hiện tại không vào được game " + zoneName + " này!");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
            aResPkg.addMessage(resEnter);
        } finally {
            aResPkg.addMessage(resEnter);
        }
        
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
