package sol3675.middleearththaumaturgy.wand;

import dev.tc4port.thaumcraft.api.Registration;
import dev.tc4port.thaumcraft.api.research.ResearchRequirement;
import dev.tc4port.thaumcraft.api.wand.WandMaterialId;
import dev.tc4port.thaumcraft.api.wand.WandPartApi;
import dev.tc4port.thaumcraft.api.wand.WandRechargeSpec;
import dev.tc4port.thaumcraft.api.wand.WandRodSpec;
import dev.tc4port.thaumcraft.api.wand.behavior.WandBehaviorApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;
import sol3675.middleearththaumaturgy.MiddleEarthThaumaturgy;
import sol3675.middleearththaumaturgy.common.items.METTItems;
import sol3675.middleearththaumaturgy.references.LibMisc;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers the faction wand rods with Thaumcraft 4R.
 *
 * <p>TC4R resolves wand parts through a runtime index rather than the 1.7.10 static maps, so the
 * rods are installed once during common setup; the returned {@link Registration} handles are kept
 * for the lifetime of the process.
 */
public final class WandRegistration {
    private static final String THAUMIC_TINKERER = "thaumictinkerer";
    private static final List<Registration> HANDLES = new ArrayList<>();

    private WandRegistration() {
    }

    public static void register() {
        boolean tinkerer = ModList.get() != null && ModList.get().isLoaded(THAUMIC_TINKERER);
        int order = 0;
        for (FactionWand wand : FactionWands.KEPT) {
            order = registerRod(wand, false, order);
            if (tinkerer) {
                order = registerRod(wand, true, order);
            }
        }
        MiddleEarthThaumaturgy.LOGGER.info("Registered {} faction wand rods (Thaumic Tinkerer rods: {})",
                order, tinkerer);
    }

    private static int registerRod(FactionWand wand, boolean tinkerer, int order) {
        RegistryObject<Item> core = METTItems.WAND_CORES.get(wand.key());
        if (core == null || core.get() == null) {
            return order;
        }
        String suffix = tinkerer ? "_tt" : "";
        WandMaterialId material = WandMaterialId.of(
                new ResourceLocation(LibMisc.MODID, "faction_" + wand.key() + suffix));
        WandRodSpec spec = new WandRodSpec(
                core.getId(),
                material,
                tinkerer ? FactionWand.TINKERER_CAPACITY : FactionWand.CAPACITY,
                FactionWand.CRAFT_COST,
                WandRechargeSpec.none(),
                ResearchRequirement.none(),
                new ResourceLocation(LibMisc.MODID, "textures/models/wand_rod_" + (tinkerer ? "tt_" : "") + wand.key() + ".png"),
                false,
                tinkerer,
                false,
                order);
        HANDLES.add(WandPartApi.registerRod(spec));
        HANDLES.add(WandBehaviorApi.register(material, new FactionWandBehavior(wand.factionId(), wand.disposition(), tinkerer)));
        return order + 1;
    }
}
