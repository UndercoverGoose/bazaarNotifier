package io.github.undercovergoose.bazaarNotifier;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "betterBazaarNotifierThatActuallyWorks", version = "1.0.0")
public class Main {
    public static final Minecraft MC = Minecraft.getMinecraft();
    public static Overlay overlay = new Overlay();
    public static JsonObject itemNameToId;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        TickEvent tick = new TickEvent();
        MinecraftForge.EVENT_BUS.register(tick);
        MinecraftForge.EVENT_BUS.register(overlay);

        Runnable xyz = () -> {
            try {
                Request req = new Request();
                JsonObject res = null;
                try {
                    res = req.jsonGet("https://undercovergoose.github.io/bazaarNotifier/bazaarConversion.json");
                    if(res == null) {
                        System.err.println("BetterBazaarNotifierThatActuallyWorks > Failed to fetch conversion table. (43)");
                        return;
                    }
                }catch(Exception ignored) {
                    System.err.println("BetterBazaarNotifierThatActuallyWorks > Failed to fetch conversion table. (47)");
                    return;
                }
                itemNameToId = res;
            }catch(Exception ignored) {
                System.err.println("BetterBazaarNotifierThatActuallyWorks > Failed to fetch conversion table. (51)");
            }
        };
        new Thread(xyz).start();
    }
}
