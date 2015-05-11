/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.EnterZoneRequest;
import dreamgame.protocol.messages.EnterZoneResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

/**
 *
 * @author Dinhpv
 */
public class LogoutBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(ChatBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "LogoutBusiness - handleMessage");
	}
//        MessageFactory msgFactory = aSession.getMessageFactory();
        aSession.leaveAllRoom(aResPkg);
        aSession.sendFriendNotification(aSession.getUserName() + " thoát game!");
//        aSession.setUserName("");
//        long t = 0;
//        aSession.setUID(0L);
	aSession.close();
	System.out.println("No of user: " + aSession.getManager().numUser());
//        aSession.getJoinedRooms();
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
