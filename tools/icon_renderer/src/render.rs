//! Software rasterizer that follows GeckoLib's model conventions: X-mirrored geometry, bone and
//! cube rotations Rz(+rz) * Ry(-ry) * Rx(-rx) about X-negated pivots, per-face and box UVs,
//! uv_rotation, mirror, inflate. Faces are drawn double-sided (GeckoLib renders NoCull).

use crate::resolve::Job;
use serde_json::Value;
use std::collections::{HashMap, HashSet};
use std::path::Path;

type V3 = [f64; 3];
type M3 = [[f64; 3]; 3];


fn mul(a: &M3, b: &M3) -> M3 {
    let mut o = [[0.0; 3]; 3];
    for i in 0..3 {
        for j in 0..3 {
            o[i][j] = a[i][0] * b[0][j] + a[i][1] * b[1][j] + a[i][2] * b[2][j];
        }
    }
    o
}
fn apply(m: &M3, v: V3) -> V3 {
    [
        m[0][0] * v[0] + m[0][1] * v[1] + m[0][2] * v[2],
        m[1][0] * v[0] + m[1][1] * v[1] + m[1][2] * v[2],
        m[2][0] * v[0] + m[2][1] * v[1] + m[2][2] * v[2],
    ]
}
fn add(a: V3, b: V3) -> V3 {
    [a[0] + b[0], a[1] + b[1], a[2] + b[2]]
}
fn sub(a: V3, b: V3) -> V3 {
    [a[0] - b[0], a[1] - b[1], a[2] - b[2]]
}
fn rot_x(a: f64) -> M3 {
    let (s, c) = a.sin_cos();
    [[1.0, 0.0, 0.0], [0.0, c, -s], [0.0, s, c]]
}
fn rot_y(a: f64) -> M3 {
    let (s, c) = a.sin_cos();
    [[c, 0.0, s], [0.0, 1.0, 0.0], [-s, 0.0, c]]
}
fn rot_z(a: f64) -> M3 {
    let (s, c) = a.sin_cos();
    [[c, -s, 0.0], [s, c, 0.0], [0.0, 0.0, 1.0]]
}
/// Bedrock/GeckoLib euler in degrees.
fn euler_geo(r: V3) -> M3 {
    mul(&mul(&rot_z(r[2].to_radians()), &rot_y(-r[1].to_radians())), &rot_x(-r[0].to_radians()))
}

fn vec3(v: Option<&Value>) -> Option<V3> {
    let a = v?.as_array()?;
    Some([a.first()?.as_f64()?, a.get(1)?.as_f64()?, a.get(2)?.as_f64()?])
}
fn vec2(v: Option<&Value>) -> Option<[f64; 2]> {
    let a = v?.as_array()?;
    Some([a.first()?.as_f64()?, a.get(1)?.as_f64()?])
}

pub fn read_json(path: &Path) -> Result<Value, String> {
    let text = std::fs::read_to_string(path).map_err(|e| format!("{}: {e}", path.display()))?;
    serde_json::from_str(text.trim_start_matches('\u{feff}')).map_err(|e| format!("{}: {e}", path.display()))
}

// ------------------------------------------------------------------------------------------------
// Images
// ------------------------------------------------------------------------------------------------

pub struct Image {
    pub w: u32,
    pub h: u32,
    pub rgba: Vec<u8>,
}

impl Image {
    pub fn load(path: &Path) -> Result<Image, String> {
        let err = |e: String| format!("{}: {e}", path.display());
        let file = std::fs::File::open(path).map_err(|e| err(e.to_string()))?;
        let mut decoder = png::Decoder::new(std::io::BufReader::new(file));
        decoder.set_transformations(png::Transformations::EXPAND | png::Transformations::STRIP_16);
        let mut reader = decoder.read_info().map_err(|e| err(e.to_string()))?;
        let mut buf = vec![0; reader.output_buffer_size()];
        let info = reader.next_frame(&mut buf).map_err(|e| err(e.to_string()))?;
        buf.truncate(info.buffer_size());
        let rgba: Vec<u8> = match info.color_type {
            png::ColorType::Rgba => buf,
            png::ColorType::Rgb => buf.chunks_exact(3).flat_map(|p| [p[0], p[1], p[2], 255]).collect(),
            png::ColorType::Grayscale => buf.iter().flat_map(|&g| [g, g, g, 255]).collect(),
            png::ColorType::GrayscaleAlpha => buf.chunks_exact(2).flat_map(|p| [p[0], p[0], p[0], p[1]]).collect(),
            other => return Err(err(format!("unsupported colour type {other:?}"))),
        };
        Ok(Image { w: info.width, h: info.height, rgba })
    }

    pub fn save(&self, path: &Path) -> Result<(), String> {
        let err = |e: String| format!("{}: {e}", path.display());
        let file = std::fs::File::create(path).map_err(|e| err(e.to_string()))?;
        let mut encoder = png::Encoder::new(std::io::BufWriter::new(file), self.w, self.h);
        encoder.set_color(png::ColorType::Rgba);
        encoder.set_depth(png::BitDepth::Eight);
        encoder.set_compression(png::Compression::Best);
        let mut writer = encoder.write_header().map_err(|e| err(e.to_string()))?;
        writer.write_image_data(&self.rgba).map_err(|e| err(e.to_string()))
    }

    /// Scale to fit within `fit` px (nearest-neighbour, aspect kept) and centre on a transparent
    /// `size` x `size` canvas - the format SBW's container slot expects.
    pub fn square_canvas(&self, size: u32, fit: u32) -> Image {
        let (w, h, fit_f) = (self.w as f64, self.h as f64, fit as f64);
        let ratio = fit_f / w.max(h);
        let nw = ((w * ratio).round_ties_even() as u32).clamp(1, size);
        let nh = ((h * ratio).round_ties_even() as u32).clamp(1, size);
        let (ox, oy) = ((size - nw) / 2, (size - nh) / 2);
        let mut out = Image { w: size, h: size, rgba: vec![0; (size * size * 4) as usize] };
        for y in 0..nh {
            let sy = (((y as f64 + 0.5) * h / nh as f64) as u32).min(self.h - 1);
            for x in 0..nw {
                let sx = (((x as f64 + 0.5) * w / nw as f64) as u32).min(self.w - 1);
                let s = ((sy * self.w + sx) * 4) as usize;
                let d = (((y + oy) * size + x + ox) * 4) as usize;
                out.rgba[d..d + 4].copy_from_slice(&self.rgba[s..s + 4]);
            }
        }
        out
    }
}

// ------------------------------------------------------------------------------------------------
// Geometry
// ------------------------------------------------------------------------------------------------

struct Bone<'a> {
    name: &'a str,
    parent: Option<usize>,
    pivot: V3, // X already negated
    rotation: V3,
    cubes: &'a [Value],
}

struct Tri {
    xy: [[f64; 2]; 3],
    depth: [f64; 3],
    uv: [[f64; 2]; 3],
    shade: f64,
}

/// Bone-local -> model affine transform (rotation, translation), memoised.
fn bone_world(i: usize, bones: &[Bone], cache: &mut Vec<Option<(M3, V3)>>) -> (M3, V3) {
    if let Some(m) = cache[i] {
        return m;
    }
    let bone = &bones[i];
    let r = euler_geo(bone.rotation);
    let m = match bone.parent {
        Some(p) => {
            let (pr, pt) = bone_world(p, bones, cache);
            (mul(&pr, &r), add(apply(&pr, sub(bone.pivot, bones[p].pivot)), pt))
        }
        None => (r, bone.pivot),
    };
    cache[i] = Some(m);
    m
}

fn shade_face(n: V3) -> f64 {
    // key light: top, slight front-left
    let l = [-0.35f64, 0.88, 0.32];
    let ll = (l[0] * l[0] + l[1] * l[1] + l[2] * l[2]).sqrt();
    let diff = ((n[0] * l[0] + n[1] * l[1] + n[2] * l[2]) / ll).max(0.0);
    (0.42 + 0.62 * diff - 0.06 * n[0].max(0.0)).clamp(0.34, 1.0)
}

/// The four UV corners of a face in GeckoLib's vertex order, in texture pixels.
fn face_uvs(u: f64, v: f64, us: f64, vs: f64, mirror: bool, rotation: i64) -> [[f64; 2]; 4] {
    let (mut u0, mut u1) = (u, u + us);
    let (v0, v1) = (v, v + vs);
    if !mirror {
        std::mem::swap(&mut u0, &mut u1);
    }
    match rotation.rem_euclid(360) {
        90 => [[u1, v0], [u1, v1], [u0, v1], [u0, v0]],
        180 => [[u1, v1], [u0, v1], [u0, v0], [u1, v0]],
        270 => [[u0, v1], [u0, v0], [u1, v0], [u1, v1]],
        _ => [[u0, v0], [u1, v0], [u1, v1], [u0, v1]],
    }
}

const FACE_NAMES: [&str; 6] = ["west", "east", "north", "south", "up", "down"];

/// UVs for one face of a cube, or None if the face has no UV (it is then not drawn).
fn cube_face_uvs(uv: &Value, face: usize, size: V3, mirror: bool) -> Option<[[f64; 2]; 4]> {
    if let Some(obj) = uv.as_object() {
        let f = obj.get(FACE_NAMES[face])?;
        let [u, v] = vec2(f.get("uv"))?;
        let [us, vs] = vec2(f.get("uv_size"))?;
        let rotation = f.get("uv_rotation").and_then(Value::as_i64).unwrap_or(0);
        return Some(face_uvs(u, v, us, vs, mirror, rotation));
    }
    // Box UV: GeckoLib floors the cube size and unwraps the classic cross layout.
    let [u, v] = vec2(Some(uv))?;
    let (x, y, z) = (size[0].floor(), size[1].floor(), size[2].floor());
    let (fu, fv, fus, fvs) = match face {
        0 => (u + z + x, v + z, z, y),
        1 => (u, v + z, z, y),
        2 => (u + z, v + z, x, y),
        3 => (u + z + x + z, v + z, x, y),
        4 => (u + z, v, x, z),
        _ => (u + z + x, v + z, x, -z),
    };
    Some(face_uvs(fu, fv, fus, fvs, mirror, 0))
}

pub fn render(job: &Job, scale: f64) -> Result<Option<Image>, String> {
    let doc = read_json(&job.geo)?;
    let geometry = doc
        .get("minecraft:geometry")
        .and_then(|g| g.get(0))
        .ok_or_else(|| format!("{}: no minecraft:geometry", job.geo.display()))?;
    let desc = geometry.get("description");
    let tex_w = desc.and_then(|d| d.get("texture_width")).and_then(Value::as_f64).unwrap_or(16.0);
    let tex_h = desc.and_then(|d| d.get("texture_height")).and_then(Value::as_f64).unwrap_or(16.0);
    let texture = Image::load(&job.texture)?;

    let empty = Vec::new();
    let raw_bones = geometry.get("bones").and_then(Value::as_array).unwrap_or(&empty);
    let mut index: HashMap<&str, usize> = HashMap::new();
    for (i, b) in raw_bones.iter().enumerate() {
        index.insert(b.get("name").and_then(Value::as_str).unwrap_or(""), i);
    }
    let bones: Vec<Bone> = raw_bones
        .iter()
        .map(|b| {
            let pivot = vec3(b.get("pivot")).unwrap_or([0.0; 3]);
            Bone {
                name: b.get("name").and_then(Value::as_str).unwrap_or(""),
                parent: b.get("parent").and_then(Value::as_str).and_then(|p| index.get(p).copied()),
                pivot: [-pivot[0], pivot[1], pivot[2]],
                rotation: vec3(b.get("rotation")).unwrap_or([0.0; 3]),
                cubes: b.get("cubes").and_then(Value::as_array).map(Vec::as_slice).unwrap_or(&[]),
            }
        })
        .collect();

    let hidden = hidden_bones(&bones, &job.hide);
    let view = mul(&rot_x(job.pitch.to_radians()), &rot_y(job.yaw.to_radians()));
    let mut cache = vec![None; bones.len()];
    let mut tris: Vec<Tri> = Vec::new();

    for (bi, bone) in bones.iter().enumerate() {
        if hidden[bi] {
            continue;
        }
        let (bone_r, bone_t) = bone_world(bi, &bones, &mut cache);
        for cube in bone.cubes {
            let (Some(origin), Some(size)) = (vec3(cube.get("origin")), vec3(cube.get("size"))) else { continue };
            let Some(uv) = cube.get("uv") else { continue };
            let inflate = cube.get("inflate").and_then(Value::as_f64).unwrap_or(0.0);
            let mirror = cube.get("mirror").and_then(Value::as_bool).unwrap_or(false);
            // GeckoLib mirrors X into the origin.
            let ox = -(origin[0] + size[0]);
            let (x0, x1) = (ox - inflate, ox + size[0] + inflate);
            let (y0, y1) = (origin[1] - inflate, origin[1] + size[1] + inflate);
            let (z0, z1) = (origin[2] - inflate, origin[2] + size[2] + inflate);
            // GeckoLib VertexSet naming: Back = minX, Front = maxX, Left = minZ, Right = maxZ.
            let (blb, brb, tlb, trb) = ([x0, y0, z0], [x0, y0, z1], [x0, y1, z0], [x0, y1, z1]);
            let (tlf, trf, blf, brf) = ([x1, y1, z0], [x1, y1, z1], [x1, y0, z0], [x1, y0, z1]);
            let quad_w = ([trb, tlb, blb, brb], [-1.0, 0.0, 0.0]);
            let quad_e = ([tlf, trf, brf, blf], [1.0, 0.0, 0.0]);
            let quad_n = ([tlb, tlf, blf, blb], [0.0, 0.0, -1.0]);
            let quad_s = ([trf, trb, brb, brf], [0.0, 0.0, 1.0]);
            let quad_u = ([trb, trf, tlf, tlb], [0.0, 1.0, 0.0]);
            let quad_d = ([blb, blf, brf, brb], [0.0, -1.0, 0.0]);
            // Mirror swaps the west/east vertex sets, and up/down too for per-face UVs.
            let swap_ud = mirror && uv.is_object();
            let quads: [([V3; 4], V3); 6] = [
                if mirror { quad_e } else { quad_w },
                if mirror { quad_w } else { quad_e },
                quad_n,
                quad_s,
                if swap_ud { quad_d } else { quad_u },
                if swap_ud { quad_u } else { quad_d },
            ];
            let cube_rot = vec3(cube.get("rotation")).map(|r| {
                let p = vec3(cube.get("pivot")).unwrap_or([0.0; 3]);
                (euler_geo(r), [-p[0], p[1], p[2]])
            });

            for (face, (points, normal)) in quads.iter().enumerate() {
                let Some(uvs) = cube_face_uvs(uv, face, size, mirror) else { continue };
                let mut normal = *normal;
                let mut xy = [[0.0; 2]; 4];
                let mut depth = [0.0; 4];
                for (k, point) in points.iter().enumerate() {
                    let mut p = *point;
                    if let Some((r, pivot)) = &cube_rot {
                        p = add(apply(r, sub(p, *pivot)), *pivot);
                    }
                    let world = add(apply(&bone_r, sub(p, bone.pivot)), bone_t);
                    let v = apply(&view, world);
                    xy[k] = [v[0] * scale, -v[1] * scale];
                    depth[k] = v[2];
                }
                if let Some((r, _)) = &cube_rot {
                    normal = apply(r, normal);
                }
                let mut nv = apply(&view, apply(&bone_r, normal));
                let len = (nv[0] * nv[0] + nv[1] * nv[1] + nv[2] * nv[2]).sqrt();
                if len < 1e-9 {
                    continue;
                }
                nv = [nv[0] / len, nv[1] / len, nv[2] / len];
                if nv[2] < 0.0 {
                    nv = [-nv[0], -nv[1], -nv[2]];
                }
                let shade = shade_face(nv);
                for [a, b, c] in [[0, 1, 2], [0, 2, 3]] {
                    tris.push(Tri {
                        xy: [xy[a], xy[b], xy[c]],
                        depth: [depth[a], depth[b], depth[c]],
                        uv: [uvs[a], uvs[b], uvs[c]],
                        shade,
                    });
                }
            }
        }
    }
    Ok(rasterize(&tris, &texture, tex_w, tex_h))
}

/// Bones that are not drawn: the job's explicit hide list, every non-default attachment variant
/// (see HumveeAttachments: "Attachments" -> category -> variants, variant 0 is the default), and
/// everything below a hidden bone.
fn hidden_bones(bones: &[Bone], hide: &[String]) -> Vec<bool> {
    let mut roots: HashSet<usize> = HashSet::new();
    for (i, bone) in bones.iter().enumerate() {
        if hide.iter().any(|h| h == bone.name) {
            roots.insert(i);
        }
    }
    if let Some(attachments) = bones.iter().position(|b| b.name == "Attachments") {
        for (ci, category) in bones.iter().enumerate() {
            if category.parent != Some(attachments) {
                continue;
            }
            let variants = bones.iter().enumerate().filter(|(_, b)| b.parent == Some(ci)).map(|(i, _)| i);
            roots.extend(variants.skip(1));
        }
    }
    (0..bones.len())
        .map(|i| {
            let mut cur = Some(i);
            while let Some(c) = cur {
                if roots.contains(&c) {
                    return true;
                }
                cur = bones[c].parent;
            }
            false
        })
        .collect()
}

fn rasterize(tris: &[Tri], tex: &Image, tex_w: f64, tex_h: f64) -> Option<Image> {
    if tris.is_empty() {
        return None;
    }
    let (mut min_x, mut min_y, mut max_x, mut max_y) = (f64::MAX, f64::MAX, f64::MIN, f64::MIN);
    for p in tris.iter().flat_map(|t| t.xy.iter()) {
        min_x = min_x.min(p[0]);
        max_x = max_x.max(p[0]);
        min_y = min_y.min(p[1]);
        max_y = max_y.max(p[1]);
    }
    let (min_x, min_y) = (min_x.floor(), min_y.floor());
    let w = (max_x.ceil() - min_x) as usize + 2;
    let h = (max_y.ceil() - min_y) as usize + 2;
    let mut img = vec![0u8; w * h * 4];
    let mut zbuf = vec![f64::MIN; w * h];
    // UVs are in the geo's declared texture space; scale to the actual image size.
    let (usx, usy) = (tex.w as f64 / tex_w, tex.h as f64 / tex_h);
    let (tw, th) = (tex.w as i64, tex.h as i64);

    for tri in tris {
        let p: Vec<[f64; 2]> = tri.xy.iter().map(|q| [q[0] - min_x, q[1] - min_y]).collect();
        let fx = |f: fn(f64, f64) -> f64, k: usize| p.iter().map(|q| q[k]).reduce(f).unwrap();
        let bx0 = fx(f64::min, 0).floor().max(0.0) as usize;
        let bx1 = (fx(f64::max, 0).ceil().max(0.0) as usize).min(w - 1);
        let by0 = fx(f64::min, 1).floor().max(0.0) as usize;
        let by1 = (fx(f64::max, 1).ceil().max(0.0) as usize).min(h - 1);
        let [[x1, y1], [x2, y2], [x3, y3]] = [p[0], p[1], p[2]];
        let d = (y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3);
        if d.abs() < 1e-9 {
            continue;
        }
        for py in by0..=by1 {
            let gy = py as f64 + 0.5;
            for px in bx0..=bx1 {
                let gx = px as f64 + 0.5;
                let l1 = ((y2 - y3) * (gx - x3) + (x3 - x2) * (gy - y3)) / d;
                let l2 = ((y3 - y1) * (gx - x3) + (x1 - x3) * (gy - y3)) / d;
                let l3 = 1.0 - l1 - l2;
                if l1 < -0.001 || l2 < -0.001 || l3 < -0.001 {
                    continue;
                }
                let z = l1 * tri.depth[0] + l2 * tri.depth[1] + l3 * tri.depth[2];
                let at = py * w + px;
                if z <= zbuf[at] {
                    continue;
                }
                let u = l1 * tri.uv[0][0] + l2 * tri.uv[1][0] + l3 * tri.uv[2][0];
                let v = l1 * tri.uv[0][1] + l2 * tri.uv[1][1] + l3 * tri.uv[2][1];
                let ui = ((u * usx) as i64).clamp(0, tw - 1);
                let vi = ((v * usy) as i64).clamp(0, th - 1);
                let t = ((vi * tw + ui) * 4) as usize;
                let texel = &tex.rgba[t..t + 4];
                if texel[3] <= 10 {
                    continue; // alpha test
                }
                let o = at * 4;
                for c in 0..3 {
                    img[o + c] = (texel[c] as f64 * tri.shade).clamp(0.0, 255.0) as u8;
                }
                img[o + 3] = texel[3];
                zbuf[at] = z;
            }
        }
    }

    // crop to content
    let (mut cx0, mut cy0, mut cx1, mut cy1) = (usize::MAX, usize::MAX, 0, 0);
    for y in 0..h {
        for x in 0..w {
            if img[(y * w + x) * 4 + 3] > 0 {
                cx0 = cx0.min(x);
                cx1 = cx1.max(x);
                cy0 = cy0.min(y);
                cy1 = cy1.max(y);
            }
        }
    }
    if cx0 == usize::MAX {
        return None;
    }
    let (cw, ch) = (cx1 - cx0 + 1, cy1 - cy0 + 1);
    let mut rgba = Vec::with_capacity(cw * ch * 4);
    for y in cy0..=cy1 {
        rgba.extend_from_slice(&img[(y * w + cx0) * 4..(y * w + cx1 + 1) * 4]);
    }
    Some(Image { w: cw as u32, h: ch as u32, rgba })
}
