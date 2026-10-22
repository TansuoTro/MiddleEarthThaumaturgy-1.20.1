package sol3675.middleearththaumaturgy.common.blocks;

import dev.tc4port.thaumcraft.api.aspect.AspectId;
import dev.tc4port.thaumcraft.api.data.ItemStateKey;
import dev.tc4port.thaumcraft.api.item.ItemStatePlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import sol3675.middleearththaumaturgy.common.blocks.entity.EssentiaJarBlockEntity;
import sol3675.middleearththaumaturgy.references.LibMisc;

import java.util.function.IntSupplier;

/**
 * A jar that stores more essentia than Thaumcraft's 64-unit warded jar.
 *
 * <p>The donor's three jars were {@code TileJarFillable} subclasses that only overrode the maximum
 * amount and the suction curve. TC4R's jar entity is final, so this block carries the parameters
 * instead: the storage id is what the datapack capacity lookup is keyed by, and the suction values
 * reproduce each donor jar.
 *
 * <p>Like 1.7's {@code BlockCompressedEssentiaJars} the jar is only 10/16 wide and 12/16 tall, and
 * it remembers which way it faces so the label can be drawn on that side.
 */
public class EssentiaJarBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** {@code setBlockBounds(0.1875F, 0.0F, 0.1875F, 0.8125F, 0.75F, 0.8125F)} in the donor. */
    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 12.0D, 13.0D);

    private final String storagePath;
    private final IntSupplier defaultCapacity;
    private final int baseSuction;
    private final int filteredSuction;
    private final boolean scaledSuction;

    public EssentiaJarBlock(String storagePath, IntSupplier defaultCapacity, int baseSuction, int filteredSuction,
                            boolean scaledSuction, BlockBehaviour.Properties properties) {
        super(properties);
        this.storagePath = storagePath;
        this.defaultCapacity = defaultCapacity;
        this.baseSuction = baseSuction;
        this.filteredSuction = filteredSuction;
        this.scaledSuction = scaledSuction;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public static BlockBehaviour.Properties jarProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isViewBlocking((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false);
    }

    public ResourceLocation storageId() {
        return new ResourceLocation(LibMisc.MODID, this.storagePath);
    }

    public int defaultCapacity() {
        return Math.max(1, this.defaultCapacity.getAsInt());
    }

    /** Suction this jar advertises, following the donor's per-jar curve. */
    public int suction(boolean filtered, int amount, int capacity) {
        int base = filtered ? this.filteredSuction : this.baseSuction;
        if (!this.scaledSuction || capacity <= 0) {
            return base;
        }
        return base + (int) ((long) amount * 8L / capacity);
    }

    // ------------------------------------------------------------------- shape

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    // ------------------------------------------------------------------- label

    /**
     * The donor's label handling: a paper label sets the jar's filter and turns the jar to face the
     * player, and sneaking with an empty hand pops it back off the labelled side.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EssentiaJarBlockEntity jar)) {
            return InteractionResult.PASS;
        }
        Direction facing = state.getValue(FACING);
        if (player.isShiftKeyDown() && jar.filter() != null && hit.getDirection() == facing) {
            if (!level.isClientSide) {
                AspectId removed = jar.clearFilter();
                if (removed != null) {
                    Item label = labelItem();
                    if (label != null) {
                        ((ServerLevel) level).addFreshEntity(new ItemEntity(level,
                                pos.getX() + 0.5D + facing.getStepX() / 3.0D, pos.getY() + 0.5D,
                                pos.getZ() + 0.5D + facing.getStepZ() / 3.0D, new ItemStack(label)));
                    }
                    level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        ItemStack held = player.getItemInHand(hand);
        Item label = labelItem();
        if (label == null || !held.is(label) || jar.filter() != null) {
            return InteractionResult.PASS;
        }
        AspectId selected = jar.aspect();
        if (selected == null) {
            selected = labelAspect(held);
        }
        if (selected == null) {
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide && jar.setFilter(selected)) {
            held.shrink(1);
            level.setBlock(pos, state.setValue(FACING, player.getDirection().getOpposite()), Block.UPDATE_CLIENTS);
            level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.4F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * The label's own aspect. TC4R keeps it in a registered item-state key rather than in item
     * aspects, and the key lives outside the API jar, so it is looked up by id.
     */
    @Nullable
    private static AspectId labelAspect(ItemStack label) {
        ItemStateKey<?> key = ItemStatePlatform.find(new ResourceLocation("thaumcraft", "jar_filter"));
        if (key == null) {
            return null;
        }
        Object value = ItemStatePlatform.get(label, key);
        return value instanceof AspectId aspect ? aspect : null;
    }

    @Nullable
    private static Item labelItem() {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation("thaumcraft", "jar_label"));
    }

    // ------------------------------------------------------------- block entity

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaJarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return (tickLevel, pos, tickState, entity) -> {
            if (entity instanceof EssentiaJarBlockEntity jar) {
                EssentiaJarBlockEntity.serverTick(tickLevel, pos, tickState, jar);
            }
        };
    }
}
