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
public class GetUserDataResponse extends AbstractResponseMessage {

    public String mErrorMsg;
    public String imageData="";
    public boolean getImage=false;
    public int uid;
    public boolean getHistroy=false;
    
    public boolean chargeAppleMoney=false;
    public long new_cash;
    
    public Vector<ChargeHistoryEntity>  mChargeHistory;
    public long newMoney=0;

    public boolean chargeMoney=false;
    public String chargeMsg="";
    
    public void setSuccess(int aCode,String msg,long money)
    {
        mCode = aCode;
        chargeMsg = msg;
        newMoney = money;

    }
    
    public void setChargeSuccess(int aCode, Vector<ChargeHistoryEntity>  ac)
    {
        mCode = aCode;
        mChargeHistory = ac;
        getHistroy=true;
    }
    
    public void setSuccess(int aCode) {
        mCode = aCode;        
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        mErrorMsg = aErrorMsg;
    }

    public IResponseMessage createNew() {
        return new GetUserDataResponse();
    }
}