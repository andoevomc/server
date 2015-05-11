package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class RegisterRequest extends AbstractRequestMessage
{

    public String mUsername;
    public String mPassword;
    public int mAge;
    public boolean isMale;
    public String mail;
    public String phone;
    public String cp;
    public String clientType="";
    
    public String mobileVersion="";
    public int downloadid=0;
    
    public IRequestMessage createNew()
    {
        return new RegisterRequest();
    }
}
