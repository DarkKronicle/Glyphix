package io.github.darkkronicle.glyphix.text;

import com.google.common.collect.Lists;
import io.github.darkkronicle.glyphix.font.ContextualGlyph;
import io.github.darkkronicle.glyphix.font.EmojiFont;
import io.github.darkkronicle.glyphix.vanilla.EmojiLayerHolder;
import io.github.darkkronicle.glyphix.vanilla.LigatureFontStorage;
import net.minecraft.client.font.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.List;

public class GlyphixDrawer extends ContextualCharacterVisitor {

    private final GlyphixRenderer renderer;

    private final VertexConsumerProvider vertexConsumers;
    private final boolean shadow;
    private final float brightnessMultiplier;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private final Matrix4f matrix;
    private final TextRenderer.TextLayerType layerType;
    private final int light;
    private float x;
    private float y;
    @Nullable
    private List<GlyphRenderer.Rectangle> rectangles;

    private void addRectangle(GlyphRenderer.Rectangle rectangle) {
        if (this.rectangles == null) {
            this.rectangles = Lists.newArrayList();
        }

        this.rectangles.add(rectangle);
    }

    public GlyphixDrawer(
            GlyphixRenderer renderer,
            VertexConsumerProvider vertexConsumers, float x, float y, int color, boolean shadow, Matrix4f matrix, boolean seeThrough,
            int light
    ) {
        this(
                renderer, vertexConsumers, x, y, color, shadow, matrix,
                seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, light
        );
    }

    public GlyphixDrawer(
            GlyphixRenderer renderer,
            VertexConsumerProvider vertexConsumers,
            float x,
            float y,
            int color,
            boolean shadow,
            Matrix4f matrix,
            TextRenderer.TextLayerType layerType,
            int light
    ) {
        this.renderer = renderer;
        this.vertexConsumers = vertexConsumers;
        this.x = x;
        this.y = y;
        this.shadow = shadow;
        this.brightnessMultiplier = shadow ? 0.25F : 1.0F;
        this.red = (float) (color >> 16 & 0xFF) / 255.0F * this.brightnessMultiplier;
        this.green = (float) (color >> 8 & 0xFF) / 255.0F * this.brightnessMultiplier;
        this.blue = (float) (color & 0xFF) / 255.0F * this.brightnessMultiplier;
        this.alpha = (float) (color >> 24 & 0xFF) / 255.0F;
        this.matrix = matrix;
        this.layerType = layerType;
        this.light = light;
    }

    @Override
    public boolean accept(Visited visited) {
        Style style = visited.style();
        int codepoint = visited.codepoint();
        FontStorage fontStorage = renderer.getFontStorage(visited.style().getFont());
        LigatureFontStorage ligStorage = (LigatureFontStorage) fontStorage;
        GlyphInfo<?> glyph = ligStorage.getGlyphInfo(this, renderer.validateAdvance);
        GlyphRenderer glyphRenderer = style.isObfuscated() && codepoint != 32 ? fontStorage.getObfuscatedGlyphRenderer(glyph.glyph()) : ligStorage.getGlyphRenderer(this);
        boolean bold = style.isBold();
        float f = this.alpha;
        TextColor textColor = style.getColor();
        float g;
        float h;
        float l;
        if (textColor != null) {
            int k = textColor.getRgb();
            g = (float) (k >> 16 & 0xFF) / 255.0F * this.brightnessMultiplier;
            h = (float) (k >> 8 & 0xFF) / 255.0F * this.brightnessMultiplier;
            l = (float) (k & 0xFF) / 255.0F * this.brightnessMultiplier;
        } else {
            g = this.red;
            h = this.green;
            l = this.blue;
        }

        if (!(glyphRenderer instanceof EmptyGlyphRenderer)) {
            float m = bold ? glyph.glyph().getBoldOffset() : 0.0F;
            float n = this.shadow ? glyph.glyph().getShadowOffset() : 0.0F;
            boolean tint = glyph.tint();
            if (this.shadow && !tint) {
                // Render the shadow color correctly
                tint = true;
            }
            VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(
                    getLayer(
                            glyphRenderer,
                            this.layerType,
                            tint
                    )
            );

            renderer.drawGlyph(
                    glyphRenderer, bold, style.isItalic(), m, this.x + n, this.y + n, this.matrix, vertexConsumer, g, h, l, f,
                    this.light
            );
        }

        float advance = getAdvance(ligStorage, glyph, bold);

        float n = this.shadow ? 1.0F : 0.0F;
        if (style.isStrikethrough()) {
            this.addRectangle(
                    new GlyphRenderer.Rectangle(this.x + n - 1.0F, this.y + n + 4.5F, this.x + n + advance, this.y + n + 4.5F - 1.0F, 0.01F, g, h,
                            l, f
                    ));
        }

        if (style.isUnderlined()) {
            this.addRectangle(
                    new GlyphRenderer.Rectangle(this.x + n - 1.0F, this.y + n + 9.0F, this.x + n + advance, this.y + n + 9.0F - 1.0F, 0.01F, g, h,
                            l, f
                    ));
        }
        skip(Math.max(glyph.codepointsLength() - 1, 0));
        this.x += advance;
        return true;
    }

    private float getAdvance(LigatureFontStorage ligStorage, GlyphInfo<?> glyph, boolean bold) {
        if (getCurrentIndex() + 1 < size() && glyph.glyph() instanceof ContextualGlyph context) {
            skip(1);
            GlyphInfo<?> next = ligStorage.getGlyphInfo(this, renderer.validateAdvance);
            skip(-1);
            if (next.glyph() instanceof ContextualGlyph nextContext) {
                return context.getAdvance(nextContext.getGlyphIndex());
            }
        }
        return glyph.getAdvance(bold);
    }

    private RenderLayer getLayer(GlyphRenderer renderer, TextRenderer.TextLayerType layerType, boolean tint) {
        if (tint) {
            return renderer.getLayer(layerType);
        }
        EmojiLayerHolder emojiAtlas = (EmojiLayerHolder) renderer;
        return switch (layerType) {
            case NORMAL -> emojiAtlas.glyphix$emojiLayer();
            case SEE_THROUGH -> emojiAtlas.glyphix$emojiSeeThroughLayer();
            case POLYGON_OFFSET -> emojiAtlas.glyphix$emojiPolygonOffsetLayer();
        };
    }

    public float drawLayer(int underlineColor, float x) {
        if (underlineColor != 0) {
            float f = (float) (underlineColor >> 24 & 0xFF) / 255.0F;
            float g = (float) (underlineColor >> 16 & 0xFF) / 255.0F;
            float h = (float) (underlineColor >> 8 & 0xFF) / 255.0F;
            float i = (float) (underlineColor & 0xFF) / 255.0F;
            this.addRectangle(new GlyphRenderer.Rectangle(x - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, g, h, i, f));
        }

        if (this.rectangles != null) {
            GlyphRenderer glyphRenderer = renderer.getFontStorage(net.minecraft.text.Style.DEFAULT_FONT_ID).getRectangleRenderer();
            VertexConsumer vertexConsumer = this.vertexConsumers.getBuffer(glyphRenderer.getLayer(this.layerType));

            for (GlyphRenderer.Rectangle rectangle : this.rectangles) {
                glyphRenderer.drawRectangle(rectangle, this.matrix, vertexConsumer, this.light);
            }
        }

        return this.x;
    }

}
