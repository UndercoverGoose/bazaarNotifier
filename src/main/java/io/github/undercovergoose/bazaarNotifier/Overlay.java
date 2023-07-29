package io.github.undercovergoose.bazaarNotifier;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

import static io.github.undercovergoose.bazaarNotifier.Main.*;

public class Overlay {
    public static ArrayList<Order> BazaarOrders = new ArrayList<Order>();
    public static String globalStatus = "";
    public static double globalMovingCoins = 0;
    private static final Utils utils = new Utils();
    public void drawText(String text, float x, float y) {
        MC.fontRendererObj.drawStringWithShadow(text, x, y, 0xffFFFFFF);
    }
    public static void addBazaarOrder(Order order) {
        BazaarOrders.add(order);
        calcMovingCoins();
    }
    public static void calcMovingCoins() {
        double movingCoins = 0;
        for(Order order : BazaarOrders) {
            final int quantityLeft = order.quantity - order.filledQuantity;
            movingCoins += quantityLeft * order.unitPrice;
        }
        globalMovingCoins = movingCoins;
    }
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onRenderExperienceBar(RenderGameOverlayEvent.Text event) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.85, 0.85, 0.85);
        int x = 4;
        int y = 4;
        drawText("§e§l§nBazaar Orders§r §d" + utils.prettyNum(globalMovingCoins) + " moving coins " + globalStatus, x, y);
        y += 10;
        for(Order order : BazaarOrders) {
            final String color1 = order.isBuyOrder ? "§6§l" : "§3§l";
            final String color2 = order.isBuyOrder ? "§e" : "§b";

            final String prefix = color1 + (order.isBuyOrder ? "BUY " : "SELL ") + color2;
            final String middle = color1 + (order.filledQuantity > 0 ? order.filledQuantity + "/" + order.quantity : order.quantity) + "x " + color2;
            drawText(prefix + order.productName + " " + middle + utils.prettyNum(order.unitPrice) + (color1 + " coins ") + order.status, x, y);

            y += 10;
        }
        GlStateManager.popMatrix();
    }
}
