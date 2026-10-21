# MiddleEarthThaumaturgy 1.20.1

Unofficial 1.20.1 Forge port of [MiddleEarth-Thaumaturgy](https://github.com/SOL3675/MiddleEarth-Thaumaturgy)
(MC 1.7.10, Thaumcraft 4 + the LotR mod).

Target runtime:

| component | version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.23 |
| Thaumcraft 4R | `0.1.0-20746` (runtime `4.2.3.5-1.20.1-port.0.1.0-20746`) |
| Thaumic Tinkerer | `0.1.0-20746` (optional at runtime, `-PwithTt=false` to drop it) |
| Curios | 5.14.1+1.20.1 |
| Middle-earth | 1.20.1 Fabric port, loaded through Sinytra Connector |

## Build

```bash
./gradlew build
```

TC4R and its sibling ports are not redistributable, so they resolve from `local-repo/` (a plain
Maven repository). The pinned versions need these files:

```text
local-repo/dev/tc4port/thaumcraft-forge/0.1.0-20746/
  thaumcraft-forge-0.1.0-20746.jar          (runtime)
  thaumcraft-forge-0.1.0-20746-api.jar      (compile)
  thaumcraft-forge-0.1.0-20746-sources.jar  (IDE)
  thaumcraft-forge-0.1.0-20746.pom
local-repo/dev/tc4port/thaumic-tinkerer-forge/0.1.0-20746/   (same four files)
```

They come from the `1.20.1-forge-20746.zip` / `1.20.1-forge-20746-dev.zip` release archives;
the full jars keep their `4.2.3.5-1.20.1-port.0.1.0-20746` file names in the archive and are
renamed to the `<artifactId>-<version>` Maven layout on seeding.

## Layout

- `ALIGNMENT_METT_ME.md` - how the 1.7.10 content maps onto Middle-earth 1.20.1 and TC4R, and the
  cross-loader rules that let a Forge mod talk to a Connector-loaded Fabric mod.
- `build/agents/mett_align.py` - regenerates `build/agents/mett_me_alignment.json` and
  `build/agents/mett_alignment_auto.md` from the donor sources and the Middle-earth catalogue.
- `build/agents/gen_aspects.py` - regenerates the TC4R aspect data files and copies the aspect textures.
- `build/agents/gen_wand_assets.py` - regenerates the wand-core item models, textures and translations.

## Status

| phase | state |
|---|---|
| P1 build skeleton | done - `compileJava` and `jar` green against TC4R 20746 |
| P2 Middle-earth interop layer | done - `compat/me/MiddleEarthCompat` (registry ids, faction/disposition via reflection, NPC spawn eggs) |
| P3 aspects | 25 of the donor's 39 data-pack aspects ported (13 faction aspects without a Middle-earth counterpart and `nazgul` are dropped) |
| P4a faction wands | 11 rod items + rods + faction recharge behaviour; Thaumic Tinkerer variants (1000 vis, glowing) register when TT is present |
| P4b other content | not started - compressed essentia jars, bookshelf inventarium, storage, rings, foci |
| P5 recipes/research | not started |
| P6 runtime verification | not started |
