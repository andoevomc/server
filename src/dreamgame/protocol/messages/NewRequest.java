package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

/**
 *
 * @author binh_lethanh
 */
public class NewRequest extends AbstractRequestMessage {

    public long mid;
    public long moneyBet;
    public int roomType;
    public long uid;
    public String password;
    public int zise;
    public String roomName;
    public int roomPosition=0;
    //Caro
    public int mRow;
    public int mCol;
    //Phom
    public boolean isKhan = false;
    public boolean isAn = false;
    public boolean isTai = false;
    public int testCode = 0;
//Cờ tướng
    public int available = 0;//chấp quân gì
    public int totalTime = 0;//tong thoi gian tran dau

    public IRequestMessage createNew() {
        return new NewRequest();
    }
}
