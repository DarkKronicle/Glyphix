package io.github.darkkronicle.glyphix.mixin;


import com.google.gson.JsonObject;
import io.github.darkkronicle.glyphix.font.TTFFontLoader;
import net.minecraft.client.font.FontLoader;
import net.minecraft.client.font.FontType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontType.class)
public class FontTypeMixin {

    @Inject(method = "createLoader", cancellable = true, at = @At("HEAD"))
    private void getType(JsonObject json, CallbackInfoReturnable<FontLoader> cir) {
        // Use our own fancy ttf
        if ((Object) this != FontType.TTF) {
            return;
        }
        cir.setReturnValue(TTFFontLoader.fromJson(json));
    }

}
