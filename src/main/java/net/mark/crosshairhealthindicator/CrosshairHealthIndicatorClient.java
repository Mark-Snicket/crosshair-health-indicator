package net.mark.crosshairhealthindicator;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;

import java.util.Locale;

public class CrosshairHealthIndicatorClient implements ClientModInitializer {
    public static final String MOD_ID = "crosshair-health-indicator";
    public static ModConfig config;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CROSSHAIR,
                Identifier.fromNamespaceAndPath(MOD_ID, "crosshair-health"),
                CrosshairHealthIndicatorClient::renderIndicator
        );
    }


    private static void renderIndicator(GuiGraphics graphics, DeltaTracker tracker) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!shouldRender(minecraft)) return;

        int x = (graphics.guiWidth() - 15) / 2 + 8 + config.xOffset;
        int y = (graphics.guiHeight() - 15) / 2 + 13 + config.yOffset;
        int color = getColor(minecraft);

        String text = config.displayHealthInHearts ? getPlayerHeartsAsFormattedString(minecraft.player) : getPlayerHealthAsFormattedString(minecraft.player);

        graphics.drawCenteredString(
                minecraft.font,
                text,
                x, y,
                color
        );
    }

    private static boolean shouldRender(Minecraft minecraft) {
        if (!config.enableMod) return false;
        if (minecraft.player == null) return false;
        if (!minecraft.options.getCameraType().isFirstPerson()) return false;
        if (config.alwaysShow) return true;
        return minecraft.player.gameMode() == GameType.SURVIVAL || minecraft.player.gameMode() == GameType.ADVENTURE;
    }

    private static int getColor(Minecraft minecraft) {
        if (config.warningColor.enableWarningColor) {
            assert minecraft.player != null;
            float health = roundToDisplayPrecision(minecraft.player.getHealth()); // round as the health is displayed

            if (health <= config.warningColor.changeColorBelowHealth) {
                return 0xFF000000 | config.warningColor.warningColor;
            }
        }
        return 0xFF000000 | config.textColor;
    }


    private static float roundToDisplayPrecision(float value) {
        int decimals = Math.clamp(config.decimalPlaces, 0, 2);

        if (decimals == 0) return (float) Math.ceil(value); // always round up, as minecraft does with hearts

        float factor = (float) Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    private static String formatHealthValue(float value) {
        int decimals = Math.clamp(config.decimalPlaces, 0, 2);
        float rounded = roundToDisplayPrecision(value);

        if (decimals == 0) return String.valueOf((int) rounded);
        return String.format(Locale.ROOT, "%." + decimals + "f", rounded); // Locale.ROOT so the separator is always a dot
    }

    public static String getPlayerHealthAsFormattedString(LocalPlayer player) {
        return formatHealthValue(player.getHealth());
    }

    public static String getPlayerHeartsAsFormattedString(LocalPlayer player) {
        return formatHealthValue(player.getHealth() / 2); // / 2 to get playerHealth in hearts
    }
}