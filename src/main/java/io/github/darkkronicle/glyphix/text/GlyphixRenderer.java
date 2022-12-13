package io.github.darkkronicle.glyphix.text;

import net.minecraft.client.font.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import java.util.function.Function;

public class GlyphixRenderer extends TextRenderer {

    private static final Vec3f FORWARD_SHIFT = new Vec3f(0.0F, 0.0F, 0.03F);

    public GlyphixRenderer(
            Function<Identifier, FontStorage> fontStorageAccessor,
            boolean validateAdvance
    ) {
        super(fontStorageAccessor, validateAdvance);
        this.handler = new GlyphixTextHandler(this);
    }

    private static int tweakTransparency(int argb) {
        // 11111100000000000000000000000000
        // Essentially just checks to see if there is 0 transparency data (old code)
        return (argb & 0xFC000000) == 0 ? argb | 0xFF000000 : argb;
    }

    @Override
    protected int drawInternal(
            String text,
            float x,
            float y,
            int color,
            boolean shadow,
            Matrix4f matrix,
            VertexConsumerProvider vertexConsumers,
            boolean seeThrough,
            int backgroundColor,
            int light,
            boolean mirror
    ) {
        if (mirror) {
            text = this.mirror(text);
        }

        color = tweakTransparency(color);
        Matrix4f matrix4f = new Matrix4f(matrix);
        if (shadow) {
            this.drawLayer(text, x, y, color, true, matrix, vertexConsumers, seeThrough, backgroundColor, light);
            matrix4f.addToLastColumn(FORWARD_SHIFT);
        }

        x = this.drawLayer(text, x, y, color, false, matrix4f, vertexConsumers, seeThrough, backgroundColor, light);
        return (int)x + (shadow ? 1 : 0);
    }

    @Override
    protected float drawLayer(
            OrderedText text,
            float x,
            float y,
            int color,
            boolean shadow,
            Matrix4f matrix,
            VertexConsumerProvider vertexConsumerProvider,
            boolean seeThrough,
            int underlineColor,
            int light
    ) {
        GlyphixDrawer drawer = new GlyphixDrawer(this, vertexConsumerProvider, x, y, color, shadow, matrix, seeThrough, light);
        text.accept(drawer);
        drawer.done();
        return drawer.drawLayer(underlineColor, x);
    }


    protected float drawLayer(
            String text,
            float x,
            float y,
            int color,
            boolean shadow,
            Matrix4f matrix,
            VertexConsumerProvider vertexConsumerProvider,
            boolean seeThrough,
            int underlineColor,
            int light
    ) {
        GlyphixDrawer drawer = new GlyphixDrawer(this, vertexConsumerProvider, x, y, color, shadow, matrix, seeThrough, light);
        TextVisitFactory.visitFormatted(text, Style.EMPTY, drawer);
        drawer.done();
        return drawer.drawLayer(underlineColor, x);
    }

    @Override
    protected void drawGlyph(
            GlyphRenderer glyphRenderer, boolean bold, boolean italic, float weight, float x, float y, Matrix4f matrix,
            VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light
    ) {
        super.drawGlyph(glyphRenderer, bold, italic, weight, x, y, matrix, vertexConsumer, red, green, blue, alpha, light);
    }

}
