package io.github.undercovergoose.bazaarNotifier;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import static io.github.undercovergoose.bazaarNotifier.Main.MC;

public class Message {
    private IChatComponent message = new ChatComponentText("");

    public void addText(String text, Object... events) {
        ChatComponentText component = new ChatComponentText(text);
        if(events.length == 0) {
            message.appendSibling(component);
            return;
        }
        ChatStyle style = new ChatStyle();
        for(Object event : events) {
            if(event instanceof HoverEvent) style.setChatHoverEvent((HoverEvent)event);
            if(event instanceof ClickEvent) style.setChatClickEvent((ClickEvent)event);
        }
        component.setChatStyle(style);
        message.appendSibling(component);
    }
    public HoverEvent createHover(String text) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(text));
    }
    public String join(String by, String... lines) {
        String text = "";
        for(String line : lines) {
            if(text.equals("")) text = line;
            else text = text + by + line;
        }
        return text;
    }
    public void send() {
        MC.thePlayer.addChatComponentMessage(message);
    }
}