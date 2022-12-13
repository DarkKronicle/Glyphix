package io.github.darkkronicle.glyphix.text;

import net.minecraft.text.Style;

public interface GlyphVisitable {

    boolean accept(int index, int characterPos, Style style, GlyphInfo<?> glyph);

}
