package io.github.undercovergoose.bazaarNotifier.mixin;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.Date;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.undercovergoose.bazaarNotifier.Order;
import io.github.undercovergoose.bazaarNotifier.Overlay;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
  private static String cleanColour(String in) {
      return in.replaceAll("(?i)\\u00A7.", "");
  }
  private GuiChest castGuiChest(GuiContainer container) {
    try {
      return (GuiChest) container;
    } catch (ClassCastException e) {}
    return null;
  }
  private static double parseDouble(String in) {
    String pre = in.replaceAll("[A-z, :\"]", "").trim();
    if(pre.length() == 0) return 0;
    return Double.parseDouble(pre);
  }
  private static String parseItemName(String in) {
    if(in.contains("BUY")) return in.substring(4);
    if(in.contains("SELL")) return in.substring(5);
    return in;
  }
  
  private boolean isValidInventory = false;
  private ArrayList<Order> bazaarOrders = new ArrayList<Order>();
  private ArrayList<Integer> bazaarOrderSlots = new ArrayList<>();

  private void parseStack(ItemStack stack, int slot) {
    try {
      String itemName = cleanColour(stack.getDisplayName()).toLowerCase();
      if(!itemName.contains("buy") && !itemName.contains("sell")) return;
      NBTTagList itemLore = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
      int amountIndex = 2;
      int filledIndex = 3;
      int unitPriceIndex = 4;

      String filledLine = cleanColour(itemLore.get(filledIndex).toString()).toLowerCase();
      boolean hasFilledItems = filledLine.contains("filled");
      if(hasFilledItems) unitPriceIndex = 5;

      final int amount = (int) parseDouble(cleanColour(itemLore.get(amountIndex).toString()));
      final double unitPrice = parseDouble(cleanColour(itemLore.get(unitPriceIndex).toString()));
      final int filledQuantity = hasFilledItems && amount < 1000 ? (int) parseDouble(filledLine.split("/")[0]) : 0;
      boolean buyOrder = itemName.contains("buy");

      Order order = new Order();
      order.productName = parseItemName(cleanColour(stack.getDisplayName()));
      order.isBuyOrder = buyOrder;
      order.unitPrice = unitPrice;
      order.quantity = amount;
      order.filledQuantity = filledQuantity;
      order.status = "§8§lUnknown";
      order.createdAt = new Date().getTime();
      bazaarOrders.add(order);
      bazaarOrderSlots.add(slot);
      Overlay.BazaarOrders = bazaarOrders;
    }catch(Exception e) {}
  }

  @Inject(method = "initGui", at = @At("HEAD"))
  public void initGui(CallbackInfo ci) {
    GuiContainer $this = (GuiContainer) (Object) this;
    GuiChest eventGui = castGuiChest($this);
    if(eventGui == null) return;
    ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
    String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
    if(!containerName.toLowerCase().contains("bazaar order")) return;
    isValidInventory = true;
    bazaarOrders = new ArrayList<Order>();
    bazaarOrderSlots = new ArrayList<>();
  }

  @Inject(method = "drawSlot", at = @At("HEAD"))
  public void drawSlot(Slot slotArg, CallbackInfo ci) {
    if(slotArg == null || !isValidInventory) return;
    if(slotArg.getSlotIndex() != 0) return;
    GuiContainer $this = (GuiContainer) (Object) this;
    GuiChest eventGui = castGuiChest($this);
    if(eventGui == null) return;
    ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
    for(Slot slot : cc.inventorySlots) {
      ItemStack stack = slot.getStack();
      int slotIndex = slot.getSlotIndex();
      if(stack == null || bazaarOrderSlots.contains(slotIndex)) continue;
      parseStack(stack, slotIndex);
    }
  }

  @Inject(method = "onGuiClosed", at = @At("HEAD"))
  public void guiClosed(CallbackInfo ci) {
    if(isValidInventory) {
      Overlay.BazaarOrders = bazaarOrders;
      Overlay.calcMovingCoins();
    }
    isValidInventory = false;
  }
}
