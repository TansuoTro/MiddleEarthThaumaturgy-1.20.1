package sol3675.middleearththaumaturgy.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import sol3675.middleearththaumaturgy.common.blocks.METTBlocks;
import sol3675.middleearththaumaturgy.references.LibMisc;

/** Client-only wiring: the compressed jars draw their contents themselves. */
@Mod.EventBusSubscriber(modid = LibMisc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class METTClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(METTBlocks.ESSENTIA_JAR_ENTITY.get(), EssentiaJarRenderer::new);
    }

    private METTClientSetup() {
    }
}
