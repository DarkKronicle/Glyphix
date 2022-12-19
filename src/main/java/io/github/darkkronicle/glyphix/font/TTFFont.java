package io.github.darkkronicle.glyphix.font;


import io.github.darkkronicle.glyphix.text.OversampleRenderableGlyph;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.*;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;
import java.util.stream.IntStream;

@Environment(value = EnvType.CLIENT)
public class TTFFont implements Font {
    private final ByteBuffer fontData;
    private final STBTTFontinfo info;
    private final float oversample;
    private final IntSet excludedCharacters = new IntArraySet();
    private final float shiftX;
    private final float shiftY;
    private final float scaleFactor;
    private final float ascent;
    private final int oversampleWidth = 4;
    private final float size;
    private STBTTPackContext pack;

    public TTFFont(
            ByteBuffer fontData, STBTTFontinfo info, float size, float oversample, float shiftX, float shiftY, String excludedCharacters
    ) {
        this.fontData = fontData;
        this.info = info;
        this.size = size;
        this.oversample = oversample;
        excludedCharacters.codePoints().forEach(this.excludedCharacters::add);
        this.shiftX = shiftX * oversample;
        this.shiftY = shiftY * oversample;
        this.scaleFactor = STBTruetype.stbtt_ScaleForPixelHeight(info, size * oversample);
        this.pack = STBTTPackContext.malloc();
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
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
                    x1.get(0), x2.get(0), -y2.get(0), -y1.get(0), width, (float) leftSideBearing.get(0) * this.scaleFactor, glyphIndex, codePoint);
        }
    }

    @Override
    public void close() {
        this.info.free();
        MemoryUtil.memFree(this.fontData);
    }

    @Override
    public IntSet getProvidedGlyphs() {
        return IntStream.range(0, 65535)
                        .filter(codePoint -> !this.excludedCharacters.contains(codePoint))
                        .collect(IntOpenHashSet::new, IntCollection::add, IntCollection::addAll);
    }

    @Environment(value = EnvType.CLIENT)
    protected class TTFGlyph implements ContextualGlyph {
        private final int width;
        private final int textureWidth;
        private final int height;
        private final float bearingX;
        private final float ascent;
        private final float advance;
        private final int glyphIndex;
        private final int unicode;

        protected TTFGlyph(int x1, int x2, int y1, int y2, float width, float sideBearing, int glyphIndex, int unicode) {
            this.width = (x2 - x1);
            this.textureWidth = this.width;
            this.height = y2 - y1;
            this.advance = width / TTFFont.this.oversample;
            this.bearingX = (sideBearing + + TTFFont.this.shiftX) / TTFFont.this.oversample;
            this.ascent = (TTFFont.this.ascent - (float) y2 + TTFFont.this.shiftY) / TTFFont.this.oversample;
            this.glyphIndex = glyphIndex;
            this.unicode = unicode;
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public GlyphRenderer bake(Function<RenderableGlyph, GlyphRenderer> function) {
            return function.apply(new OversampleRenderableGlyph() {

                private STBTTPackedchar.Buffer charData;

                @Override
                public int getWidth() {
                    return TTFGlyph.this.textureWidth;
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
                    NativeImage nativeImage;


//                    STBTruetype.nstbtt_PackBegin(
//                            pack.address(),
//                            nativeImage.pointer,
//                            width * oversampleWidth,
//                            height,
//                            0,
//                            1,
//                            0
//                    );
//                    STBTruetype.stbtt_PackSetOversampling(pack, oversampleWidth, 1);
//                    charData = STBTTPackedchar.create(1);
//                    STBTruetype.stbtt_PackFontRange(pack, fontData, 0, size, unicode, charData);
//                    STBTruetype.stbtt_PackEnd(pack);


                    ByteBuffer data = STBTruetype.stbtt_GetGlyphSDF(info, scaleFactor, glyphIndex, 0, (byte) 128, -16, new int[]{TTFGlyph.this.width}, new int[]{TTFGlyph.this.height}, new int[]{x}, new int[]{y});
//                    nativeImage = new NativeImage(
//                            NativeImage.Format.RGBA,
//                            TTFGlyph.this.width,
//                            TTFGlyph.this.height,
//                            true,
//                            MemoryUtil.memAddress(data)
//                    );
//                    data.rewind();
                    nativeImage = new NativeImage(
                            NativeImage.Format.LUMINANCE,
                            TTFGlyph.this.textureWidth,
                            TTFGlyph.this.height,
                            false
                    );
                    try {
                        int bx = 0;
                        int by = 0;
                        while (data.hasRemaining()) {
                            byte b = data.get();
                            nativeImage.setLuminance(bx, by, b);
                            bx++;
                            if (bx >= TTFGlyph.this.width) {
                                by++;
                                if (by == TTFGlyph.this.height) {
                                    break;
                                }
                                bx = 0;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error with character " + Character.toString(unicode));
                        nativeImage.makeGlyphBitmapSubpixel(
                                TTFFont.this.info, TTFGlyph.this.glyphIndex, TTFGlyph.this.textureWidth, TTFGlyph.this.height,
                                TTFFont.this.scaleFactor, TTFFont.this.scaleFactor, TTFFont.this.shiftX, TTFFont.this.shiftY, 0, 0
                        );
                    }

                    nativeImage.upload(0, x, y, 0, 0, TTFGlyph.this.textureWidth, TTFGlyph.this.height, false, true);
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

