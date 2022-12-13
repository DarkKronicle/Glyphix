package io.github.darkkronicle.glyphix.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.darkkronicle.glyphix.vanilla.LigatureFontStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class GlyphixTextHandler extends TextHandler {

    private final GlyphixRenderer renderer;

    public GlyphixTextHandler(GlyphixRenderer renderer) {
        super(null);
        this.renderer = renderer;
    }

    private float getWidth(ContextualCharacterVisitor visitor, boolean skip) {
        return getWidth(visitor, skip, false);
    }

    private float getWidth(ContextualCharacterVisitor visitor, boolean skip, boolean backwards) {
        ContextualCharacterVisitor.Visited current = visitor.current();
        GlyphInfo<?> glyph = ((LigatureFontStorage) renderer.getFontStorage(current.style().getFont()))
                .getGlyphInfo(visitor, renderer.validateAdvance);
        if (skip) {
            visitor.skip(glyph.codepointsLength() - 1);
        }
        return glyph.getAdvance(current.style().isBold());
    }

    @Override
    public float getWidth(@Nullable String text) {
        if (text == null) {
            return 0.0F;
        } else {
            MutableFloat mutableFloat = new MutableFloat();
            ContextualCharacterVisitor visitor = new ContextualCharacterVisitor() {
                @Override
                public boolean accept(Visited visited) {
                    mutableFloat.add(getWidth(this, true));
                    return true;
                }
            };
            TextVisitFactory.visitFormatted(text, Style.EMPTY, visitor);
            visitor.done();
            return mutableFloat.floatValue();
        }
    }

    @Override
    public float getWidth(StringVisitable text) {
        MutableFloat mutableFloat = new MutableFloat();
        ContextualCharacterVisitor visitor = new ContextualCharacterVisitor() {
            @Override
            public boolean accept(Visited visited) {
                mutableFloat.add(getWidth(this, true));
                return true;
            }
        };
        TextVisitFactory.visitFormatted(text, Style.EMPTY, visitor);
        visitor.done();
        return mutableFloat.floatValue();
    }

    @Override
    public float getWidth(OrderedText text) {
        MutableFloat mutableFloat = new MutableFloat();
        ContextualCharacterVisitor visitor = new ContextualCharacterVisitor() {
            @Override
            public boolean accept(Visited visited) {
                mutableFloat.add(getWidth(this, true));
                return true;
            }
        };
        text.accept(visitor);
        visitor.done();
        return mutableFloat.floatValue();
    }

    @Override
    public String trimToWidthBackwards(String text, int maxWidth, Style style) {
        MutableFloat mutableFloat = new MutableFloat();
        MutableInt mutableInt = new MutableInt(text.length());
        GlyphVisitFactory.visit(renderer, text, style, true, (index, characterPos, style1, glyph) -> {
            if (mutableFloat.addAndGet(glyph.getAdvance(style1.isBold())) > (float) maxWidth) {
                return false;
            } else {
                mutableInt.setValue(characterPos);
                return true;
            }
        });
        return text.substring(mutableInt.intValue());
    }

    @Override
    public int getTrimmedLength(String text, int maxWidth, Style style) {
        WidthLimitingVisitor widthLimitingVisitor = new WidthLimitingVisitor(maxWidth);
        TextVisitFactory.visitForwards(text, style, widthLimitingVisitor);
        widthLimitingVisitor.done();
        return widthLimitingVisitor.getLength();
    }

    @Override
    public int getLimitedStringLength(String text, int maxWidth, Style style) {
        WidthLimitingVisitor widthLimitingVisitor = new WidthLimitingVisitor(maxWidth);
        TextVisitFactory.visitFormatted(text, style, widthLimitingVisitor);
        widthLimitingVisitor.done();
        return widthLimitingVisitor.getLength();
    }

    @Nullable
    public Style getStyleAt(StringVisitable text, int x) {
        WidthLimitingVisitor widthLimitingVisitor = new WidthLimitingVisitor((float) x);
        return text.visit(
                           (style, textx) -> {
                               TextVisitFactory.visitFormatted(textx, style, widthLimitingVisitor);
                               return widthLimitingVisitor.done() ? Optional.empty() : Optional.of(style);
                           }, Style.EMPTY
                   )
                   .orElse(null);
    }

    @Nullable
    public Style getStyleAt(OrderedText text, int x) {
        WidthLimitingVisitor widthLimitingVisitor = new WidthLimitingVisitor(x);
        text.accept(widthLimitingVisitor);
        widthLimitingVisitor.done();
        return widthLimitingVisitor.lastValidStyle;
    }

    public StringVisitable trimToWidth(StringVisitable text, int width, Style style) {
        TextCollector collector = new TextCollector();
        final WidthLimitingVisitor widthLimitingVisitor = new WidthLimitingVisitor(width) {

            private Style previousStyle = null;
            private StringBuilder previous = new StringBuilder();

            @Override
            public boolean accept(Visited visited) {
                GlyphInfo lig = ((LigatureFontStorage) renderer.getFontStorage(visited.style().getFont())).getGlyphInfo(
                        this, renderer.validateAdvance);
                String toAdd = Arrays.stream(lig.codepoints())
                                     .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                     .toString();
                if (super.accept(visited)) {
                    if (visited.style() != previousStyle) {
                        if (!previous.isEmpty()) {
                            collector.add(StringVisitable.styled(previous.toString(), previousStyle));
                            previous = new StringBuilder();
                        }
                        previousStyle = visited.style();
                    }
                    previous.append(toAdd);
                } else {
                    if (!previous.isEmpty()) {
                        collector.add(StringVisitable.styled(previous.toString(), previousStyle));
                    }
                    return false;
                }
                return true;
            }
        };
        GlyphVisitFactory.visit(text, widthLimitingVisitor);
        return collector.getCombined();
    }

    public List<TextHandler.MatchResult> getStyleMatchResults(OrderedText text, Predicate<Style> stylePredicate) {
        StylePredicateVisitor stylePredicateVisitor = new StylePredicateVisitor(stylePredicate);
        text.accept(stylePredicateVisitor);
        stylePredicateVisitor.done();
        return stylePredicateVisitor.getResults();
    }

    public void wrapLines(
            String text, int maxWidth, Style resetStyle, boolean retainTrailingWordSplit, TextHandler.LineWrappingConsumer consumer
    ) {
        int startingIndex = 0;
        int length = text.length();

        LineBreakingVisitor lineBreakingVisitor;
        for (Style startingStyle = resetStyle; startingIndex < length; startingStyle = lineBreakingVisitor.getEndingStyle()) {
            lineBreakingVisitor = new LineBreakingVisitor(maxWidth);
            boolean broken = GlyphVisitFactory.visit(renderer, text, startingIndex, startingStyle, resetStyle, lineBreakingVisitor);
            if (broken) {
                consumer.accept(startingStyle, startingIndex, length);
                break;
            }

            int lastBreak = lineBreakingVisitor.getEndingIndex();
            char characterAtBreak = text.charAt(lastBreak);
            int trailingWord = (characterAtBreak != '\n' && characterAtBreak != ' ') ? lastBreak : lastBreak + 1;
            consumer.accept(startingStyle, startingIndex, retainTrailingWordSplit ? trailingWord : lastBreak);
            startingIndex = trailingWord;
        }
    }

    public void wrapLines(StringVisitable text, int maxWidth, Style style, BiConsumer<StringVisitable, Boolean> lineConsumer) {
        List<TextHandler.StyledString> list = Lists.newArrayList();
        text.visit((stylex, textx) -> {
            if (!textx.isEmpty()) {
                list.add(new TextHandler.StyledString(textx, stylex));
            }

            return Optional.empty();
        }, style);
        LineWrappingCollector lineWrappingCollector = new LineWrappingCollector(list);
        boolean bl = true;
        boolean bl2 = false;
        boolean bl3 = false;

        while(bl) {
            bl = false;
            LineBreakingVisitor lineBreakingVisitor = new LineBreakingVisitor((float)maxWidth);

            for (TextHandler.StyledString styledString : lineWrappingCollector.parts) {
                boolean bl4 = GlyphVisitFactory.visit(renderer, styledString.literal, 0, styledString.style, style, lineBreakingVisitor);
                if (!bl4) {
                    int i = lineBreakingVisitor.getEndingIndex();
                    Style style2 = lineBreakingVisitor.getEndingStyle();
                    char c = lineWrappingCollector.charAt(i);
                    boolean bl5 = c == '\n';
                    boolean bl6 = bl5 || c == ' ';
                    bl2 = bl5;
                    StringVisitable stringVisitable = lineWrappingCollector.collectLine(i, bl6 ? 1 : 0, style2);
                    lineConsumer.accept(stringVisitable, bl3);
                    bl3 = !bl5;
                    bl = true;
                    break;
                }

                lineBreakingVisitor.offset(styledString.literal.length());
            }
        }

        StringVisitable stringVisitable2 = lineWrappingCollector.collectRemainers();
        if (stringVisitable2 != null) {
            lineConsumer.accept(stringVisitable2, bl3);
        } else if (bl2) {
            lineConsumer.accept(StringVisitable.EMPTY, false);
        }
    }


    private class StylePredicateVisitor extends ContextualCharacterVisitor {
        private final Predicate<Style> stylePredicate;
        private float totalWidth;
        private final ImmutableList.Builder<MatchResult> results = ImmutableList.builder();
        private float styleStartWidth;
        private boolean lastTestResult;

        private StylePredicateVisitor(Predicate<Style> stylePredicate) {
            this.stylePredicate = stylePredicate;
        }

        @Override
        public boolean accept(Visited visited) {
            boolean bl = this.stylePredicate.test(visited.style());
            if (this.lastTestResult != bl) {
                if (bl) {
                    this.onStyleMatchStart();
                } else {
                    this.onStyleMatchEnd();
                }
            }

            this.totalWidth += getWidth(this, true);
            return true;
        }

        private void onStyleMatchStart() {
            this.lastTestResult = true;
            this.styleStartWidth = this.totalWidth;
        }

        private void onStyleMatchEnd() {
            float f = this.totalWidth;
            this.results.add(new TextHandler.MatchResult(this.styleStartWidth, f));
            this.lastTestResult = false;
        }

        public List<MatchResult> getResults() {
            if (this.lastTestResult) {
                this.onStyleMatchEnd();
            }

            return this.results.build();
        }
    }


    public class WidthLimitingVisitor extends ContextualCharacterVisitor {
        private float widthLeft;
        private int length;
        private Style lastValidStyle = null;

        public WidthLimitingVisitor(float maxWidth) {
            this.widthLeft = maxWidth;
        }

        @Override
        public boolean accept(Visited visited) {
            LigatureFontStorage lig = (LigatureFontStorage) renderer.getFontStorage(visited.style().getFont());
            GlyphInfo<?> glyph = lig.getGlyphInfo(this, renderer.validateAdvance);
            this.widthLeft -= glyph.getAdvance(visited.style().isBold());
            if (this.widthLeft >= 0.0F) {
                this.length = visited.index() + glyph.charLength();
                lastValidStyle = visited.style();
                return true;
            } else {
                return false;
            }
        }

        public int getLength() {
            return this.length;
        }

        public void resetLength() {
            this.length = 0;
        }
    }

    @Environment(EnvType.CLIENT)
    public class LineBreakingVisitor implements GlyphVisitable {
        private final float maxWidth;
        private int endIndex = -1;
        private Style endStyle = Style.EMPTY;
        private boolean nonEmpty;
        private float totalWidth;
        private int lastSpaceBreak = -1;
        private Style lastSpaceStyle = Style.EMPTY;
        private int count;
        private int startOffset;

        public LineBreakingVisitor(float maxWidth) {
            this.maxWidth = Math.max(maxWidth, 1.0F);
        }

        @Override
        public boolean accept(int index, int characterIndex, Style style, GlyphInfo glyph) {
            int i = characterIndex + this.startOffset;
            switch (glyph.codepoints()[0]) {
                case 10: // Line feed character
                    return this.breakLine(i, style);
                case 32: // Space
                    this.lastSpaceBreak = i;
                    this.lastSpaceStyle = style;
                default:
                    float f = glyph.getAdvance(style.isBold());
                    this.totalWidth += f;
                    if (!this.nonEmpty || !(this.totalWidth > this.maxWidth)) {
                        this.nonEmpty |= f != 0.0F;
                        this.count = i + glyph.charLength();
                        return true;
                    } else {
                        return this.lastSpaceBreak != -1 ? this.breakLine(this.lastSpaceBreak, this.lastSpaceStyle)
                                                         : this.breakLine(i, style);
                    }
            }
        }

        private boolean breakLine(int finishIndex, Style finishStyle) {
            this.endIndex = finishIndex;
            this.endStyle = finishStyle;
            return false;
        }

        private boolean hasLineBreak() {
            return this.endIndex != -1;
        }

        public int getEndingIndex() {
            return this.hasLineBreak() ? this.endIndex : this.count;
        }

        public Style getEndingStyle() {
            return this.endStyle;
        }

        public void offset(int extraOffset) {
            this.startOffset += extraOffset;
        }
    }

}
