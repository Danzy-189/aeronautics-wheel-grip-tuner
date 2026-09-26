package dev.danzy189.wheelgriptuner.mixin;

import dev.danzy189.wheelgriptuner.api.GripTunableWheel;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity", remap = false)
public abstract class WheelMountGripMixin implements GripTunableWheel {
    @Unique private static final String WHEEL_GRIP_TUNER_NBT = "WheelGripTunerMultiplier";
    @Unique private static final double WHEEL_GRIP_TUNER_MIN = 0.25;
    @Unique private static final double WHEEL_GRIP_TUNER_MAX = 3.00;
    @Unique private static final double WHEEL_GRIP_TUNER_STEP = 0.25;

    @Unique private double wheelGripTuner$gripMultiplier = 1.0;

    @Override
    public double wheelGripTuner$adjustGrip(int direction) {
        double raw = wheelGripTuner$gripMultiplier + direction * WHEEL_GRIP_TUNER_STEP;
        wheelGripTuner$gripMultiplier = Mth.clamp(
                Math.round(raw / WHEEL_GRIP_TUNER_STEP) * WHEEL_GRIP_TUNER_STEP,
                WHEEL_GRIP_TUNER_MIN,
                WHEEL_GRIP_TUNER_MAX
        );
        wheelGripTuner$markChangedAndSync();
        return wheelGripTuner$gripMultiplier;
    }

    @Override
    public double wheelGripTuner$getGrip() {
        return wheelGripTuner$gripMultiplier;
    }

    @Override
    public void wheelGripTuner$resetGrip() {
        wheelGripTuner$gripMultiplier = 1.0;
        wheelGripTuner$markChangedAndSync();
    }

    @Unique
    private void wheelGripTuner$markChangedAndSync() {
        BlockEntity self = (BlockEntity) (Object) this;
        self.setChanged();
        Level level = self.getLevel();
        if (level != null && !level.isClientSide) {
            BlockState state = self.getBlockState();
            level.sendBlockUpdated(self.getBlockPos(), state, state, 3);
        }
    }

    @Inject(method = "write", at = @At("TAIL"), remap = false)
    private void wheelGripTuner$write(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket,
            CallbackInfo ci
    ) {
        tag.putDouble(WHEEL_GRIP_TUNER_NBT, wheelGripTuner$gripMultiplier);
    }

    @Inject(method = "read", at = @At("TAIL"), remap = false)
    private void wheelGripTuner$read(
            CompoundTag tag,
            HolderLookup.Provider registries,
            boolean clientPacket,
            CallbackInfo ci
    ) {
        if (tag.contains(WHEEL_GRIP_TUNER_NBT)) {
            wheelGripTuner$gripMultiplier = Mth.clamp(
                    tag.getDouble(WHEEL_GRIP_TUNER_NBT),
                    WHEEL_GRIP_TUNER_MIN,
                    WHEEL_GRIP_TUNER_MAX
            );
        }
    }

    /**
     * Aeronautics applies lateral tire force with the -0.6 coefficient in
     * WheelMountBlockEntity#sable$physicsTick. Scaling that coefficient changes
     * lateral grip without changing drive force or suspension stiffness.
     */
    @ModifyExpressionValue(
            method = "sable$physicsTick",
            at = @At(value = "CONSTANT", args = "doubleValue=-0.6"),
            remap = false
    )
    private double wheelGripTuner$scaleLateralGrip(double original) {
        // Traction grows progressively, but not fast enough to recreate the
        // excessive rollover torque of the original linear multiplier.
        return original * Math.sqrt(wheelGripTuner$gripMultiplier);
    }

    /**
     * More grip also means more vertical suspension damping. This is the
     * "road holding" part of the setting: upward motion of the body produces a
     * stronger downward damping force and downward motion is cushioned harder,
     * so the wheels stay in contact instead of the vehicle snapping into a
     * roll. The cap keeps extreme settings numerically well behaved.
     */
    @ModifyExpressionValue(
            method = "sable$physicsTick",
            at = @At(
                    value = "FIELD",
                    target = "Lorg/joml/Vector3d;y:D"
            ),
            remap = false
    )
    private double wheelGripTuner$scaleVerticalDamping(double verticalVelocity) {
        double roadHolding = Mth.clamp(
                wheelGripTuner$gripMultiplier * wheelGripTuner$gripMultiplier,
                0.25,
                4.0
        );
        return verticalVelocity * roadHolding;
    }
}
