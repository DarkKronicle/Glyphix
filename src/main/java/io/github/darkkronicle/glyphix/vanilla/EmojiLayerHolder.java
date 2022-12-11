package io.github.darkkronicle.glyphix.vanilla;

import net.minecraft.client.render.RenderLayer;

public interface EmojiLayerHolder {

    RenderLayer glyphix$emojiLayer();

    RenderLayer glyphix$emojiSeeThroughLayer();

    RenderLayer glyphix$emojiPolygonOffsetLayer();

    void glyphix$emojiLayer(RenderLayer layer);

    void glyphix$emojiSeeThroughLayer(RenderLayer layer);

    void glyphix$emojiPolygonOffsetLayer(RenderLayer layer);

}
