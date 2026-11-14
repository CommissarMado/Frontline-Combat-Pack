//! Works out, for every vehicle id, which geo and texture the game actually renders.
//!
//! Vehicles  = every data/*/sbw/vehicle_skins/<id>.json.
//! Texture   = that file's lowest-Priority skin (the default camo).
//! Geo       = see `find_geo`.
//! Everything can be pinned per vehicle in tools/icon_overrides.json.

use crate::render::read_json;
use serde_json::Value;
use std::collections::{BTreeMap, HashMap};
use std::path::{Path, PathBuf};

pub struct Job {
    pub id: String,
    pub geo: PathBuf,
    pub texture: PathBuf,
    pub yaw: f64,
    pub pitch: f64,
    /// Bone names to leave out (their children go with them).
    pub hide: Vec<String>,
}

/// What the GeckoLib model classes for one entity class return from getModelResource.
#[derive(Default)]
struct ModelGeos {
    /// Fixed paths, e.g. "geo/bmp1_am.geo.json" -> assets/<ns>/geo/bmp1_am.geo.json.
    literals: Vec<PathBuf>,
    /// At least one model builds the path at runtime ("geo/" + name + ".geo.json").
    dynamic: bool,
}

fn walk(dir: &Path, ext: &str, out: &mut Vec<PathBuf>) {
    let Ok(entries) = std::fs::read_dir(dir) else { return };
    let mut entries: Vec<PathBuf> = entries.flatten().map(|e| e.path()).collect();
    entries.sort();
    for path in entries {
        if path.is_dir() {
            walk(&path, ext, out);
        } else if path.extension().is_some_and(|e| e == ext) {
            out.push(path);
        }
    }
}

fn asset_path(root: &Path, resource: &str) -> PathBuf {
    let (ns, path) = resource.split_once(':').unwrap_or(("fcp", resource));
    root.join("src/main/resources/assets").join(ns).join(path)
}

fn is_ident(c: char) -> bool {
    c.is_ascii_alphanumeric() || c == '_'
}

/// id -> entity class, from `RegistryObject<EntityType<Cls>> NAME = helper("id", ...)` in
/// ModEntities. Any helper name counts (register, t90m, bradley, ...).
fn registered_classes(root: &Path) -> HashMap<String, String> {
    let mut out = HashMap::new();
    let Ok(src) = std::fs::read_to_string(root.join("src/main/java/frontline/combat/fcp/init/ModEntities.java")) else {
        return out;
    };
    for (at, _) in src.match_indices("EntityType<") {
        let rest = &src[at + "EntityType<".len()..];
        let Some(end) = rest.find('>') else { continue };
        let class = rest[..end].rsplit('.').next().unwrap_or("").trim();
        let Some(eq) = rest.find('=') else { continue };
        let Some(semicolon) = rest.find(';') else { continue };
        if eq > semicolon {
            continue;
        }
        let call = &rest[eq..semicolon];
        let Some(quote) = call.find("(\"") else { continue };
        let id: String = call[quote + 2..].chars().take_while(|c| *c != '"').collect();
        if !class.is_empty() && !id.is_empty() && id.chars().all(|c| c.is_ascii_lowercase() || c.is_ascii_digit() || c == '_') {
            out.insert(id, class.to_string());
        }
    }
    out
}

/// entity class -> the geo paths its model classes return from getModelResource(<Class> x).
fn model_geos(root: &Path) -> HashMap<String, ModelGeos> {
    let mut out: HashMap<String, ModelGeos> = HashMap::new();
    let mut files = Vec::new();
    walk(&root.join("src/main/java"), "java", &mut files);
    for file in files {
        let Ok(src) = std::fs::read_to_string(&file) else { continue };
        for (at, _) in src.match_indices("getModelResource(") {
            let rest = &src[at + "getModelResource(".len()..];
            let class: String = rest.trim_start().chars().take_while(|c| is_ident(*c) || *c == '.').collect();
            let class = class.rsplit('.').next().unwrap_or("").to_string();
            let Some(open) = rest.find('{') else { continue };
            if class.is_empty() || rest[..open].contains(';') {
                continue; // a call, not the method declaration
            }
            let mut depth = 0usize;
            let mut close = rest.len();
            for (i, c) in rest[open..].char_indices() {
                match c {
                    '{' => depth += 1,
                    '}' => {
                        depth -= 1;
                        if depth == 0 {
                            close = open + i;
                            break;
                        }
                    }
                    _ => {}
                }
            }
            let body = &rest[open..close];
            let entry = out.entry(class).or_default();
            for (g, _) in body.match_indices("\"geo/") {
                let literal: String = body[g + 1..].chars().take_while(|c| *c != '"').collect();
                if literal.ends_with(".json") {
                    // namespace: `new ResourceLocation("ns", "geo/...")`, else FCP.MODID
                    let before = body[..g].trim_end().trim_end_matches(',').trim_end();
                    let ns = before
                        .strip_suffix('"')
                        .and_then(|b| b.rsplit_once('"'))
                        .filter(|(head, _)| head.trim_end().ends_with('('))
                        .map(|(_, ns)| ns)
                        .unwrap_or("fcp");
                    let path = asset_path(root, &format!("{ns}:{literal}"));
                    if !entry.literals.contains(&path) {
                        entry.literals.push(path);
                    }
                } else {
                    entry.dynamic = true;
                }
            }
        }
    }
    out
}

/// id -> (geo, texture) from the SBW item JSONs (assets/fcp/sbw/**/*.json). Last-resort fallback.
fn item_models(root: &Path) -> HashMap<String, (Option<PathBuf>, Option<PathBuf>)> {
    let mut out = HashMap::new();
    let mut files = Vec::new();
    walk(&root.join("src/main/resources/assets/fcp/sbw"), "json", &mut files);
    for file in files {
        let Ok(doc) = read_json(&file) else { continue };
        let Some(id) = doc.get("ID").and_then(Value::as_str).and_then(|s| s.rsplit(':').next()) else { continue };
        let model = doc.get("Model");
        let get = |key: &str| model.and_then(|m| m.get(key)).and_then(Value::as_str).map(|rl| asset_path(root, rl));
        out.insert(id.to_string(), (get("Model"), get("Texture")));
    }
    out
}

/// id -> default skin texture, from data/*/sbw/vehicle_skins/<id>.json (lowest Priority wins).
fn default_skins(root: &Path) -> BTreeMap<String, PathBuf> {
    let mut out = BTreeMap::new();
    let Ok(namespaces) = std::fs::read_dir(root.join("src/main/resources/data")) else { return out };
    for ns in namespaces.flatten() {
        let mut files = Vec::new();
        walk(&ns.path().join("sbw/vehicle_skins"), "json", &mut files);
        for file in files {
            let Ok(doc) = read_json(&file) else { continue };
            let best = doc
                .get("Skins")
                .and_then(Value::as_array)
                .into_iter()
                .flatten()
                .filter_map(|s| Some((s.get("Priority").and_then(Value::as_f64).unwrap_or(0.0), s.get("Texture")?.as_str()?)))
                .reduce(|a, b| if b.0 < a.0 { b } else { a });
            if let (Some((_, texture)), Some(id)) = (best, file.file_stem().and_then(|s| s.to_str())) {
                out.insert(id.to_string(), asset_path(root, texture));
            }
        }
    }
    out
}

/// The geo a vehicle renders with:
///  1. the model class's fixed geo, when that is the only thing the entity class ever returns;
///  2. geo/<id>.geo.json (or geo/<id>.json) - what every name-driven model resolves to. An entity
///     class shared by many vehicles (all the HMMWVs, plus the Toyota HMV's fixed geo) lands here,
///     so each variant gets its own geo instead of whichever fixed one was found first;
///  3. any fixed geo of the class; 4. the SBW item JSON.
fn find_geo(root: &Path, id: &str, models: Option<&ModelGeos>, item: Option<&PathBuf>) -> Option<PathBuf> {
    let geo_dir = root.join("src/main/resources/assets/fcp/geo");
    let named = [format!("{id}.geo.json"), format!("{id}.json")].into_iter().map(|n| geo_dir.join(n)).find(|p| p.exists());
    let literals: Vec<&PathBuf> = models.map(|m| m.literals.iter().filter(|p| p.exists()).collect()).unwrap_or_default();
    let fixed_only = models.is_some_and(|m| !m.dynamic) && literals.len() == 1;
    if fixed_only {
        return Some(literals[0].clone());
    }
    named.or_else(|| literals.first().map(|p| (*p).clone())).or_else(|| item.filter(|p| p.exists()).cloned())
}

/// Resolves every vehicle. Returns the render jobs (sorted by id) and a list of ids that could
/// not be resolved, with the reason.
pub fn resolve(root: &Path, yaw: f64, pitch: f64) -> (Vec<Job>, Vec<String>) {
    let skins = default_skins(root);
    let classes = registered_classes(root);
    let models = model_geos(root);
    let items = item_models(root);
    let overrides = match read_json(&root.join("tools/icon_overrides.json")) {
        Ok(Value::Object(map)) => map,
        _ => Default::default(),
    };

    let mut ids: Vec<String> = skins.keys().cloned().collect();
    ids.extend(overrides.keys().filter(|k| !skins.contains_key(*k)).cloned());

    let mut jobs = Vec::new();
    let mut problems = Vec::new();
    for id in ids {
        let over = overrides.get(&id);
        let over_path = |key: &str| over.and_then(|o| o.get(key)).and_then(Value::as_str).map(|p| root.join(p));
        let over_num = |key: &str, default: f64| over.and_then(|o| o.get(key)).and_then(Value::as_f64).unwrap_or(default);
        let item = items.get(&id);
        let geo = over_path("geo").or_else(|| {
            find_geo(root, &id, classes.get(&id).and_then(|c| models.get(c)), item.and_then(|i| i.0.as_ref()))
        });
        let texture = over_path("texture")
            .or_else(|| skins.get(&id).filter(|p| p.exists()).cloned())
            .or_else(|| item.and_then(|i| i.1.clone()).filter(|p| p.exists()));
        let hide = over
            .and_then(|o| o.get("hide"))
            .and_then(Value::as_array)
            .map(|a| a.iter().filter_map(Value::as_str).map(str::to_string).collect())
            .unwrap_or_default();
        match (geo, texture) {
            (Some(geo), Some(texture)) if geo.exists() && texture.exists() => {
                jobs.push(Job { id, geo, texture, yaw: over_num("yaw", yaw), pitch: over_num("pitch", pitch), hide })
            }
            (None, _) => problems.push(format!("{id} (no geo)")),
            (Some(geo), _) if !geo.exists() => problems.push(format!("{id} (missing {})", geo.display())),
            _ => problems.push(format!("{id} (no texture)")),
        }
    }
    jobs.sort_by(|a, b| a.id.cmp(&b.id));
    (jobs, problems)
}
