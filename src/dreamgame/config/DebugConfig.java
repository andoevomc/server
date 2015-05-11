/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.config;

/**
 *
 * @author Administrator
 */
public class DebugConfig {
    //TODO: (for release) change this for release
//    public static final boolean FOR_DEBUG = true;
    public static final boolean FOR_DEBUG = false;
    
    public static int CALL_LEVEL = 0;
    
    // print method entry message based on level given
    public static void printMethodMsg(int level, String msg) {
	System.out.print(level);
	for (int i = 0; i < level; i++) {
	    System.out.print("--");
	}
	System.out.print(" ");
	System.out.println(msg);
    }
}
