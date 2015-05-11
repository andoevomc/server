package dreamgame.protocol.messages.json;

import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.ReadyRequest;
import dreamgame.protocol.messages.ReadyResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONArray;
import phom.data.PhomPlayer;

public class ReadyJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(ReadyJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // request messsage
            ReadyRequest matchReady = (ReadyRequest) aDecodingObj;
            // parsing
            matchReady.matchID = jsonData.getLong("match_id");
            matchReady.uid = jsonData.getLong("uid");

            
            return true;
        } catch (Throwable t) {
            mLog.error("[DECODER] " + aDecodingObj.getID(), t);
            return false;
        }
    }

    public void addPhomData(ReadyResponse matchJoin, JSONObject jCell, PhomPlayer player) {
        try {
            jCell.put("id", player.id);
            jCell.put("username", player.username);
            jCell.put("level", player.level);
            jCell.put("avatar", player.avatarID);
            jCell.put("money", player.cash);
            jCell.put("isReady", player.isReady);
            jCell.put("isAuto", player.isAutoPlay );
        } catch (Exception e) {
        }

    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();

            encodingObj.put("mid", aResponseMessage.getID());

            ReadyResponse matchReady = (ReadyResponse) aResponseMessage;

            encodingObj.put("code", matchReady.mCode);
            encodingObj.put("uid", matchReady.mUid);

            if (matchReady.mCode == ResponseCode.FAILURE) {
                encodingObj.put("error_msg", matchReady.mErrorMsg);

            } else if (matchReady.mCode == ResponseCode.SUCCESS) {

                if (matchReady.mPlayerPhom!=null)
                {
                        JSONArray arrValues = new JSONArray();

                        for (PhomPlayer player : matchReady.mPlayerPhom)
                        {
                            JSONObject jCell = new JSONObject();
                            addPhomData(matchReady, jCell, player);
                            jCell.put("isObserve", false);
                            arrValues.put(jCell);
                        }

                        for (PhomPlayer player : matchReady.mWaitingPlayerPhom) {
                            JSONObject jCell = new JSONObject();
                            addPhomData(matchReady, jCell, player);
                            jCell.put("isObserve", true);
                            arrValues.put(jCell);
                        }
                        encodingObj.put("table_values", arrValues);
                }
            }

            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
