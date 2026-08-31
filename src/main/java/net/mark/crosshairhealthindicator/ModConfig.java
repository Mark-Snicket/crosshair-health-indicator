package net.mark.crosshairhealthindicator;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = CrosshairHealthIndicatorClient.MOD_ID)
public class ModConfig implements ConfigData {

    public boolean enableMod = true;
    public boolean displayHealthInHearts = false;

    public boolean alwaysShow = false;

    @ConfigEntry.Gui.Excluded
    public static final int TEXT_COLOR_DEFAULT = 0xFFFFFF;
    @ConfigEntry.ColorPicker
    public int textColor = TEXT_COLOR_DEFAULT;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 2)
    public int decimalPlaces = 1;

    public int xOffset = 0;
    public int yOffset = 0;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    WarningColor warningColor = new WarningColor();
    public static class WarningColor implements ConfigData {

        public boolean enableWarningColor = false;

        @ConfigEntry.Gui.Excluded
        public static final int WARNING_COLOR_DEFAULT = 0xFF0000;
        @ConfigEntry.ColorPicker
        public int warningColor = WARNING_COLOR_DEFAULT;

        public int changeColorBelowHealth = 6;
    }
}