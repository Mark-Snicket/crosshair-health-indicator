package net.mark.crosshairhealthindicator;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
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
                Identifier.fromNamespaceAndPath(MOD_ID, "crosshair"),
                CrosshairHealthIndicatorClient::renderIndicator
        );
    }


    private static void renderIndicator(GuiGraphicsExtractor graphicsExtractor, DeltaTracker tracker) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!shouldRender(minecraft)) return;

        int x = ((graphicsExtractor.guiWidth() - 15) / 2 + 2) + config.positionModification.offsetX;
        int y = ((graphicsExtractor.guiHeight() - 15) / 2 + 13 + config.positionModification.offsetY);
        int color = getColor(minecraft);

        String text = config.displayHealthInHearts ? getPlayerHeartsAsFormattedString(minecraft.player) : getPlayerHealthAsFormattedString(minecraft.player);

        graphicsExtractor.text(
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

        if (health >= 10) {
            return String.valueOf(Math.round(health));
        }
        return String.valueOf((float) (Math.round(health * 10)) / 10); // strips health down to a single decimal digit

    }

    public static String getPlayerHeartsAsFormattedString(LocalPlayer player) {
        float hearts = player.getHealth() / 2; // / 2 to get playerHealth in hearts

        if (hearts >= 10) {
            return String.valueOf(Math.round(hearts));
        }
        return String.valueOf((float) (Math.round(hearts * 10)) / 10); // strips hearts down to a single decimal digit

    }
}