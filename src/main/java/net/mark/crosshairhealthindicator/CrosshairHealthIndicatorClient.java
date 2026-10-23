package net.mark.crosshairhealthindicator;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
                Identifier.fromNamespaceAndPath(MOD_ID, "crosshair-health"),
                CrosshairHealthIndicatorClient::renderIndicator
        );
    }


    private static void renderIndicator(GuiGraphicsExtractor graphicsExtractor, DeltaTracker tracker) {
        Minecraft minecraft = Minecraft.getInstance();

        if (!shouldRender(minecraft)) return;

        int x = ((graphicsExtractor.guiWidth() - 15) / 2 + 8) + config.positionModification.offsetX;
        int y = ((graphicsExtractor.guiHeight() - 15) / 2 + 13) + config.positionModification.offsetY;

        float health = minecraft.player.getHealth() + (config.absorptionHandling.addToTotal ? minecraft.player.getAbsorptionAmount() : 0);
        String text = config.displayHealthInHearts ? getHeartsAsFormattedString(health) : getHealthAsFormattedString(health);

        graphicsExtractor.centeredText(
                minecraft.font,
                text,
                x, y,
                0xFF000000 | getIndicatorColor(text)
        );

        if (config.absorptionHandling.createDisplay) {
            float absorption = minecraft.player.getAbsorptionAmount();
            if (config.absorptionHandling.showWhenZero || absorption > 0) {
                String absorptionText = config.displayHealthInHearts ? getHeartsAsFormattedString(absorption) : getHealthAsFormattedString(absorption);

                graphicsExtractor.centeredText(
                        minecraft.font,
                        absorptionText,
                        x + config.absorptionHandling.offsetX,
                        y + config.absorptionHandling.offsetY,
                        0xFF000000 | config.absorptionHandling.color
                );
            }
        }
    }

    private static boolean shouldRender(Minecraft minecraft) {
        if (!config.enableMod) return false;
        if (minecraft.player == null) return false;
        if (!minecraft.options.getCameraType().isFirstPerson()) return false;
        if (config.alwaysShow) return true;
        return minecraft.player.gameMode() == GameType.SURVIVAL || minecraft.player.gameMode() == GameType.ADVENTURE;
    }

    private static int getIndicatorColor(String healthString) {
        if (config.warningColor.enableWarningColor) {
            float health = Float.parseFloat(healthString);

            if (health <= config.warningColor.useColorBelow) {
                return config.warningColor.color;
            }
        }
        return config.color;
    }

    public static String getHealthAsFormattedString(float health) {
        if (!config.preciseHealth) {
            if (health % 1 >= 0) health++; // always round up, as minecraft does with hearts
            return String.valueOf((int) health);
        }

        if (health >= 10) {
            return String.valueOf(Math.round(health));
        }
        return String.valueOf((float) (Math.round(health * 10)) / 10); // strips health down to a single decimal digit

    }

    public static String getHeartsAsFormattedString(float health) {
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