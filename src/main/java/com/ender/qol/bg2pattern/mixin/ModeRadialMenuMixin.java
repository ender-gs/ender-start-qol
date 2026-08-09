package com.ender.qol.bg2pattern.mixin;

import com.direwolf20.buildinggadgets2.client.screen.ModeRadialMenu;
import com.direwolf20.buildinggadgets2.client.screen.ModeRadialMenu.ScreenPosition;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.ender.qol.bg2pattern.SchematicToPatternConverter;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.Predicate;

/**
 * Mixin into Building Gadgets 2 ModeRadialMenu to inject the "Copy to Pattern"
 * button
 * and manage its visibility in tick() right after the conditionalButtons loop
 * finishes.
 */
@Mixin(value = ModeRadialMenu.class, remap = false)
public abstract class ModeRadialMenuMixin extends Screen {

    private static final Logger LOGGER = LogManager.getLogger();

    @Shadow
    @Final
    private List<Button> conditionalButtons;

    @Shadow
    private BaseMode mode;

    @Shadow
    private ItemStack getGadget() {
        throw new AssertionError();
    }

    @Shadow
    private void updateButtons() {
        throw new AssertionError();
    }

    protected ModeRadialMenuMixin(Component title) {
        super(title);
    }

    private Button createPositionedIconActionable(Component message, String icon, ScreenPosition position,
            boolean isSelectable, Predicate<Boolean> action) {
        try {
            Class<?> clazz = Class
                    .forName("com.direwolf20.buildinggadgets2.client.screen.ModeRadialMenu$PositionedIconActionable");
            Constructor<?> ctor = clazz.getDeclaredConstructor(Component.class, String.class, ScreenPosition.class,
                    boolean.class, Predicate.class);
            ctor.setAccessible(true);
            return (Button) ctor.newInstance(message, icon, position, isSelectable, action);
        } catch (Throwable t) {
            LOGGER.error("[EnderQoL] Reflection error creating PositionedIconActionable: {}", t.getMessage());
            return null;
        }
    }

    @Inject(method = "m_7856_", at = @At("TAIL"), remap = false)
    private void onModeRadialMenuInitTail(CallbackInfo ci) {
        ItemStack tool = getGadget();

        if (!tool.isEmpty() && tool.getItem() instanceof GadgetCopyPaste) {
            if (this.mode != null && GadgetNBT.hasCopyUUID(tool) && this.mode.getId().getPath().equals("paste")) {
                Button copyToPattern = createPositionedIconActionable(
                        Component.translatable("enderqol.ae2bg2.copy_to_pattern"),
                        "copypaste_materiallist",
                        ScreenPosition.RIGHT,
                        false,
                        send -> {
                            if (send) {
                                if (this.mode != null && GadgetNBT.hasCopyUUID(tool)
                                        && this.mode.getId().getPath().equals("paste")) {
                                    Player player = getMinecraft() != null ? getMinecraft().player : null;
                                    if (player != null) {
                                        SchematicToPatternConverter.onToPatternButtonClicked(player, tool);
                                    }
                                }
                            }
                            return false;
                        });

                if (copyToPattern == null)
                    return;

                Player player = getMinecraft() != null ? getMinecraft().player : null;
                boolean hasTerminal = SchematicToPatternConverter.hasWirelessPatternTerminal(player);
                boolean hasSelection = SchematicToPatternConverter.hasValidSelection(tool);
                copyToPattern.active = hasSelection && hasTerminal;

                addRenderableWidget(copyToPattern);
                if (this.conditionalButtons != null) {
                    this.conditionalButtons.add(copyToPattern);
                }
            }
        }
    }

    @Inject(method = "m_86600_", at = @At("TAIL"), remap = false)
    private void onAfterConditionalButtonsLoopInTick(CallbackInfo ci) {
        ItemStack tool = getGadget();
        if (!tool.isEmpty() && tool.getItem() instanceof GadgetCopyPaste) {
            boolean showButton = this.mode != null && GadgetNBT.hasCopyUUID(tool)
                    && this.mode.getId().getPath().equals("paste");
            Component targetMessage = Component.translatable("enderqol.ae2bg2.copy_to_pattern");

            Player player = getMinecraft() != null ? getMinecraft().player : null;
            boolean hasTerminal = SchematicToPatternConverter.hasWirelessPatternTerminal(player);
            boolean hasSelection = SchematicToPatternConverter.hasValidSelection(tool);
            boolean isActive = hasSelection && hasTerminal;

            boolean changed = false;
            if (this.conditionalButtons != null) {
                for (Button button : this.conditionalButtons) {
                    if (button.getMessage().equals(targetMessage)) {
                        if (button.visible != showButton) {
                            button.visible = showButton;
                            changed = true;
                        }
                        if (button.active != isActive) {
                            button.active = isActive;
                        }
                    }
                }
            }

            if (changed) {
                this.updateButtons();
            }
        }
    }
}
