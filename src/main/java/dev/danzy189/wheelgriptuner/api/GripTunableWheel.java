package dev.danzy189.wheelgriptuner.api;

import net.minecraft.core.BlockPos;

/** Implemented on Aeronautics wheel mounts at runtime by the mixin. */
public interface GripTunableWheel {
    double wheelGripTuner$adjust(String key, int direction);
    BlockPos wheelGripTuner$getDifferentialPartner();
    void wheelGripTuner$pairDifferential(BlockPos partner);
    void wheelGripTuner$unpairDifferential();
    DifferentialMode wheelGripTuner$cycleDifferential(int direction);
    double wheelGripTuner$getSurfaceGrip();

    enum DifferentialMode {
        OPEN,
        LIMITED_SLIP,
        LOCKED;

        public DifferentialMode cycle(int direction) {
            DifferentialMode[] values = values();
            return values[Math.floorMod(ordinal() + direction, values.length)];
        }
    }
}
