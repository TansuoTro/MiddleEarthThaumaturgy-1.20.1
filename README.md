# MiddleEarthThaumaturgy 1.20.1

Unofficial 1.20.1 Forge port of [MiddleEarth-Thaumaturgy](https://github.com/SOL3675/MiddleEarth-Thaumaturgy)
(MC 1.7.10, Thaumcraft 4 + the LotR mod).

Target runtime:

| component | version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.23 |
| Thaumcraft 4R | `0.1.0-20744` (runtime `4.2.3.5-1.20.1-port.0.1.0-20744`) |
| Curios | 5.14.1+1.20.1 |
| Middle-earth | 1.20.1 Fabric port, loaded through Sinytra Connector |

## Build

```bash
./gradlew build
```

TC4R is not redistributable, so it resolves from `local-repo/` (a plain Maven repository).
Seed the pinned version by copying these four files from another project's local repository:

```text
local-repo/dev/tc4port/thaumcraft-forge/0.1.0-20744/
  thaumcraft-forge-0.1.0-20744.jar
  thaumcraft-forge-0.1.0-20744-api.jar
  thaumcraft-forge-0.1.0-20744-sources.jar
  thaumcraft-forge-0.1.0-20744.pom
```

Until 20744 is seeded, build against the version that is present:

```bash
./gradlew build -Pthaumcraft_version=0.1.0-20740
```

## Layout

- `ALIGNMENT_METT_ME.md` - how the 1.7.10 content maps onto Middle-earth 1.20.1 and TC4R, and the
  cross-loader rules that make a Forge mod able to talk to a Connector-loaded Fabric mod.
- `build/agents/mett_align.py` - regenerates `build/agents/mett_me_alignment.json` and
  `build/agents/mett_alignment_auto.md` from the donor sources and the Middle-earth catalogue.
- `build/agents/gen_aspects.py` - regenerates the TC4R aspect data files and copies the aspect
  textures from the donor.

## Status

| phase | state |
|---|---|
| P1 build skeleton | done - `compileJava` green |
| P2 Middle-earth interop layer | done - `compat/me/MiddleEarthCompat` (registry ids, faction/disposition via reflection, NPC spawn eggs) |
| P3 aspects | 25 of the donor's 39 data-pack aspects ported (13 faction aspects without a Middle-earth counterpart and `nazgul` are dropped) |
| P4 content | not started |
| P5 recipes/research | not started |
| P6 runtime verification | not started |
