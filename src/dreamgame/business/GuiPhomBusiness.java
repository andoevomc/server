package dreamgame.business;

import dreamgame.config.DebugConfig;
import java.util.ArrayList;

import org.slf4j.Logger;

import phom.data.PhomTable;
import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.protocol.messages.GuiPhomRequest;
import dreamgame.protocol.messages.GuiPhomResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;

public class GuiPhomBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(GuiPhomBusiness.class);
            public ArrayList<Integer> stringToList(String str){
                ArrayList<Integer> res = new ArrayList<Integer>();
                String[] cards1=str.split("#");
                for (int i=0;i<cards1.length;i++)
                    res.add(Integer.parseInt(cards1[i]));
                
                return res;
            }
	    
    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, 
    		IResponsePackage aResPkg) {
    	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "GuiPhomBusiness - handleMessage");
	}
	
        mLog.debug("[Gui Phom]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        GuiPhomResponse resGui =
                (GuiPhomResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            GuiPhomRequest rqGui = (GuiPhomRequest) aReqMsg;
            long uid = aSession.getUID();
            long matchID = rqGui.matchID;
            ArrayList<Integer> cards = stringToList(rqGui.cards);
            
            Zone zone = aSession.findZone(aSession.getCurrentZone());
            Room room = zone.findRoom(matchID);
            
            PhomTable table = (PhomTable) room.getAttactmentData();
            
            //Gui
            if (!table.checkGui(uid, cards))
            {
                resGui.setFailure(ResponseCode.FAILURE, "Quân bài này không tồn tại!");
                aSession.write(resGui);
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }
            
            if(table.gui(uid, cards, rqGui.dUID, rqGui.phomID)){ // U gui
                resGui.cards = rqGui.cards;
            	resGui.setSuccess(ResponseCode.SUCCESS, rqGui.dUID, uid, rqGui.phomID);
            	room.broadcastMessage(resGui, aSession, true);
            	
            	EndMatchResponse endMatchRes = (EndMatchResponse) 
            				msgFactory.getResponseMessage(MessagesID.MATCH_END);
                // set the result
                endMatchRes.setZoneID(ZoneID.PHOM);
                endMatchRes.matchId = table.matchID;
		
                if (table.forScores.size() > 0) {
                                endMatchRes.setSuccess(ResponseCode.SUCCESS,
                                        table.forScores, table.getWinner());
                            } else {
                                endMatchRes.setSuccess(ResponseCode.SUCCESS,
                                        table.getPlayings(), table.getWinner());
                            }
                
                endMatchRes.uType=3;
                room.broadcastMessage(endMatchRes, aSession, true);
                room.setPlaying(false);

                table.resetPlayers();
                table.gameStop();                
            }else {
                String phom1=table.getCurrentPhom();
            
            if (table.per_u_status)
            {
                resGui.phom=phom1;
                resGui.u=true;
            }
            
                resGui.cards = rqGui.cards;
            	resGui.setSuccess(ResponseCode.SUCCESS, rqGui.dUID, uid, rqGui.phomID);
            	room.broadcastMessage(resGui, aSession, true);
            }

            Thread.sleep(500);
            
        } catch (Throwable t) {
            t.printStackTrace();
            resGui.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra, bạn vui lòng thử lại!");
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
