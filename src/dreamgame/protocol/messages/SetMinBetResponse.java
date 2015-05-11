/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author Thomc
 */
public class SetMinBetResponse extends AbstractResponseMessage {

    public String errMgs;
    public long moneyBet;

    public IResponseMessage createNew() {
        return new TimeOutResponse();
    }

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        errMgs = aErrorMsg;
    }

    public void setSuccess(int aCode, long moneyBet_) {
        mCode = aCode;
        this.moneyBet = moneyBet_;
    }
}
