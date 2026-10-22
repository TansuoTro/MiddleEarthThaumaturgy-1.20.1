package sol3675.middleearththaumaturgy;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sol3675.middleearththaumaturgy.common.METTCreativeTab;
import sol3675.middleearththaumaturgy.common.blocks.METTBlocks;
import sol3675.middleearththaumaturgy.common.items.METTItems;
import sol3675.middleearththaumaturgy.compat.me.MiddleEarthCompat;
import sol3675.middleearththaumaturgy.config.METTConfig;
import sol3675.middleearththaumaturgy.focus.BedrockRemoverFocus;
import sol3675.middleearththaumaturgy.focus.METTFoci;
import sol3675.middleearththaumaturgy.focus.SummonFocus;
import sol3675.middleearththaumaturgy.references.LibMisc;
import sol3675.middleearththaumaturgy.wand.WandRegistration;

/**
 * Entry point. Everything that touches Middle-earth goes through
 * {@link MiddleEarthCompat}, which resolves content by registry id so the mod keeps working
 * whether Middle-earth is present (through Sinytra Connector) or not.
 */
@Mod(LibMisc.MODID)
public class MiddleEarthThaumaturgy {
    public static final Logger LOGGER = LoggerFactory.getLogger(LibMisc.MODNAME);

    public MiddleEarthThaumaturgy() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        METTItems.ITEMS.register(bus);
        METTBlocks.BLOCKS.register(bus);
        METTBlocks.BLOCK_ITEMS.register(bus);
        METTBlocks.BLOCK_ENTITIES.register(bus);
        METTCreativeTab.TABS.register(bus);
        // Touching the focus registry is what makes its static initialiser run; the items themselves
        // go into the same deferred register as everything else.
        METTFoci.SUMMON.size();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, METTConfig.SPEC, "mett-common.toml");
        bus.addListener(this::commonSetup);
        LOGGER.info("Middle-Earth Thaumaturgy {} loading; Middle-earth present: {}",
                LibMisc.VERSION, MiddleEarthCompat.isLoaded());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // TC4R installs wand parts through a runtime index, so the parts go in after registration
        // has finished and before any server starts.
        event.enqueueWork(WandRegistration::register);
        // The focus actions are indexed at runtime too, and the wand predicts on the client, so they
        // are registered on both sides.
        event.enqueueWork(BedrockRemoverFocus::register);
        event.enqueueWork(SummonFocus::register);
    }
}
