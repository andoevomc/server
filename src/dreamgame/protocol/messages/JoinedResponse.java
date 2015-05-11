/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractResponseMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;

/**
 *
 * @author binh_lethanh
 */
public class JoinedResponse extends AbstractResponseMessage {

    public long mUid;
    public int mType;
    public String username;
    public int level;
    public int avatar;
    public long cash;
    public int zoneID;
    public boolean isAn, isTaiGui;
    //Caro
    public boolean mIsPlayer;
    public boolean mIsStarting;
    public boolean mIsYourTurn;
    public int capacity;

    public void setCapacity(int c) {
        capacity = c;
    }

    public void setSuccess(int aCode, long aUid, boolean aIsPlayer, boolean aIsStarting, int zone) {
        mCode = aCode;
        mUid = aUid;
        mIsPlayer = aIsPlayer;
        mIsStarting = aIsStarting;
        zoneID = zone;
    }

    public void setValue(boolean aIsYourTurn, int aType) {
        mIsYourTurn = aIsYourTurn;
        mType = aType;
    }

    public void setPhomInfo(boolean isAn, boolean isTaiGui) {
        this.isAn = isAn;
        this.isTaiGui = isTaiGui;
    }

    public void setSuccess(int aCode, long aUid, String un, int l, int av, long money, int zone) {
        mCode = aCode;
        mUid = aUid;
        username = un;
        level = l;
        avatar = av;
        cash = money;
        zoneID = zone;
    }

    public void setValue(int aType) {
        mType = aType;
    }

    public IResponseMessage createNew() {
        return new JoinedResponse();
    }
}
