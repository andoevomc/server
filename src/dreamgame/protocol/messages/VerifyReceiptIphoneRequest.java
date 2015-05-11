/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.protocol.messages;

import dreamgame.gameserver.framework.protocol.AbstractRequestMessage;
import dreamgame.gameserver.framework.protocol.IRequestMessage;

/**
 *
 * @author thohd
 */
public class VerifyReceiptIphoneRequest extends AbstractRequestMessage
{
    public String receipt = "";
    public boolean isSandbox = false;
    
    public IRequestMessage createNew()
    {
        return new VerifyReceiptIphoneRequest();
    }

}
