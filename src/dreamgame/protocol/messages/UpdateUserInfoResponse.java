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
public class UpdateUserInfoResponse extends AbstractResponseMessage
{

    public String mErrorMsg;
    public void setSuccess(int aCode)
    {
        mCode = aCode;
    }
    
    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew()
    {
        return new UpdateUserInfoResponse();
    }
}
