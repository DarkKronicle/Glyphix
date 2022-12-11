package io.github.darkkronicle.glyphix.text;

import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class ContextualCharacterVisitor implements CharacterVisitor {

    private final List<Visited> visited = new ArrayList<>();
    private int currentIndex = 0;

    public void done() {
        while (currentIndex < visited.size()) {
            Visited v = visited.get(currentIndex);
            if (!accept(v)) {
                return;
            }
            currentIndex++;
        }
    }

    public abstract boolean accept(Visited visited);

    public Optional<Visited> getNext(boolean skip) {
        if (currentIndex >= visited.size()) {
            return Optional.empty();
        }
        if (skip) {
            skip();
        }
        return Optional.of(visited.get(currentIndex + 1));
    }

    public int size() {
        return visited.size();
    }

    public Visited get(int index) {
        if (index >= visited.size()) {
            return null;
        }
        return visited.get(index);
    }

    public List<Visited> getNextWhileFiltered(boolean skip, Function<Visited, Boolean> filter) {
        return getNextWhileFiltered(skip, -1, filter);
    }

    public List<Visited> getNextWhileFiltered(boolean skip, int max, Function<Visited, Boolean> filter) {
        List<Visited> next = new ArrayList<>();
        int current = 0;
        while (currentIndex < visited.size() - 1 && filter.apply(get(currentIndex + 1))) {
            next.add(get(currentIndex + 1));
            if (skip) {
                skip();
            }
            current += 1;
            if (current >= max) {
                break;
            }
        }
        return next;
    }

    public void skip() {
        currentIndex += 1;
    }

    public void back() {
        currentIndex -= 1;
    }

    @Override
    public boolean accept(int index, Style style, int codePoint) {
        // Compile list of everything
        visited.add(new Visited(index, style, codePoint));
        return true;
    }

    public record Visited(int index, Style style, int codepoint) {

    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public Visited current() {
        return get(currentIndex);
    }

    public void skip(int amount) {
        currentIndex += amount;
    }
}
