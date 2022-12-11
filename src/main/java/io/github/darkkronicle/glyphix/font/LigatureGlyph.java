package io.github.darkkronicle.glyphix.font;

import net.minecraft.client.font.Glyph;

public interface LigatureGlyph extends Glyph {

    default int characterLength() {
        return 1;
    }

}
