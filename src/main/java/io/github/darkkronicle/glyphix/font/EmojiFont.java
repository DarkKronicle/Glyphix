package io.github.darkkronicle.glyphix.font;

import com.google.gson.*;
import io.github.darkkronicle.glyphix.Glyphix;
import io.github.darkkronicle.glyphix.text.ContextualCharacterVisitor;
import io.github.darkkronicle.glyphix.text.GlyphInfo;
import io.github.darkkronicle.glyphix.text.GlyphVisitable;
import io.github.darkkronicle.glyphix.vanilla.LigatureFont;
import it.unimi.dsi.fastutil.ints.*;
import lombok.*;
import net.minecraft.client.font.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class EmojiFont implements LigatureFont {

    private final EmojiFontAtlas atlas;
    private final int length = 8;

    public EmojiFont(EmojiFontAtlas atlas) {
        this.atlas = atlas;
    }

    @Override
    public IntSet getProvidedGlyphs() {
        return atlas.positions.keySet();
    }

    @Override
    public void close() {
        atlas.image.close();
    }

    @Nullable
    @Override
    public Glyph getGlyph(int codePoint) {
        EmojiLocation[] locations = atlas.positions.get(codePoint);
        if (locations == null || locations.length == 0) {
            return null;
        }
        // Just get the first one
        EmojiLocation location = Arrays.stream(locations).filter(loc -> loc.codepoints.length == 1).findFirst().orElse(null);
        if (location == null) {
            return null;
        }
        return location.getCached(this).glyph();
    }

    public static int utf8ToCodepoint(String utf8) {
        int number = Integer.valueOf(utf8, 16);
        if (number < 0x80) {
            // 0xxxxxxx
            return number;
        }
        if (number < 0x800) {
            // 110xxxxx 10xxxxxx
            int one = (number & 0b00111111) + 0b10000000;
            int two = ((number >>> 6) & 0b00011111) + 0b11000000;
            return (two << 8) + one;
        }
        if (number < 0x10000) {
            // 11110xxx 10xxxxxx 10xxxxxx
            int one = (number & 0b00111111) + 0b10000000;
            int two = ((number >>> 6) & 0b00111111) + 0b10000000;
            int three = ((number >>> 12) & 0b00000111) + 0b11110000;
            return (three << 16) + (two << 8) + one;
        }
        if (number < 0x200000) {
            // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            int one = (number & 0b00111111) + 0b10000000;
            int two = ((number >>> 6) & 0b00111111) + 0b10000000;
            int three = ((number >>> 12) & 0b00111111) + 0b10000000;
            int four = ((number >>> 18) & 0b00000111) + 0b11110000;
            return (four << 24) + (three << 16) + (two << 8) + one;
        }
        return -1;
    }

    public static String codepointToUtf8(int codepoint) {
        // https://stackoverflow.com/questions/6240055/manually-converting-unicode-codepoints-into-utf-8-and-utf-16
        String string = Character.toString(codepoint);
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        if (bytes.length == 4) {
            return utf8From4(bytes);
        }
        if (bytes.length == 3) {
            return utf8From3(bytes);
        }
        return null;
    }

    private static String utf8From1(byte[] bytes) {
        // 0xxxxxxx
        int one = bytes[0] & 0b01111111;
        return Integer.toHexString(one);
    }

    private static String utf8From2(byte[] bytes) {
        // 110xxxxx 10xxxxxx
        int one = bytes[0] & 0b00011111;
        int two = bytes[1] & 0b00111111;
        int utf = (one << 6) + (two);
        return Integer.toHexString(utf);
    }

    private static String utf8From3(byte[] bytes) {
        // 11110xxx 10xxxxxx 10xxxxxx
        int one = bytes[0] & 0b00000111;
        int two = bytes[1] & 0b00111111;
        int three = bytes[2] & 0b00111111;
        int utf = (one << 12) + (two << 6) + (three);
        return Integer.toHexString(utf);
    }

    private static String utf8From4(byte[] bytes) {
        // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
        int one = bytes[0] & 0b00000111;
        int two = bytes[1] & 0b00111111;
        int three = bytes[2] & 0b00111111;
        int four = bytes[3] & 0b00111111;

        // Add up all the x's to get the original one
        int utf = (one << 18) + (two << 12) + (three << 6) + (four);
        return Integer.toHexString(utf);
    }

    private boolean valid(ContextualCharacterVisitor visitor, int index, EmojiLocation find) {
        for (int i = 1; i < find.getCodepoints().length; i++) {
            int codepoint = find.getCodepoints()[i];
            ContextualCharacterVisitor.Visited vis = visitor.get(index + i);
            if (vis == null || vis.codepoint() != codepoint) {
                return false;
            }
        }
        return true;
    }

    public EmojiLocation getLocation(ContextualCharacterVisitor visitor, EmojiLocation[] locations) {
        int i = visitor.getCurrentIndex();
        return Arrays.stream(locations)
                     .filter(find -> valid(visitor, i, find))
                     .max(Comparator.comparingInt(location -> location.codepoints.length))
                     .orElse(null);
    }

    @Override
    public Glyph getGlyph(ContextualCharacterVisitor visitor) {
        int codepoint = visitor.current().codepoint();
        EmojiLocation[] locations = atlas.positions.get(codepoint);
        if (locations == null || locations.length == 0) {
            return null;
        }
        EmojiLocation found = getLocation(visitor, locations);
        if (found == null) {
            return null;
        }
        return found.getCached(this).glyph();
    }

    @Override
    public GlyphInfo<EmojiGlyph> getGlyphInfo(ContextualCharacterVisitor visitor) {
        int codepoint = visitor.current().codepoint();
        EmojiLocation[] locations = atlas.positions.get(codepoint);
        if (locations == null || locations.length == 0) {
            return null;
        }
        EmojiLocation found = getLocation(visitor, locations);
        if (found == null) {
            return null;
        }
        return found.getCached(this);
    }

    @RequiredArgsConstructor
    public class EmojiGlyph implements Glyph {

        protected final int x;
        protected final int y;
        protected final int characterLength;
        protected final int charCount;

        protected GlyphRenderer baked;

        @Override
        public float getAdvance() {
            return length;
        }

        @Override
        public GlyphRenderer bake(Function<RenderableGlyph, GlyphRenderer> function) {
            if (baked == null) {
                baked = function.apply(new RenderableGlyph() {
                    @Override
                    public int getWidth() {
                        return atlas.length;
                    }

                    @Override
                    public int getHeight() {
                        return atlas.length;
                    }

                    @Override
                    public void upload(int x, int y) {
                        atlas.image.upload(0, x, y, EmojiGlyph.this.x, EmojiGlyph.this.y, atlas.length, atlas.length, true, false);
                        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
                    }

                    @Override
                    public boolean hasColor() {
                        return true;
                    }

                    @Override
                    public float getOversample() {
                        return (float) atlas.length / length;
                    }

                    @Override
                    public float getAscent() {
                        return RenderableGlyph.super.getAscent() - 1;
                    }
                });
            }
            return baked;
        }
    }

    public static class EmojiFontAtlas {

        protected NativeImage image;
        protected int length;
        protected Int2ObjectMap<EmojiLocation[]> positions;

        private EmojiFontAtlas(NativeImage image, int length, Int2ObjectMap<EmojiLocation[]> positions) {
            this.image = image;
            this.length = length;
            this.positions = positions;
        }

        @Nullable
        public static EmojiFontAtlas fromJson(NativeImage image, JsonObject object) {
            if (!object.has("length")) {
                return null;
            }
            if (!object.has("positions")) {
                return null;
            }
            try {
                int length = object.get("length").getAsInt();
                JsonArray columns = object.get("positions").getAsJsonArray();
                int columnI = 0;
                Map<Integer, EmojiLocation[]> positions = new HashMap<>();
                for (JsonElement element : columns) {
                    JsonArray column = element.getAsJsonArray();
                    int rowI = 0;
                    for (JsonElement el : column) {
                        String row = el.getAsString();
                        String[] parts = row.split("-");
                        int start = Integer.valueOf(parts[0], 16);
                        EmojiLocation[] locations = positions.computeIfAbsent(start, integer -> new EmojiLocation[1]);
                        EmojiLocation[] newLocation;
                        if (locations[0] == null) {
                            newLocation = locations;
                        } else {
                            newLocation = new EmojiLocation[locations.length + 1];
                            System.arraycopy(locations, 0, newLocation, 0, locations.length);
                        }
                        int[] codepoints = Arrays.stream(parts).mapToInt(str -> Integer.valueOf(str, 16)).toArray();
                        newLocation[newLocation.length - 1] = new EmojiLocation(
                                new Vec2i(rowI, columnI), codepoints);
                        positions.put(start, newLocation);
                        rowI++;
                    }
                    columnI++;
                }
                return new EmojiFontAtlas(image, length, new Int2ObjectOpenHashMap<>(positions));
            } catch (ClassCastException e) {
                return null;
            }
        }

    }

    private GlyphInfo<EmojiGlyph> createGlyph(Vec2i position, int characterLength, int[] codepoints) {
        return new GlyphInfo<>(new EmojiGlyph(position.x * atlas.length, position.y * atlas.length, characterLength, codepoints.length), codepoints);
    }

    @EqualsAndHashCode
    public static class EmojiLocation {

        @Getter
        private Vec2i position;
        @Getter
        private int[] codepoints;

        public EmojiLocation(Vec2i position, int[] codepoints) {
            this.position = position;
            this.codepoints = codepoints;
        }

        private GlyphInfo<EmojiGlyph> cached = null;

        public GlyphInfo<EmojiGlyph> getCached(EmojiFont font) {
            if (cached == null) {
                cached = font.createGlyph(position, codepoints.length, codepoints);
            }
            return cached;
        }

    }

    public static class Loader implements FontLoader {

        @Nullable
        @Override
        public EmojiFont load(ResourceManager manager) {
            Identifier png = new Identifier(Glyphix.MOD_ID, "emoji/emoji_atlas.png");
            Identifier json = new Identifier(Glyphix.MOD_ID, "emoji/emoji_positions.json");

            try (
                    InputStream imageStream = manager.open(png);
                    BufferedReader jsonStream = manager.openAsReader(json);
            ) {

                NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, imageStream);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonObject obj = JsonHelper.deserialize(gson, jsonStream, JsonObject.class);
                EmojiFontAtlas atlas = EmojiFontAtlas.fromJson(nativeImage, obj);

                return new EmojiFont(atlas);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Value
    private static class Vec2i {

        int x;
        int y;

    }


}
