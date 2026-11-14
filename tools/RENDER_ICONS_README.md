# Vehicle creative-tab icon renderer

`tools/icon_renderer` is a **build-time** Rust program that renders every FCP
vehicle's `.geo.json` model into an **isometric** 128x128 PNG. It never runs at
game runtime. All ~200 icons take about a second.

The output goes to:

```
src/main/resources/assets/superbwarfare/textures/vehicle_icon/container/<vehicle_id>.png
```

FCP's container-icon decorator draws the PNG at that path in place of the generic
container crate, so **no code or item-JSON changes are needed**. Drop the PNG in,
the icon appears.

The generated icons are committed to the repo, so a machine without Rust still
builds: the Gradle task is skipped and the committed icons are used.

## Requirements

* `cargo` (install Rust from https://rustup.rs). The first run downloads and
  compiles two small crates (`png`, `serde_json`); after that it is instant.

No game, no Blockbench, no OpenGL. It is a self-contained software rasterizer.

## Running it

Via Gradle (also runs automatically before `processResources` when inputs changed):

```
./gradlew renderVehicleIcons
```

Or directly, from `tools/icon_renderer`:

```
cargo run --release -- --batch ../..                  # all vehicles
cargo run --release -- --batch ../.. m939,bmp2,lav25  # only these ids
cargo run --release -- --list ../..                   # show the geo + texture each id resolves to
cargo run --release -- <geo.json> <texture.png> <out.png> [yaw] [pitch]   # one model, uncropped scale
```

`--list` is the first thing to check when an icon looks wrong: a jumbled icon
almost always means the wrong geo or texture was picked for that id.

## How a vehicle is resolved to geo + texture

The vehicle list is every `data/*/sbw/vehicle_skins/<id>.json`. Missiles and other
non-vehicle entities have no skins file and are skipped.

* **Texture**: the vehicle's **default skin** - the entry with the lowest
  `Priority` in its skins file. (Models that layer two textures in-game are
  rendered with the primary skin only.)
* **Geometry**, first match wins:
  1. The fixed geo the entity's GeckoLib model class returns from
     `getModelResource`, when that is the *only* thing that entity class ever
     returns (e.g. `huey_m60_door_guns` -> `huey_m60.geo.json`).
  2. `assets/fcp/geo/<id>.geo.json` (or `<id>.json`). This is what name-driven
     models (`"geo/" + name + ".geo.json"`) resolve to, and it is what keeps
     entity classes shared by many vehicles apart: every HMMWV, Caiman, Dingo and
     the Toyota HMV use `HumveeUnarmedEntity`, and each still gets its own geo.
  3. Any fixed geo of the entity class, then the SBW item JSON (`Model.Model`).

Anything that can't be resolved is listed at the end of the run. Pin it in
`tools/icon_overrides.json`:

```json
{
  "my_vehicle": {
    "geo":     "src/main/resources/assets/fcp/geo/my_vehicle.geo.json",
    "texture": "src/main/resources/assets/fcp/textures/entity/my_vehicle/skin.png",
    "yaw": 45,
    "pitch": 35.264,
    "hide": ["camo_net", "some_bone"]
  }
}
```

An entry may carry any subset of those keys; whatever you omit is auto-resolved.
Overrides always win, so they can also force a specific camo or camera angle
(handy for vehicles modelled facing the other way). `hide` leaves out the named
bones and everything parented to them - use it for parts the game only shows
conditionally (loaded missiles, fitted cages, nets).

## Attachments

Geos with an `Attachments` bone (the HMMWVs, Caimans, Dingos, Abrams, ...) hold
several mutually-exclusive variants per category (`Attachments` -> category ->
variants). The game shows one variant per category; the icon shows variant 0 of
each and hides the rest, instead of drawing every bumper and snorkel on top of
each other.

## Rendering details

* **Angle** - true isometric: yaw `135`, pitch `35.264` (`atan(1/sqrt 2)`), showing
  the front of the vehicle. Pin a different angle per vehicle in the overrides.
* **Framing** - the render is cropped tight to the model, scaled
  (nearest-neighbour) so its longest side is 120 px, and centred on a transparent
  128x128 canvas.
* **GeckoLib fidelity** - the rasterizer follows GeckoLib's model conventions:
  X-mirrored geometry, cube/bone rotation `Rz(+rz) * Ry(-ry) * Rx(-rx)` about
  X-negated pivots, per-cube pivot/rotation/inflate/mirror, bone hierarchy,
  per-face UVs with `uv_rotation`, and box UVs. Faces are drawn double-sided, as
  GeckoLib does.

## How the icons replace the crate (FCP-side, SBW-version-independent)

* `client/FcpContainerIconDecorator` draws the icon over the container slot in GUIs.
* `mixins/ContainerBlockItemRendererMixin` suppresses SBW's 3D crate in the GUI for
  FCP vehicles that have an icon, so the icon fully replaces it.
* Both are scoped to `fcp:` vehicles only (via the container's `EntityType` tag), so
  SBW's own containers are untouched. The mixin uses `require = 0`, so if it can't
  apply on some SBW version the mod still loads (the icon just draws over the crate).

If a specific vehicle still shows a crate: confirm its PNG exists with `<id>`
exactly the entity path, and that the build included it
(`jar tf build/libs/*.jar | grep vehicle_icon`).

## Known limitations

* `t14_armata` is registered as a `WolfEntity` (a meme placeholder), so its icon is
  the wolf - which is what the game renders.
* On-texture lettering can read mirrored because of the X-mirror; invisible at
  icon size.
* Bones the game hides at runtime outside the `Attachments` system are drawn
  unless listed under `hide` in the overrides.
