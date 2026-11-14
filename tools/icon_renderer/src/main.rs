//! icon_renderer - build-time isometric vehicle-icon renderer for FCP.
//!
//!   ./gradlew renderVehicleIcons                                  (all vehicles)
//!   cargo run --release -- --batch <project-root> [id1,id2,...]
//!   cargo run --release -- --list <project-root>
//!   cargo run --release -- <geo> <texture> <out.png> [yaw] [pitch]
//!
//! Software-rasterizes each GeckoLib .geo.json with its default skin into a 128x128 PNG.
//! See tools/RENDER_ICONS_README.md.

mod render;
mod resolve;

use std::path::{Path, PathBuf};
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::Mutex;
use std::time::Instant;

const DEFAULT_YAW: f64 = 135.0;
const DEFAULT_PITCH: f64 = 35.264;
const ICON_SIZE: u32 = 128;
const ICON_FIT: u32 = 120; // int(128 * 0.94)

fn main() {
    let args: Vec<String> = std::env::args().skip(1).collect();
    let code = match args.first().map(String::as_str) {
        Some("--batch") => {
            let root = PathBuf::from(args.get(1).map(String::as_str).unwrap_or("."));
            let ids = args.get(2).map(|s| s.split(',').map(str::to_string).collect::<Vec<_>>());
            batch(&root, ids)
        }
        Some("--list") => list(Path::new(args.get(1).map(String::as_str).unwrap_or("."))),
        Some(_) if args.len() >= 3 => single(&args),
        _ => {
            eprintln!("usage: icon_renderer --batch <project-root> [id1,id2,...]");
            eprintln!("       icon_renderer --list <project-root>      (show which geo + texture each id resolves to)");
            eprintln!("       icon_renderer <geo.json> <texture.png> <out.png> [yaw] [pitch]");
            2
        }
    };
    std::process::exit(code);
}

/// Prints "id<TAB>geo<TAB>texture" for every vehicle, to check what the resolver picked.
fn list(root: &Path) -> i32 {
    let (jobs, problems) = resolve::resolve(root, DEFAULT_YAW, DEFAULT_PITCH);
    let rel = |p: &Path| p.strip_prefix(root).unwrap_or(p).display().to_string();
    for job in &jobs {
        println!("{}\t{}\t{}", job.id, rel(&job.geo), rel(&job.texture));
    }
    for problem in &problems {
        eprintln!("unresolved: {problem}");
    }
    0
}

fn single(args: &[String]) -> i32 {
    let yaw = args.get(3).and_then(|s| s.parse().ok()).unwrap_or(DEFAULT_YAW);
    let pitch = args.get(4).and_then(|s| s.parse().ok()).unwrap_or(DEFAULT_PITCH);
    let job = resolve::Job {
        id: String::new(),
        geo: PathBuf::from(&args[0]),
        texture: PathBuf::from(&args[1]),
        yaw,
        pitch,
        hide: Vec::new(),
    };
    match render::render(&job, 10.0) {
        Ok(Some(img)) => match img.save(Path::new(&args[2])) {
            Ok(()) => {
                println!("saved {} ({}x{})", args[2], img.w, img.h);
                0
            }
            Err(e) => {
                eprintln!("{e}");
                1
            }
        },
        Ok(None) => {
            eprintln!("empty render");
            1
        }
        Err(e) => {
            eprintln!("{e}");
            1
        }
    }
}

fn batch(root: &Path, ids: Option<Vec<String>>) -> i32 {
    let start = Instant::now();
    let out_dir = root.join("src/main/resources/assets/superbwarfare/textures/vehicle_icon/container");
    if let Err(e) = std::fs::create_dir_all(&out_dir) {
        eprintln!("[icon_renderer] cannot create {}: {e}", out_dir.display());
        return 1;
    }
    let (mut jobs, mut problems) = resolve::resolve(root, DEFAULT_YAW, DEFAULT_PITCH);
    if let Some(ids) = &ids {
        for id in ids {
            if !jobs.iter().any(|j| &j.id == id) {
                problems.push(format!("{id} (unknown id)"));
            }
        }
        jobs.retain(|j| ids.contains(&j.id));
    }

    let next = AtomicUsize::new(0);
    let failed = Mutex::new(Vec::<String>::new());
    let written = AtomicUsize::new(0);
    let threads = std::thread::available_parallelism().map(|n| n.get()).unwrap_or(4).min(jobs.len().max(1));
    std::thread::scope(|scope| {
        for _ in 0..threads {
            scope.spawn(|| loop {
                let i = next.fetch_add(1, Ordering::Relaxed);
                let Some(job) = jobs.get(i) else { break };
                let result = render::render(job, 4.5).and_then(|img| match img {
                    None => Err("empty render".to_string()),
                    Some(img) => img.square_canvas(ICON_SIZE, ICON_FIT).save(&out_dir.join(format!("{}.png", job.id))),
                });
                match result {
                    Ok(()) => {
                        written.fetch_add(1, Ordering::Relaxed);
                    }
                    Err(e) => failed.lock().unwrap().push(format!("{} ({e})", job.id)),
                }
            });
        }
    });

    problems.extend(failed.into_inner().unwrap());
    problems.sort();
    println!(
        "[icon_renderer] wrote {} icons to {} in {:.2}s",
        written.load(Ordering::Relaxed),
        out_dir.display(),
        start.elapsed().as_secs_f64()
    );
    if !problems.is_empty() {
        println!("[icon_renderer] unresolved ({}): {}", problems.len(), problems.join(", "));
    }
    0
}
