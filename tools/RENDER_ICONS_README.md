# Vehicle creative-tab icon renderer

`render_icons.py` is a **build-time** tool that renders every FCP vehicle's
`.geo.json` model into a tightly-cropped **isometric** PNG. It runs once when you
regenerate icons — never at game runtime.

The output goes to:

```
src/main/resources/assets/superbwarfare/textures/vehicle_icon/container/<vehicle_id>.png
```

SuperbWarfare's `ContainerItemDecorator` automatically detects a PNG at that path
for a given vehicle id and draws it in place of the generic container crate — so
**no code or item-JSON changes are needed**. Drop the PNG in, the icon appears.

The generated icons are committed to the repo, so a normal build/compile needs
nothing extra. You only re-run this when a vehicle model or texture changes.

## Requirements

* `python3`
* `numpy`, `Pillow`  →  `pip install numpy Pillow`

No game, no Blockbench, no OpenGL. It's a self-contained software rasterizer.

## Running it

Via Gradle (recommended):

```
./gradlew renderVehicleIcons
```

Or directly:

```
python3 tools/render_icons.py --batch .                 # all vehicles
python3 tools/render_icons.py --batch . m939,bmp2,lav25 # only these ids
```

Render a single model to an arbitrary file (for testing / a custom angle):

```
python3 tools/render_icons.py <geo.json> <texture.png> <out.png> [yaw] [pitch]
# default yaw=45  pitch=35.264  (true isometric, cab/front to the front-left)
```

To make icon regeneration part of every build (Gradle skips it when nothing
changed), uncomment this line at the bottom of `build.gradle`:

```
// processResources.dependsOn 'renderVehicleIcons'
```

## How a vehicle is resolved to geo + texture

For each entity id the tool finds:

* **Geometry** — from the SBW item JSON (`Model.Model`) if present, otherwise
  `assets/fcp/geo/<id>.geo.json`.
* **Texture** — the vehicle's **assigned default skin**: the tool reads
  `ModEntities.java` to map the id to its entity class, then takes the first
  entry of that class's `CAMO_TEXTURES` array (e.g. `m939` -> `m939_green.png`,
  `bmp2` -> `bmp1_2/bmp_2_rem_tex_1_1_1.png`). Exactly one texture per model, and
  always one actually assigned to it. If that can't be read it falls back to a
  folder/name heuristic, then the item-JSON `Texture`. (Models that layer two
  textures in-game are rendered with the primary skin only.)

Anything that can't be resolved automatically is listed at the end of the run.
Add it to `tools/icon_overrides.json`:

```json
{
  "my_vehicle": {
    "geo":     "src/main/resources/assets/fcp/geo/my_vehicle.geo.json",
    "texture": "src/main/resources/assets/fcp/textures/entity/my_vehicle/skin.png"
  }
}
```

Overrides always win, so you can also use them to force a specific camo for the
icon of any vehicle. You can also pin a per-vehicle camera angle by adding `yaw`
and/or `pitch` to any entry (handy for vehicles modelled facing the other way):

```json
{ "some_tank": { "yaw": 45, "pitch": 35.264 } }
```

An override entry may carry any subset of `geo`, `texture`, `yaw`, `pitch` —
whatever you omit is auto-resolved. The shipped `icon_overrides.json` uses
geo-only entries for the handful of variants whose geo file name differs from
the entity id (e.g. `matv_crow` -> `matv_crows.geo.json`); their textures still
come from `CAMO_TEXTURES`.

## Rendering details

* **Angle** — true isometric: yaw `225°`, pitch `35.264°` (`atan(1/√2)`), showing
  the **front** of the vehicle. Change per-call with the optional yaw/pitch args,
  or edit the defaults in `batch()`. A few vehicles are modelled facing the
  opposite way in model-space, so their front shows at a different yaw — pin those
  per-vehicle (see below) instead of moving the global default.
* **Framing** — the image is cropped tight to the model (all transparent excess
  removed), then downscaled so the longest side is 128 px (nearest-neighbour, so
  it stays crisp). SBW squishes the icon into the 16×16 slot; a tight crop keeps
  the model as large as possible. If you'd rather have square icons (no slot
  squish), pad to square after the crop in `batch()`.
* **GeckoLib fidelity** — the rasterizer replicates GeckoLib's exact model
  conventions: X-mirrored geometry, cube/bone rotation `Rz(+rz)·Ry(-ry)·Rx(-rx)`
  applied around (X-negated) pivots, per-cube pivot/rotation/inflate, bone
  hierarchy, and per-face UVs. That's why turrets, canopies and angled armor come
  out correctly instead of exploding.

## If the icons don't replace the crate in-game

The icon fully replaces the container crate — SBW's `ContainerBlockItemRenderer`
skips drawing the crate in the GUI whenever
`superbwarfare:textures/vehicle_icon/container/<id>.png` exists, and its
`ContainerItemDecorator` blits the icon in its place. If you still see the crate:

1. **Rebuild so the icons reach the runtime.** In a dev run the client loads
   `build/resources/main`, not `src`. Run `./gradlew build` (or restart
   `runClient`) so `processResources` copies the new PNGs across. In-game, press
   **F3+T** to reload resources.
2. **Confirm they're in the jar/resources** at
   `assets/superbwarfare/textures/vehicle_icon/container/<id>.png`, with `<id>`
   exactly the entity path (`m939`, `dpv_m240`, ...).
3. **SBW must be >= 0.8.9.** The vehicle-icon replacement feature was added to
   SuperbWarfare on 2026-05-31 (mod_version 0.8.9). The latest *release* at the
   time of writing is 0.8.8, which has **no** icon detection - the crate always
   shows regardless of these PNGs. Quick check: do SBW's *own* vehicles (Bradley,
   BMP-2, LAV-25) show picture icons in the creative tab, or crates? If they show
   crates too, your SBW is < 0.8.9 - update it (and bump FCP's
   `curse.maven:superb-warfare-...` dependency to a 0.8.9+ file). If SBW's own
   vehicles show icons but FCP's don't, it's a resource path/build problem, not
   the feature.

## Known limitations

* `t14_armata` is registered as a `WolfEntity` (a meme placeholder) with no
  vehicle geo, so it's skipped. Add an override if you give it a real model.
* On-texture lettering (e.g. the "TOYOTA" tailgate) can read mirrored because of
  the X-mirror; invisible at 16 px icon size, so it's left as-is.
* Variant vehicles that share one geo but use different skins rely on the texture
  heuristic; if a variant picks the wrong skin, pin it in `icon_overrides.json`.
