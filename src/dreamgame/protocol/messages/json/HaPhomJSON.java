package dreamgame.protocol.messages.json;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;


import dreamgame.data.ResponseCode;
import dreamgame.protocol.messages.HaPhomRequest;
import dreamgame.protocol.messages.HaPhomResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;



public class HaPhomJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(HaPhomJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        try {
            // request data
            JSONObject jsonData = (JSONObject) aEncodedObj;
            // cancel request message
            HaPhomRequest ha = (HaPhomRequest) aDecodingObj;

            ha.matchID = jsonData.getLong("match_id");
            ha.u = jsonData.getInt("u");
            String temp = jsonData.getString("cards");
            ha.cards1 = temp;
            
            if (ha.u == 0) {
                /*JSONArray cardsJSON = jsonData.getJSONArray("cards");
                for (int i = 0; i < cardsJSON.length(); i++) {
                    JSONArray phomJSON = cardsJSON.getJSONArray(i);
                    ArrayList<Integer> phom = new ArrayList<Integer>();
                    for (int j = 0; j < phomJSON.length(); j++) {
                        JSONObject c = phomJSON.getJSONObject(j);
                        phom.add(c.getInt("card"));
                    }
                    ha.cards.add(phom);

                }*/
                
                try{
                    ha.cards = getCards(temp);
                }catch(Exception e){

                }

            } else {
                ha.card = jsonData.getInt("card");
                ha.cards = getCards(temp);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private  ArrayList<ArrayList<Integer>> getCards(String input) throws Exception {
         ArrayList<ArrayList<Integer>> res = new ArrayList<ArrayList<Integer>>();
         String[] i1 = input.split(";");
         for(String i : i1){
              ArrayList<Integer> temp = new ArrayList<Integer>();
              String[] i2 = i.split("#");
              for(String j : i2){
                  temp.add(Integer.parseInt(j));
              }
              res.add(temp);
         }
         return res;
    }
    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            HaPhomResponse ha = (HaPhomResponse) aResponseMessage;
            encodingObj.put("code", ha.mCode);
            if (ha.mCode == ResponseCode.SUCCESS) {
                
                encodingObj.put("U", ha.u);
                encodingObj.put("uid", ha.id);
                
                if (ha.u == 1) {
                    encodingObj.put("card", ha.card);
                }
                //JSONObject phomsJSON = new JSONObject();
//                for (int i = 0; i < ha.cards.size(); i++) {
//                    ArrayList<Integer> phom = ha.cards.get(i);
//                    JSONArray phomJSON = new JSONArray();
//                    for (int c : phom) {
//                        JSONObject obj = new JSONObject();
//                        obj.put("card", c);
//                        phomJSON.put(obj);
//                    }
//                    phomsJSON.put(phomJSON);
//                }
                encodingObj.put("phoms", ha.cards);
                encodingObj.put("guiBai", ha.guiBai);

            } else {
                encodingObj.put("error", ha.message);
            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
