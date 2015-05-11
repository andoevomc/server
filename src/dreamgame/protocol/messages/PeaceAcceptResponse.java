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
public class PeaceAcceptResponse extends AbstractResponseMessage {

    public String message;
    public long ownerMoney;
    public long playerMoney;

    public void setSuccess(int aCode) {
        mCode = aCode;
    }

    public void setFailure(int aCode, String msg) {
        mCode = aCode;
        message = msg;
    }

    public void setMoneyEndMatch(long ownerMoney_, long playerMoney_) {
        ownerMoney = ownerMoney_;
        playerMoney = playerMoney_;
    }

    public IResponseMessage createNew() {
        return new PeaceAcceptResponse();
    }
}
