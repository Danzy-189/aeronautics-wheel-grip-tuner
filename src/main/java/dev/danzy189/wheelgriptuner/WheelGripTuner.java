package dev.danzy189.wheelgriptuner;

import dev.danzy189.wheelgriptuner.item.GripTunerItem;
import dev.danzy189.wheelgriptuner.network.CycleTunerModePayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(WheelGripTuner.MOD_ID)
public final class WheelGripTuner {
    public static final String MOD_ID = "wheel_grip_tuner";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredItem<Item> GRIP_TUNER = ITEMS.registerItem(
            "grip_tuner",
            properties -> new GripTunerItem(properties.stacksTo(1))
    );

    public WheelGripTuner(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(WheelGripTuner::addCreativeTabContents);
        modEventBus.addListener(WheelGripTuner::registerPayloads);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(GRIP_TUNER.get());
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                CycleTunerModePayload.TYPE,
                CycleTunerModePayload.STREAM_CODEC,
                CycleTunerModePayload::handle
        );
    }
}
