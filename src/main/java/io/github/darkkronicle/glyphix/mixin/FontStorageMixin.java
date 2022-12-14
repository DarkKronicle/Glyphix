package io.github.darkkronicle.glyphix.mixin;

import io.github.darkkronicle.glyphix.text.ContextualCharacterVisitor;
import io.github.darkkronicle.glyphix.text.GlyphInfo;
import io.github.darkkronicle.glyphix.vanilla.LigatureFont;
import io.github.darkkronicle.glyphix.vanilla.LigatureFontStorage;
import net.minecraft.client.font.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(FontStorage.class)
public abstract class FontStorageMixin implements LigatureFontStorage {

    @Shadow @Final private List<Font> fonts;

    @Shadow public abstract Glyph getGlyph(int codePoint, boolean validateAdvance);

    @Shadow
    private static boolean isAdvanceInvalid(Glyph glyph) {
        return false;
    }

    @Shadow public abstract GlyphRenderer getGlyphRenderer(int codePoint);

    @Shadow protected abstract GlyphRenderer getGlyphRenderer(RenderableGlyph c);

    @Override
    public Glyph getGlyph(ContextualCharacterVisitor visitor, boolean validateAdvance) {
        int codepoint = visitor.current().codepoint();
        boolean advancedCheck = false;
        for (Font font : this.fonts) {
            if (!(font instanceof LigatureFont<?> ligFont)) {
                continue;
            }
            Glyph glyph = ligFont.getGlyph(visitor);
            if (glyph != null) {
                advancedCheck = true;
                if (!isAdvanceInvalid(glyph)) {
                    return glyph;
                }
            }
        }
        if (advancedCheck) {
            return null;
        }
        return this.getGlyph(codepoint, validateAdvance);
    }

    @Override
    public GlyphInfo<?> getGlyphInfo(ContextualCharacterVisitor visitor, boolean validateAdvance) {
        int codepoint = visitor.current().codepoint();
        boolean advancedCheck = false;
        for (Font font : this.fonts) {
            if (!(font instanceof LigatureFont<?> ligFont)) {
                continue;
            }
            GlyphInfo<?> glyph = ligFont.getGlyphInfo(visitor);
            if (glyph == null) {
                continue;
            }
            advancedCheck = true;
            if (!isAdvanceInvalid(glyph.glyph())) {
                return glyph;
            }
        }
        if (advancedCheck) {
            return null;
        }
        return new GlyphInfo<>(this.getGlyph(codepoint, validateAdvance), new int[]{codepoint}, true);
    }

    @Override
    public GlyphRenderer getGlyphRenderer(ContextualCharacterVisitor visitor) {
        int codepoint = visitor.current().codepoint();
        for (Font font : this.fonts) {
            if (!(font instanceof LigatureFont<?> ligFont)) {
                continue;
            }
            Glyph glyph = ligFont.getGlyph(visitor);
            if (glyph != null) {
                return glyph.bake(this::getGlyphRenderer);
            }
        }
        return this.getGlyphRenderer(codepoint);
    }

}
