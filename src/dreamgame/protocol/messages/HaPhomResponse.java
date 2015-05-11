package dreamgame.protocol.messages;

import java.util.ArrayList;


import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import org.json.JSONArray;

public class HaPhomResponse extends AbstractResponseMessage {

    public String message;
    //public ArrayList<ArrayList<Integer>> cards;
    public int u;
    public int card;
    public String cards;
    public JSONArray guiBai = null;
    public int id=0;
//    public void setSuccess(int aCode, ArrayList<ArrayList<Integer>> cas, int U, int card)
//    {
//        mCode = aCode;
//        cards = cas;
//        u = U;
//        this.card = card;
//    }

    public void setSuccess(int aCode, int U, int card) {
        mCode = aCode;
        //cards = cas;
        u = U;
        this.card = card;
    }

    public void setFailure(int aCode, String msg) {
        mCode = aCode;
        message = msg;
    }

    public IResponseMessage createNew() {
        return new HaPhomResponse();
    }
}
