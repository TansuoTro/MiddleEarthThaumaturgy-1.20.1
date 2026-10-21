package sol3675.middleearththaumaturgy.wand;

import sol3675.middleearththaumaturgy.compat.me.MiddleEarthCompat;

/**
 * One faction wand rod: the Middle-earth faction whose standing charges it, and the donor's
 * texture/identity key.
 *
 * <p>The donor had 24 of these. Only the factions Middle-earth actually ships are kept; see
 * {@code ALIGNMENT_METT_ME.md} for the dropped ones.
 */
public record FactionWand(String key, String factionId, String disposition) {

    /** Capacity of the ordinary faction rod, matching the donor's {@code new WandRod(..., 100, ...)}. */
    public static final int CAPACITY = 100;

    /** Capacity of the Thaumic Tinkerer rod, matching the donor's 1000. */
    public static final int TINKERER_CAPACITY = 1000;

    /** Assembly cost shared by both rods in the donor. */
    public static final int CRAFT_COST = 12;

    public boolean good() {
        return MiddleEarthCompat.GOOD.equals(disposition);
    }
}
