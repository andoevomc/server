package dreamgame.protocol.messages.json;

import dreamgame.data.PostEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.GetPostListRequest;
import dreamgame.protocol.messages.GetPostListResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class GetPostListJSON implements IMessageProtocol {

    private final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(GetPostListJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            JSONObject jsonData = (JSONObject) aEncodedObj;
            GetPostListRequest rq = (GetPostListRequest) aDecodingObj;
            if (jsonData.has("start")) {
                rq.start = jsonData.getInt("start");
            }
            if (jsonData.has("len")) {
                rq.length = jsonData.getInt("len");
            }
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
            GetPostListResponse getStatusList = (GetPostListResponse) aResponseMessage;
            encodingObj.put("code", getStatusList.mCode);
            if (getStatusList.mCode == ResponseCode.FAILURE) {
            } else if (getStatusList.mCode == ResponseCode.SUCCESS) {
                JSONArray arrRooms = new JSONArray();
                if (getStatusList.mPostList != null) {
                    for (PostEntity postEntity : getStatusList.mPostList) {
                        JSONObject jRoom = new JSONObject();
                        jRoom.put("postID", postEntity.postID);
                        jRoom.put("title", postEntity.title);
                        jRoom.put("avatar", postEntity.avatarID);
                        jRoom.put("content", postEntity.content);
                        jRoom.put("date", postEntity.postDate);
                        jRoom.put("isNew", postEntity.isNewComment);
                        arrRooms.put(jRoom);
                    }
                }
                encodingObj.put("post_list", arrRooms);
            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
