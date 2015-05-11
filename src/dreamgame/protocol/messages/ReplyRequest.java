/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

/**
 *
 * @author binh_lethanh
 */
public class ReplyRequest extends AbstractRequestMessage
{

    public long mMatchId;
    public boolean mIsAccept;
    public long buddy_uid;
    public long uid;
    
    public IRequestMessage createNew()
    {
        return new ReplyRequest();
    }
}
