/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.data.MessageOfflineEntity;
import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import java.util.Vector;

/**
 *
 * @author Dinhpv
 */
public class OfflineMessageResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public Vector<MessageOfflineEntity> mPostList;

    public void setSuccess(int aCode, Vector<MessageOfflineEntity> aPostList) {
        mCode = aCode;
        mPostList = aPostList;
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new OfflineMessageResponse();
    }
}
