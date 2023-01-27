package io.github.darkkronicle.glyphix.text;

import io.github.darkkronicle.glyphix.Glyphix;
import ladysnake.satin.api.managed.ManagedCoreShader;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class GlyphixLayers extends RenderLayer {

    public static final ManagedCoreShader SHADER_EMOJI = ShaderEffectManager.getInstance().manageCoreShader(
            new Identifier(Glyphix.MOD_ID, "rendertype_emoji")
    );

    public static final ManagedCoreShader SHADER_EMOJI_INTENSITY = ShaderEffectManager.getInstance().manageCoreShader(
            new Identifier(Glyphix.MOD_ID, "rendertype_emoji_intensity")
    );

    public static final ManagedCoreShader SHADER_EMOJI_SEE_THROUGH = ShaderEffectManager.getInstance().manageCoreShader(
            new Identifier(Glyphix.MOD_ID, "rendertype_emoji_see_through")
    );

    public static final ManagedCoreShader SHADER_EMOJI_INTENSITY_SEE_THROUGH = ShaderEffectManager.getInstance().manageCoreShader(
            new Identifier(Glyphix.MOD_ID, "rendertype_emoji_intensity_see_through")
    );


    protected static final RenderPhase.ShaderProgram EMOJI_PROGRAM = new RenderPhase.ShaderProgram(SHADER_EMOJI::getProgram);
    protected static final RenderPhase.ShaderProgram EMOJI_INTENSITY_PROGRAM = new RenderPhase.ShaderProgram(SHADER_EMOJI_INTENSITY::getProgram);
    protected static final RenderPhase.ShaderProgram TRANSPARENT_EMOJI_PROGRAM = new RenderPhase.ShaderProgram(SHADER_EMOJI_SEE_THROUGH::getProgram);
    protected static final RenderPhase.ShaderProgram TRANSPARENT_EMOJI_INTENSITY_PROGRAM = new RenderPhase.ShaderProgram(
            SHADER_EMOJI_INTENSITY_SEE_THROUGH::getProgram
    );

    public static final Function<Identifier, RenderLayer> TEXT = Util.memoize(
            texture -> of(
                    "text",
                    RenderLayer.MultiPhaseParameters.builder()
                                                    .program(RenderPhase.TEXT_PROGRAM)
                                                    .texture(new RenderPhase.Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> TEXT_INTENSITY = Util.memoize(
            texture -> of(
                    "emoji_intensity",
                    RenderLayer.MultiPhaseParameters.builder()
                                                    .program(RenderPhase.TEXT_INTENSITY_PROGRAM)
                                                    .texture(new RenderPhase.Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> TEXT_POLYGON_OFFSET = Util.memoize(
            texture -> of(
                    "text_polygon_offset",
                    RenderLayer.MultiPhaseParameters.builder()
                                                    .program(RenderPhase.TEXT_PROGRAM)
                                                    .texture(new RenderPhase.Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .layering(POLYGON_OFFSET_LAYERING)
                                                    .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize(
            texture -> of(
                    "text_intensity_polygon_offset",
                    RenderLayer.MultiPhaseParameters.builder()
                                                    .program(RenderPhase.TEXT_INTENSITY_PROGRAM)
                                                    .texture(new RenderPhase.Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .layering(POLYGON_OFFSET_LAYERING)
                                                    .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> TEXT_SEE_THROUGH = Util.memoize(
            texture -> of(
                    "text_see_through",
                    RenderLayer.MultiPhaseParameters.builder()
                                                    .program(RenderPhase.TRANSPARENT_TEXT_PROGRAM)
                                                    .texture(new RenderPhase.Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .depthTest(ALWAYS_DEPTH_TEST)
                                                    .writeMaskState(COLOR_MASK)
                                                    .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> TEXT_INTENSITY_SEE_THROUGH = Util.memoize(
            texture -> of(
                    "text_intensity_see_through",
                    MultiPhaseParameters.builder()
                                        .program(RenderPhase.TRANSPARENT_TEXT_INTENSITY_PROGRAM)
                                        .texture(new Texture(texture, true, false))
                                        .transparency(TRANSLUCENT_TRANSPARENCY)
                                        .lightmap(ENABLE_LIGHTMAP)
                                        .depthTest(ALWAYS_DEPTH_TEST)
                                        .writeMaskState(COLOR_MASK)
                                        .build(false)
            )
    );

    public static final Function<Identifier, RenderLayer> EMOJI = Util.memoize(
            texture -> of(
                    "emoji",
                    RenderLayer.MultiPhaseParameters.builder()
                                                    .program(EMOJI_PROGRAM)
                                                    .texture(new RenderPhase.Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> EMOJI_INTENSITY = Util.memoize(
            texture -> of(
                    "emoji_intensity",
                    RenderLayer.MultiPhaseParameters.builder()
                                                    .program(EMOJI_INTENSITY_PROGRAM)
                                                    .texture(new RenderPhase.Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> EMOJI_POLYGON_OFFSET = Util.memoize(
            texture -> of(
                    "emoji_polygon_offset",
                    RenderLayer.MultiPhaseParameters.builder()
                                                    .program(EMOJI_PROGRAM)
                                                    .texture(new RenderPhase.Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .layering(POLYGON_OFFSET_LAYERING)
                                                    .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> EMOJI_INTENSITY_POLYGON_OFFSET = Util.memoize(
            texture -> of(
                    "emoji_intensity_polygon_offset",
                    RenderLayer.MultiPhaseParameters.builder()
                                                    .program(EMOJI_INTENSITY_PROGRAM)
                                                    .texture(new RenderPhase.Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .layering(POLYGON_OFFSET_LAYERING)
                                                    .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> EMOJI_SEE_THROUGH = Util.memoize(
            texture -> of(
                    "emoji_see_through",
                    RenderLayer.MultiPhaseParameters.builder()
                                                    .program(TRANSPARENT_EMOJI_PROGRAM)
                                                    .texture(new RenderPhase.Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .depthTest(ALWAYS_DEPTH_TEST)
                                                    .writeMaskState(COLOR_MASK)
                                                    .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> EMOJI_INTENSITY_SEE_THROUGH = Util.memoize(
            texture -> of(
                    "emoji_intensity_see_through",
                    MultiPhaseParameters.builder()
                                                    .program(TRANSPARENT_EMOJI_INTENSITY_PROGRAM)
                                                    .texture(new Texture(texture, true, false))
                                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                                    .lightmap(ENABLE_LIGHTMAP)
                                                    .depthTest(ALWAYS_DEPTH_TEST)
                                                    .writeMaskState(COLOR_MASK)
                                                    .build(false)
            )
    );

    private GlyphixLayers(
            String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling,
            boolean translucent, Runnable startAction, Runnable endAction
    ) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    private static RenderLayer.MultiPhase of(
            String name,
            MultiPhaseParameters phases
    ) {
        return new RenderLayer.MultiPhase(name, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, VertexFormat.DrawMode.QUADS, 256, false,
                true, phases);
    }


}
