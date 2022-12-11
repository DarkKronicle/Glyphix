package io.github.darkkronicle.glyphix.vanilla;

import io.github.darkkronicle.glyphix.text.ContextualCharacterVisitor;
import net.minecraft.client.font.Font;
import net.minecraft.client.font.Glyph;

public interface LigatureFont extends Font {

    Glyph getGlyph(ContextualCharacterVisitor visitor);

}
