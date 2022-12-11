package io.github.darkkronicle.glyphix.vanilla;

import io.github.darkkronicle.glyphix.text.ContextualCharacterVisitor;
import net.minecraft.client.font.Glyph;
import net.minecraft.client.font.GlyphRenderer;

public interface LigatureFontStorage {

    Glyph getGlyph(ContextualCharacterVisitor visitor, boolean validateAdvance);

    GlyphRenderer getGlyphRenderer(ContextualCharacterVisitor visitor);

}
