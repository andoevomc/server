package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class LoginRequest extends AbstractRequestMessage
{    
   
    public String mUsername;
    public String mPassword;
    public String mobileVersion="";
    public String flashVersion="";
    public String screen="";
    public String device="";
    public String cp="0";
    public String clientType="";
    public boolean updateLoginMessage=false;
    
    public int downloadid=0;
    
    public boolean shutDown= false;

    public IRequestMessage createNew()
    {
        return new LoginRequest();
    }

}
