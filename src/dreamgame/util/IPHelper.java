/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Administrator
 */
public class IPHelper {
    public static String parseIPFromString(String ipStr) {
	Pattern pattern = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
	Matcher matcher;
	String IP = "";
	matcher = pattern.matcher(ipStr);
	while (matcher.find()) {
	    IP = matcher.group();
	}
	return IP;
    }
}
