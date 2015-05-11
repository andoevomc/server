package dreamgame.business;

import dreamgame.chan.data.ChanPoker;
//import dreamgame.chan.data.ChanTable;
import dreamgame.config.DebugConfig;
import org.slf4j.Logger;

import phom.data.PhomTable;
import phom.data.Poker;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.BocPhomRequest;
import dreamgame.protocol.messages.BocPhomResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import phom.data.PhomPlayer;

public class BocPhomBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(BocPhomBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "BocPhomBusiness - handleMessage");
	}
        mLog.debug("[Boc Phom]: Catch");
        MessageFactory msgFactory = aSession.getMessageFactory();
        BocPhomResponse resBoc =
                (BocPhomResponse) msgFactory.getResponseMessage(aReqMsg.getID());

        boolean sent = false;
        try {
            BocPhomRequest rqBoc = (BocPhomRequest) aReqMsg;

            long uid = aSession.getUID();


            System.out.println("uid = " + uid + " ;  rqBoc.uid : " + rqBoc.uid);

            if (rqBoc.uid > -1) {
                uid = rqBoc.uid;
            }

            long matchID = rqBoc.matchID;
            resBoc.uid = uid;

            Zone zone = aSession.findZone(aSession.getCurrentZone());
            Room room = zone.findRoom(matchID);

            if (aSession.getCurrentZone() == ZoneID.GAME_CHAN) {

//                ChanTable table = (ChanTable) room.getAttactmentData();
//                ChanPoker p = table.getCard(uid);
//                resBoc.setSuccess(ResponseCode.SUCCESS, p.toInt());
//
//                room.broadcastMessage(resBoc, aSession, true);
//                sent = true;
//                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                return 1;

            } else if (aSession.getCurrentZone() == ZoneID.PHOM) {
                PhomTable table = (PhomTable) room.getAttactmentData();

                if (table.restCards.size() <= 0 || table.isPlaying == false) {
                    System.out.println("Error! Khong con card de boc!");
                    if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                    return 1;
                }

                //Boc
                Poker p = table.getCard(uid);
                resBoc.setSuccess(ResponseCode.SUCCESS, p.toInt());

                String phom1 = table.getCurrentPhom();

                resBoc.phom = phom1;
                if (table.per_u_status) {
                    resBoc.u = true;
                }

                if (table.isHaBaiTurn()) {
                    resBoc.phom = phom1;
                    resBoc.haBaiFlag = true;
                }

                System.out.println("Boc Card: " + p.toString());

                BocPhomResponse broadcast =
                        (BocPhomResponse) msgFactory.getResponseMessage(aReqMsg.getID());

                broadcast.setSuccess(ResponseCode.SUCCESS);
                broadcast.uid=aSession.getUID();
                if (rqBoc.uid > -1) {
                    broadcast.uid=rqBoc.uid;
                    //autoplay                                
                    if (!table.currentPlayer.isAutoPlay) {
                        room.broadcastMessage(broadcast, aSession.getManager().findSession(rqBoc.uid), false);
                        ISession as = aSession.getManager().findSession(rqBoc.uid);
                        as.write(resBoc);
                    } else {
                        room.broadcastMessage(broadcast, aSession, true);
                    }
                    resBoc = null;
                } else {
                    room.broadcastMessage(broadcast, aSession, false);
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
            resBoc.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra!");
        } finally {
            if ((resBoc != null && !sent)) {
                aResPkg.addMessage(resBoc);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
