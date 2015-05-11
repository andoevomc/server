/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages.json;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.JoinedResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author binh_lethanh
 */
public class JoinedJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(JoinedJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            JoinedResponse matchJoined = (JoinedResponse) aResponseMessage;
            encodingObj.put("code", matchJoined.mCode);
//            System.out.println("VAI LUA CHAM COM CHAM VI EN ZONE AYDI:" + matchJoined.zoneID);
            if (matchJoined.mCode == ResponseCode.FAILURE) {
            } else if (matchJoined.mCode == ResponseCode.SUCCESS) {
                switch (matchJoined.zoneID) {
                    case ZoneID.CARO: {
                        encodingObj.put("uid", matchJoined.mUid);
                        encodingObj.put("is_player", matchJoined.mIsPlayer);
                        encodingObj.put("is_starting", matchJoined.mIsStarting);
                        if (matchJoined.mIsStarting && matchJoined.mIsPlayer) {
                            encodingObj.put("is_your_turn", matchJoined.mIsYourTurn);
                            encodingObj.put("type", matchJoined.mType);
                        }
                        break;
                    }
                    case ZoneID.PHOM:
                    case ZoneID.BACAY:
                    case ZoneID.TIENLEN_MB:
                    case ZoneID.TIENLEN_DEMLA:
                    case ZoneID.TIENLEN:
                    case ZoneID.POKER:
                    case ZoneID.XITO:
                    case ZoneID.GAME_CHAN:
                    case ZoneID.OTT:
                    case ZoneID.BAUCUA: {
                        encodingObj.put("uid", matchJoined.mUid);
                        encodingObj.put("username", matchJoined.username);
                        encodingObj.put("level", matchJoined.level);
                        encodingObj.put("avatar", matchJoined.avatar);
                        encodingObj.put("cash", matchJoined.cash);
                        break;
                    }
                    case ZoneID.COTUONG: {
                        encodingObj.put("uid", matchJoined.mUid);
                        encodingObj.put("username", matchJoined.username);
                        encodingObj.put("level", matchJoined.level);
                        encodingObj.put("avatar", matchJoined.avatar);
                        encodingObj.put("cash", matchJoined.cash);
                        break;
                    }
                    case ZoneID.MAUBINH: {
                        encodingObj.put("uid", matchJoined.mUid);
                        encodingObj.put("username", matchJoined.username);
                        encodingObj.put("level", matchJoined.level);
                        encodingObj.put("avatar", matchJoined.avatar);
                        encodingObj.put("cash", matchJoined.cash);
                        break;
                    }
                    default:
                        break;
                }


            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
