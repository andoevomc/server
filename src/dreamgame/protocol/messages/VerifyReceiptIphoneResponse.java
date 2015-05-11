/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.data.ChargeHistoryEntity;
import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import java.util.Vector;

/**
 *
 * @author thohd
 */
public class VerifyReceiptIphoneResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public long cash=0;
    public int chargeID;

    public void setSuccess(int aCode, int aChargeID, long money)
    {
        mCode = aCode;
        chargeID = aChargeID;
        cash = money;
    }
    
    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new VerifyReceiptIphoneResponse();
    }
}