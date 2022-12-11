package io.github.darkkronicle.glyphix.text;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.util.TextCollector;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GlyphixTextHandler extends TextHandler {

    public GlyphixTextHandler(GlyphixRenderer renderer) {
        super(null);
    }

    @Override
    public float getWidth(@Nullable String text) {
        return super.getWidth(text);
    }

    @Override
    public float getWidth(StringVisitable text) {
        return super.getWidth(text);
    }

    @Override
    public float getWidth(OrderedText text) {
        return super.getWidth(text);
    }

    @Override
    public String trimToWidthBackwards(String text, int maxWidth, Style style) {
        return super.trimToWidthBackwards(text, maxWidth, style);
    }

    class StylePredicateVisitor extends ContextualCharacterVisitor {
        private final Predicate<Style> stylePredicate;
        private float totalWidth;
        private final ImmutableList.Builder<MatchResult> results = ImmutableList.builder();
        private float styleStartWidth;
        private boolean lastTestResult;

        StylePredicateVisitor(Predicate<Style> stylePredicate) {
            this.stylePredicate = stylePredicate;
        }

        @Override
        public boolean accept(Visited visited) {
            boolean bl = this.stylePredicate.test(style);
            if (this.lastTestResult != bl) {
                if (bl) {
                    this.onStyleMatchStart();
                } else {
                    this.onStyleMatchEnd();
                }
            }

            this.totalWidth += TextHandler.this.widthRetriever.getWidth(j, style);
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

        public WidthLimitingVisitor(float maxWidth) {
            this.widthLeft = maxWidth;
        }

        @Override
        public boolean accept(Visited visited) {
            this.widthLeft -= TextHandler.this.widthRetriever.getWidth(j, style);
            if (this.widthLeft >= 0.0F) {
                this.length = i + Character.charCount(j);
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
    public class LineBreakingVisitor extends ContextualCharacterVisitor {
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
        public boolean accept(Visited visited) {
            int k = i + this.startOffset;
            switch(j) {
                case 10:
                    return this.breakLine(k, style);
                case 32:
                    this.lastSpaceBreak = k;
                    this.lastSpaceStyle = style;
                default:
                    float f = TextHandler.this.widthRetriever.getWidth(j, style);
                    this.totalWidth += f;
                    if (!this.nonEmpty || !(this.totalWidth > this.maxWidth)) {
                        this.nonEmpty |= f != 0.0F;
                        this.count = k + Character.charCount(j);
                        return true;
                    } else {
                        return this.lastSpaceBreak != -1 ? this.breakLine(this.lastSpaceBreak, this.lastSpaceStyle) : this.breakLine(k, style);
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

    public static class LineWrappingCollector {
        private final List<TextHandler.StyledString> parts;
        private String joined;

        public LineWrappingCollector(List<TextHandler.StyledString> parts) {
            this.parts = parts;
            this.joined = (String)parts.stream().map(part -> part.literal).collect(Collectors.joining());
        }

        public char charAt(int index) {
            return this.joined.charAt(index);
        }

        public StringVisitable collectLine(int lineLength, int skippedLength, Style style) {
            TextCollector textCollector = new TextCollector();
            ListIterator<TextHandler.StyledString> listIterator = this.parts.listIterator();
            int i = lineLength;
            boolean bl = false;

            while(listIterator.hasNext()) {
                TextHandler.StyledString styledString = listIterator.next();
                String string = styledString.literal;
                int j = string.length();
                if (!bl) {
                    if (i > j) {
                        textCollector.add(styledString);
                        listIterator.remove();
                        i -= j;
                    } else {
                        String string2 = string.substring(0, i);
                        if (!string2.isEmpty()) {
                            textCollector.add(StringVisitable.styled(string2, styledString.style));
                        }

                        i += skippedLength;
                        bl = true;
                    }
                }

                if (bl) {
                    if (i <= j) {
                        String string2 = string.substring(i);
                        if (string2.isEmpty()) {
                            listIterator.remove();
                        } else {
                            listIterator.set(new TextHandler.StyledString(string2, style));
                        }
                        break;
                    }

                    listIterator.remove();
                    i -= j;
                }
            }

            this.joined = this.joined.substring(lineLength + skippedLength);
            return textCollector.getCombined();
        }

        @Nullable
        public StringVisitable collectRemainers() {
            TextCollector textCollector = new TextCollector();
            this.parts.forEach(textCollector::add);
            this.parts.clear();
            return textCollector.getRawCombined();
        }
    }
}
