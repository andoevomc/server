/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONArray;

/**
 *
 * @author Dinhpv
 */
public class LotteryResponse extends AbstractResponseMessage {

    
    public int type;
    public String mErrorMsg;
    public JSONArray lotRes;

    public void setFailure(int aCode, String aErrorMsg)
    {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public void setSuccess(int aCode)
    {
        mCode = aCode;
    }

    public IResponseMessage createNew()
    {
        return new LotteryResponse();
    }
}
