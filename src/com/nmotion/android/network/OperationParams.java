package com.nmotion.android.network;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class OperationParams extends HashMap<String, Object> {

    private static final long serialVersionUID = 1064551409148229285L;

    public String generateOperationUri() {
        String result = "";
        Object[] keys = this.keySet().toArray();
        for (int pos = 0; pos < keys.length; pos++) {
            Object key = keys[pos];
            result += String.format("%s=%s", key, get(key));
            if (pos < (keys.length - 1)) {
                result += "&";
            }
        }
        return result;
    }

    public ArrayList<NameValuePair> generateOperationValues() {
        ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
        Object[] keys = this.keySet().toArray();
        for (int pos = 0; pos < keys.length; pos++) {
            Object key = keys[pos];
            values.add(new BasicNameValuePair((String) key, get(key).toString()));
        }
        return values;
    }

}
