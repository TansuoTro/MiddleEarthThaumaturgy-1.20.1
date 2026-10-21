# METT -> Middle-earth (ME 1.20.1) alignment

对齐目标：把 **MiddleEarth-Thaumaturgy (METT, MC 1.7.10 / TC4 / LOTR-mod v34+)** 的内容对齐到
**Middle-earth 1.20.1 (Fabric, 经 Sinytra Connector 跑在 Forge 上)** + **Thaumcraft 4R (TC4R 20746)**。

- Donor: `H:/MinecraftMods/MiddleEarth-Thaumaturgy` (branch `v36`, 73 java files / 9.4k LOC)
- Target mod: `H:/MinecraftMods/MiddleEarth-1.20.1` (namespace `me:`, package `net.jukoz.me`)
- 机器可读对齐表: `build/agents/mett_me_alignment.json`
- 自动生成的完整候选清单: `build/agents/mett_alignment_auto.md`

## 0. TL;DR

| 维度 | 结论 |
|---|---|
| 1.7.10 -> 1.20.1 (Forge) | 可行。73 个文件、9.4k LOC，TC4 侧全部有 TC4R 对应物（aspect / research / wand / focus / essentia / infusion） |
| LOTR -> ME | **不是 1:1**。24 个 LOTR 派系只有 11 个在 ME 存在；586 个物品引用里约 3/4 有 ME 候选，但名字全变，需逐条确认 |
| Connector 跨加载器 | 可行，前提是 **不编译期依赖 ME**：只用 `BuiltInRegistries` 按 id 取物品/方块/实体 + 可选反射取派系数据 |
| 需要重设计的机制 | LOTR 的数值 alignment -> ME 的「已加入派系 + 外交态度」；24 张派系工作台 -> 1 张 artisan_table |

## 1. 跨加载器互操作规则（硬约束）

ME 是 Fabric 模组，在 Forge 上由 Connector 把 MC 引用重映射到 Forge 运行时命名空间；
**模组自己的类名/成员名不改**，但方法签名里出现的 MC 类型会被重映射。因此：

1. **禁止编译期依赖 ME**。不要 `compileOnly` ME 的 Fabric dev jar：yarn 类名
   (`net.minecraft.item.ItemStack`) 与运行时官方/SRG 名不一致，调用点会 `NoSuchMethodError`。
2. 取内容一律走注册表：`BuiltInRegistries.ITEM.get(new ResourceLocation("me", path))`。
   找不到就降级（跳过该 aspect / 该配方），不要崩。
3. 需要 ME 的玩家状态（已加入派系）时用 **反射**，且必须 try/catch + 缓存 + 失败降级。
4. ME 的 NPC 全部是 **单一实体 `me:npc`** + `npc_type` 数据；召唤请用
   `me:<npc_type 路径，点变下划线>_spawn_egg` 物品，不要直接 new 实体。

## 2. 派系对齐

LOTR 24 个派系 -> ME 11 个（ME 共 18 个 faction 数据条目，含 2 个子派系 + 2 个 fallback）。

| LOTR 1.7.10 | ME 1.20.1 | 处置 |
|---|---|---|
| GONDOR | `me:gondor` | 保留 |
| HOBBIT | `me:shire` | 保留（改名） |
| DURINS_FOLK | `me:longbeards.erebor` | 保留（并到 Longbeards） |
| BLUE_MOUNTAINS | `me:longbeards` | 保留（并到 Longbeards） |
| GUNDABAD | `me:hobgoblin_tribes.gundabad` | 保留 |
| WOOD_ELF | `me:woodland_realm` | 保留 |
| LOTHLORIEN | `me:lothlorien` | 保留 |
| ISENGARD | `me:isengard` | 保留 |
| ROHAN | `me:rohan` | 保留 |
| MORDOR | `me:mordor` | 保留 |
| DALE | `me:dale` | 保留 |
| RANGER_NORTH (Arnor) | — | 删除 |
| HIGH_ELF (Lindon) | — | 删除 |
| ANGMAR | — | 删除 |
| DOL_GULDUR | — | 删除（ME 只有 mordor） |
| DUNLAND | — | 删除 |
| FANGORN | — | 删除（ME 无 ent） |
| DORWINION | — | 删除 |
| RHUDEL (Rhun) | — | 删除 |
| NEAR_HARAD | — | 删除 |
| MORWAITH | — | 删除 |
| TAURETHRIM | — | 删除 |
| HALF_TROLL | — | 删除 |
| BREE | — | 删除 |

ME 独有、METT 无对应：`brigand`、`goblin_town`、`moria`、`wild_goblins`、`hobgoblin_tribes`(父)、`longbeards`(父)。

### 2.1 alignment 数值 -> 派系/外交 的替换设计

METT 用 `LOTRLevelData.getData(player).getAlignment(faction)` 的数值（100 / 1000 / 10000 三档）
驱动派系法杖回 vis。ME 没有数值 alignment，只有「玩家加入 1 个派系 + 派系间 disposition」。

替换（`FactionWandUpdate` 重写）：

| 条件 | 每 20 tick 回 vis |
|---|---|
| 玩家所属派系 == 法杖派系 | 50（原 10000 档） |
| disposition = ALLY / FRIENDLY | 10（原 1000 档） |
| disposition = NEUTRAL | 5（原 100 档） |
| 其余 / 未加入派系 | 0 |

## 3. 内容族对齐

| METT 引用（LOTR 1.7.10） | ME 1.20.1 | 说明 |
|---|---|---|
| `LOTRMod.orcBone/dwarfBone/elfBone/hobbitBone/trollBone` | 只有 `me:orc_bone` | 其余 4 种骨 **ME 不存在** -> 统一 `me:orc_bone` 或 vanilla `bone` |
| `ruby/sapphire/topaz/opal/pearl/amber` | **不存在** | 用 vanilla 宝石或 ME 钢锭系列替代 |
| `dwarfSteel/urukSteel/blueDwarfSteel` | `me:dwarven_steel_ingot` / `me:uruk_steel_ingot` / `me:khazad_steel_ingot` | 直接对应 |
| `quenditeCrystal/guldurilCrystal/nauriteGem` | 无同名 | 用 edhel/morgul 钢替代或删除 |
| `oreGem`(meta 0-6) / `rock`(meta) / `brick`(meta) | ME 是拆开的独立 id（63 个 `*_ore`、多套 stone） | 元数据族必须手工展开 |
| 24 张 `*CraftingTable` | `me:artisan_table`（唯一） | 全部合并；METT 的 craft-table GUI 废弃 |
| `alloyForge/elvenForge/orcForge/dwarvenForge` | `me:forge`（唯一） | 合并 |
| `banner`(meta 0-21) | `me:held_banner` + banner_pattern | 无 22 色 meta |
| `fangornPlant`(meta) | 无 | 删除或换 ME 植物 |

## 4. 覆盖率（自动候选，未人工确认）

`build/agents/mett_align.py` 对 donor 引用做词法对齐（类型词 + 派系/材质词）：

| 域 | 有候选 | 总数 | 说明 |
|---|---|---|---|
| 派系 | 11 | 24 | 人工确认 |
| NPC（召唤焦点） | 14 | 96 | 多数 LOTR NPC 在 ME 无同类 |
| 物品（aspect 目标） | 447 | 586 | **候选**，含误配，需逐条确认 |
| 方块 | 58 | 121 | 同上 |
| `LOTRMod.*` 字段 | 89 | 111 | 同上 |

> 候选 != 对齐。JSON 里每条都有 `confidence`；只有 `>=0.75` 且人工抽查过的才能落成代码。

## 5. TC4 -> TC4R 映射

| METT (TC4 1.7.10) | TC4R 20746 |
|---|---|
| `thaumcraft.api.aspects.Aspect` / `AspectList` | `dev.tc4port.thaumcraft.api.aspect.AspectId` / `AspectDefinition` / `AspectAmounts` / `AspectApi` |
| `ThaumcraftApi.registerObjectTag` | `AspectApi` / 数据驱动 aspect 注册 |
| `ResearchItem` / `ResearchCategories` | `dev.tc4port.thaumcraft.api.research.*`（JSON 数据包驱动） |
| `WandRod` / `WandCap` / `ItemWandCasting` | `WandApi` + `WandMaterialId` + `WandRodSpec` / `WandCapSpec` / `CustomWandItem` |
| `ItemFocusBasic` / `FocusUpgradeType` | `focus.FocusApi` / `FocusProfileDefinition` / `FocusActionApi` |
| `IWandRodOnUpdate`（派系回 vis） | `wand.behavior.WandServerBehavior` |
| `BlockJar` / `TileJar`（压缩要素罐） | `essentia.EssentiaApi` + `EssentiaCapabilities.SOURCE/TRANSPORT` |
| `ConfigBlocks/ConfigItems` | 无公开对应，改查 TC4R 注册表 |
| Baubles `IBauble` | **Curios**（TC4R 强依赖 curios 5.14+）+ `VisDiscountGear`/`WarpingGear`/`RunicGear` |
| TravellersGear / Automagy 兼容 | **删除**（无 1.20.1 版本） |
| Thaumic Tinkerer 兼容 | 可选：TC4R 侧有 `thaumic-tinkerer-forge-20746` |

## 6. 工作分解

- **P1** 构建骨架：MDG legacyforge + Forge 47.4.20 + TC4R 20746 + curios；mod 入口、config、创造标签页。
- **P2** ME 互操作层 `mett/compat/me/`：注册表查询、派系服务（反射 + 缓存 + 降级）、NPC 生成蛋召唤。
- **P3** aspect 层：14 个种族 aspect + 11 个派系 aspect；可对齐的物品 aspect 重新注册。
- **P4** 内容：3 种压缩要素罐 + bookshelf inventarium + storage、法杖核/帽、指环、焦点。
- **P5** 配方 + 研究（TC4R 数据包格式）。
- **P6** 运行时验证：`G:/MinecraftGames/MiddleEarth-Connector-DEV`（Forge 47.4.23 + Connector beta.49，bridge 48790）。
