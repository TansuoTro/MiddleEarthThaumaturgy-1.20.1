package sol3675.middleearththaumaturgy.wand;

import sol3675.middleearththaumaturgy.compat.me.MiddleEarthCompat;

import java.util.List;

/**
 * The faction wands that survive the port, in the donor's declaration order.
 *
 * <p>Dispositions mirror Middle-earth's own faction data (verified against
 * {@code data/me/me/faction/*.json}): Gondor, the Shire, both dwarf realms, the Woodland Realm,
 * Lothlorien, Rohan and Dale are {@code GOOD}; Gundabad, Isengard and Mordor are {@code EVIL}.
 */
public final class FactionWands {
    public static final List<FactionWand> KEPT = List.of(
            new FactionWand("gondor", "me:gondor", MiddleEarthCompat.GOOD),
            new FactionWand("hobbit", "me:shire", MiddleEarthCompat.GOOD),
            new FactionWand("dwarf", "me:longbeards.erebor", MiddleEarthCompat.GOOD),
            new FactionWand("blue_mountains", "me:longbeards", MiddleEarthCompat.GOOD),
            new FactionWand("gundabad", "me:hobgoblin_tribes.gundabad", MiddleEarthCompat.EVIL),
            new FactionWand("wood_elf", "me:woodland_realm", MiddleEarthCompat.GOOD),
            new FactionWand("galadhrim", "me:lothlorien", MiddleEarthCompat.GOOD),
            new FactionWand("uruk_hai", "me:isengard", MiddleEarthCompat.EVIL),
            new FactionWand("rohan", "me:rohan", MiddleEarthCompat.GOOD),
            new FactionWand("mordor", "me:mordor", MiddleEarthCompat.EVIL),
            new FactionWand("dale", "me:dale", MiddleEarthCompat.GOOD)
    );

    private FactionWands() {
    }
}
