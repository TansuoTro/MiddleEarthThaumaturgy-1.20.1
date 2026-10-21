package sol3675.middleearththaumaturgy.wand;

import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.aspect.VisChannel;
import dev.tc4port.thaumcraft.api.aspect.VisCost;
import dev.tc4port.thaumcraft.api.wand.WandView;
import dev.tc4port.thaumcraft.api.wand.behavior.WandServerBehavior;
import dev.tc4port.thaumcraft.api.wand.behavior.WandServerContext;
import net.minecraft.server.level.ServerPlayer;
import sol3675.middleearththaumaturgy.compat.me.MiddleEarthCompat;

import java.util.EnumMap;
import java.util.Map;

/**
 * Recharges a faction wand from the owner's standing with that faction.
 *
 * <p>The donor read {@code LOTRLevelData.getData(player).getAlignment(faction)} and refilled every
 * primal when it sat below 90% of capacity. Middle-earth has no numeric alignment, so
 * {@link MiddleEarthCompat#visTier} substitutes the faction the player joined plus the disposition
 * graph, and the donor's three alignment bands become three tiers.
 *
 * <p>Thaumic Tinkerer's rods recharge four times as fast, matching the donor's separate
 * {@code FactionWandUpdateTT} progression.
 */
public final class FactionWandBehavior implements WandServerBehavior {
    private final String factionId;
    private final String disposition;
    private final boolean tinkerer;

    public FactionWandBehavior(String factionId, String disposition, boolean tinkerer) {
        this.factionId = factionId;
        this.disposition = disposition;
        this.tinkerer = tinkerer;
    }

    @Override
    public void serverTick(WandServerContext context) {
        ServerPlayer player = context.player();
        int tier = MiddleEarthCompat.visTier(player, factionId, disposition);
        int perChannel = recovery(tier);
        if (perChannel <= 0) {
            return;
        }
        WandView view = context.view();
        Map<VisChannel, Integer> amounts = new EnumMap<>(VisChannel.class);
        for (VisChannel channel : VisChannel.values()) {
            int current = view.visCentivis().getOrDefault(channel, 0);
            int capacity = view.capacityCentivis().getOrDefault(channel, 0);
            // The donor only refills a primal that is below 90% of the rod's maximum.
            if (capacity > 0 && current < capacity * 9 / 10) {
                amounts.put(channel, perChannel * 100);
            }
        }
        if (amounts.isEmpty()) {
            return;
        }
        context.insert(VisCost.ofCentivis(amounts), VisAction.EXECUTE);
    }

    private int recovery(int tier) {
        if (tinkerer) {
            return switch (tier) {
                case MiddleEarthCompat.TIER_HIGH -> 400;
                case MiddleEarthCompat.TIER_MID -> 100;
                case MiddleEarthCompat.TIER_LOW -> 20;
                default -> 0;
            };
        }
        return MiddleEarthCompat.visRecovery(tier);
    }
}
