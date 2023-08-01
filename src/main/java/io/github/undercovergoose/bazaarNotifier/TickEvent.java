package io.github.undercovergoose.bazaarNotifier;

import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Date;

public class TickEvent {
    private static long lastCheck = 0;
    public static JsonObject bazaarItems = null;

    private static final Utils utils = new Utils();
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void renderTickEvent(net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent event) {
        final long now = new Date().getTime();
        if(now - lastCheck > 12000) {
            lastCheck = now;
            Runnable xyz = () -> {
                try {
                    fetchBazaarItems();
                    for(Order order : Overlay.BazaarOrders) order.checkOrder();
                }catch(Exception ignored) {
                    System.err.println(ignored);
                }
            };
            new Thread(xyz).start();
        }
    }
    private static void fetchBazaarItems() {
        Request req = new Request();
        Overlay.globalStatus = "§e§lREQUESTING";
        JsonObject res = null;
        try {
            res = req.jsonGet("https://api.hypixel.net/skyblock/bazaar");
            if(res == null) {
                Overlay.globalStatus = "§c§lFAILED";
                return;
            }
        }catch(Exception ignored) {
            Overlay.globalStatus = "§c§lFAILED";
            return;
        }
        if(utils.jsonGet(res, "products").equals("null")) {
            Overlay.globalStatus = "§c§lPARSE ERROR";
            return;
        }
        Overlay.globalStatus = "";
        bazaarItems = res;
    }
}
