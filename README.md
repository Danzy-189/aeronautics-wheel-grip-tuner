# Aeronautics Wheel Grip Tuner

NeoForge 1.21.1 addon for **Create: Aeronautics / Offroad**. It adds a handheld tool that changes each wheel mount's lateral grip multiplier.

## Why

Aeronautics wheel mounts apply a lateral tire force inside `WheelMountBlockEntity#sable$physicsTick`. High lateral grip can produce enough roll torque to flip a tall or narrow vehicle during a sharp turn. This addon lets the player lower that force per wheel without changing drive force or suspension stiffness.

## Usage

1. Craft the **Wheel Grip Tuner**.
2. Right-click an Offroad wheel mount to increase grip by `0.10x`.
3. Sneak + right-click to decrease grip by `0.10x`.
4. The allowed range is `0.10x` to `2.00x`; default is `1.00x`.
5. For rollover-prone cars, start around `0.60x–0.80x` on all wheels and test again.

Lower values reduce rollover tendency but also make the vehicle slide more. This is intentional: the tool exposes the grip/rollover tradeoff rather than adding an invisible stabilizing force.

## Compatibility

- Minecraft `1.21.1`
- NeoForge `21.1.235` through `21.1.248`
- Requires the `offroad` module included with Create: Aeronautics
- Create: Tracks+ is optional. Both tuners can coexist; this mod stores its own multiplier and NBT key.

## Technical notes

- The tuning pattern follows Tracks+'s suspension key: server-side adjustment, per-wheel persistent data, block-entity sync, and action-bar feedback.
- A Mixin scales only Aeronautics' `-0.6` lateral-force coefficient.
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
