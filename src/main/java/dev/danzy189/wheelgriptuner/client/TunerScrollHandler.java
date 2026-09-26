package dev.danzy189.wheelgriptuner.client;

import dev.danzy189.wheelgriptuner.WheelGripTuner;
import dev.danzy189.wheelgriptuner.item.GripTunerItem;
import dev.danzy189.wheelgriptuner.network.CycleTunerModePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = WheelGripTuner.MOD_ID, value = Dist.CLIENT)
public final class TunerScrollHandler {
    private TunerScrollHandler() {
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (event.isCanceled() || event.getScrollDeltaY() == 0) return;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.screen != null) return;
        if (!(player.getMainHandItem().getItem() instanceof GripTunerItem)
                && !(player.getOffhandItem().getItem() instanceof GripTunerItem)) {
            return;
        }

        int direction = event.getScrollDeltaY() > 0 ? 1 : -1;
        PacketDistributor.sendToServer(new CycleTunerModePayload(direction));
        event.setCanceled(true);
    }
}