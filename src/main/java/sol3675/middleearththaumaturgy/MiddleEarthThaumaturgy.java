package sol3675.middleearththaumaturgy;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sol3675.middleearththaumaturgy.compat.me.MiddleEarthCompat;
import sol3675.middleearththaumaturgy.references.LibMisc;

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
        LOGGER.info("Middle-Earth Thaumaturgy {} loading; Middle-earth present: {}",
                LibMisc.VERSION, MiddleEarthCompat.isLoaded());
    }
}
