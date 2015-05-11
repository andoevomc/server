package dreamgame.business;

//import dreamgame.chan.data.ChanTable;
import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import phom.data.PhomTable;
import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.AnPhomRequest;
import dreamgame.protocol.messages.AnPhomResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;

public class AnPhomBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(BocPhomBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, 
    		IResponsePackage aResPkg) {
    	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "AnPhomBusiness - handleMessage");
	}
        mLog.debug("[An Phom]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        AnPhomResponse resAn =
                (AnPhomResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            AnPhomRequest rqAn = (AnPhomRequest) aReqMsg;
            long uid = aSession.getUID();
            
//            if (rqAn.botUid>0) uid=rqAn.botUid;
                
            long matchID = rqAn.matchID;
            Zone zone = aSession.findZone(aSession.getCurrentZone());
            Room room = zone.findRoom(matchID);
            
            if (aSession.getCurrentZone()==ZoneID.GAME_CHAN){
                
//                ChanTable table = (ChanTable) room.getAttactmentData();            
//                //An
//                long money = table.eat(uid,rqAn.cardValue);
//                int i1=(int)table.getPrePlayerID(uid);
//                resAn.setSuccess(ResponseCode.SUCCESS,money,uid,i1);
//                resAn.prePlayer = table.playings.get(i1).id;
//                resAn.cardValue=rqAn.cardValue;
                
            }else{
            PhomTable table = (PhomTable) room.getAttactmentData();
            
            //An

            long money = table.eat(uid);
            resAn.cardValue=table.currPoker.toInt();
            
            int i1=(int)table.getPrePlayerID(uid);
            resAn.setSuccess(ResponseCode.SUCCESS,money,uid,i1);
            resAn.prePlayer = table.playings.get(i1).id;
             
            resAn.swap1=table.swap1;
            resAn.swap2=table.swap2;
            
            String phom1=table.getCurrentPhom();            
            resAn.phom=phom1;
            if (table.per_u_status)
            {                
                resAn.u=true;
            }
            
            if ( table.isHaBaiTurn() )
            {           
                resAn.phom=phom1;
                resAn.haBaiFlag=true;
            }
            }
            
            if (resAn!=null)
                room.broadcastMessage(resAn, aSession, true);
        } catch (Throwable t) {
            t.printStackTrace();
            resAn.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra!");
        } finally {
            
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
