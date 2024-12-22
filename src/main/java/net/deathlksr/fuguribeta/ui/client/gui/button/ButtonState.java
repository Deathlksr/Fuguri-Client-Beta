/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.ui.client.gui.button;

import net.deathlksr.fuguribeta.FuguriBeta;
import net.deathlksr.fuguribeta.features.module.modules.client.ClickGUIModule;

public class ButtonState {
    public int x, y;
    public int width, height;
    public int hoverFade = 0;
    public String text;

    public ButtonState(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = 132;
        this.height = 12;
    }

    public boolean updateHover(int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        if (hovered) {
            if (hoverFade < 40) hoverFade += 10;
        } else {
            if (hoverFade > 0) {
                hoverFade -= 10;
                FuguriBeta.INSTANCE.getTipSoundManager().getButtonpressSound().asyncPlay(ClickGUIModule.INSTANCE.getVolume());
            }
        }

        return hovered;
    }

    public int getHoverFade() {
        return hoverFade;
    }

    public void setHoverFade(int hoverFade) {
        this.hoverFade = hoverFade;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
