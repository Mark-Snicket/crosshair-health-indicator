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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;

public class CrosshairHealthIndicatorClient implements ClientModInitializer {
    public static final String MOD_ID = "crosshair-health-indicator";
    public static ModConfig config;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CROSSHAIR,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "crosshair-health"),
                CrosshairHealthIndicatorClient::renderIndicator
        );
    }


    private static void renderIndicator(GuiGraphics graphics, DeltaTracker tracker) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!shouldRender(minecraft)) return;

        int x = ((graphics.guiWidth() - 15) / 2 + 8) + config.positionModification.offsetX;
        int y = ((graphics.guiHeight() - 15) / 2 + 13 + config.positionModification.offsetY);
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
            float health = Math.round(minecraft.player.getHealth() * 10) / 10.0f; // round as in getPlayerHealthAsFormattedString()

            if (health <= config.warningColor.changeColorBelowHealth) {
                return 0xFF000000 | config.warningColor.warningColor;
            }
        }
        return 0xFF000000 | config.textColor;
    }

    public static String getPlayerHealthAsFormattedString(LocalPlayer player) {
        float health = player.getHealth();

        if (!config.preciseHealth) {
            if (health % 1 >= 0) health++; // always round up, as minecraft does with hearts
            return String.valueOf((int) health);
        }

        if (health >= 10) {
            return String.valueOf(Math.round(health));
        }
        return String.valueOf((float) (Math.round(health * 10)) / 10); // strips health down to a single decimal digit

    }

    public static String getPlayerHeartsAsFormattedString(LocalPlayer player) {
        float health = player.getHealth();
        float hearts = health / 2; // / 2 to get playerHealth in hearts

        if (!config.preciseHealth) {
            if (health % 1 >= 0) health = (int) health + 1; // always round up, as minecraft does with hearts
            if (health % 2 == 1) return String.valueOf(health / 2); // return float if uneven
            return String.valueOf((int) health / 2);
        }

        if (hearts >= 10) {
            return String.valueOf(Math.round(hearts));
        }
        return String.valueOf((float) (Math.round(hearts * 10)) / 10); // strips hearts down to a single decimal digit

    }
}