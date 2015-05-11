/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.config.DebugConfig;
import dreamgame.protocol.messages.LostResponse;
import dreamgame.protocol.messages.LostRequest;

import java.util.Vector;
import org.slf4j.Logger;
import dreamgame.data.ResponseCode;
import dreamgame.data.UserEntity;
import dreamgame.data.ZoneID;
import dreamgame.databaseDriven.DatabaseDriver;
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
 * @author Admin
 */
public class LostBusiness extends AbstractBusiness {

    private static final Logger mLog = LoggerContext.getLoggerFactory().getLogger(TimeOutBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "LostBusiness - handleMessage");
	}
        boolean isSuccess = false;
        MessageFactory msgFactory = aSession.getMessageFactory();
        LostResponse resLost = (LostResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        try {
            mLog.debug("[ SUGGEST ]: Catch");
            LostRequest rqLost = (LostRequest) aReqMsg;
            Zone bacayZone = aSession.findZone(aSession.getCurrentZone());
            Room currentRoom = bacayZone.findRoom(rqLost.mMatchId);
            UserEntity newUser = DatabaseDriver.getUserInfo(rqLost.player_friend_id);
            if (newUser != null) {
                if (currentRoom != null) {
                    switch (aSession.getCurrentZone()) {
                        case ZoneID.COTUONG: {
                            CoTuongTable currentTable = (CoTuongTable) currentRoom.getAttactmentData();

                            if (!currentTable.isPlaying) {
                                resLost.setFailure(ResponseCode.FAILURE,
                                        "Bạn đã thoát khỏi room");
                                if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                                return -1;
                            }
                            if (aSession.getUID() == currentTable.owner.id) {
                                currentTable.updateCash(false);
                                currentTable.setCurrPlayer(false);
                            } else {
                                currentTable.updateCash(true);
                                currentTable.setCurrPlayer(true);
                            }

                            resLost.setMoneyEndMatch(DatabaseDriver.getUserMoney(currentTable.owner.id), DatabaseDriver.getUserMoney(currentTable.player.id));

                            long player_friend_id = rqLost.player_friend_id;
//                            ISession playerFriendSession = aSession.getManager().findSession(player_friend_id);
                            resLost.setSuccess(ResponseCode.SUCCESS, player_friend_id, aSession.getUserName());
//                            playerFriendSession.write(resTimeOut);
                            currentRoom.broadcastMessage(resLost, aSession, true);
                            isSuccess = true;
                            currentRoom.setPlaying(false);
                            currentTable.mIsEnd = true;
                            currentTable.destroy();
                            if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
                            return 1;
                        }
                    }
                } else {
                    resLost.setFailure(ResponseCode.FAILURE,
                            "Bạn đã thoát khỏi room");
                }

            } else {
                resLost.setFailure(ResponseCode.FAILURE, "Không tìm thấy bạn chơi nữa!");
            }
        } catch (Throwable t) {
            resLost.setFailure(ResponseCode.FAILURE, "bị lỗi!");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if (resLost != null && !isSuccess) {
                aResPkg.addMessage(resLost);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}
