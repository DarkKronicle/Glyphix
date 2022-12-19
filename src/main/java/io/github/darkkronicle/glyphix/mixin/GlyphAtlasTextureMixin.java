package io.github.darkkronicle.glyphix.mixin;

import io.github.darkkronicle.glyphix.Glyphix;
import io.github.darkkronicle.glyphix.text.GlyphixLayers;
import io.github.darkkronicle.glyphix.text.OversampleGlyphRenderer;
import io.github.darkkronicle.glyphix.text.OversampleRenderableGlyph;
import net.minecraft.client.font.GlyphAtlasTexture;
import net.minecraft.client.font.GlyphRenderer;
import net.minecraft.client.font.RenderableGlyph;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlyphAtlasTexture.class)
public abstract class GlyphAtlasTextureMixin extends AbstractTexture {

    @Shadow @Final private GlyphAtlasTexture.Slot rootSlot;

    @Shadow private RenderLayer textLayer;

    @Shadow private RenderLayer seeThroughTextLayer;

    @Shadow private RenderLayer polygonOffsetTextLayer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Identifier id, boolean hasColor, CallbackInfo ci) {
        this.textLayer = hasColor ? GlyphixLayers.TEXT.apply(id) : GlyphixLayers.TEXT_INTENSITY.apply(id);
        this.seeThroughTextLayer = hasColor ? GlyphixLayers.TEXT_SEE_THROUGH.apply(id) : GlyphixLayers.TEXT_INTENSITY_SEE_THROUGH.apply(id);
        this.polygonOffsetTextLayer = hasColor ? GlyphixLayers.TEXT_POLYGON_OFFSET.apply(id) : GlyphixLayers.EMOJI_INTENSITY_POLYGON_OFFSET.apply(id);
    }


    @Inject(method = "getGlyphRenderer", at = @At("HEAD"), cancellable = true)
    private void getGlyphRenderer(RenderableGlyph glyph, CallbackInfoReturnable<GlyphRenderer> cir) {
        if (!(glyph instanceof OversampleRenderableGlyph over)) {
            return;
        }
//        GlyphAtlasTexture.Slot slot = this.rootSlot.findSlotFor(glyph);
//        if (slot != null) {
//            this.bindTexture();
//            glyph.upload(slot.x, slot.y);
//            OversampleGlyphRenderer render = new OversampleGlyphRenderer(
//                    this.textLayer,
//                    this.seeThroughTextLayer,
//                    this.polygonOffsetTextLayer,
//                    ((float) slot.x + 0.01F) / 256.0F,
//                    ((float) slot.x - 0.01F + (float) glyph.getWidth()) / 256.0F,
//                    ((float) slot.y + 0.01F) / 256.0F,
//                    ((float) slot.y - 0.01F + (float) glyph.getHeight()) / 256.0F,
//                    glyph.getXMin(),
//                    glyph.getXMax(),
//                    glyph.getYMin(),
//                    glyph.getYMax(),
//                    over
//            );
//            cir.setReturnValue(render);
//        }
    }

}
