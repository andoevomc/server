package dreamgame.protocol.messages;

import java.util.ArrayList;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

public class HaPhomRequest extends AbstractRequestMessage {
	public int u; // 0 : Ko U, 1: U, 2: UKhan 
	public long matchID;
        public long uid=-1;
	// Khong U
	public ArrayList<ArrayList<Integer>> cards = new ArrayList<ArrayList<Integer>>();;
	public String cards1;
	//U - cay oanh di de U
	public int card;
    public IRequestMessage createNew()
    {
        return new HaPhomRequest();
    }

}
