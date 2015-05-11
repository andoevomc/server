/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.performant;

import java.util.Vector;

/**
 *
 * @author Root
 */
public interface INetworkListener
{

    public void onEvent(int aEvent);
    
    @SuppressWarnings("unchecked")
    public void onResponse(Vector aResponses);
}
