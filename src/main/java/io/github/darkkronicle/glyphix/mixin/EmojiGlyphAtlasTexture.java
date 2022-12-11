package io.github.darkkronicle.glyphix.mixin;

import io.github.darkkronicle.glyphix.vanilla.EmojiLayerHolder;
import io.github.darkkronicle.glyphix.text.GlyphixLayers;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.font.GlyphAtlasTexture;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Accessors(fluent = true, chain = false)
@Mixin(GlyphAtlasTexture.class)
public abstract class EmojiGlyphAtlasTexture implements EmojiLayerHolder {

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

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(Identifier id, boolean hasColor, CallbackInfo ci) {
        this.glyphix$emojiLayer = GlyphixLayers.EMOJI_INTENSITY.apply(id);
        this.glyphix$emojiSeeThroughLayer = GlyphixLayers.EMOJI_INTENSITY_SEE_THROUGH.apply(id);
        this.glyphix$emojiPolygonOffsetLayer = GlyphixLayers.EMOJI_INTENSITY_POLYGON_OFFSET.apply(id);
    }

    @Inject(method = "getGlyphRenderer", at = @At(value = "RETURN", ordinal = 1))
    private void getGlyphRenderer(CallbackInfoReturnable<GlyphRenderer> original) {
        EmojiLayerHolder layerHolder = (EmojiLayerHolder) original.getReturnValue();
        layerHolder.glyphix$emojiLayer(glyphix$emojiLayer);
        layerHolder.glyphix$emojiSeeThroughLayer(glyphix$emojiSeeThroughLayer);
        layerHolder.glyphix$emojiPolygonOffsetLayer(glyphix$emojiPolygonOffsetLayer);
    }

}
