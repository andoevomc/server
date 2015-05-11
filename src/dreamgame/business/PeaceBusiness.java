/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.session.ISession;

import org.slf4j.Logger;


import java.util.Hashtable;

import dreamgame.data.MessagesID;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.protocol.messages.JoinPlayerResponse;
import dreamgame.protocol.messages.OutResponse;
import dreamgame.protocol.messages.PeaceRequest;
import dreamgame.protocol.messages.PeaceResponse;
import dreamgame.protocol.messages.TurnRequest;
import dreamgame.protocol.messages.TurnResponse;
import dreamgame.bacay.data.BacayTable;
import dreamgame.bacay.data.BacayPlayer;
import dreamgame.bacay.data.Poker;

import dreamgame.config.DebugConfig;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.cotuong.data.CoTuongTable;
import dreamgame.cotuong.data.moveFromTo;
//import dreamgame.oantuti.data.OantutiTable;
import org.slf4j.Logger;

import phom.data.PhomTable;
import phom.data.Utils;
import dreamgame.tienlen.data.TienLenTable;

/**
 *
 * @author Dinhpv
 */
public class PeaceBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(PeaceBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PeaceBusiness - handleMessage");
	}
        MessageFactory msgFactory = aSession.getMessageFactory();
        PeaceResponse resMatchTurn = (PeaceResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            PeaceRequest rqMatchTurn = (PeaceRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room currentRoom = bacayZone.findRoom(rqMatchTurn.mMatchId);
            if (currentRoom != null) {
                switch (aSession.getCurrentZone()) {
                    case ZoneID.COTUONG: {
                        CoTuongTable currentTable = (CoTuongTable) currentRoom.getAttactmentData();
                        if (!currentTable.isPlaying) {
                            resMatchTurn.setFailure(ResponseCode.FAILURE,
                                    "Bạn đã thoát khỏi room");
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return -1;
                        }
                        long uid = rqMatchTurn.uid;
                        ISession ownerSession = aSession.getManager().findSession(uid);
                        resMatchTurn.setSuccess(ResponseCode.SUCCESS, uid);
                        ownerSession.write(resMatchTurn);
                        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                        return 1;
                    }
                }
            }
        } catch (Throwable t) {
            resMatchTurn.setFailure(ResponseCode.FAILURE, "Bị lỗi ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
//            if ((resMatchTurn != null)) {
//                aResPkg.addMessage(resMatchTurn);
//            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
