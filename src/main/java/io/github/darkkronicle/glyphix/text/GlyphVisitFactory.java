package io.github.darkkronicle.glyphix.text;

import io.github.darkkronicle.glyphix.vanilla.LigatureFontStorage;
import lombok.experimental.UtilityClass;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class GlyphVisitFactory {

    public void visit(TextRenderer renderer, String text, Style style, boolean backwards, GlyphVisitable visitable) {
        List<GlyphInfo<?>> glyphs = new ArrayList<>();
        final int[] totalLength = {0};
        ContextualCharacterVisitor context = new ContextualCharacterVisitor() {
            @Override
            public boolean accept(Visited visited) {
                LigatureFontStorage lig = (LigatureFontStorage) renderer.getFontStorage(visited.style().getFont());
                GlyphInfo<?> glyph = lig.getGlyphInfo(this, renderer.validateAdvance);
                this.skip(glyph.codepointsLength() - 1);
                totalLength[0] += glyph.charLength();
                glyphs.add(glyph);
                return true;
            }
        };
        TextVisitFactory.visitFormatted(text, style, context);
        context.done();
        if (backwards) {
            int characterIndex = totalLength[0];
            for (int i = glyphs.size() - 1; i >= 0; i--) {
                GlyphInfo<?> g = glyphs.get(i);
                characterIndex -= g.charLength();
                if (!visitable.accept(i, characterIndex, style, g, i < glyphs.size() - 1 ? glyphs.get(i + 1) : null)) {
                    return;
                }
            }
        } else {
            int characterIndex = 0;
            for (int i = 0; i <= glyphs.size() - 1; i++) {
                GlyphInfo<?> g = glyphs.get(i);
                if (!visitable.accept(i, characterIndex, style, g, i < glyphs.size() - 1 ? glyphs.get(i + 1) : null)) {
                    return;
                }
                characterIndex += g.charLength();
            }
        }
    }

    public static void visit(StringVisitable text, ContextualCharacterVisitor characterVisitor) {
        TextVisitFactory.visitFormatted(text, Style.EMPTY, characterVisitor);
        characterVisitor.done();
    }

    public static boolean visit(
            GlyphixRenderer renderer, String text, int startingIndex, Style startingStyle, Style resetStyle,
            GlyphVisitable visitor
    ) {
        List<GlyphInfo<?>> glyphs = new ArrayList<>();
        ContextualCharacterVisitor context = new ContextualCharacterVisitor() {
            @Override
            public boolean accept(Visited visited) {
                LigatureFontStorage lig = (LigatureFontStorage) renderer.getFontStorage(visited.style().getFont());
                GlyphInfo<?> glyph = lig.getGlyphInfo(this, renderer.validateAdvance);
                this.skip(glyph.codepointsLength() - 1);
                glyphs.add(glyph);
                return true;
            }
        };
        TextVisitFactory.visitFormatted(text, startingIndex, startingStyle, resetStyle, context);
        context.done(false);
        int characterIndex = 0;
        for (int i = 0; i <= glyphs.size() - 1; i++) {
            GlyphInfo<?> g = glyphs.get(i);
            if (!visitor.accept(i + startingIndex, characterIndex + startingIndex, context.get(i).style(), g, i < glyphs.size() - 1 ? glyphs.get(i + 1) : null)) {
                return false;
            }
            characterIndex += g.charLength();
        }
        return true;
    }
}
