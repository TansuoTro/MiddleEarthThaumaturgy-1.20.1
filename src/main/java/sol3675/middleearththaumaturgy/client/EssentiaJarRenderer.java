package sol3675.middleearththaumaturgy.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.tc4port.thaumcraft.api.aspect.AspectApi;
import dev.tc4port.thaumcraft.api.aspect.AspectDefinition;
import dev.tc4port.thaumcraft.api.aspect.AspectId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import sol3675.middleearththaumaturgy.common.blocks.EssentiaJarBlock;
import sol3675.middleearththaumaturgy.common.blocks.entity.EssentiaJarBlockEntity;

/**
 * The donor's {@code TileCompressedEssentialJarsRenderer}, in 1.20 form.
 *
 * <p>1.7 drew three things per jar: a box of the stored aspect between y 1/16 and
 * 1/16 + 0.65 of the jar's interior, a paper label with the filtered aspect on the side the jar
 * faces, and then the glass model over both. The glass is now a block model, so this renderer only
 * has to reproduce the liquid and the label, using the same bounds, colours and brightness floor as
 * the original.
 */
public final class EssentiaJarRenderer implements BlockEntityRenderer<EssentiaJarBlockEntity> {

    /** {@code ConfigBlocks.blockJar.iconLiquid} in 1.7; TC4R keeps it as an animated atlas sprite. */
    private static final ResourceLocation LIQUID = new ResourceLocation("thaumcraft", "block/animatedglow");

    /** {@code UtilsFX.renderQuadCenteredFromTexture("textures/models/label.png", ...)}. */
    private static final ResourceLocation LABEL = new ResourceLocation("thaumcraft", "textures/models/label.png");

    private static final float LABEL_CENTER_Y = 0.41F;
    private static final float LABEL_HALF_SIZE = 0.25F;
    private static final float ASPECT_HALF_SIZE = 0.168F;

    /** The donor's label quad sat at z = 0.315 in a mirrored space, i.e. 0.815 from the block origin. */
    private static final float PLANE = 0.187F;
    private static final float ASPECT_PLANE = 0.186F;

    public EssentiaJarRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EssentiaJarBlockEntity jar, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = jar.getBlockState().hasProperty(EssentiaJarBlock.FACING)
                ? jar.getBlockState().getValue(EssentiaJarBlock.FACING)
                : Direction.NORTH;
        if (jar.amount() > 0 && jar.aspect() != null) {
            renderLiquid(jar.aspect(), jar.amount(), jar.capacity(), poseStack, buffer, packedLight);
        }
        if (jar.filter() != null) {
            renderLabel(facing, jar.filter(), jar.getBlockPos().getX(), poseStack, buffer);
        }
    }

    private static void renderLiquid(AspectId aspect, int amount, int capacity, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight) {
        AspectDefinition definition = AspectApi.get(aspect);
        if (definition == null) {
            return;
        }
        int color = definition.color();
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        float min = 0.25F;
        float max = 0.75F;
        float bottom = 0.0625F;
        float top = bottom + Math.min(1.0F, (float) amount / Math.max(1, capacity)) * 0.625F;
        // The donor compared the already packed world light against a raw 200.
        int light = Math.max(200, packedLight);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(LIQUID);
        VertexConsumer vertices = buffer.getBuffer(RenderType.solid());
        PoseStack.Pose pose = poseStack.last();
        horizontal(vertices, pose, min, max, bottom, min, max, false, sprite, red, green, blue, light);
        horizontal(vertices, pose, min, max, top, min, max, true, sprite, red, green, blue, light);
        verticalZ(vertices, pose, min, max, bottom, top, min, false, sprite, red, green, blue, light);
        verticalZ(vertices, pose, min, max, bottom, top, max, true, sprite, red, green, blue, light);
        verticalX(vertices, pose, min, max, bottom, top, min, true, sprite, red, green, blue, light);
        verticalX(vertices, pose, min, max, bottom, top, max, false, sprite, red, green, blue, light);
    }

    private static void renderLabel(Direction facing, AspectId filter, int legacyX, PoseStack poseStack,
                                    MultiBufferSource buffer) {
        float rotation = (filter.serialized().hashCode() + legacyX + legacyFacing(facing)) % 4 - 2;
        poseStack.pushPose();
        poseStack.translate(0.5F, LABEL_CENTER_Y, 0.5F);
        switch (facing) {
            case NORTH -> poseStack.mulPose(Axis.ZN.rotationDegrees(rotation));
            case SOUTH -> poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            case WEST -> poseStack.mulPose(Axis.XN.rotationDegrees(rotation));
            case EAST -> poseStack.mulPose(Axis.XP.rotationDegrees(rotation));
            default -> {
            }
        }
        poseStack.translate(-0.5F, -LABEL_CENTER_Y, -0.5F);
        emitQuad(buffer.getBuffer(RenderType.entityCutoutNoCull(LABEL)), poseStack.last(), facing,
                0.5F - LABEL_HALF_SIZE, 0.5F + LABEL_HALF_SIZE,
                LABEL_CENTER_Y - LABEL_HALF_SIZE, LABEL_CENTER_Y + LABEL_HALF_SIZE,
                0.0F, 1.0F, 0xFFFFFF, 255, LightTexture.FULL_BRIGHT, PLANE);
        AspectDefinition definition = AspectApi.get(filter);
        if (definition != null) {
            emitQuad(buffer.getBuffer(RenderType.entityCutoutNoCull(definition.texture())), poseStack.last(), facing,
                    0.5F - ASPECT_HALF_SIZE, 0.5F + ASPECT_HALF_SIZE,
                    LABEL_CENTER_Y - ASPECT_HALF_SIZE, LABEL_CENTER_Y + ASPECT_HALF_SIZE,
                    0.0F, 1.0F, 0xFFFFFF, 255, LightTexture.FULL_BRIGHT, ASPECT_PLANE);
        }
        poseStack.popPose();
    }

    private static int legacyFacing(Direction facing) {
        return switch (facing) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
        };
    }

    private static void emitQuad(VertexConsumer vertices, PoseStack.Pose pose, Direction facing,
                                 float minHorizontal, float maxHorizontal, float minY, float maxY,
                                 float minU, float maxU, int color, int alpha, int light, float plane) {
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        switch (facing) {
            case NORTH -> {
                vertex(vertices, pose, minHorizontal, minY, plane, red, green, blue, alpha, minU, 1, light, 0, 0, -1);
                vertex(vertices, pose, minHorizontal, maxY, plane, red, green, blue, alpha, minU, 0, light, 0, 0, -1);
                vertex(vertices, pose, maxHorizontal, maxY, plane, red, green, blue, alpha, maxU, 0, light, 0, 0, -1);
                vertex(vertices, pose, maxHorizontal, minY, plane, red, green, blue, alpha, maxU, 1, light, 0, 0, -1);
            }
            case SOUTH -> {
                float z = 1.0F - plane;
                vertex(vertices, pose, maxHorizontal, minY, z, red, green, blue, alpha, minU, 1, light, 0, 0, 1);
                vertex(vertices, pose, maxHorizontal, maxY, z, red, green, blue, alpha, minU, 0, light, 0, 0, 1);
                vertex(vertices, pose, minHorizontal, maxY, z, red, green, blue, alpha, maxU, 0, light, 0, 0, 1);
                vertex(vertices, pose, minHorizontal, minY, z, red, green, blue, alpha, maxU, 1, light, 0, 0, 1);
            }
            case WEST -> {
                vertex(vertices, pose, plane, minY, maxHorizontal, red, green, blue, alpha, minU, 1, light, -1, 0, 0);
                vertex(vertices, pose, plane, maxY, maxHorizontal, red, green, blue, alpha, minU, 0, light, -1, 0, 0);
                vertex(vertices, pose, plane, maxY, minHorizontal, red, green, blue, alpha, maxU, 0, light, -1, 0, 0);
                vertex(vertices, pose, plane, minY, minHorizontal, red, green, blue, alpha, maxU, 1, light, -1, 0, 0);
            }
            case EAST -> {
                float x = 1.0F - plane;
                vertex(vertices, pose, x, minY, minHorizontal, red, green, blue, alpha, minU, 1, light, 1, 0, 0);
                vertex(vertices, pose, x, maxY, minHorizontal, red, green, blue, alpha, minU, 0, light, 1, 0, 0);
                vertex(vertices, pose, x, maxY, maxHorizontal, red, green, blue, alpha, maxU, 0, light, 1, 0, 0);
                vertex(vertices, pose, x, minY, maxHorizontal, red, green, blue, alpha, maxU, 1, light, 1, 0, 0);
            }
            default -> {
            }
        }
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose, float x, float y, float z,
                               int red, int green, int blue, int alpha, float u, float v, int light,
                               float normalX, float normalY, float normalZ) {
        vertices.vertex(pose.pose(), x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }

    private static void horizontal(VertexConsumer vertices, PoseStack.Pose pose, float minX, float maxX, float y,
                                   float minZ, float maxZ, boolean up, TextureAtlasSprite sprite,
                                   int red, int green, int blue, int light) {
        if (up) {
            liquid(vertices, pose, minX, y, maxZ, red, green, blue, sprite.getU0(), sprite.getV1(), light, 0, 1, 0);
            liquid(vertices, pose, maxX, y, maxZ, red, green, blue, sprite.getU1(), sprite.getV1(), light, 0, 1, 0);
            liquid(vertices, pose, maxX, y, minZ, red, green, blue, sprite.getU1(), sprite.getV0(), light, 0, 1, 0);
            liquid(vertices, pose, minX, y, minZ, red, green, blue, sprite.getU0(), sprite.getV0(), light, 0, 1, 0);
        } else {
            liquid(vertices, pose, minX, y, minZ, red, green, blue, sprite.getU0(), sprite.getV0(), light, 0, -1, 0);
            liquid(vertices, pose, maxX, y, minZ, red, green, blue, sprite.getU1(), sprite.getV0(), light, 0, -1, 0);
            liquid(vertices, pose, maxX, y, maxZ, red, green, blue, sprite.getU1(), sprite.getV1(), light, 0, -1, 0);
            liquid(vertices, pose, minX, y, maxZ, red, green, blue, sprite.getU0(), sprite.getV1(), light, 0, -1, 0);
        }
    }

    private static void verticalZ(VertexConsumer vertices, PoseStack.Pose pose, float minX, float maxX,
                                  float minY, float maxY, float z, boolean south, TextureAtlasSprite sprite,
                                  int red, int green, int blue, int light) {
        float nz = south ? 1 : -1;
        liquid(vertices, pose, south ? maxX : minX, minY, z, red, green, blue, sprite.getU0(), sprite.getV1(), light, 0, 0, nz);
        liquid(vertices, pose, south ? maxX : minX, maxY, z, red, green, blue, sprite.getU0(), sprite.getV0(), light, 0, 0, nz);
        liquid(vertices, pose, south ? minX : maxX, maxY, z, red, green, blue, sprite.getU1(), sprite.getV0(), light, 0, 0, nz);
        liquid(vertices, pose, south ? minX : maxX, minY, z, red, green, blue, sprite.getU1(), sprite.getV1(), light, 0, 0, nz);
    }

    private static void verticalX(VertexConsumer vertices, PoseStack.Pose pose, float minZ, float maxZ,
                                  float minY, float maxY, float x, boolean west, TextureAtlasSprite sprite,
                                  int red, int green, int blue, int light) {
        float nx = west ? -1 : 1;
        liquid(vertices, pose, x, minY, west ? maxZ : minZ, red, green, blue, sprite.getU0(), sprite.getV1(), light, nx, 0, 0);
        liquid(vertices, pose, x, maxY, west ? maxZ : minZ, red, green, blue, sprite.getU0(), sprite.getV0(), light, nx, 0, 0);
        liquid(vertices, pose, x, maxY, west ? minZ : maxZ, red, green, blue, sprite.getU1(), sprite.getV0(), light, nx, 0, 0);
        liquid(vertices, pose, x, minY, west ? minZ : maxZ, red, green, blue, sprite.getU1(), sprite.getV1(), light, nx, 0, 0);
    }

    private static void liquid(VertexConsumer vertices, PoseStack.Pose pose, float x, float y, float z,
                               int red, int green, int blue, float u, float v, int light,
                               float normalX, float normalY, float normalZ) {
        vertices.vertex(pose.pose(), x, y, z)
                .color(red, green, blue, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }
}
