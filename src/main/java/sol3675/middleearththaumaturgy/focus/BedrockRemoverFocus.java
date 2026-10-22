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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import sol3675.middleearththaumaturgy.references.LibMisc;

import java.util.EnumSet;

/**
 * The bedrock remover focus: unbinds one block of bedrock for a very large amount of Vis.
 *
 * <p>The donor's focus cost 1000 air, 1000 earth, 500 order and 2000 entropy centivis - ten, ten,
 * five and twenty Vis - and refused the block at {@code y == 0} so the world floor could not be
 * opened. The modern world floor sits at {@link Level#getMinBuildHeight()}, which is what the guard
 * checks here.
 *
 * <p>Everything that changes the world waits for {@link VisAction#EXECUTE}: the same call is made
 * once with {@link VisAction#SIMULATE} first, and the two must agree for the cast to be paid.
 */
public final class BedrockRemoverFocus {
    public static final ResourceLocation ACTION = new ResourceLocation(LibMisc.MODID, "bedrock_remover");
    public static final FocusId ID = FocusId.of(new ResourceLocation(LibMisc.MODID, "focus_bedrock_remover"));
    /** How far the focus reaches, in blocks. */
    private static final int RANGE = 10;

    private BedrockRemoverFocus() {
    }

    /** Registers the action with Thaumcraft's wand. Called once, on both sides. */
    public static void register() {
        FocusActionApi.register(new FocusActionSpec(ACTION, new FocusActionTarget.Focus(ID),
                FocusInput.PRIMARY, EnumSet.of(FocusTargetKind.BLOCK), FocusCastMode.INSTANT,
                ResearchRequirement.none(), RANGE, 0, ActionPresentationAudience.PREDICTED),
                BedrockRemoverFocus::handle);
    }

    private static FocusActionResult handle(FocusActionContext context, VisAction action) {
        // An instant cast also receives a hold gesture every interval tick; only the press may fire.
        if (context.interaction().gesture() != FocusGesture.PRESS) {
            return FocusActionResult.PASS;
        }
        if (!(context.authoritativeHit() instanceof BlockHitResult hit)) {
            return FocusActionResult.PASS;
        }
        ServerLevel level = context.player().serverLevel();
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.BEDROCK) || pos.getY() <= level.getMinBuildHeight()) {
            return FocusActionResult.PASS;
        }
        if (!context.pay(context.configuredCost(), action).affordable()) {
            return FocusActionResult.FAILED;
        }
        if (action == VisAction.EXECUTE) {
            level.removeBlock(pos, false);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            context.setCooldown(10);
        }
        return FocusActionResult.SUCCESS;
    }
}
