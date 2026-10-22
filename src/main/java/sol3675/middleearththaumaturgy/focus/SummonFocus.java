package sol3675.middleearththaumaturgy.focus;

import dev.tc4port.thaumcraft.api.aspect.VisAction;
import dev.tc4port.thaumcraft.api.focus.FocusId;
import dev.tc4port.thaumcraft.api.focus.action.FocusActionApi;
import dev.tc4port.thaumcraft.api.focus.action.FocusActionContext;
import dev.tc4port.thaumcraft.api.focus.action.FocusActionResult;
import dev.tc4port.thaumcraft.api.focus.action.FocusActionSpec;
import dev.tc4port.thaumcraft.api.focus.action.FocusActionTarget;
import dev.tc4port.thaumcraft.api.focus.action.FocusCastMode;
import dev.tc4port.thaumcraft.api.focus.action.FocusGesture;
import dev.tc4port.thaumcraft.api.focus.action.FocusInput;
import dev.tc4port.thaumcraft.api.focus.action.FocusTargetKind;
import dev.tc4port.thaumcraft.api.presentation.ActionPresentationAudience;
import dev.tc4port.thaumcraft.api.research.ResearchRequirement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import sol3675.middleearththaumaturgy.compat.me.MiddleEarthCompat;
import sol3675.middleearththaumaturgy.references.LibMisc;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The summon focus: calls one soldier of the focus' faction to the block the wand points at.
 *
 * <p>The donor kept 24 foci in one metadata family and rolled a weighted table of Lord of the Rings
 * NPC classes, gated at 100 alignment for the rank and file and 500 for the elites. Metadata
 * variants do not exist in 1.20.1, so every faction gets its own focus item and its own catalog
 * entry; {@link SummonTable} holds what each one may call.
 *
 * <p>The NPC is spawned by using Middle-earth's own spawn egg for the chosen {@code npc_type},
 * which keeps this class free of any Middle-earth type reference.
 */
public final class SummonFocus {
    /** How far the focus reaches, in blocks. */
    private static final int RANGE = 8;
    /** Pause after a successful summon, so a held wand cannot fill the area in one second. */
    private static final int COOLDOWN = 20;

    private static final Map<String, FocusId> IDS = new LinkedHashMap<>();

    static {
        for (SummonTable.Faction faction : SummonTable.ALL) {
            IDS.put(faction.key(), FocusId.of(new ResourceLocation(LibMisc.MODID, "focus_summon_" + faction.key())));
        }
    }

    /** The catalog id of the focus for a faction key such as {@code gondor}. */
    public static FocusId id(String key) {
        return IDS.get(key);
    }

    private SummonFocus() {
    }

    /** Registers one action per faction. Called once, on both sides. */
    public static void register() {
        for (SummonTable.Faction faction : SummonTable.ALL) {
            FocusActionApi.register(new FocusActionSpec(
                    new ResourceLocation(LibMisc.MODID, "summon_" + faction.key()),
                    new FocusActionTarget.Focus(id(faction.key())), FocusInput.PRIMARY,
                    EnumSet.of(FocusTargetKind.BLOCK), FocusCastMode.INSTANT,
                    ResearchRequirement.none(), RANGE, 0, ActionPresentationAudience.PREDICTED),
                    (context, action) -> handle(faction, context, action));
        }
    }

    private static FocusActionResult handle(SummonTable.Faction faction, FocusActionContext context, VisAction action) {
        // An instant cast also receives a hold gesture every interval tick; only the press may fire.
        if (context.interaction().gesture() != FocusGesture.PRESS) {
            return FocusActionResult.PASS;
        }
        if (!(context.authoritativeHit() instanceof BlockHitResult hit)) {
            return FocusActionResult.PASS;
        }
        ServerPlayer player = context.player();
        int tier = MiddleEarthCompat.summonTier(player, faction.factionId(), faction.disposition());
        if (tier == MiddleEarthCompat.TIER_NONE) {
            if (action == VisAction.EXECUTE) {
                player.displayClientMessage(
                        Component.translatable("mett.focus.summon.standing",
                                Component.translatable("mett.faction." + faction.key())), true);
            }
            return FocusActionResult.FAILED;
        }
        List<String> pool = tier == MiddleEarthCompat.TIER_HIGH ? faction.elites() : faction.rankAndFile();
        // Middle-earth must actually have this faction's spawn eggs; checked on a fixed entry so the
        // simulation and the execution agree. The roll itself only happens when the cast is paid.
        if (MiddleEarthCompat.npcSpawnEgg(pool.get(0)).isEmpty()) {
            return FocusActionResult.PASS;
        }
        if (!context.pay(context.configuredCost(), action).affordable()) {
            return FocusActionResult.FAILED;
        }
        if (action == VisAction.EXECUTE) {
            ItemStack egg = MiddleEarthCompat.npcSpawnEgg(pool.get(player.getRandom().nextInt(pool.size())));
            BlockPos target = hit.getBlockPos().relative(hit.getDirection());
            BlockHitResult placeAt = new BlockHitResult(Vec3.atCenterOf(target), hit.getDirection(), target, false);
            InteractionHand hand = context.hand();
            egg.useOn(new UseOnContext(player, hand, placeAt));
            context.setCooldown(COOLDOWN);
        }
        return FocusActionResult.SUCCESS;
    }
}
