package sol3675.middleearththaumaturgy.compat.me;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import sol3675.middleearththaumaturgy.MiddleEarthThaumaturgy;

import java.lang.reflect.Method;

/**
 * The only place that talks to Middle-earth.
 *
 * <p>Middle-earth is a Fabric mod loaded through Sinytra Connector, so its classes are remapped
 * into the Forge runtime namespace and must never be referenced at compile time: a method whose
 * descriptor mentions a Minecraft type would bind against the wrong signature. Content is
 * therefore resolved by registry id, and the little state we need (the player's faction and its
 * disposition) is read reflectively, by method name, with every failure degrading to
 * "not available".
 */
public final class MiddleEarthCompat {
    /** Middle-earth's mod id: the 1.20.1 port registers as {@code me}, upstream 1.21 used {@code middle-earth}. */
    public static final String MOD_ID = "me";
    public static final String LEGACY_MOD_ID = "middle-earth";

    public static final String GOOD = "GOOD";
    public static final String NEUTRAL = "NEUTRAL";
    public static final String EVIL = "EVIL";

    /** Recharge tiers, replacing the donor's numeric alignment bands (100 / 1000 / 10000). */
    public static final int TIER_NONE = 0;
    public static final int TIER_LOW = 1;
    public static final int TIER_MID = 2;
    public static final int TIER_HIGH = 3;

    private static Boolean loaded;
    private static Method getPlayerFaction;
    private static Method factionGetId;
    private static Method factionIsHostileToward;
    private static Method factionGetDisposition;
    private static Method factionGetDispositionString;
    private static Method spawnEggForNpcType;
    private static boolean spawnEggLookupFailed;
    private static boolean warnedMissingEgg;
    private static boolean reflectionFailed;

    private MiddleEarthCompat() {
    }

    public static boolean isLoaded() {
        if (loaded == null) {
            ModList mods = ModList.get();
            loaded = mods != null && (mods.isLoaded(MOD_ID) || mods.isLoaded(LEGACY_MOD_ID));
        }
        return loaded;
    }

    // ------------------------------------------------------------------ content

    public static ResourceLocation id(String path) {
        ResourceLocation id = ResourceLocation.tryBuild(MOD_ID, path);
        return id == null ? new ResourceLocation(MOD_ID, "missing") : id;
    }

    public static @Nullable Item item(String path) {
        if (!isLoaded()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryBuild(MOD_ID, path);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return null;
        }
        return BuiltInRegistries.ITEM.get(id);
    }

    public static @Nullable Block block(String path) {
        if (!isLoaded()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryBuild(MOD_ID, path);
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            return null;
        }
        return BuiltInRegistries.BLOCK.get(id);
    }

    public static boolean hasItem(String path) {
        return item(path) != null;
    }

    public static ItemStack stack(String path) {
        return stack(path, 1);
    }

    public static ItemStack stack(String path, int count) {
        Item item = item(path);
        return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    // ------------------------------------------------------------- faction state

    /** The faction the player joined, as {@code me:gondor}, or {@code null} when there is none. */
    public static @Nullable String playerFactionId(Player player) {
        Object faction = playerFaction(player);
        return faction == null ? null : factionId(faction);
    }

    /** The disposition ({@code GOOD} / {@code NEUTRAL} / {@code EVIL}) of the player's faction. */
    public static @Nullable String playerDisposition(Player player) {
        Object faction = playerFaction(player);
        if (faction == null || !bindReflection() || factionGetDisposition == null) {
            return null;
        }
        try {
            // DispositionType is an enum with a display string; the string is what the data files use.
            if (factionGetDispositionString != null) {
                Object name = factionGetDispositionString.invoke(faction);
                if (name != null) {
                    return name.toString();
                }
            }
            Object disposition = factionGetDisposition.invoke(faction);
            return disposition == null ? null : disposition.toString();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    /**
     * Recharge tier for a faction wand.
     *
     * <ul>
     *   <li>{@link #TIER_HIGH} - the player belongs to the wand's own faction.</li>
     *   <li>{@link #TIER_MID} - same disposition and not hostile.</li>
     *   <li>{@link #TIER_LOW} - not hostile, other disposition.</li>
     *   <li>{@link #TIER_NONE} - hostile, or no faction at all.</li>
     * </ul>
     *
     * @param wandFactionId   Middle-earth faction id of the wand, e.g. {@code me:gondor}
     * @param wandDisposition that faction's disposition, a constant of this class
     */
    public static int visTier(Player player, String wandFactionId, String wandDisposition) {
        Object playerFaction = playerFaction(player);
        if (playerFaction == null) {
            return TIER_NONE;
        }
        String playerFactionId = factionId(playerFaction);
        if (playerFactionId != null && isSameOrSubfaction(playerFactionId, wandFactionId)) {
            return TIER_HIGH;
        }
        if (isHostileToward(playerFaction, wandFactionId)) {
            return TIER_NONE;
        }
        String playerDisposition = playerDisposition(player);
        return wandDisposition.equals(playerDisposition) ? TIER_MID : TIER_LOW;
    }

    /**
     * Whether the player may call on this faction at all, and how strong the call may be.
     *
     * <p>Replaces the donor's numeric alignment bands: {@link #TIER_MID} or better can summon the
     * rank and file, and only membership of the faction itself ({@link #TIER_HIGH}) reaches the
     * elites. {@link #TIER_NONE} means the faction is hostile or the player has no faction.
     */
    public static int summonTier(Player player, String factionId, String factionDisposition) {
        return visTier(player, factionId, factionDisposition);
    }

    /** Vis restored per update tick for a tier, matching the donor's 0/5/10/50 progression. */
    public static int visRecovery(int tier) {
        return switch (tier) {
            case TIER_HIGH -> 50;
            case TIER_MID -> 10;
            case TIER_LOW -> 5;
            default -> 0;
        };
    }

    /**
     * Whether the player's faction is the given faction or one of its subfactions.
     *
     * <p>Middle-earth names a subfaction after its parent ({@code me:longbeards} has
     * {@code me:longbeards.erebor}), and a player who joins Erebor carries the subfaction id, so a
     * plain string comparison would deny the parent's own wand and foci to its members.
     */
    private static boolean isSameOrSubfaction(String playerFactionId, String factionId) {
        return playerFactionId.equals(factionId) || playerFactionId.startsWith(factionId + ".");
    }

    private static @Nullable String factionId(Object faction) {
        if (!bindReflection() || factionGetId == null) {
            return null;
        }
        try {
            Object id = factionGetId.invoke(faction);
            return id == null ? null : id.toString();
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static boolean isHostileToward(Object faction, String otherFactionId) {
        if (!bindReflection() || factionIsHostileToward == null) {
            return false;
        }
        ResourceLocation other = ResourceLocation.tryParse(otherFactionId);
        if (other == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(factionIsHostileToward.invoke(faction, other));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }

    private static @Nullable Object playerFaction(Player player) {
        if (!isLoaded() || !bindReflection() || getPlayerFaction == null) {
            return null;
        }
        try {
            return getPlayerFaction.invoke(null, player, player.level());
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    // ------------------------------------------------------------------- NPCs

    /**
     * A usable spawn egg for one {@code npc_type} id such as {@code gondor.soldier}.
     *
     * <p>Middle-earth does <b>not</b> register an item per NPC type. {@code item.me.<npc_type>_spawn_egg}
     * is only a language key: the helper names the generic {@code me:npc_spawn_egg} stack with it, and
     * the stack itself carries {@code EntityTag.InitializationData} plus the faction and race
     * components. So the egg has to be built by Middle-earth, which is what
     * {@code NpcSpawnEggHelper.getSpawnEgg} does; the plain item lookup below only ever succeeds for
     * the legacy per-entity eggs ({@code me:gondorian_soldier_spawn_egg} and friends).
     */
    public static ItemStack npcSpawnEgg(Level level, String npcTypeId) {
        ResourceLocation id = ResourceLocation.tryBuild(MOD_ID, npcTypeId);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Method helper = spawnEggForNpcType();
        if (helper != null && level != null) {
            try {
                Object built = helper.invoke(null, level, id);
                if (built instanceof ItemStack stack && !stack.isEmpty()) {
                    return stack;
                }
            } catch (ReflectiveOperationException | RuntimeException exception) {
                spawnEggLookupFailed = true;
            }
        }
        // Fallback for the legacy per-entity eggs; the dotted npc_type ids have no item.
        Item egg = item(npcTypeId + "_spawn_egg");
        if (egg == null) {
            if (!warnedMissingEgg) {
                warnedMissingEgg = true;
                MiddleEarthThaumaturgy.LOGGER.warn(
                        "No Middle-earth spawn egg for npc type {}; the helper lookup failed", npcTypeId);
            }
            return ItemStack.EMPTY;
        }
        return new ItemStack(egg);
    }

    /**
     * Middle-earth's own {@code NpcSpawnEggHelper.getSpawnEgg(Level, ResourceLocation)}.
     *
     * <p>The per-type spawn-egg items are only display shells: an NPC is spawned from the generic
     * {@code me:npc_spawn_egg} carrying {@code EntityTag.InitializationData} (plus the faction and
     * race components). The helper is what builds that stack, and it is also what the creative tab
     * uses, so it is the only reliable way to obtain a working egg for an {@code npc_type}.
     */
    private static @Nullable Method spawnEggForNpcType() {
        if (spawnEggLookupFailed) {
            return null;
        }
        if (spawnEggForNpcType != null) {
            return spawnEggForNpcType;
        }
        try {
            Class<?> helper = Class.forName("net.jukoz.me.entity.npcs.initializer.NpcSpawnEggHelper");
            for (Method method : helper.getMethods()) {
                Class<?>[] parameters = method.getParameterTypes();
                if (method.getName().equals("getSpawnEgg") && parameters.length == 2
                        && parameters[0].isAssignableFrom(Level.class)
                        && parameters[1].isAssignableFrom(ResourceLocation.class)) {
                    method.setAccessible(true);
                    spawnEggForNpcType = method;
                    return method;
                }
            }
        } catch (ClassNotFoundException | LinkageError exception) {
            MiddleEarthThaumaturgy.LOGGER.warn("Middle-earth spawn egg helper unavailable: {}", exception.toString());
        }
        spawnEggLookupFailed = true;
        return null;
    }

    // -------------------------------------------------------------- reflection

    private static boolean bindReflection() {
        if (reflectionFailed) {
            return false;
        }
        if (getPlayerFaction != null) {
            return true;
        }
        try {
            Class<?> service = Class.forName("net.jukoz.me.resources.persistent_datas.PlayerDataService");
            Class<?> faction = Class.forName("net.jukoz.me.resources.datas.factions.Faction");
            getPlayerFaction = findMethod(service, "getPlayerFaction", 2);
            factionGetId = findMethod(faction, "getId", 0);
            factionIsHostileToward = findMethod(faction, "isHostileToward", 1);
            factionGetDisposition = findMethod(faction, "getDisposition", 0);
            factionGetDispositionString = findMethod(faction, "getDispositionString", 0);
            if (getPlayerFaction == null || factionGetId == null) {
                reflectionFailed = true;
                return false;
            }
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            reflectionFailed = true;
            MiddleEarthThaumaturgy.LOGGER.warn("Middle-earth faction API unavailable: {}", exception.toString());
            return false;
        }
    }

    private static @Nullable Method findMethod(Class<?> owner, String name, int parameterCount) {
        for (Method method : owner.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }
}
