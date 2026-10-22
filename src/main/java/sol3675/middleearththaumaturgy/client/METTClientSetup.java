package sol3675.middleearththaumaturgy.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import sol3675.middleearththaumaturgy.common.blocks.METTBlocks;
import sol3675.middleearththaumaturgy.references.LibMisc;

/** Client-only wiring for the compressed jars. */
@Mod.EventBusSubscriber(modid = LibMisc.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class METTClientSetup {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(METTBlocks.ESSENTIA_JAR_ENTITY.get(), EssentiaJarRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        // The jar item is the jar model, so the item has to draw through the translucent layer as
        // well; otherwise the glass is written opaque in the GUI and in hand.
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(METTBlocks.COMPRESSED_ESSENTIA_JAR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(METTBlocks.X2_COMPRESSED_ESSENTIA_JAR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(METTBlocks.EXTRALARGE_ESSENTIA_JAR.get(), RenderType.translucent());
        });
    }

    private METTClientSetup() {
    }
}
