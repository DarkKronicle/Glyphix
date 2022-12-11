package io.github.darkkronicle.glyphix.mixin;

import com.google.gson.Gson;
import io.github.darkkronicle.glyphix.font.EmojiFont;
import net.minecraft.client.font.Font;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(targets = "net/minecraft/client/font/FontManager$1")
public class FontManagerLoaderMixin {

    @Inject(
            method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Ljava/util/Map;",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void prepare(
            ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<Map<Identifier, List<Font>>> cir, Gson gson, Map<Identifier, List<Font>> fonts
    ) {

        EmojiFont font = new EmojiFont.Loader().load(resourceManager);
        for (Identifier id : fonts.keySet()) {
            fonts.get(id).add(font);
        }
        cir.setReturnValue(fonts);
    }

}
