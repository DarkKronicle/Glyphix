package io.github.darkkronicle.glyphix.text;

import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.stb.STBTTAlignedQuad;

public class OversampleGlyphRenderer extends GlyphRenderer {

    private final OversampleRenderableGlyph glyph;

    public OversampleGlyphRenderer(
            RenderLayer textLayer, RenderLayer seeThroughTextLayer,
            RenderLayer polygonOffsetTextLayer, float minU, float maxU, float minV, float maxV, float minX,
            float maxX, float minY, float maxY, OversampleRenderableGlyph glyph
    ) {
        super(textLayer, seeThroughTextLayer, polygonOffsetTextLayer, minU, maxU, minV, maxV, minX, maxX, minY, maxY);
        this.glyph = glyph;
    }

    @Override
    public void draw(boolean italic, float x, float y, Matrix4f matrix, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light) {
        float x1 = x + this.minX;
        float x2 = x + this.maxX;
        float bottomY = this.minY - 3.0F;
        float topY = this.maxY - 3.0F;
        float y1 = y + bottomY;
        float y2 = y + topY;
        float bottomShift = italic ? 1.0F - 0.25F * bottomY : 0.0F;
        float topShift = italic ? 1.0F - 0.25F * topY : 0.0F;
        vertexConsumer.vertex(matrix, x1 + bottomShift, y1, 0.0F).color(red, green, blue, alpha).texture(this.minU, this.minV).light(light).next();
        vertexConsumer.vertex(matrix, x1 + topShift, y2, 0.0F).color(red, green, blue, alpha).texture(this.minU, this.maxV).light(light).next();
        vertexConsumer.vertex(matrix, x2 + topShift, y2, 0.0F).color(red, green, blue, alpha).texture(this.maxU, this.maxV).light(light).next();
        vertexConsumer.vertex(matrix, x2 + bottomShift, y1, 0.0F).color(red, green, blue, alpha).texture(this.maxU, this.minV).light(light).next();
    }


}
