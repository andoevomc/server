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
public class GetUserDataRequest extends AbstractRequestMessage
{

    public int getImage=0;
    public int getUserAvatar=0;
    public boolean getChargeHistory=false;
    public int start=0;
    public int length=0;
    public int uid=0;
    
    public boolean chargeCard=false;
    public boolean chargeAppleMoney=false;
    
    public int cardId=0;
    public String serial="";
    public String code="";
    public String cp="";
    public IRequestMessage createNew()
    {
        return new GetUserDataRequest();
    }

}
