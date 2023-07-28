package io.github.undercovergoose.bazaarNotifier.mixin;

// // import net.minecraft.client.Minecraft;
// import net.minecraft.client.gui.inventory.GuiChest;
// import net.minecraft.client.gui.inventory.GuiContainer;
// // import net.minecraft.client.settings.KeyBinding;
// // import net.minecraft.entity.player.InventoryPlayer;
// import net.minecraft.inventory.ContainerChest;
// import net.minecraft.inventory.Slot;
// import net.minecraft.item.ItemStack;
// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.injection.At;
// import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// @Mixin(GuiContainer.class)
// public class MixinGuiContainer {
//     private static String cleanColour(String in) {
//         return in.replaceAll("(?i)\\u00A7.", "");
//     }
//     @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
//     public void drawSlot(Slot slot, CallbackInfo ci) {
//         if (slot == null) return;
//         GuiContainer $this = (GuiContainer) (Object) this;
//         GuiChest eventGui = (GuiChest) $this;
//         ContainerChest cc = (ContainerChest) eventGui.inventorySlots;
//         String containerName = cc.getLowerChestInventory().getDisplayName().getUnformattedText();
//         System.out.println("GUI drawn with name: " + containerName);
//         if(!containerName.contains("Bazaar Orders")) return;
//         int size = cc.inventorySlots.size();
//         for(int i = 0; i < size; i++) {
//             ItemStack slotStack = cc.inventorySlots.get(i).getStack();
//             if(slotStack == null) continue;
//             String itemName = cleanColour(slotStack.getDisplayName());
//             if(!(itemName.contains("BUY") || itemName.contains("SELL"))) continue;
//             System.out.println("Found item in GUI: " + itemName);
//         }
//     }
// }
