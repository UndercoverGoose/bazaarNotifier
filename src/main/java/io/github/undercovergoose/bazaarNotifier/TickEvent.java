package io.github.undercovergoose.bazaarNotifier;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Date;

public class TickEvent {
    private static String cleanColour(String in) {
        return in.replaceAll("(?i)\\u00A7.", "");
    }
    private static String parseItemName(String in) {
        if(in.contains("BUY")) return in.substring(4);
        if(in.contains("SELL")) return in.substring(5);
        return in;
    }
    private static double parseDouble(String in) {
        String pre = in.replaceAll("[A-z, :\"]", "").trim();
        if(pre.length() == 0) return 0;
        return Double.parseDouble(pre);
    }
    private static long lastCheck = 0;
    public static JsonObject bazaarItems = null;
    private static ArrayList<Order> BazaarOrdersTemp = new ArrayList<Order>();
    private static boolean writeTemp = false;

    // private static int alt = 0;
    private static void writeTemp() {
        if(writeTemp) {
            Overlay.BazaarOrders = BazaarOrdersTemp;
            writeTemp = false;
            lastCheck = new Date().getTime() - 11500;
        }
        return;
    }
    private static final Utils utils = new Utils();
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void renderTickEvent(net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent event) {
//        alt++;
//        if(alt / 60 > 1) alt = 0;
//        else return;
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
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null) { writeTemp(); return; };
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) { writeTemp(); return; };
        if (!(screen instanceof GuiChest)) { writeTemp(); return; };

        ContainerChest chest = ((ContainerChest) Minecraft.getMinecraft().thePlayer.openContainer);
        String chestTitle = chest.getLowerChestInventory().getName();
        if (!chestTitle.contains("Bazaar Orders")) { writeTemp(); return; };

        IInventory chestInventory = chest.getLowerChestInventory();
        ArrayList<Order> newBazaarOrders = new ArrayList<Order>();
        for (int item = 0; item < chestInventory.getSizeInventory(); item++) {
            if (chestInventory.getStackInSlot(item) == null) continue;
            ItemStack stack = chestInventory.getStackInSlot(item);
            String itemNameRaw = cleanColour(stack.getDisplayName());
            if(!(itemNameRaw.contains("BUY") || itemNameRaw.contains("SELL"))) continue;
            final Order order = parseStackAsOrder(stack);
            newBazaarOrders.add(order);
        }
        BazaarOrdersTemp = newBazaarOrders;
        writeTemp = true;
//        for(Order order : overlay.BazaarOrders) order.checkOrder();
    }
    private static Order parseStackAsOrder(ItemStack item) {
        String itemNameRaw = cleanColour(item.getDisplayName());
        String itemName = parseItemName(itemNameRaw);
        boolean buyOrder = itemNameRaw.contains("BUY");
//        boolean sellOrder = itemNameRaw.contains("SELL");

        NBTTagList itemLore = item.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        int amountIndex = 2;
        int filledIndex = 3;
        int unitPriceIndex = 4;
        String filledLine = cleanColour(itemLore.get(filledIndex).toString());
        boolean hasFilledItems = filledLine.contains("Filled");
        if(hasFilledItems) unitPriceIndex = 5;

        final int amount = (int) parseDouble(cleanColour(itemLore.get(amountIndex).toString()));
        final double unitPrice = parseDouble(cleanColour(itemLore.get(unitPriceIndex).toString()));
        final int filledQuantity = hasFilledItems ? (int) parseDouble(filledLine.split("/")[0]) : 0;

        Order order = new Order();
        order.productName = itemName;
        order.isBuyOrder = buyOrder;
        order.unitPrice = unitPrice;
        order.quantity = amount;
        order.filledQuantity = filledQuantity;
        order.status = "§8§lUnknown";
        order.createdAt = new Date().getTime();
        return order;
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
