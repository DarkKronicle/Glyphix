package io.github.darkkronicle.glyphix.mixin;

import io.github.darkkronicle.glyphix.text.GlyphixRenderer;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(FontManager.class)
public class FontManagerMixin {

    @Shadow @Final
    Map<Identifier, FontStorage> fontStorages;

    @Shadow private Map<Identifier, Identifier> idOverrides;

    @Shadow @Final private FontStorage missingStorage;

    @Inject(method = "createTextRenderer", at = @At("HEAD"), cancellable = true)
    private void createGlyphixRenderer(CallbackInfoReturnable<TextRenderer> cir) {
        cir.setReturnValue(new GlyphixRenderer(id -> this.fontStorages.getOrDefault(this.idOverrides.getOrDefault(id, id), this.missingStorage), false));
    }

    @Inject(method = "createAdvanceValidatingTextRenderer", at = @At("HEAD"), cancellable = true)
    private void createGlyphixRendererValidateAdvance(CallbackInfoReturnable<TextRenderer> cir) {
        cir.setReturnValue(new GlyphixRenderer(id -> this.fontStorages.getOrDefault(this.idOverrides.getOrDefault(id, id), this.missingStorage), true));
    }

}
