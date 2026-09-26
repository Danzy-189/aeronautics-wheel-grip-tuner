package dev.danzy189.wheelgriptuner.network;

import dev.danzy189.wheelgriptuner.WheelGripTuner;
import dev.danzy189.wheelgriptuner.item.GripTunerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CycleTunerModePayload(int direction) implements CustomPacketPayload {
    public static final Type<CycleTunerModePayload> TYPE =
            new Type<>(WheelGripTuner.id("cycle_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CycleTunerModePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    CycleTunerModePayload::direction,
                    CycleTunerModePayload::new
            );

    public static void handle(CycleTunerModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof GripTunerItem)) {
                stack = player.getOffhandItem();
            }
            if (!(stack.getItem() instanceof GripTunerItem)) return;

            int direction = payload.direction() < 0 ? -1 : 1;
            GripTunerItem.TuningMode mode = GripTunerItem.cycleMode(stack, direction);
            player.displayClientMessage(
                    Component.translatable(
                            "message.wheel_grip_tuner.mode",
                            mode.title()
                    ).withStyle(ChatFormatting.AQUA),
                    true
            );
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}