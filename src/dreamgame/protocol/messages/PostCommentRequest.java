/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

/**
 *
 * @author Dinhpv
 */
public class PostCommentRequest extends AbstractRequestMessage {

    public String name;
    public String note;
    public int postID;

    public IRequestMessage createNew() {
        return new PostCommentRequest();
    }
}
