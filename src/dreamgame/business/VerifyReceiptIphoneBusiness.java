/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dreamgame.business;

import dreamgame.config.DebugConfig;
import dreamgame.data.ChargeHistoryEntity;
import dreamgame.data.ResponseCode;
import dreamgame.databaseDriven.DatabaseDriver;
import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.protocol.AbstractBusiness;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponsePackage;
import dreamgame.gameserver.framework.protocol.MessageFactory;
import dreamgame.gameserver.framework.session.ISession;
import dreamgame.protocol.messages.GetUserDataRequest;
import dreamgame.protocol.messages.GetUserDataResponse;
import dreamgame.protocol.messages.VerifyReceiptIphoneRequest;
import dreamgame.protocol.messages.VerifyReceiptIphoneResponse;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

/**
 *
 * @author thohd
 */
public class VerifyReceiptIphoneBusiness extends AbstractBusiness {

    private static final Logger mLog =
            LoggerContext.getLoggerFactory().getLogger(VerifyReceiptIphoneBusiness.class);

    public int handleMessage(ISession aSession, IRequestMessage aReqMsg, IResponsePackage aResPkg) {
	int previousMethodCallLevel;
	if (DebugConfig.FOR_DEBUG) {
	    previousMethodCallLevel = DebugConfig.CALL_LEVEL;
	    DebugConfig.CALL_LEVEL ++;
	    DebugConfig.printMethodMsg(DebugConfig.CALL_LEVEL, "VerifyReceiptIphoneBusiness - handleMessage");
	}

        MessageFactory msgFactory = aSession.getMessageFactory();
        VerifyReceiptIphoneResponse res = (VerifyReceiptIphoneResponse) msgFactory.getResponseMessage(aReqMsg.getID());
        VerifyReceiptIphoneRequest rq = (VerifyReceiptIphoneRequest) aReqMsg;
        try {
	    JSONObject result = null;
	    // make request to apple server to confirm the receipt
	    try {
		mLog.debug("VerifyReceiptIphoneBusiness - receipt = {}", rq.receipt);
                
//		String encodedPurchasedData = Base64.encodeBase64String(rq.receipt.getBytes());                
//		String appleUrl = "https://sandbox.itunes.apple.com/verifyReceipt";     // sandbox mode
                
                String appleUrl = "https://buy.itunes.apple.com/verifyReceipt";       // production mode
                if (rq.isSandbox) {
                    appleUrl = "https://sandbox.itunes.apple.com/verifyReceipt";     // sandbox mode
                }

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(appleUrl);
                
                JSONObject requestReceiptData = new JSONObject();
                requestReceiptData.put("receipt-data", rq.receipt);
                httppost.setEntity(new StringEntity(requestReceiptData.toString()));

		//Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
		    InputStream instream = entity.getContent();
		    try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(instream, writer, "UTF-8");
			String content = writer.toString();
			mLog.debug("response from apple store: " + content);
			result = new JSONObject(content);
		    } finally {
			instream.close();
		    }
		}
		else 
		    mLog.debug("no response from apple store.");
	    } catch (Throwable t) {
		mLog.error("error: ", t);
		res.setFailure(ResponseCode.FAILURE, "Có lỗi kết nối với Apple Store, bạn vui lòng thử lại!");
		result = null;
	    }
	    
	    // update db if result is ok
	    if (result != null) {
                try {
                    int status = result.getInt("status");
                    if (status == 0) {
                        JSONObject receipt = result.getJSONObject("receipt");
                        String unique_identifier = receipt.getString("unique_identifier");
                        String transaction_id = receipt.getString("transaction_id");
                        int quantity = Integer.parseInt(receipt.getString("quantity"));
                        String product_id_s = receipt.getString("product_id");
                        int product_id = Integer.parseInt(product_id_s.replaceAll("com\\.gamesvn\\.gosol\\.thegioibai\\.itune", ""));
                        Date purchase_date = new Date(Long.parseLong(receipt.getString("purchase_date_ms")));
                        String username = aSession.getUserName();
                        int dbResult = DatabaseDriver.logItunesCharge(unique_identifier, transaction_id, purchase_date, quantity, product_id, username);
                        if (dbResult > 0) {
                            mLog.debug("success itunes charge: username = " + username + " ; game_money = " + dbResult);
                            res.setSuccess(ResponseCode.SUCCESS, product_id, dbResult);
                        }
                        else {
                            mLog.debug("db action failed");
                            res.setFailure(ResponseCode.FAILURE, "Order không thành công, bạn vui lòng thử lại!");
                        }
                    }
                    else {
                        mLog.debug("wrong receipt: status = " + status);
                        res.setFailure(ResponseCode.FAILURE, "Order không thành công, bạn vui lòng thử lại!");
                    }
                }
                catch (Exception e) {
                    mLog.debug("malformed response data from apple store !!!");
                    res.setFailure(ResponseCode.FAILURE, "Có lỗi dữ liệu với Apple Store, bạn vui lòng thử lại!");
                }
	    }
	    
        } 
	catch (Throwable t) {
            res.setFailure(ResponseCode.FAILURE, "Có lỗi xảy ra, bạn vui lòng thử lại!");
            mLog.error("Process message " + aReqMsg.getID() + " error.", t);
        } finally {
            if ((res != null)) {
                aResPkg.addMessage(res);
            }
        }
        if (DebugConfig.FOR_DEBUG) { DebugConfig.CALL_LEVEL = previousMethodCallLevel; }
        return 1;
    }
}