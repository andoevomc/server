package dreamgame.business;

import dreamgame.config.DebugConfig;
import java.util.Hashtable;

import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.GetRoomMoneyRequest;
import dreamgame.protocol.messages.GetRoomMoneyResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;

public class GetRoomMoneyBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(GetRoomMoneyBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
    	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GetRoomMoneyBusiness - handleMessage");
	}
        mLog.debug("[GET ROOM MONEY LIST]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        GetRoomMoneyResponse resGetRoomMoneyList = (GetRoomMoneyResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            GetRoomMoneyRequest rq = (GetRoomMoneyRequest) aReqMsg;
            
            long uid = aSession.getUID();
            mLog.debug("[GET ROOM MONEY LIST]:" + uid);

            if (rq.getHelp)
            {
                resGetRoomMoneyList.setSuccess(ResponseCode.SUCCESS,true);
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }

            Hashtable<Integer, Long>[] list = DatabaseDriver.getRoomMoneyList();
	    Hashtable<Integer, Long>[] list_poker = DatabaseDriver.getRoomPokerMoneyList();
            resGetRoomMoneyList.setSuccess(ResponseCode.SUCCESS, list, list_poker);


            /*for (int i=0;i<5;i++)
            {
                GetRoomMoneyResponse t1 = (GetRoomMoneyResponse) msgFactory.getResponseMessage(aReqMsg.getID());
                Hashtable<Integer, Long> list1= new Hashtable();
                list1.put(new Integer(i), (long)i);
                t1.setSuccess(ResponseCode.SUCCESS, list1);
                aSession.write(t1);
            }*/
            
        } catch (Throwable t) {
            resGetRoomMoneyList.setFailure(ResponseCode.FAILURE, "Không thể kết nối đến cơ sở dữ liệu ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((resGetRoomMoneyList != null) ) {
                aResPkg.addMessage(resGetRoomMoneyList);
            }
        }

        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
