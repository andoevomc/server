package dreamgame.tienlen.data;

public class Utils {
    // Sort an array of Poker increament

    public static void quicksort(int low, int high, Poker[] input) {
        int i = low, j = high;
        Poker pivot = input[low + (high - low) / 2];
        while (i <= j) {
            while (!input[i].isGreater(pivot)) {
                i++;
            }
            while (input[j].isGreater(pivot)) {
                j--;
            }
            if (i <= j) {
                Poker temp = input[i];
                input[i] = input[j];
                input[j] = temp;

                i++;
                j--;
            }
        }
        // Recursion
        if (low < j) {
            quicksort(low, j, input);
        }
        if (i < high) {
            quicksort(i, high, input);
        }
    }
    //Tien Len

    public static String[] stringSplit(String source, String search) {
        String s1 = source;
        int count = 0;
        while (s1.indexOf(search) > 0) {
            s1 = s1.substring(s1.indexOf(search) + 1);
            count++;
        }

        if (count == 0) {
            return null;
        }

        s1 = source;
        String[] s2 = new String[count + 1];
        for (int i = 0; i < count + 1; i++) {
            if (i == count) {
                s2[i] = s1;
            } else {
                s2[i] = s1.substring(0, s1.indexOf(search));
                s1 = s1.substring(s1.indexOf(search) + 1);
            }
        }
        return s2;
    }
//Thomc Tienlen

    public static byte[] stringToByte(String str) {

        String[] strCards = stringSplit(str, "#");
        if ((strCards == null || strCards.length == 0) && str.length() != 0) {
            byte[] cards = new byte[1];
            cards[0] = Byte.parseByte(str);
            return cards;
        } else {
            byte[] cards = new byte[strCards.length];
            for (int i = 0; i < strCards.length; i++) {
                cards[i] = Byte.parseByte(strCards[i]);
            }
            return cards;
        }
    }

    public static String bytesToString(byte[] cards) {
        if (cards == null || cards.length == 0) {
            return "";
        } else {
            String strCard = "";
            for (int i = 0; i < cards.length; i++) {
                if (i == 0) {
                    strCard += cards[i];
                } else {
                    strCard += "#" + cards[i];
                }
            }
            return strCard;
        }
    }

    public static byte[] sortCards(byte[] cards) {
        if (cards == null) {
            return null;
        }
        for (int i = 0; i < cards.length - 1; i++) {
            for (int j = i + 1; j < cards.length; j++) {
                if (getValue(cards[i]) >= getValue(cards[j])) {
                    if (getValue(cards[i]) == getValue(cards[j])) {
                        if (getType(cards[i]) > getType(cards[j])) {
                            byte tempCard = cards[j];
                            cards[j] = cards[i];
                            cards[i] = tempCard;
                        }
                    } else {
                        byte tempCard = cards[j];
                        cards[j] = cards[i];
                        cards[i] = tempCard;
                    }
                }
            }
        }
        return cards;
    }
    //Trả về loại quân theo độ mạnh tăng dần: bích tép rô cơ

    public static int getType(int b) {
        int type = (b - 1) / 13;
        switch (type) {
            case 0:
//            tép
                return 2;
            case 1:
//            bích
                return 1;
            case 2:
//            rô
                return 3;
            case 3:
//            cơ
                return 4;
            default:
                return -1;
        }
    }
    //check 2 quân cùng chất hay ko

    public static boolean isSameType(byte card1, byte card2) {
        return getType(card1) == (getType(card2));
    }
    //check 2 quân cùng màu hay ko

    public static boolean isSameColor(byte card1, byte card2) {
        if (((getType(card1) <= 2) && (getType(card2) <= 2)) || ((getType(card1) > 2) && (getType(card2) > 2))) {
            return true;
        }
        return false;
    }
//Chuyển quân 2 và quân Át ra cuối bảng độ mạnh

    public static int getValue(int b) {
        int value = (b - 1) % 13;
        switch (value) {
            case 0:
                return 11;
            case 1:
                return 12;
            default:
                return value - 2;
        }
    }
    //So sánh độ mạnh của 2 quân bài

    public static boolean isBigger(byte card1, byte card2) {
        //nếu cùng số --> so sánh chất
        if (Utils.getValue(card1) == Utils.getValue(card2)) {
            return (Utils.getType(card1) > Utils.getType(card2));
        } else {
            return Utils.getValue(card1) > Utils.getValue(card2);
        }
    }

    public static Poker numToPoker(int input) {
        switch (input) {
            case 1:
                return new Poker(3, PokerType.Pic);
            case 2:
                return new Poker(3, PokerType.Tep);
            case 3:
                return new Poker(3, PokerType.Ro);
            case 4:
                return new Poker(3, PokerType.Co);
            case 5:
                return new Poker(4, PokerType.Pic);
            case 6:
                return new Poker(4, PokerType.Tep);
            case 7:
                return new Poker(4, PokerType.Ro);
            case 8:
                return new Poker(4, PokerType.Co);
            case 9:
                return new Poker(5, PokerType.Pic);
            case 10:
                return new Poker(5, PokerType.Tep);
            case 11:
                return new Poker(5, PokerType.Ro);
            case 12:
                return new Poker(5, PokerType.Co);
            case 13:
                return new Poker(6, PokerType.Pic);
            case 14:
                return new Poker(6, PokerType.Tep);
            case 15:
                return new Poker(6, PokerType.Ro);
            case 16:
                return new Poker(6, PokerType.Co);
            case 17:
                return new Poker(7, PokerType.Pic);
            case 18:
                return new Poker(7, PokerType.Tep);
            case 19:
                return new Poker(7, PokerType.Ro);
            case 20:
                return new Poker(7, PokerType.Co);
            case 21:
                return new Poker(8, PokerType.Pic);
            case 22:
                return new Poker(8, PokerType.Tep);
            case 23:
                return new Poker(8, PokerType.Ro);
            case 24:
                return new Poker(8, PokerType.Co);
            case 25:
                return new Poker(9, PokerType.Pic);
            case 26:
                return new Poker(9, PokerType.Tep);
            case 27:
                return new Poker(9, PokerType.Ro);
            case 28:
                return new Poker(9, PokerType.Co);
            case 29:
                return new Poker(10, PokerType.Pic);
            case 30:
                return new Poker(10, PokerType.Tep);
            case 31:
                return new Poker(10, PokerType.Ro);
            case 32:
                return new Poker(10, PokerType.Co);
            case 33:
                return new Poker(11, PokerType.Pic);
            case 34:
                return new Poker(11, PokerType.Tep);
            case 35:
                return new Poker(11, PokerType.Ro);
            case 36:
                return new Poker(11, PokerType.Co);
            case 37:
                return new Poker(12, PokerType.Pic);
            case 38:
                return new Poker(12, PokerType.Tep);
            case 39:
                return new Poker(12, PokerType.Ro);
            case 40:
                return new Poker(12, PokerType.Co);
            case 41:
                return new Poker(13, PokerType.Pic);
            case 42:
                return new Poker(13, PokerType.Tep);
            case 43:
                return new Poker(13, PokerType.Ro);
            case 44:
                return new Poker(13, PokerType.Co);
            case 45:
                return new Poker(1, PokerType.Pic);
            case 46:
                return new Poker(1, PokerType.Tep);
            case 47:
                return new Poker(1, PokerType.Ro);
            case 48:
                return new Poker(1, PokerType.Co);
            case 49:
                return new Poker(2, PokerType.Pic);
            case 50:
                return new Poker(2, PokerType.Tep);
            case 51:
                return new Poker(2, PokerType.Ro);
            case 52:
                return new Poker(2, PokerType.Co);
            default:
                return new Poker(0, PokerType.Pic);
        }
    }
}
