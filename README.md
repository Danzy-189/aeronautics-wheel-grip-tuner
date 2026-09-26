# Aeronautics Wheel Grip Tuner

NeoForge 1.21.1 addon for **Create: Aeronautics / Offroad**. It adds a handheld tool that changes each wheel mount's lateral grip multiplier.

## Why

Aeronautics wheel mounts apply lateral tire force and vertical suspension
damping inside `WheelMountBlockEntity#sable$physicsTick`. This addon turns both
into one understandable per-wheel "road grip" setting.

## Controls

1. Craft the **Wheel Grip Tuner**.
2. Hold it and scroll the mouse wheel to select a tuning mode.
3. Right-click an Offroad wheel mount to increase the selected value.
4. Sneak + right-click to decrease it.

Available modes:

- **Road grip:** `25–300%`, in `25%` steps. It controls progressive lateral
  traction and vertical road-holding damping.
- **Ride height:** `-0.30–+0.60` blocks, in `0.05` steps.
- **Suspension travel:** `50–200%`, in `10%` steps.
- **Contact radius:** `75–130%`, in `5%` steps. It changes the effective
  physical tire radius without scaling the rendered model.
- **Differential:** select one wheel, then another wheel on the opposite side
  of the same axle. New pairs start in limited-slip mode. Right-click either
  paired wheel to cycle Open → Limited-slip → Locked. Sneak + right-click
  disconnects the pair.

Differential behavior:

- **Open:** both wheel drive forces are limited by the lower-traction side.
- **Limited-slip:** partially transfers usable drive toward the side that
  retains grip.
- **Locked:** cancels the local surface drive reduction so both wheel mounts
  continue applying drive together.

Road grip uses a progressive square-root curve while the vertical
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
