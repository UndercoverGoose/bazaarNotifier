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
    private boolean canSendStateMessage = true;
    private int lastStateLog = 0;
    private void sendFilledMessage() {
        if(!canSendStateMessage || lastStateLog == 1) return;
        Message msg = new Message();
        String prefix = this.isBuyOrder ? "Buy" : "Sell";
        msg.addText("§6[Bazaar] §eYour §a" + prefix + " Order §efor §a" + this.quantity + "§7x §f" + this.productName + " §ewas filled§7!");
        msg.send();
        canSendStateMessage = false;
        lastStateLog = 1;
    }
    private void sendOutdatedMessage(boolean tied) {
        if(!canSendStateMessage || lastStateLog == 2) return;
        Message msg = new Message();
        String prefix = this.isBuyOrder ? "Buy" : "Sell";
        String suffix = tied ? "was matched§7!" : "is no longer the best§7!";
        msg.addText("§6[Bazaar] §eYour §a" + prefix + " Order §efor §a" + this.quantity + "§7x §f" + this.productName + " §e" + suffix);
        msg.send();
        lastStateLog = 2;
    }
    private void sendRevivedMessage(String oldStatus) {
        if(!canSendStateMessage || lastStateLog == 3) return;
        boolean wasBest = oldStatus.contains("#1") || oldStatus.contains("Unknown") || oldStatus.contains("Not Found");
        if(!wasBest) {
            Message msg = new Message();
            String prefix = this.isBuyOrder ? "Buy" : "Sell";
            msg.addText("§6[Bazaar] §eYour §a" + prefix + " Order §efor §a" + this.quantity + "§7x §f" + this.productName + " §ehas been revived§7!");
            msg.send();
        }
        lastStateLog = 3;
    }
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
        this.status = "§a§l§kM§r §a§lFilled §a§l§kM";
        this.filledQuantity = this.quantity;
        this.sendFilledMessage();
        return true;
    }
    private void updateFilledQuantity(int newQuantity) {
        if(this.quantity < 1000 && (filledQuantity != newQuantity && newQuantity > filledQuantity)) {
            int dif = newQuantity - filledQuantity;
            Message msg = new Message();
            String prefix = this.isBuyOrder ? "purchased" : "sold";
            msg.addText("§6[Bazaar] §eYou " + prefix + " §a" + dif + "§7x §f" + this.productName + " §efor §a" + utils.prettyNum(this.unitPrice*dif) + " coins! §e(§a" + newQuantity + "§e/" + this.quantity + ")");
            msg.send();
        }
        this.filledQuantity = newQuantity;
        Overlay.calcMovingCoins();
    }
    public void checkOrder() {
        if(this.quantityLeft() == 0) {
            this.status = "§a§l§kM§r §a§lFilled §a§l§kM";
            this.sendFilledMessage();
            return;
        }
        if(this.skyblockItemId() == "unresolved") {
            this.status = "§4§lUnresolvable";
            this.canSendStateMessage = true;
            return;
        }
        if(bazaarItems == null) return;
        String skyblockId = "products." + this.skyblockItemId();
        if(utils.jsonGet(bazaarItems, skyblockId).equals("null")) {
            this.status = "§4§lError";
            this.canSendStateMessage = true;
            return;
        }
        if(this.isBuyOrder) {
            // sell_summary
            JsonArray data = utils.jsonGetArray(bazaarItems, skyblockId + ".sell_summary");
            if(data.size() == 0) {
                this.status = "§4§lNo Orders";
                this.canSendStateMessage = true;
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
                        this.sendRevivedMessage(this.status);
                        this.status = "§2§l#1";
                        updateFilledQuantity(this.quantity - (int) quantity);
                    }
                    else {
                        this.status = "§e§l#1 - Tied §e+" + Math.max((int)(quantity - this.quantityLeft()), 0) + " items";
                        this.sendOutdatedMessage(true);
                    }
                }else {
                    if(orders == 1) updateFilledQuantity(this.quantity - (int) quantity);;
                    double topPrice = utils.jsonGet((JsonObject) data.get(0), "pricePerUnit", -1);
                    this.status = "§c§l#" + (i+1) + " §c+" + utils.prettyNum(topPrice - this.unitPrice) + " coins";
                    this.sendOutdatedMessage(false);
                }
                return;
            }
        }else {
            JsonArray data = utils.jsonGetArray(bazaarItems, skyblockId + ".buy_summary");
            if(data.size() == 0) {
                this.status = "§4§lNo Orders";
                this.canSendStateMessage = true;
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
                        this.sendRevivedMessage(this.status);
                        this.status = "§2§l#1";
                        updateFilledQuantity(this.quantity - (int) quantity);
                    }
                    else {
                        this.status = "§e§l#1 - Tied §e+" + Math.max((int)(quantity - this.quantityLeft()), 0) + " items";
                        this.sendOutdatedMessage(true);
                    }
                    return;
                }else {
                    if(orders == 1) updateFilledQuantity(this.quantity - (int) quantity);
                    double topPrice = utils.jsonGet((JsonObject) data.get(0), "pricePerUnit", -1);
                    this.status = "§c§l#" + (i + 1) + " §c-" + utils.prettyNum(this.unitPrice - topPrice) + " coins";
                    this.sendOutdatedMessage(false);
                }
                return;
            }
        }
        this.status = "§4§lNot Found";
        this.canSendStateMessage = true;
    }
}
