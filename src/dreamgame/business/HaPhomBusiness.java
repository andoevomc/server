package dreamgame.business;

//import dreamgame.chan.data.ChanTable;
import dreamgame.config.DebugConfig;
import java.util.ArrayList;

import org.slf4j.Logger;
import phom.data.PhomTable;
import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.protocol.messages.HaPhomRequest;
import dreamgame.protocol.messages.HaPhomResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;

public class HaPhomBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(HaPhomBusiness.class);

    private  ArrayList<ArrayList<Integer>> getCards(String input) throws Exception {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "HaPhomBusiness - getCards: input = " + input);
	}
	
         ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>();
         String[] i1 = input.split(";");
         for(String i : i1){
              ArrayList<Integer> temp = new ArrayList<Integer>();
              String[] i2 = i.split("#");
              for(String j : i2){
                  temp.add(Integer.parseInt(j));
              }
              res.add(temp);
         }
         if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
         return res;
    }
    
    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) {
	
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "HaPhomBusiness - handleMessage");
	}
        mLog.debug("[Ha Phom]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        HaPhomResponse resHa =
                (HaPhomResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            HaPhomRequest rqHa = (HaPhomRequest) aReqMsg;
            long uid = aSession.getUID();

            if (rqHa.uid > 0) {
                uid = rqHa.uid;
            }

            long matchID = rqHa.matchID;
            ArrayList<ArrayList<Integer>> cards = rqHa.cards;
            resHa.cards = rqHa.cards1;
            resHa.id = (int)uid;

            Zone zone = aSession.findZone(aSession.getCurrentZone());
            Room room = zone.findRoom(matchID);

            PhomTable table = (PhomTable) room.getAttactmentData();

            if (!table.isPlaying) {
                mLog.error("Error Haphom. Khi da ket thuc van! " + table.turnInfo());
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }
            //Ha
            if (!table.checkHaPhom(uid, cards, rqHa.u, rqHa.card)) {
                resHa.setFailure(ResponseCode.FAILURE, "Phỏm không hợp lệ.");
                aSession.write(resHa);
                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;
            }else
            {
                if (cards.size()>0){
                    cards=getCards(table.phomStr1);
                    resHa.cards=table.phomStr1;
                }
            }            
            
            table.haPhom(uid, cards, rqHa.u, rqHa.card);
            resHa.guiBai=table.getGuiBai();
            
            resHa.setSuccess(ResponseCode.SUCCESS, rqHa.u, rqHa.card);
            room.broadcastMessage(resHa, aSession, true);

            if (rqHa.u == 1 || table.currentPlayer.uType > 0) { // U
                Thread.sleep(500);

                EndMatchResponse endMatchRes = (EndMatchResponse) msgFactory.getResponseMessage(MessagesID.MATCH_END);
                // set the result
                endMatchRes.setZoneID(ZoneID.PHOM);
                endMatchRes.uType = rqHa.u;
                
                System.out.println("table.forScores.size() : "+table.forScores.size());
                
                if (table.forScores.size() > 0) {
                    endMatchRes.setSuccess(ResponseCode.SUCCESS,
                            table.forScores, table.getWinner());
                } else {
                    endMatchRes.setSuccess(ResponseCode.SUCCESS,
                            table.getPlayings(), table.getWinner());
                }
                endMatchRes.matchId=matchID;

                room.broadcastMessage(endMatchRes, aSession,
                        true);
                room.setPlaying(false);
                table.resetPlayers();
                table.gameStop();
            }

        } catch (Throwable t) {
            HaPhomRequest rqHa = (HaPhomRequest) aReqMsg;
            resHa.setSuccess(ResponseCode.SUCCESS, rqHa.u, rqHa.card);
            Zone zone = aSession.findZone(aSession.getCurrentZone());
            long matchID = rqHa.matchID;
            Room room = zone.findRoom(matchID);
            room.broadcastMessage(resHa, aSession, true);

            t.printStackTrace();
            resHa.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra, bạn vui lòng thử lại!");
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
