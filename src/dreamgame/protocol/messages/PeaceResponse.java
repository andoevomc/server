/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author Dinhpv
 */
public class PeaceResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public long uid;

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public void setSuccess(int aCode, long uid) {
        mCode = aCode;
        this.uid = uid;

    }

    public IResponseMessage createNew() {
        return new PeaceResponse();
    }
}
