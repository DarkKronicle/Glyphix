package io.github.darkkronicle.glyphix.vanilla;

import io.github.darkkronicle.glyphix.text.ContextualCharacterVisitor;
import io.github.darkkronicle.glyphix.text.GlyphInfo;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.Glyph;

public interface LigatureFont extends Font {

    Glyph getGlyph(ContextualCharacterVisitor visitor);

    <G extends Glyph> GlyphInfo<G> getGlyphInfo(ContextualCharacterVisitor visitor);
}
