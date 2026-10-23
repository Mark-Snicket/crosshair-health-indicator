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

    public boolean preciseHealth = true;

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    PositionModification positionModification = new PositionModification();
    public static class PositionModification implements ConfigData {
        public int offsetX = 0;
        public int offsetY = 0;
    }


    @ConfigEntry.Gui.CollapsibleObject()
    WarningColor warningColor = new WarningColor();
    public static class WarningColor implements ConfigData {
        public boolean enableWarningColor = false;

        @ConfigEntry.Gui.Excluded
        public static final int WARNING_COLOR_DEFAULT = 0xFF0000;
        @ConfigEntry.ColorPicker
        public int warningColor = WARNING_COLOR_DEFAULT;

        public int changeColorBelowHealth = 6;
    }


    @ConfigEntry.Gui.CollapsibleObject()
    AbsorptionHandling absorptionHandling = new AbsorptionHandling();
    public static class AbsorptionHandling implements ConfigData {
        public boolean addToTotal = false;
        public boolean createDisplay = true;
        public boolean showWhenZero = false;

        @ConfigEntry.Gui.Excluded
        public static final int ABSORPTION_COLOR_DEFAULT = 0xD4AF37;
        @ConfigEntry.ColorPicker
        public int color = ABSORPTION_COLOR_DEFAULT;

        public int offsetX = 0;
        public int offsetY = 10;
    }
}