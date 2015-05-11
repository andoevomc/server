/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.config.DebugConfig;
import java.util.Vector;

import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.protocol.messages.AcceptJoinPlayerRequest;
import dreamgame.protocol.messages.AcceptJoinPlayerResponse;
import dreamgame.protocol.messages.EndMatchResponse;
import dreamgame.protocol.messages.PeaceAcceptRequest;
import dreamgame.protocol.messages.PeaceAcceptResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.room.Room;
import dreamgame.gameserver.framework.room.Zone;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.cotuong.data.CoTuongPlayer;
import dreamgame.cotuong.data.CoTuongTable;

/**
 *
 * @author Dinhpv
 */
public class PeaceAcceptBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(PeaceAcceptBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg,
            IResponsePackage aResPkg) throws ServerException {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "PeaceAcceptBusiness - handleMessage");
	}
        boolean isSuccess = false;
        String username = null;
        MessageFactory msgFactory = aSession.getMessageFactory();
        PeaceAcceptResponse resAcceptJoin = (PeaceAcceptResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            mLog.debug("[ACCEPT JOIN PLAYER ROOM]: Catch");
            PeaceAcceptRequest rqAcceptJoin = (PeaceAcceptRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room currentRoom = bacayZone.findRoom(rqAcceptJoin.mMatchId);
            UserEntity newUser = DatabaseDriver.getUserInfo(rqAcceptJoin.uid);
            if (newUser != null) {
                long moneyOfPlayer = newUser.money;
                long uid = newUser.mUid;
                username = newUser.mUsername;
                if (currentRoom != null) {
                    switch (aSession.getCurrentZone()) {
                        case ZoneID.COTUONG: {
                            ISession playerSession = currentRoom.getSessionByID(uid);
                            if (rqAcceptJoin.isAccept) {
                                CoTuongTable currentTable = (CoTuongTable) currentRoom.getAttactmentData();
                                currentTable.mIsEnd = true;
//                                currentTable.updatePeaceCash();
                                resAcceptJoin.setMoneyEndMatch(DatabaseDriver.getUserMoney(currentTable.owner.id), DatabaseDriver.getUserMoney(currentTable.player.id));

//                                currentTable.resetBoard();
                                resAcceptJoin.setSuccess(
                                        ResponseCode.SUCCESS);
                                currentRoom.broadcastMessage(resAcceptJoin, aSession, true);
                                currentTable.setCurrPlayer(true);
                                currentTable.destroy();
                                isSuccess = true;
                            } else {
                                resAcceptJoin.setFailure(
                                        ResponseCode.FAILURE,
                                        "Yêu cầu của bạn bị từ chối!");
                                playerSession.write(resAcceptJoin);
                                isSuccess = true;
                            }
                            break;
                        }
                        default:
                            break;
                    }

                } else {
                    resAcceptJoin.setFailure(ResponseCode.FAILURE,
                            "Bạn đã thoát khỏi room");
                }
            } else {
                resAcceptJoin.setFailure(ResponseCode.FAILURE, username
                        + " không tồn tại");
            }
        } catch (Throwable t) {
            resAcceptJoin.setFailure(ResponseCode.FAILURE, "Bị lỗi ");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if (resAcceptJoin != null) {
                if (!isSuccess) {
                    aResPkg.addMessage(resAcceptJoin);
                }
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
