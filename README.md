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
| P4b other content | done - 3 compressed essentia jars, 10 metal storage blocks, inventarium bookshelf, warpward ring (Curios), one bedrock-remover focus and one summon focus per kept faction (11) |
| P5 recipes/research | done - 46 arcane recipes, 12 infusion recipes, 10 storage recipes, one METT research category with 7 entries, object aspects for all 70 items |
| P6 runtime verification | verified in the Sinytra Connector instance - the mod loads beside TC4R/TT/Curios/TerraBlender and Middle-earth, the 12 focus profiles and 7 research entries parse, every item and block registers and places, and no METT model or recipe errors are logged |

### Deliberately not ported

| donor content | why |
|---|---|
| the material and nugget metadata families (23 items) | Middle-earth already ships the metals and nuggets they stood for, so the recipes use `me:` items directly instead of a parallel item family |
| handheld faction crafter and its craft-table GUI | Middle-earth's own artisan table does the same job with its own matching rules; a second crafting GUI would duplicate it |
| the inventarium half of the bookshelf | it implemented Automagy's `IInventariumSlotUpgrade`, and Automagy has no 1.20.1 build; the enchantment bonus is kept |
| Travellers Gear titles | Travellers Gear has no 1.20.1 build and the titles were cosmetic |

