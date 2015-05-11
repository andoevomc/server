package dreamgame.gameserver.framework.room;

import dreamgame.data.SimpleTable;

public class RoomEntity
{
  public long mRoomId;
  public String mRoomName;
  public String mRoomOwnerName;
  public int mCapacity;
  public int mEnteringSize;
  public int mPlayingSize;
  public Object mAttactmentData;
  public String mPassword;
  public long moneyBet;
  public int channel;
  public int roomPosition;
  
  public boolean isFakeRoom;
  
//  public long getJoinMoney(){
//      return ((SimpleTable) mAttactmentData).getJoinMoney();
//  }
}