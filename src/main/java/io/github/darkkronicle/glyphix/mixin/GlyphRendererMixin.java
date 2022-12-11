package io.github.darkkronicle.glyphix.mixin;

import io.github.darkkronicle.glyphix.vanilla.EmojiLayerHolder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Accessors(fluent = true, chain = false)
@Mixin(GlyphRenderer.class)
public abstract class GlyphRendererMixin implements EmojiLayerHolder {

    @Setter
    @Getter
    @Unique
    private RenderLayer glyphix$emojiLayer;

    @Setter
    @Getter
    @Unique
    private RenderLayer glyphix$emojiSeeThroughLayer;

    @Setter
    @Getter
    @Unique
    private RenderLayer glyphix$emojiPolygonOffsetLayer;

}
