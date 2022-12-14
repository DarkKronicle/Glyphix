package io.github.darkkronicle.glyphix.vanilla;

import io.github.darkkronicle.glyphix.text.ContextualCharacterVisitor;
import io.github.darkkronicle.glyphix.text.GlyphInfo;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.Glyph;

public interface LigatureFont<G extends Glyph> extends Font {

    Glyph getGlyph(ContextualCharacterVisitor visitor);

    GlyphInfo<G> getGlyphInfo(ContextualCharacterVisitor visitor);
}
