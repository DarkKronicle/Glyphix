package io.github.darkkronicle.glyphix.font;

import net.minecraft.client.font.Glyph;

public interface ContextualGlyph extends Glyph {

    int getGlyphIndex();

    float getAdvance(int nextIndex);

}
