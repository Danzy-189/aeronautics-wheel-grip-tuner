package dev.danzy189.wheelgriptuner.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.danzy189.wheelgriptuner.api.GripTunableWheel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.ryanhcode.offroad.content.blocks.wheel_mount.WheelMountBlockEntity", remap = false)
public abstract class WheelMountGripMixin implements GripTunableWheel {
    @Unique private static final String WHEEL_GRIP_TUNER_NBT = "WheelGripTunerMultiplier";
    @Unique private static final double WHEEL_GRIP_TUNER_MIN = 0.25;
    @Unique private static final double WHEEL_GRIP_TUNER_MAX = 3.00;
    @Unique private static final String RIDE_HEIGHT_NBT = "WheelGripTunerRideHeight";
    @Unique private static final String TRAVEL_NBT = "WheelGripTunerTravel";
    @Unique private static final String CONTACT_RADIUS_NBT = "WheelGripTunerContactRadius";
    @Unique private static final String DIFF_PARTNER_NBT = "WheelGripTunerDiffPartner";
    @Unique private static final String DIFF_MODE_NBT = "WheelGripTunerDiffMode";

    @Unique private double wheelGripTuner$gripMultiplier = 1.0;
    @Unique private double wheelGripTuner$rideHeight = 0.0;
    @Unique private double wheelGripTuner$travelMultiplier = 1.0;
    @Unique private double wheelGripTuner$contactRadius = 1.0;
    @Unique private long wheelGripTuner$diffPartner = Long.MIN_VALUE;
    @Unique private DifferentialMode wheelGripTuner$diffMode = DifferentialMode.LIMITED_SLIP;

    @Shadow(remap = false)
    private double touchingFriction;

    @Override
    public double wheelGripTuner$adjust(String key, int direction) {
        double value;
        switch (key) {
            case "grip" -> {
                wheelGripTuner$gripMultiplier = wheelGripTuner$snap(
                        wheelGripTuner$gripMultiplier,
                        direction,
                        0.25,
                        0.25,
                        3.0
                );
                value = wheelGripTuner$gripMultiplier;
            }
            case "ride_height" -> {
                wheelGripTuner$rideHeight = wheelGripTuner$snap(
                        wheelGripTuner$rideHeight,
                        direction,
                        0.05,
                        -0.30,
                        0.60
                );
                value = wheelGripTuner$rideHeight;
            }
            case "suspension_travel" -> {
                wheelGripTuner$travelMultiplier = wheelGripTuner$snap(
                        wheelGripTuner$travelMultiplier,
                        direction,
                        0.10,
                        0.50,
                        2.00
                );
                value = wheelGripTuner$travelMultiplier;
            }
            case "contact_radius" -> {
                wheelGripTuner$contactRadius = wheelGripTuner$snap(
                        wheelGripTuner$contactRadius,
                        direction,
                        0.05,
                        0.75,
                        1.30
                );
                value = wheelGripTuner$contactRadius;
            }
            default -> {
                return 0.0;
            }
        }
        wheelGripTuner$markChangedAndSync();
        return value;
    }

    @Override
    public BlockPos wheelGripTuner$getDifferentialPartner() {
        return wheelGripTuner$diffPartner == Long.MIN_VALUE
                ? null
                : BlockPos.of(wheelGripTuner$diffPartner);
    }

    @Override
    public void wheelGripTuner$pairDifferential(BlockPos partner) {
        wheelGripTuner$diffPartner = partner.asLong();
        wheelGripTuner$diffMode = DifferentialMode.LIMITED_SLIP;
        wheelGripTuner$markChangedAndSync();
    }

    @Override
    public void wheelGripTuner$unpairDifferential() {
        wheelGripTuner$diffPartner = Long.MIN_VALUE;
        wheelGripTuner$diffMode = DifferentialMode.LIMITED_SLIP;
        wheelGripTuner$markChangedAndSync();
    }

    @Override
    public DifferentialMode wheelGripTuner$cycleDifferential(int direction) {
        wheelGripTuner$diffMode = wheelGripTuner$diffMode.cycle(direction);
        wheelGripTuner$markChangedAndSync();
        return wheelGripTuner$diffMode;
    }

    @Override
    public double wheelGripTuner$getSurfaceGrip() {
        return touchingFriction;
    }

    @Unique
    private static double wheelGripTuner$snap(
            double value,
            int direction,
            double step,
            double min,
            double max
    ) {
        return Mth.clamp(
                Math.round((value + direction * step) / step) * step,
                min,
                max
        );
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
        tag.putDouble(RIDE_HEIGHT_NBT, wheelGripTuner$rideHeight);
        tag.putDouble(TRAVEL_NBT, wheelGripTuner$travelMultiplier);
        tag.putDouble(CONTACT_RADIUS_NBT, wheelGripTuner$contactRadius);
        tag.putInt(DIFF_MODE_NBT, wheelGripTuner$diffMode.ordinal());
        if (wheelGripTuner$diffPartner != Long.MIN_VALUE) {
            tag.putLong(DIFF_PARTNER_NBT, wheelGripTuner$diffPartner);
        } else {
            tag.remove(DIFF_PARTNER_NBT);
        }
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
        if (tag.contains(RIDE_HEIGHT_NBT)) {
            wheelGripTuner$rideHeight = Mth.clamp(tag.getDouble(RIDE_HEIGHT_NBT), -0.30, 0.60);
        }
        if (tag.contains(TRAVEL_NBT)) {
            wheelGripTuner$travelMultiplier = Mth.clamp(tag.getDouble(TRAVEL_NBT), 0.50, 2.00);
        }
        if (tag.contains(CONTACT_RADIUS_NBT)) {
            wheelGripTuner$contactRadius = Mth.clamp(tag.getDouble(CONTACT_RADIUS_NBT), 0.75, 1.30);
        }
        wheelGripTuner$diffPartner = tag.contains(DIFF_PARTNER_NBT)
                ? tag.getLong(DIFF_PARTNER_NBT)
                : Long.MIN_VALUE;
        if (tag.contains(DIFF_MODE_NBT)) {
            DifferentialMode[] modes = DifferentialMode.values();
            wheelGripTuner$diffMode = modes[Math.floorMod(tag.getInt(DIFF_MODE_NBT), modes.length)];
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
        return original * Math.sqrt(
                wheelGripTuner$gripMultiplier * wheelGripTuner$contactRadius
        );
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

    /**
     * Changes both the physical and client-side effective tire radius while
     * leaving the rendered wheel model untouched.
     */
    @ModifyExpressionValue(
            method = {"sable$physicsTick", "tick"},
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/offroad/content/components/TireLike;radius()F"
            ),
            remap = false
    )
    private float wheelGripTuner$scaleContactRadius(float original) {
        return (float) (original * wheelGripTuner$contactRadius);
    }

    /**
     * Aeronautics uses 0.65 blocks as its normal suspension extension. Travel
     * scales the available movement and ride height offsets the resting body.
     */
    @ModifyExpressionValue(
            method = {"sable$physicsTick", "computeMaxExtension"},
            at = @At(value = "CONSTANT", args = "doubleValue=0.65"),
            remap = false
    )
    private double wheelGripTuner$tuneSuspensionExtension(double original) {
        return Mth.clamp(
                original * wheelGripTuner$travelMultiplier + wheelGripTuner$rideHeight,
                0.20,
                1.60
        );
    }

    /**
     * Approximate axle differential using the two wheels' live surface
     * friction. Open mode is limited by the partner wheel, LSD transfers part
     * of that loss, and locked mode cancels the local surface drive reduction.
     */
    @ModifyExpressionValue(
            method = "sable$physicsTick",
            at = @At(value = "CONSTANT", args = "doubleValue=1.75"),
            remap = false
    )
    private double wheelGripTuner$applyDifferential(double original) {
        BlockPos partnerPos = wheelGripTuner$getDifferentialPartner();
        if (partnerPos == null) return original;

        BlockEntity self = (BlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) return original;
        BlockEntity partnerEntity = level.getBlockEntity(partnerPos);
        if (!(partnerEntity instanceof GripTunableWheel partner)) return original;

        double own = Mth.clamp(touchingFriction, 0.10, 1.0);
        double other = Mth.clamp(partner.wheelGripTuner$getSurfaceGrip(), 0.10, 1.0);
        double factor = switch (wheelGripTuner$diffMode) {
            case OPEN -> other;
            case LIMITED_SLIP -> Math.sqrt(other);
            case LOCKED -> Math.min(1.0 / own, 4.0);
        };
        return original * factor;
    }
}
