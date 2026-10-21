package sol3675.middleearththaumaturgy.wand;

import java.util.List;

/**
 * Wand cap materials. Values are the donor's {@code MiddleEarthWandCap} arguments: the vis cost
 * modifier and the assembly cost.
 */
public record WandCapMaterial(String key, float costModifier, int craftCost) {

    public static final List<WandCapMaterial> ALL = List.of(
            new WandCapMaterial("mithril", 0.65F, 6),
            new WandCapMaterial("orcsteel", 0.95F, 4),
            new WandCapMaterial("dwarvensteel", 0.95F, 4),
            new WandCapMaterial("galvorn", 0.8F, 5),
            new WandCapMaterial("uruksteel", 0.95F, 4),
            new WandCapMaterial("morgulsteel", 0.8F, 5),
            new WandCapMaterial("bluedwarvensteel", 0.95F, 4),
            new WandCapMaterial("blackuruksteel", 0.9F, 4),
            new WandCapMaterial("elvensteel", 0.9F, 4),
            new WandCapMaterial("gildediron", 0.95F, 4)
    );
}
