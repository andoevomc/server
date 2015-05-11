package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class BuyAvatarRequest extends AbstractRequestMessage
{
    public int avatarID;
    public long uid;
    
    public boolean chargeCard=false;
    public boolean change_award=false;
    public boolean chargeAppleMoney=false;
    public boolean uploadAvatar=false;
    
    public int cardId=0;
    public String serial="";
    public String code="";
    public String data="";
    
    public IRequestMessage createNew()
    {
        return new BuyAvatarRequest();
    }
}
