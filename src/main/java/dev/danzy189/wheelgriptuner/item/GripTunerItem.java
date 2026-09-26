package dev.danzy189.wheelgriptuner.item;

import dev.danzy189.wheelgriptuner.api.GripTunableWheel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Locale;

public final class GripTunerItem extends Item {
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

        if (!level.isClientSide) {
            double value = wheel.wheelGripTuner$adjustGrip(direction);
            if (player != null) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.wheel_grip_tuner.adjusted",
                                String.format(Locale.ROOT, "%.0f%%", value * 100.0)
                        ).withStyle(value < 1.0 ? ChatFormatting.GREEN : ChatFormatting.GOLD),
                        true
                );
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
