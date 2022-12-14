package io.github.darkkronicle.glyphix.font;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;
import java.util.stream.IntStream;

@Environment(value = EnvType.CLIENT)
public class TTFFont implements Font {
    private final ByteBuffer buffer;
    private final STBTTFontinfo info;
    private final float oversample;
    private final IntSet excludedCharacters = new IntArraySet();
    private final float shiftX;
    private final float shiftY;
    private final float scaleFactor;
    private final float ascent;

    public TTFFont(
            ByteBuffer buffer, STBTTFontinfo info, float size, float oversample, float shiftX, float shiftY, String excludedCharacters
    ) {
        this.buffer = buffer;
        this.info = info;
        this.oversample = oversample;
        excludedCharacters.codePoints().forEach(this.excludedCharacters::add);
        this.shiftX = shiftX * oversample;
        this.shiftY = shiftY * oversample;
        this.scaleFactor = STBTruetype.stbtt_ScaleForPixelHeight(info, size * oversample);
        try (MemoryStack memoryStack = MemoryStack.stackPush();) {
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            IntBuffer intBuffer3 = memoryStack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(info, intBuffer, intBuffer2, intBuffer3);
            this.ascent = (float) intBuffer.get(0) * this.scaleFactor;
        }
    }

    @Override
    @Nullable
    public Glyph getGlyph(int codePoint) {
        if (this.excludedCharacters.contains(codePoint)) {
            return null;
        }
        if (codePoint == 106) {
            System.out.println("Here");
        }
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            int glyphIndex = STBTruetype.stbtt_FindGlyphIndex(this.info, codePoint);
            if (glyphIndex == 0) {
                return null;
            }
            IntBuffer x1 = memoryStack.mallocInt(1);
            IntBuffer x2 = memoryStack.mallocInt(1);
            IntBuffer y1 = memoryStack.mallocInt(1);
            IntBuffer y2 = memoryStack.mallocInt(1);
            IntBuffer advancedWidth = memoryStack.mallocInt(1); // Offset from current horizontal position to next horizontal position
            IntBuffer leftSideBearing = memoryStack.mallocInt(1); // Offset from current horizontal position to left edge
            STBTruetype.stbtt_GetGlyphHMetrics(this.info, glyphIndex, advancedWidth, leftSideBearing);
            STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(
                    this.info,
                    glyphIndex,
                    this.scaleFactor,
                    this.scaleFactor,
                    this.shiftX,
                    this.shiftY,
                    x1,
                    y1,
                    x2,
                    y2
            );
            float width = (float) advancedWidth.get(0) * this.scaleFactor;
            int texWidth = x2.get(0) - x1.get(0);
            int texHeight = y2.get(0) - y1.get(0);
            if (texWidth <= 0 || texHeight <= 0) {
                return (Glyph.EmptyGlyph) () -> width / this.oversample;
            }
            return new TTFGlyph(
                    x1.get(0), x2.get(0), -y2.get(0), -y1.get(0), width, (float) leftSideBearing.get(0) * this.scaleFactor, glyphIndex);
        }
    }

    @Override
    public void close() {
        this.info.free();
        MemoryUtil.memFree(this.buffer);
    }

    @Override
    public IntSet getProvidedGlyphs() {
        return IntStream.range(0, 65535)
                        .filter(codePoint -> !this.excludedCharacters.contains(codePoint))
                        .collect(IntOpenHashSet::new, IntCollection::add, IntCollection::addAll);
    }

    @Environment(value = EnvType.CLIENT)
    protected class TTFGlyph implements ContextualGlyph {
        final int width;
        final int height;
        final float bearingX;
        final float ascent;
        private final float advance;
        final int glyphIndex;

        protected TTFGlyph(int x1, int x2, int y1, int y2, float width, float sideBearing, int glyphIndex) {
            this.width = x2 - x1;
            this.height = y2 - y1;
            this.advance = width / TTFFont.this.oversample;
            this.bearingX = (sideBearing + + TTFFont.this.shiftX) / TTFFont.this.oversample;
            this.ascent = (TTFFont.this.ascent - (float) y2 + TTFFont.this.shiftY) / TTFFont.this.oversample;
            this.glyphIndex = glyphIndex;
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public GlyphRenderer bake(Function<RenderableGlyph, GlyphRenderer> function) {
            return function.apply(new RenderableGlyph() {

                @Override
                public int getWidth() {
                    return TTFGlyph.this.width;
                }

                @Override
                public int getHeight() {
                    return TTFGlyph.this.height;
                }

                @Override
                public float getOversample() {
                    return TTFFont.this.oversample;
                }

                @Override
                public float getBearingX() {
                    return TTFGlyph.this.bearingX;
                }

                @Override
                public float getAscent() {
                    return TTFGlyph.this.ascent;
                }

                @Override
                public void upload(int x, int y) {
                    NativeImage nativeImage = new NativeImage(
                            NativeImage.Format.LUMINANCE,
                            TTFGlyph.this.width,
                            TTFGlyph.this.height,
                            false
                    );
                    nativeImage.makeGlyphBitmapSubpixel(
                            TTFFont.this.info, TTFGlyph.this.glyphIndex, TTFGlyph.this.width, TTFGlyph.this.height,
                            TTFFont.this.scaleFactor, TTFFont.this.scaleFactor, TTFFont.this.shiftX, TTFFont.this.shiftY, 0, 0
                    );
                    nativeImage.upload(0, x, y, 0, 0, TTFGlyph.this.width, TTFGlyph.this.height, false, true);
                }

                @Override
                public boolean hasColor() {
                    return false;
                }
            });
        }

        @Override
        public int getGlyphIndex() {
            return glyphIndex;
        }

        @Override
        public float getAdvance(int nextIndex) {
            return getAdvance() + (STBTruetype.stbtt_GetGlyphKernAdvance(info, glyphIndex, nextIndex) * scaleFactor) / oversample;
        }
    }

}

