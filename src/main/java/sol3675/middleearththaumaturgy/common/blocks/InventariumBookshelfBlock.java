package sol3675.middleearththaumaturgy.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The inventarium bookshelf: a bookshelf of stored knowledge that enchants far harder than oak.
 *
 * <p>The donor block implemented Automagy's {@code IInventariumSlotUpgrade} and allowed a million
 * inventarium slots. Automagy has no 1.20.1 build, so that half is dropped; what remains is the
 * donor's other half - a bookshelf worth fifteen enchantment levels instead of one.
 */
public class InventariumBookshelfBlock extends Block {

    public InventariumBookshelfBlock(Properties properties) {
        super(properties);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return 15.0F;
    }
}
