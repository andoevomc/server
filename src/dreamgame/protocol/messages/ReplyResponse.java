/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author binh_lethanh
 */
public class ReplyResponse extends AbstractResponseMessage
{

    public String mErrorMsg;
    public boolean mIsAccept;
    public long source_uid; // who are accepted
    public String username;

    public void setSuccess(int aCode, boolean aIsAccept, long source, String name)
    {
        mCode = aCode;
        mIsAccept = aIsAccept;
        source_uid = source;
        username = name;
    }

    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew()
    {
        return new ReplyResponse();
    }
}
