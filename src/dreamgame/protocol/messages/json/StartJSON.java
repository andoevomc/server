package dreamgame.protocol.messages.json;

//import org.json.JSONArray;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

//import bacay.data.PlayerInMatch;
import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.StartRequest;
import dreamgame.protocol.messages.StartResponse;
import dreamgame.bacay.data.Poker;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class StartJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(StartJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // request messsage
            StartRequest matchStart = (StartRequest) aDecodingObj;
            // parsing
            matchStart.mMatchId = jsonData.getLong("match_id");

            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            StartResponse matchStart = (StartResponse) aResponseMessage;
            encodingObj.put("code", matchStart.mCode);
            if (matchStart.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", matchStart.mErrorMsg);
            } else if (matchStart.mCode == ResponseCode.SUCCESS) {
                //encodingObj.put("match_id", matchStart.mRoomId);
                /*
                PlayerInMatch roomOwner = matchStart.roomOwner;
                encodingObj.put("roomOwner_id", roomOwner.id);
                encodingObj.put("roomOwner_username", roomOwner.username);
                encodingObj.put("roomOwner_avatar", roomOwner.avatarID);
                encodingObj.put("roomOwner_cash", roomOwner.cash);
                encodingObj.put("roomOwner_level", roomOwner.level);
                 */
                switch (matchStart.zoneID) {
                    case ZoneID.BACAY: {
                        Poker first = matchStart.pokers[0];
                        Poker second = matchStart.pokers[1];
                        Poker third = matchStart.pokers[2];
                        encodingObj.put("firstPlayer_id", matchStart.firstPlayer.id);
                        JSONArray arrValues = new JSONArray();
                        JSONObject jCell = new JSONObject();
                        jCell.put("number", first.getNum());
                        jCell.put("type", first.pokerTypeToInt(first.getType()));
                        arrValues.put(jCell);
                        encodingObj.put("first_poker", arrValues);

                        arrValues = new JSONArray();
                        jCell = new JSONObject();
                        jCell.put("number", second.getNum());
                        jCell.put("type", second.pokerTypeToInt(second.getType()));
                        arrValues.put(jCell);
                        encodingObj.put("second_poker", arrValues);

                        arrValues = new JSONArray();
                        jCell = new JSONObject();
                        jCell.put("number", third.getNum());
                        jCell.put("type", third.pokerTypeToInt(third.getType()));
                        arrValues.put(jCell);
                        encodingObj.put("third_poker", arrValues);
                        break;
                    }
                    case ZoneID.OTT: {

                        break;
                    }
                    case ZoneID.PHOM: {

                        break;
                    }
                    default:
                        break;
                }
            }
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}

