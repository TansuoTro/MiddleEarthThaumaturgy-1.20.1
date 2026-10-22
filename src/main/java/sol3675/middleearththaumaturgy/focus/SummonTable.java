package sol3675.middleearththaumaturgy.focus;

import sol3675.middleearththaumaturgy.wand.FactionWand;
import sol3675.middleearththaumaturgy.wand.FactionWands;

import java.util.ArrayList;
import java.util.List;

/**
 * Which Middle-earth NPCs a faction's summon focus can call, by standing.
 *
 * <p>The donor listed 24 factions and rolled a weighted table of Lord of the Rings entity classes:
 * the rank and file below 500 alignment, the elites above it. Middle-earth registers one NPC entity
 * with a data-driven {@code npc_type} per rank, so each entry here is an {@code npc_type} id and the
 * roll becomes a uniform pick from a short list.
 *
 * <p>The alignment bands map onto {@code MiddleEarthCompat}'s tiers: the rank-and-file list needs
 * {@code TIER_MID} (a non-hostile faction of the same disposition) and the elite list needs
 * {@code TIER_HIGH} (membership of the faction itself).
 */
public final class SummonTable {

    /** One faction's summonable NPCs. {@code key} matches the wand core and focus id suffix. */
    public record Faction(String key, String factionId, String disposition,
                          List<String> rankAndFile, List<String> elites) {
    }

    public static final List<Faction> ALL = List.of(
            new Faction("gondor", "me:gondor", "GOOD",
                    List.of("gondor.militia", "gondor.soldier"),
                    List.of("gondor.veteran", "gondor.knight", "gondor.citadel_guard")),
            new Faction("hobbit", "me:shire", "GOOD",
                    List.of("shire.militia", "shire.peasant"),
                    List.of("shire.shirriff")),
            new Faction("dwarf", "me:longbeards.erebor", "GOOD",
                    List.of("longbeards.erebor.militia", "longbeards.erebor.soldier"),
                    List.of("longbeards.erebor.veteran", "longbeards.erebor.elite", "longbeards.erebor.gatewarden")),
            new Faction("blue_mountains", "me:longbeards", "GOOD",
                    List.of("longbeards.erebor.archer", "longbeards.erebor.miner"),
                    List.of("longbeards.erebor.gatewarden", "longbeards.erebor.leader")),
            new Faction("gundabad", "me:hobgoblin_tribes.gundabad", "EVIL",
                    List.of("hobgoblin_tribes.gundabad.militia", "hobgoblin_tribes.gundabad.goblin"),
                    List.of("hobgoblin_tribes.gundabad.warrior", "hobgoblin_tribes.gundabad.veteran",
                            "hobgoblin_tribes.gundabad.rider")),
            new Faction("wood_elf", "me:woodland_realm", "GOOD",
                    List.of("woodland_realm.warrior", "woodland_realm.hunter"),
                    List.of("woodland_realm.sentinel", "woodland_realm.elven_kings_guard",
                            "woodland_realm.commander")),
            new Faction("galadhrim", "me:lothlorien", "GOOD",
                    List.of("lothlorien.warrior", "lothlorien.civilian"),
                    List.of("lothlorien.sentinel", "lothlorien.knight", "lothlorien.guard")),
            new Faction("uruk_hai", "me:isengard", "EVIL",
                    List.of("isengard.snaga", "isengard.scout"),
                    List.of("isengard.warrior", "isengard.berserker", "isengard.orthanc_guard")),
            new Faction("rohan", "me:rohan", "GOOD",
                    List.of("rohan.militia", "rohan.soldier"),
                    List.of("rohan.knight", "rohan.royal_guard", "rohan.eorling_marshal")),
            new Faction("mordor", "me:mordor", "EVIL",
                    List.of("mordor.snaga", "mordor.militia"),
                    List.of("mordor.warrior", "mordor.black_numenorean", "mordor.veteran")),
            new Faction("dale", "me:dale", "GOOD",
                    List.of("dale.militia", "dale.soldier"),
                    List.of("dale.knight", "dale.elite_archer", "dale.sergeant"))
    );

    /** The faction whose key matches the given wand key, or {@code null}. */
    public static Faction byKey(String key) {
        for (Faction faction : ALL) {
            if (faction.key().equals(key)) {
                return faction;
            }
        }
        return null;
    }

    /**
     * Every summon table must name a faction the faction wands kept, otherwise a summon focus would
     * exist for a faction whose wand does not. Checked once at class load.
     */
    static {
        List<String> wandKeys = new ArrayList<>();
        for (FactionWand wand : FactionWands.KEPT) {
            wandKeys.add(wand.key());
        }
        for (Faction faction : ALL) {
            if (!wandKeys.contains(faction.key())) {
                throw new IllegalStateException("Summon table without a faction wand: " + faction.key());
            }
        }
    }

    private SummonTable() {
    }
}
