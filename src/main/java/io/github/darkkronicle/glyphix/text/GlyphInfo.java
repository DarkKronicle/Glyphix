package io.github.darkkronicle.glyphix.text;

import net.minecraft.client.font.Glyph;

import java.util.Arrays;

public record GlyphInfo<G extends Glyph>(G glyph, int[] codepoints) {

    public float getAdvance() {
        return glyph.getAdvance(false);
    }

    public float getAdvance(boolean bold) {
        return glyph.getAdvance(bold);
    }

    public int charLength() {
        return Arrays.stream(codepoints).map(Character::charCount).sum();
    }

    public int codepointsLength() {
        return codepoints.length;
    }

}
