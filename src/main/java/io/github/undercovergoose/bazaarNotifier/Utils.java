package io.github.undercovergoose.bazaarNotifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.DecimalFormat;

public class Utils {
    public String jsonGet(JsonObject json, String pathStr) {
        String[] path = pathStr.replace(".","/").split("/");
        String data = null;
        JsonObject obj = json;
        for(String key : path) {
            data = obj.get(key).toString();
            try {
                Object newObj = new JsonParser().parse(data);
                obj = (JsonObject) newObj;
            }catch(Exception e) {}
        }
        return data;
    }
    public JsonArray jsonGetArray(JsonObject json, String pathStr) {
        try {
            return new JsonParser().parse(jsonGet(json, pathStr)).getAsJsonArray();
        }catch(Exception ignored) {}
        return new JsonArray();
    }
    public String jsonGet(JsonObject json, String pathStr, String defaultValue) {
        try {
            return jsonGet(json, pathStr);
        }catch(Exception ignored) {}
        return defaultValue;
    }
    public double jsonGet(JsonObject json, String pathStr, double defaultValue) {
        try {
            return Double.parseDouble(jsonGet(json, pathStr));
        }catch(Exception ignored) {}
        return defaultValue;
    }
    public String prettyNum(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,##0.0");
        return formatter.format(amount);
    }
}
