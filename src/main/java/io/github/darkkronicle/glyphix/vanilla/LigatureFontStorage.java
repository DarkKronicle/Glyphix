package io.github.darkkronicle.glyphix.vanilla;

import io.github.darkkronicle.glyphix.text.ContextualCharacterVisitor;
import io.github.darkkronicle.glyphix.text.GlyphInfo;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;

public interface LigatureFontStorage {

    Glyph getGlyph(ContextualCharacterVisitor visitor, boolean validateAdvance);

    GlyphRenderer getGlyphRenderer(ContextualCharacterVisitor visitor);

    <G extends Glyph> GlyphInfo<G> getGlyphInfo(ContextualCharacterVisitor visitor, boolean validateAdvance);

}
