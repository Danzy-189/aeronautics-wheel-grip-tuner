package dev.danzy189.wheelgriptuner.item;

import dev.danzy189.wheelgriptuner.api.GripTunableWheel;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Locale;

public final class GripTunerItem extends Item {
    private static final String MODE_TAG = "WheelGripTunerMode";
    private static final String FIRST_DIFF_POS_TAG = "WheelGripTunerFirstDiffPos";

    public GripTunerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof GripTunableWheel wheel)) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        int direction = player != null && player.isShiftKeyDown() ? -1 : 1;
        TuningMode mode = getMode(context.getItemInHand());

        if (!level.isClientSide) {
            if (mode == TuningMode.DIFFERENTIAL) {
                tuneDifferential(context, wheel, direction);
            } else {
                double value = wheel.wheelGripTuner$adjust(mode.key, direction);
                displayValue(player, mode, value);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void tuneDifferential(
            UseOnContext context,
            GripTunableWheel wheel,
            int direction
    ) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (player != null && player.isShiftKeyDown()
                && wheel.wheelGripTuner$getDifferentialPartner() != null) {
            BlockPos partnerPos = wheel.wheelGripTuner$getDifferentialPartner();
            BlockEntity partnerEntity = context.getLevel().getBlockEntity(partnerPos);
            wheel.wheelGripTuner$unpairDifferential();
            if (partnerEntity instanceof GripTunableWheel partner) {
                partner.wheelGripTuner$unpairDifferential();
            }
            clearFirstDifferentialPos(stack);
            player.displayClientMessage(
                    Component.translatable("message.wheel_grip_tuner.diff_unpaired")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return;
        }

        if (wheel.wheelGripTuner$getDifferentialPartner() != null) {
            GripTunableWheel.DifferentialMode newMode =
                    wheel.wheelGripTuner$cycleDifferential(direction);
            BlockEntity partnerEntity = context.getLevel().getBlockEntity(
                    wheel.wheelGripTuner$getDifferentialPartner()
            );
            if (partnerEntity instanceof GripTunableWheel partner) {
                while (partner.wheelGripTuner$cycleDifferential(1) != newMode) {
                    // Three modes; loop converges in at most two additional steps.
                }
            }
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.wheel_grip_tuner.diff_mode",
                                Component.translatable("mode.wheel_grip_tuner.diff."
                                        + newMode.name().toLowerCase(Locale.ROOT))
                        ).withStyle(ChatFormatting.AQUA),
                        true
                );
            }
            return;
        }

        BlockPos first = getFirstDifferentialPos(stack);
        if (first == null || first.equals(clickedPos)) {
            setFirstDifferentialPos(stack, clickedPos);
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.wheel_grip_tuner.diff_first")
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
            }
            return;
        }

        BlockEntity firstEntity = context.getLevel().getBlockEntity(first);
        if (!(firstEntity instanceof GripTunableWheel firstWheel)) {
            setFirstDifferentialPos(stack, clickedPos);
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable("message.wheel_grip_tuner.diff_first_missing")
                                .withStyle(ChatFormatting.RED),
                        true
                );
            }
            return;
        }

        firstWheel.wheelGripTuner$pairDifferential(clickedPos);
        wheel.wheelGripTuner$pairDifferential(first);
        clearFirstDifferentialPos(stack);
        if (player != null) {
            player.displayClientMessage(
                    Component.translatable("message.wheel_grip_tuner.diff_paired")
                            .withStyle(ChatFormatting.GREEN),
                    true
            );
        }
    }

    private static void displayValue(Player player, TuningMode mode, double value) {
        if (player == null) return;
        String formatted = mode == TuningMode.RIDE_HEIGHT
                ? String.format(Locale.ROOT, "%+.2f", value)
                : String.format(Locale.ROOT, "%.0f%%", value * 100.0);
        player.displayClientMessage(
                Component.translatable(
                        "message.wheel_grip_tuner.adjusted",
                        mode.title(),
                        formatted
                ).withStyle(ChatFormatting.GOLD),
                true
        );
    }

    public static TuningMode cycleMode(ItemStack stack, int direction) {
        TuningMode[] values = TuningMode.values();
        TuningMode next = values[Math.floorMod(getMode(stack).ordinal() + direction, values.length)];
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(MODE_TAG, next.ordinal());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return next;
    }

    public static TuningMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        TuningMode[] values = TuningMode.values();
        return values[Math.floorMod(tag.getInt(MODE_TAG), values.length)];
    }

    private static BlockPos getFirstDifferentialPos(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(FIRST_DIFF_POS_TAG)
                ? BlockPos.of(tag.getLong(FIRST_DIFF_POS_TAG))
                : null;
    }

    private static void setFirstDifferentialPos(ItemStack stack, BlockPos pos) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putLong(FIRST_DIFF_POS_TAG, pos.asLong());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static void clearFirstDifferentialPos(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(FIRST_DIFF_POS_TAG);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public enum TuningMode {
        GRIP("grip"),
        RIDE_HEIGHT("ride_height"),
        SUSPENSION_TRAVEL("suspension_travel"),
        CONTACT_RADIUS("contact_radius"),
        DIFFERENTIAL("differential");

        public final String key;

        TuningMode(String key) {
            this.key = key;
        }

        public Component title() {
            return Component.translatable("mode.wheel_grip_tuner." + key);
        }
    }
}
