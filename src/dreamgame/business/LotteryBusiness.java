/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.business;

import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.EnterZoneRequest;
import dreamgame.protocol.messages.EnterZoneResponse;
import dreamgame.protocol.messages.LotteryRequest;
import dreamgame.protocol.messages.LotteryResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.db.DatabaseManager;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

/**
 *
 * @author Dinhpv
 */
public class LotteryBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(LotteryBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "LotteryBusiness - handleMessage");
	}
//        MessageFactory msgFactory = aSession.getMessageFactory();
        MessageFactory msgFactory = aSession.getMessageFactory();
        LotteryRequest rqLottery = (LotteryRequest) aReqMsg;
        LotteryResponse resLottery = (LotteryResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        
        if (rqLottery.type==1){ //hien ket qua so xo
            resLottery.mCode=1;
            resLottery.lotRes=DatabaseDriver.getLotery(rqLottery.date);
        }        
        
        if (resLottery!=null)
            aResPkg.addMessage(resLottery);
        
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}

