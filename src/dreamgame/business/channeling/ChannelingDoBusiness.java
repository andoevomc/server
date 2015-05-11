/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business.channeling;

import dreamgame.data.UserEntity;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.util.codec.md5.MD5;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author Administrator
 */
public abstract class ChannelingDoBusiness {
    public abstract String doLogin(ISession aSession, UserEntity user, String username, String password);
    public abstract String doRegister(ISession aSession, String username, String password);
    public abstract String doCardCharge(ISession aSession, UserEntity user, String cardNumber, String cardSerial, String telco);
    
    // do a http post
    public String doHTTPPostRequest(String url, HashMap<String,String> parameters) throws Exception {
	HttpClient httpclient = new DefaultHttpClient();
	HttpPost httppost = new HttpPost(url);

	// Add your data
	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	for (String key : parameters.keySet()) {
	    String value = parameters.get(key);
	    nameValuePairs.add(new BasicNameValuePair(key, value));
	}
	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	//Execute and get the response.
	HttpResponse response = httpclient.execute(httppost);
	HttpEntity entity = response.getEntity();

	if (entity != null) {
	    InputStream instream = entity.getContent();
	    try {
		StringWriter writer = new StringWriter();
		IOUtils.copy(instream, writer, "UTF-8");
		return writer.toString();
	    } finally {
		instream.close();
	    }
	}
	else {
	    throw new Exception("no response from apple store.");
	}
    }
    
    // md5 a string
    public String md5(String str) {
	try {
	    return MD5.md5Hex(str);
	} catch (Exception ex) {
	    return null;
	}
    }
    
    // check if a string is valid md5 hash
    public boolean isValidMD5(String s) {
       return s.matches("[a-fA-F0-9]{32}");
    }
}
