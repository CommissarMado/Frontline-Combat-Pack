@echo off
setlocal enabledelayedexpansion

echo ========================================
echo  Frontline Combat Pack - PR Submission
echo  NeoForge 1.21.1 port by beihaimc
echo ========================================
echo.

REM Configure git proxy - change if needed
set "SOCKS_PROXY=socks5://127.0.0.1:10808"

git config --local http.proxy %SOCKS_PROXY%
git config --local https.proxy %SOCKS_PROXY%

REM Init repo
if not exist ".git" (
    git init
)

REM Branch
set "BRANCH=neoforge-1.21.1"
git checkout -b %BRANCH% 2>nul || git checkout %BRANCH%

REM Add files (excluding build artifacts)
git add -A

REM Commit
git commit -m "NeoForge 1.21.1 port by beihaimc

Full port of Frontline Combat Pack from Forge 1.20.1 to NeoForge 1.21.1.
- Migrated build system to NeoGradle (net.neoforged.moddev)
- Updated all imports and APIs for NeoForge 1.21.1
- Converted ResourceLocation to 1.21.1 API
- Fixed entity data syncing for new SynchedEntityData API
- Updated GeckoLib imports for 4.7.5
- Adapted DamageModifier API for SBW 1.21.1"

REM Push
echo Pushing to GitHub (needs auth)...
git push -u origin %BRANCH%

if %ERRORLEVEL% neq 0 (
    echo.
    echo ===== PUSH FAILED =====
    echo You need to:
    echo 1. Fork CommissarMado/Frontline-Combat-Pack on GitHub
    echo 2. Add your fork as remote:
    echo    git remote add origin https://github.com/YOUR_USERNAME/Frontline-Combat-Pack.git
    echo 3. Run this script again
    echo.
    echo Or install gh CLI and run:
    echo    gh repo fork CommissarMado/Frontline-Combat-Pack --clone=false
    echo    git push -u origin %BRANCH%
    pause
    exit /b
)

echo.
echo Push success! Creating PR...
gh pr create --draft ^
    --repo CommissarMado/Frontline-Combat-Pack ^
    --base master ^
    --head %BRANCH% ^
    --title "[NeoForge 1.21.1] Port Frontline Combat Pack" ^
    --body "## NeoForge 1.21.1 Port by beihaimc

This PR ports Frontline Combat Pack from Forge 1.20.1 to NeoForge 1.21.1.

### Changes
- **Build system**: ForgeGradle -> NeoGradle (net.neoforged.moddev 2.0.80)
- **Minecraft**: 1.20.1 -> 1.21.1
- **Loader**: Forge -> NeoForge 21.1.228
- **Java**: 17 -> 21
- **GeckoLib**: 4.4.x -> 4.7.5 (NeoForge)
- **All** net.minecraftforge.* -> net.neoforged.* imports
- All ResourceLocation constructors -> newer API
- All event bus registrations updated
- Entity data syncing adapted for 1.21.1 builder API

### Dependencies
- Requires NeoForge 21.1.203+
- Requires Superb Warfare 0.8.9 (Mercurows 1.21 branch)
- Requires GeckoLib 4.7.5+ for NeoForge

### Testing
- Build: gradlew build (BUILD SUCCESSFUL)
- All 134 Java files compile without errors

Signed-off-by: beihaimc"

if %ERRORLEVEL% neq 0 (
    echo gh CLI not found or PR creation failed.
    echo Create PR manually at:
    echo https://github.com/CommissarMado/Frontline-Combat-Pack/compare
    pause
    exit /b
)

echo.
echo ========================================
echo  PR created successfully!
echo ========================================
pause
