package dreamgame.protocol.messages.json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import phom.data.Poker;

import dreamgame.data.ResponseCode;
import dreamgame.data.ZoneID;
import dreamgame.protocol.messages.GetPokerResponse;

import dreamgame.gameserver.framework.common.LoggerContext;
import dreamgame.gameserver.framework.common.ServerException;
import dreamgame.gameserver.framework.protocol.IMessageProtocol;
import dreamgame.gameserver.framework.protocol.IRequestMessage;
import dreamgame.gameserver.framework.protocol.IResponseMessage;
import dreamgame.gameserver.framework.room.Zone;
import org.jboss.netty.util.internal.UnterminatableExecutor;

public class GetPokerJSON implements IMessageProtocol {

    private final Logger mLog = LoggerContext.getLoggerFactory().getLogger(JoinedJSON.class);

    public boolean decode(Object aEncodedObj, IRequestMessage aDecodingObj) throws ServerException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object encode(IResponseMessage aResponseMessage) throws ServerException {
        try {
            JSONObject encodingObj = new JSONObject();
            // put response data into json object
            encodingObj.put("mid", aResponseMessage.getID());
            // cast response obj
            GetPokerResponse getPoker = (GetPokerResponse) aResponseMessage;
            encodingObj.put("code", getPoker.mCode);
            if (getPoker.mCode == ResponseCode.FAILURE) {
            } else if (getPoker.mCode == ResponseCode.SUCCESS) {

                encodingObj.put("uid", getPoker.uid);
                encodingObj.put("username", getPoker.name);
                encodingObj.put("matchNum", getPoker.matchNum);
                encodingObj.put("matchId", getPoker.matchId);
                if (getPoker.zoneID == ZoneID.POKER) {
                    encodingObj.put("number_card", getPoker.pokerCards.length);
                    encodingObj.put("cards", dreamgame.poker.data.Utils.bytesToString(getPoker.pokerCards));
                    if (getPoker.beginID >= 0) {
                        encodingObj.put("begin_uid", getPoker.beginID);
                    }
                    encodingObj.put("minBet", getPoker.minBet);
                    encodingObj.put("maxBet", getPoker.maxBet);
                    encodingObj.put("potMoney", getPoker.potMoney);
//                    encodingObj.put("isNewMatch", getPoker.isNewMatch);
                    JSONArray order = new JSONArray();
                    for (int i = 0; i < getPoker.order.length; i++) {
                        if (getPoker.order[i] > 0) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", getPoker.order[i]);
                            order.put(jCell);
                        }
                    }
                    encodingObj.put("order", order);
                } else if (getPoker.zoneID == ZoneID.XITO) {
                    encodingObj.put("number_card", getPoker.pokerCards.length);
                    encodingObj.put("cards", dreamgame.poker.data.Utils.bytesToString(getPoker.pokerCards));
//                    if (getPoker.beginID >= 0) {
//                        encodingObj.put("begin_uid", getPoker.beginID);
//                    }
//                    encodingObj.put("minBet", getPoker.minBet);
//                    encodingObj.put("maxBet", getPoker.maxBet);
                    encodingObj.put("potMoney", getPoker.potMoney);
//                    encodingObj.put("isNewMatch", getPoker.isNewMatch);
                    JSONArray order = new JSONArray();
                    for (int i = 0; i < getPoker.order.length; i++) {
                        if (getPoker.order[i] > 0) {
                            JSONObject jCell = new JSONObject();
                            jCell.put("id", getPoker.order[i]);
                            order.put(jCell);
                        }
                    }
                    encodingObj.put("order", order);
                } else if (getPoker.zoneID == ZoneID.BAUCUA) {
                    encodingObj.put("time", getPoker.timeBet);
                } else {
                    if (getPoker.chanCards.size() > 0) {
                        encodingObj.put("number_card", getPoker.chanCards.size());
                        String data = "" + getPoker.chanCards.get(0).toInt();
                        for (int i = 1; i < getPoker.chanCards.size(); i++) {
                            data += "#" + getPoker.chanCards.get(i).toInt();
                        }
                        encodingObj.put("cards", data);
                        if (getPoker.beginID >= 0) {
                            encodingObj.put("begin_uid", getPoker.beginID);
                        }

                        JSONArray order = new JSONArray();
                        for (int i = 0; i < getPoker.order.length; i++) {
                            if (getPoker.order[i] > 0) {
                                JSONObject jCell = new JSONObject();
                                jCell.put("id", getPoker.order[i]);
                                order.put(jCell);
                            }
                        }
                        encodingObj.put("order", order);

                    } else if (getPoker.phomCards.size() > 0) {
                        encodingObj.put("number_card", getPoker.phomCards.size());
                        String data = "" + getPoker.phomCards.get(0).toInt();
                        for (int i = 1; i < getPoker.phomCards.size(); i++) {
                            data += "#" + getPoker.phomCards.get(i).toInt();
                        }
                        encodingObj.put("cards", data);
                        if (getPoker.beginID >= 0) {
                            encodingObj.put("begin_uid", getPoker.beginID);
                        }

                        JSONArray order = new JSONArray();
                        for (int i = 0; i < getPoker.order.length; i++) {
                            if (getPoker.order[i] > 0) {
                                JSONObject jCell = new JSONObject();
                                jCell.put("id", getPoker.order[i]);
                                order.put(jCell);
                            }
                        }
                        encodingObj.put("order", order);

                    } else if (getPoker.tienlenCards_new != null) {
                        encodingObj.put("number_card", getPoker.tienlenCards_new.length);
                        encodingObj.put("cards", dreamgame.tienlen.data.Utils.bytesToString(getPoker.tienlenCards_new));
                        if (getPoker.beginID >= 0) {
                            encodingObj.put("begin_uid", getPoker.beginID);
                        }
                        encodingObj.put("isNewMatch", getPoker.isNewMatch);
                        JSONArray order = new JSONArray();
                        for (int i = 0; i < getPoker.order.length; i++) {
                            if (getPoker.order[i] > 0) {
                                JSONObject jCell = new JSONObject();
                                jCell.put("id", getPoker.order[i]);
                                order.put(jCell);
                            }
                        }
                        encodingObj.put("order", order);

                    }else if (getPoker.maubinhCards != null) {
			//deleted
                    } else {
                        if (getPoker.first_id > 0) {
                            encodingObj.put("firstPlayer_id", getPoker.first_id);
                        }
                        JSONArray arrValues = new JSONArray();
                        JSONObject jCell = new JSONObject();
                        jCell.put("number", getPoker.first.getNum());
                        jCell.put("type", getPoker.first.pokerTypeToInt(getPoker.first.getType()));
                        arrValues.put(jCell);
                        encodingObj.put("first_poker", arrValues);

                        arrValues = new JSONArray();
                        jCell = new JSONObject();
                        jCell.put("number", getPoker.second.getNum());
                        jCell.put("type", getPoker.second.pokerTypeToInt(getPoker.second.getType()));
                        arrValues.put(jCell);
                        encodingObj.put("second_poker", arrValues);

                        arrValues = new JSONArray();
                        jCell = new JSONObject();
                        jCell.put("number", getPoker.third.getNum());
                        jCell.put("type", getPoker.third.pokerTypeToInt(getPoker.third.getType()));
                        arrValues.put(jCell);
                        encodingObj.put("third_poker", arrValues);
                    }
                }
            }
            // response encoded obj
            return encodingObj;
        } catch (Throwable t) {
            mLog.error("[ENCODER] " + aResponseMessage.getID(), t);
            return null;
        }
    }
}
