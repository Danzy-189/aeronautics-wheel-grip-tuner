package dev.danzy189.wheelgriptuner.api;

/** Implemented on Aeronautics wheel mounts at runtime by the mixin. */
public interface GripTunableWheel {
    double wheelGripTuner$adjustGrip(int direction);
    double wheelGripTuner$getGrip();
    void wheelGripTuner$resetGrip();
}
