package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

public class BuyLevelResponse extends AbstractResponseMessage {

    public String errMessage;
    public long new_cash;
    public int new_level;
    public long new_moneyForLevel;

    public void setFailure(int aCode, String aErrorMsg) {
        mCode = aCode;
        errMessage = aErrorMsg;
    }

    public void setSuccess(int aCode, long cash, int level, long moneyLevel) {
        mCode = aCode;
        new_cash = cash;
        new_level = level;
        new_moneyForLevel = moneyLevel;
    }

    public IResponseMessage createNew() {
        return new BuyLevelResponse();
    }
}
