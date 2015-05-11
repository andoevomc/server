package dreamgame.protocol.messages;

import java.util.ArrayList;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class GuiPhomRequest extends AbstractRequestMessage {
	public long matchID;
	//public ArrayList<Integer> cards = new ArrayList<Integer>();;
	public long dUID;
	public int phomID;
        public String cards;
    public IRequestMessage createNew()
    {
        return new GuiPhomRequest();
    }
}
