package net.mark.crosshairhealthindicator;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
        int y = ((graphics.guiHeight() - 15) / 2 + 13) + config.positionModification.offsetY;

        float health = minecraft.player.getHealth() + (config.absorptionHandling.addToTotal ? minecraft.player.getAbsorptionAmount() : 0);
        String text = config.displayHealthInHearts ? getHeartsAsFormattedString(health) : getHealthAsFormattedString(health);

        graphics.drawCenteredString(
                minecraft.font,
                text,
                x, y,
                0xFF000000 | getIndicatorColor(text)
        );

        if (config.absorptionHandling.createDisplay) {
            float absorption = minecraft.player.getAbsorptionAmount();
            if (config.absorptionHandling.showWhenZero || absorption > 0) {
                String absorptionText = config.displayHealthInHearts ? getHeartsAsFormattedString(absorption) : getHealthAsFormattedString(absorption);

                graphics.drawCenteredString(
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

            if (health <= config.warningColor.changeColorBelowHealth) {
                return config.warningColor.color;
            }
        }
        return config.color;
    }

    public static String getHealthAsFormattedString(float health) {
        if (!config.preciseHealth) {
            return String.valueOf((int) Math.ceil(health)); // cast to int to strip decimals
        }

        if (health >= 10) {
            return String.valueOf(Math.round(health));
        }
        return String.format("%.1f", health);

    }

    public static String getHeartsAsFormattedString(float health) {
        if (!config.preciseHealth) {
            float roundedUpHealth = (float) Math.ceil(health);

            // strip decimals if hearts are even
            if (roundedUpHealth % 2 == 0) {
                return String.valueOf((int) roundedUpHealth / 2);
            } else {
                return String.valueOf(roundedUpHealth / 2);
            }
        }

        if (health / 2 >= 10) {
            return String.valueOf(Math.round(health / 2));
        }
        return String.format("%.1f", health / 2);
    }
}