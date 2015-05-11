package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class EnterZoneRequest extends AbstractRequestMessage
{
	public int zoneID;
        public int downloadID=0;
        public String mobileVersion="";
        
        public int zoneLevel=0;
        public int channelId=0;
        
    public IRequestMessage createNew()
    {
        return new EnterZoneRequest();
    }
}
