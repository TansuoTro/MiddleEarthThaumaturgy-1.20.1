package sol3675.middleearththaumaturgy.focus;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;
import sol3675.middleearththaumaturgy.common.items.METTItems;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The focus items.
 *
 * <p>The donor had two focus items: one bedrock remover and one summon focus whose 24 variants were
 * metadata values. Here the summon focus becomes one item per faction, so each can carry its own
 * catalog entry and its own translation key.
 */
public final class METTFoci {
    public static final RegistryObject<Item> BEDROCK_REMOVER = METTItems.ITEMS.register(
            "focus_bedrock_remover", () -> new ItemMETTFocus(BedrockRemoverFocus.ID));

    public static final Map<String, RegistryObject<Item>> SUMMON;

    static {
        Map<String, RegistryObject<Item>> summons = new LinkedHashMap<>();
        for (SummonTable.Faction faction : SummonTable.ALL) {
            summons.put(faction.key(), METTItems.ITEMS.register(
                    "focus_summon_" + faction.key(),
                    () -> new ItemMETTFocus(SummonFocus.id(faction.key()))));
        }
        SUMMON = Collections.unmodifiableMap(summons);
    }

    private METTFoci() {
    }
}
