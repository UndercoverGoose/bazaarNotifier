package io.github.undercovergoose.bazaarNotifier;

import static io.github.undercovergoose.bazaarNotifier.Main.itemNameToId;
import static io.github.undercovergoose.bazaarNotifier.TickEvent.bazaarItems;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Date;

public class Order {
    private static final Utils utils = new Utils();
    public String productName;
    private String skyblockItemId = "null";
    public String skyblockItemId() {
        if(!this.skyblockItemId.equals("null")) return this.skyblockItemId;
        this.skyblockItemId = utils.jsonGet(itemNameToId, this.productName, "unresolved").replaceAll("\"","");
        return this.skyblockItemId;
    };
    public int quantityLeft() {
        return this.quantity - this.filledQuantity;
    }
    public boolean isBuyOrder;
    public double unitPrice;
    public int quantity;
    public long createdAt;
    public int filledQuantity;
    public String status;
    public boolean markFilled() {
        long now = new Date().getTime();
        if(now - this.createdAt < 25000) return false;
        this.status = "§a§l§kM §a§lFilled §a§l§kM";
        this.filledQuantity = this.quantity;
        return true;
    }
    private void updateFilledQuantity(int newQuantity) {
        if(this.quantity < 1000 && (filledQuantity != newQuantity && newQuantity > filledQuantity)) {
            int dif = newQuantity - filledQuantity;
            Message msg = new Message();
            String prefix = this.isBuyOrder ? "Bought" : "Sold";
            msg.addText("§5[§dBazaar§5] §d" + prefix + " " + dif + "§5x §d§l" + this.productName + " §dfor " + utils.prettyNum(this.unitPrice*dif) + " coins§5.");
            msg.send();
        }
        this.filledQuantity = newQuantity;
    }
    public void checkOrder() {
        if(this.quantityLeft() == 0) {
            this.status = "§a§l§kM §a§lFilled §a§l§kM";
            return;
        }
        if(this.skyblockItemId() == "unresolved") {
            this.status = "§4§lUnresolvable";
            return;
        }
        if(bazaarItems == null) return;
        String skyblockId = "products." + this.skyblockItemId();
        if(utils.jsonGet(bazaarItems, skyblockId).equals("null")) {
            this.status = "§4§lError";
            return;
        }
        if(this.isBuyOrder) {
            // sell_summary
            JsonArray data = utils.jsonGetArray(bazaarItems, skyblockId + ".sell_summary");
            if(data.size() == 0) {
                this.status = "§4§lNo Orders";
                return;
            }
            for(int i = 0; i < data.size(); i++) {
                JsonObject idx = (JsonObject) data.get(i);
                double idxPrice = utils.jsonGet(idx, "pricePerUnit", -1);
                double orders = utils.jsonGet(idx, "orders", -1);
                double quantity = utils.jsonGet(idx, "amount", -1);
                if(idxPrice != this.unitPrice) {
                    if(idxPrice < this.unitPrice) {
                        boolean worked = markFilled();
                        if(worked) return;
                    }
                    continue;
                }
                if(i == 0) {
                    if(orders == 1) {
                        this.status = "§2§l#1";
                        updateFilledQuantity(this.quantity - (int) quantity);
                    }
                    else this.status = "§e§l#1 - Tied §e+" + Math.max((int)(quantity - this.quantityLeft()), 0) + " items";
                }else {
                    if(orders == 1) updateFilledQuantity(this.quantity - (int) quantity);;
                    double topPrice = utils.jsonGet((JsonObject) data.get(0), "pricePerUnit", -1);
                    this.status = "§c§l#" + (i+1) + " §c+" + utils.prettyNum(topPrice - this.unitPrice) + " coins";
                }
                return;
            }
        }else {
            JsonArray data = utils.jsonGetArray(bazaarItems, skyblockId + ".buy_summary");
            if(data.size() == 0) {
                this.status = "§4§lNo Orders";
            }
            for(int i = 0; i < data.size(); i++) {
                JsonObject idx = (JsonObject) data.get(i);
                double unitPrice = utils.jsonGet(idx, "pricePerUnit", -1);
                double orders = utils.jsonGet(idx, "orders", -1);
                double quantity = utils.jsonGet(idx, "amount", -1);
                if(unitPrice != this.unitPrice) {
                    if(unitPrice > this.unitPrice) {
                        boolean worked = markFilled();
                        if(worked) return;
                    }
                    continue;
                }
                if(i == 0) {
                    if(orders == 1) {
                        this.status = "§2§l#1";
                        updateFilledQuantity(this.quantity - (int) quantity);
                    }
                    else this.status = "§e§l#1 - Tied §e+" + Math.max((int)(quantity - this.quantityLeft()), 0) + " items";
                    return;
                }else {
                    if(orders == 1) updateFilledQuantity(this.quantity - (int) quantity);
                    double topPrice = utils.jsonGet((JsonObject) data.get(0), "pricePerUnit", -1);
                    this.status = "§c§l#" + (i + 1) + " §c-" + utils.prettyNum(this.unitPrice - topPrice) + " coins";
                }
                return;
            }
        }
        this.status = "§4§lNot Found";
    }
}
