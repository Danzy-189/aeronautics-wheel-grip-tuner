package dev.danzy189.wheelgriptuner;

import dev.danzy189.wheelgriptuner.item.GripTunerItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
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
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(GRIP_TUNER.get());
        }
    }
}
