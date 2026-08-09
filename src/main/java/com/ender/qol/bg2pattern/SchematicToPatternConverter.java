package com.ender.qol.bg2pattern;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.client.Hotkey;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.HotkeyPacket;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.locator.MenuLocator;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.ItemStackKey;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.ender.qol.EnderQoL;
import de.mari_023.ae2wtlib.wut.WUTHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Feature 1: Copy Schematic to Pattern.
 * 
 * Interfacing Building Gadgets 2 schematics/templates with Applied Energistics
 * 2 (AE2) Wireless Pattern Encoding Terminal.
 */
@Mod.EventBusSubscriber(modid = EnderQoL.MOD_ID, value = Dist.CLIENT)
public class SchematicToPatternConverter {

    private static final Logger LOGGER = LogManager.getLogger();

    private static Map<ItemStackKey, Integer> pendingRequiredItems = null;

    /**
     * Catches the exact frame the Pattern Encoding Terminal screen opens.
     * Guaranteed event-driven container population with 0 tick polling overhead.
     */
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (pendingRequiredItems == null || !(event.getScreen() instanceof PatternEncodingTermScreen)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc != null ? mc.player : null;

        if (player != null && player.containerMenu instanceof PatternEncodingTermMenu menu) {
            populatePatternTerminal(menu, pendingRequiredItems);
            pendingRequiredItems = null;
        }
    }

    /**
     * Verifies if the player has a Wireless Pattern Encoding Terminal in their
     * inventory/curios.
     */
    public static boolean hasWirelessPatternTerminal(Player player) {
        if (player == null)
            return false;
        try {
            MenuLocator locator = WUTHandler.findTerminal(player, "pattern_encoding");
            return locator != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Verifies if the Copy-Paste gadget contains a valid start and end position.
     */
    public static boolean hasValidSelection(ItemStack gadget) {
        if (gadget == null || gadget.isEmpty())
            return false;

        BlockPos startPos = GadgetNBT.getCopyStartPos(gadget);
        BlockPos endPos = GadgetNBT.getCopyEndPos(gadget);

        return startPos != null && !startPos.equals(GadgetNBT.nullPos)
                && endPos != null && !endPos.equals(GadgetNBT.nullPos);
    }

    /**
     * Callback handler when the user clicks the "Copy to Pattern" button.
     */
    public static void onToPatternButtonClicked(Player player, ItemStack gadget) {
        if (player == null || gadget == null || gadget.isEmpty())
            return;

        if (!hasValidSelection(gadget)) {
            player.displayClientMessage(Component.translatable("enderqol.ae2bg2.no_selection"), true);
            return;
        }

        if (!hasWirelessPatternTerminal(player)) {
            player.displayClientMessage(Component.translatable("enderqol.ae2bg2.no_terminal"), true);
            return;
        }

        UUID gadgetUUID = GadgetNBT.getUUID(gadget);
        if (gadgetUUID == null)
            return;

        BG2DataClient.isClientUpToDate(gadget);

        ArrayList<StatePos> blockPositions = BG2DataClient.getLookupFromUUID(gadgetUUID);
        if (blockPositions == null || blockPositions.isEmpty()) {
            LOGGER.warn("[EnderQoL] Copy selection data is syncing from server for gadget UUID: {}", gadgetUUID);
            return;
        }

        Map<ItemStackKey, Integer> requiredItems = StatePos.getItemList(blockPositions);
        if (requiredItems == null || requiredItems.isEmpty())
            return;

        // Store pending items and request server to open terminal container
        pendingRequiredItems = requiredItems;
        NetworkHandler.instance()
                .sendToServer(new HotkeyPacket(new Hotkey("wireless_pattern_encoding_terminal", null)));
    }

    /**
     * Populates the Wireless Pattern Encoding Terminal GUI slots with the required
     * items.
     */
    public static void populatePatternTerminal(PatternEncodingTermMenu menu, Map<ItemStackKey, Integer> requiredItems) {
        if (menu == null || requiredItems == null || requiredItems.isEmpty())
            return;

        menu.setMode(EncodingMode.PROCESSING);
        menu.clear();

        var inputSlots = menu.getProcessingInputSlots();
        if (inputSlots == null || inputSlots.length == 0)
            return;

        int slotIdx = 0;
        for (Map.Entry<ItemStackKey, Integer> entry : requiredItems.entrySet()) {
            if (slotIdx >= inputSlots.length)
                break;

            ItemStackKey itemKey = entry.getKey();
            int totalCount = entry.getValue();

            if (itemKey != null && itemKey.item != null && totalCount > 0) {
                ItemStack testStack = itemKey.getStack(1);
                if (testStack == null || testStack.isEmpty()) {
                    continue;
                }
                AEItemKey aeKey = AEItemKey.of(testStack);
                if (aeKey == null) {
                    continue;
                }

                // Wrap full count to bypass 64 stack limit in single slot
                ItemStack wrappedStack = GenericStack.wrapInItemStack(aeKey, totalCount);
                var slot = inputSlots[slotIdx++];

                NetworkHandler.instance().sendToServer(
                        new InventoryActionPacket(InventoryAction.SET_FILTER, slot.index, wrappedStack));
            }
        }
    }
}
