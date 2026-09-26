# Aeronautics Wheel Grip Tuner

NeoForge 1.21.1 addon for **Create: Aeronautics / Offroad**. It adds a handheld tool that changes each wheel mount's lateral grip multiplier.

## Why

Aeronautics wheel mounts apply lateral tire force and vertical suspension
damping inside `WheelMountBlockEntity#sable$physicsTick`. This addon turns both
into one understandable per-wheel "road grip" setting.

## Usage

1. Craft the **Wheel Grip Tuner**.
2. Right-click an Offroad wheel mount to increase road grip by `25%`.
3. Sneak + right-click to decrease road grip by `25%`.
4. The allowed range is `25%` to `300%`; default is `100%`.
5. Higher values now increase vertical suspension damping as well as lateral
   traction, making the wheels feel more firmly attached to the road.

The lateral force uses a progressive square-root curve while the vertical
damping grows faster (and is capped). This makes larger values feel like
stronger road holding without returning to the old excessive rollover torque.

## Compatibility

- Minecraft `1.21.1`
- NeoForge `21.1.235` through `21.1.248`
- Requires the `offroad` module included with Create: Aeronautics
- Create: Tracks+ is optional. Both tuners can coexist; this mod stores its own multiplier and NBT key.

## Technical notes

- The tuning pattern follows Tracks+'s suspension key: server-side adjustment, per-wheel persistent data, block-entity sync, and action-bar feedback.
- A Mixin scales Aeronautics' lateral-force coefficient progressively and
  increases vertical suspension damping at higher settings.
- Grip is saved as `WheelGripTunerMultiplier` in each wheel mount's block-entity NBT.
- The Mixin targets the Offroad class by name, so the project compiles without redistributing Aeronautics code or binaries.

## Building

Install JDK 21 and Gradle 8.10+, then run:

```bash
gradle build
```

The jar is written to `build/libs/`.

## Status

This is an initial source implementation. Test it in a disposable NeoForge 1.21.1 instance before using it in an important world. The injection intentionally fails fast if an Aeronautics update changes the targeted lateral-force constant.

## License

MIT. Create: Aeronautics and Create: Tracks+ are separate projects under their own licenses.
