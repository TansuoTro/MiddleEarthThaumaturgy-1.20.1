package sol3675.middleearththaumaturgy.wand;

import dev.tc4port.thaumcraft.api.Registration;
import dev.tc4port.thaumcraft.api.research.ResearchRequirement;
import dev.tc4port.thaumcraft.api.wand.WandCapSpec;
import dev.tc4port.thaumcraft.api.wand.WandMaterialId;
import dev.tc4port.thaumcraft.api.wand.WandPartApi;
import dev.tc4port.thaumcraft.api.wand.WandRechargeSpec;
import dev.tc4port.thaumcraft.api.wand.WandRodSpec;
import dev.tc4port.thaumcraft.api.wand.behavior.WandBehaviorApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;
import sol3675.middleearththaumaturgy.MiddleEarthThaumaturgy;
import sol3675.middleearththaumaturgy.common.items.METTItems;
import sol3675.middleearththaumaturgy.references.LibMisc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Registers the faction wand parts with Thaumcraft 4R.
 *
 * <p>TC4R resolves wand parts through a runtime index rather than the 1.7.10 static maps, so the
 * parts are installed once during common setup; the returned {@link Registration} handles are kept
 * for the lifetime of the process.
 *
 * <p>Three rod families, exactly as the donor: the ordinary rod (100 vis), the staff rod (250 vis,
 * runes) and the Thaumic Tinkerer rod (1000 vis, glowing, four times the recharge).
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
            order = registerRod(wand, RodFamily.WAND, order);
            order = registerRod(wand, RodFamily.STAFF, order);
            if (tinkerer) {
                order = registerRod(wand, RodFamily.TINKERER, order);
            }
        }
        for (WandCapMaterial cap : WandCapMaterial.ALL) {
            order = registerCap(cap, order);
        }
        MiddleEarthThaumaturgy.LOGGER.info("Registered faction wand parts; Thaumic Tinkerer present: {}", tinkerer);
    }

    private enum RodFamily {
        WAND("", 0, FactionWand.CAPACITY, FactionWand.CRAFT_COST, false, false, false),
        STAFF("staff_", 1, FactionWand.STAFF_CAPACITY, FactionWand.STAFF_CRAFT_COST, true, false, true),
        TINKERER("tinkerer_", 2, FactionWand.TINKERER_CAPACITY, FactionWand.CRAFT_COST, false, true, false);

        private final String prefix;
        private final int textureSet;
        private final int capacity;
        private final int craftCost;
        private final boolean staff;
        private final boolean glowing;
        private final boolean runes;

        RodFamily(String prefix, int textureSet, int capacity, int craftCost, boolean staff, boolean glowing, boolean runes) {
            this.prefix = prefix;
            this.textureSet = textureSet;
            this.capacity = capacity;
            this.craftCost = craftCost;
            this.staff = staff;
            this.glowing = glowing;
            this.runes = runes;
        }

        String materialSuffix() {
            return switch (this) {
                case WAND -> "";
                case STAFF -> "_staff";
                case TINKERER -> "_tt";
            };
        }
    }

    private static int registerRod(FactionWand wand, RodFamily family, int order) {
        RegistryObject<Item> core = switch (family) {
            case WAND -> METTItems.WAND_CORES.get(wand.key());
            case STAFF -> METTItems.STAFF_CORES.get(wand.key());
            case TINKERER -> METTItems.TINKERER_CORES.get(wand.key());
        };
        if (core == null) {
            return order;
        }
        WandMaterialId material = WandMaterialId.of(
                new ResourceLocation(LibMisc.MODID, "faction_" + wand.key() + family.materialSuffix()));
        String texture = family == RodFamily.TINKERER
                ? "textures/models/wand_rod_tt_" + wand.key() + ".png"
                : "textures/models/wand_rod_" + wand.key() + ".png";
        WandRodSpec spec = new WandRodSpec(
                core.getId(),
                material,
                family.capacity,
                family.craftCost,
                WandRechargeSpec.none(),
                ResearchRequirement.none(),
                new ResourceLocation(LibMisc.MODID, texture),
                family.staff,
                family.glowing,
                family.runes,
                order);
        HANDLES.add(WandPartApi.registerRod(spec));
        HANDLES.add(WandBehaviorApi.register(material,
                new FactionWandBehavior(wand.factionId(), wand.disposition(), family == RodFamily.TINKERER)));
        return order + 1;
    }

    private static int registerCap(WandCapMaterial cap, int order) {
        RegistryObject<Item> item = METTItems.WAND_CAPS.get(cap.key());
        if (item == null) {
            return order;
        }
        WandCapSpec spec = new WandCapSpec(
                item.getId(),
                WandMaterialId.of(new ResourceLocation(LibMisc.MODID, "cap_" + cap.key())),
                cap.costModifier(),
                Set.of(),
                cap.costModifier(),
                new ResourceLocation(LibMisc.MODID, "textures/models/wand_cap_" + cap.key() + ".png"),
                cap.craftCost(),
                ResearchRequirement.none(),
                order);
        HANDLES.add(WandPartApi.registerCap(spec));
        return order + 1;
    }
}
