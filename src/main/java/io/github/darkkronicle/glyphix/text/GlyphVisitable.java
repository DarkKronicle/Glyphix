package io.github.darkkronicle.glyphix.text;

import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

public interface GlyphVisitable {

    boolean accept(int index, int characterPos, Style style, GlyphInfo<?> glyph, @Nullable GlyphInfo<?> nextGlyph);

}
