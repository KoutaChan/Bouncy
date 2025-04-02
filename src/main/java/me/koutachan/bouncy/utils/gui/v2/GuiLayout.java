package me.koutachan.bouncy.utils.gui.v2;

import com.google.common.base.Preconditions;
import me.koutachan.bouncy.utils.gui.GuiBase;
import me.koutachan.bouncy.utils.gui.button.AbstractGuiButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class GuiLayout<T extends GuiBase<T>> {
    private final GuiBase<T> gui;
    private final List<String> layout;
    private final Map<String, AbstractGuiButton<?>> shape = new HashMap<>();

    public GuiLayout(GuiBase<T> gui, String shape) {
        this.gui = gui;
        this.layout = Arrays.stream(shape.split("\n"))
                .flatMap(row -> Arrays.stream(row.trim().split(" ")))
                .toList();
        Preconditions.checkArgument(layout.size() == gui.getSize());
    }

    public List<String> getLayout() {
        return layout;
    }

    public GuiLayout<T> set(String type, AbstractGuiButton<?> button) {
        shape.put(type, button);
        return this;
    }

    public T update() {
        for (int i = 0; i < layout.size(); i++) {
            gui.setUnsafeButton(i, shape.get(layout.get(i)));
        }
        return (T) gui;
    }

    public T updateLazy() {
        for (int i = 0; i < layout.size(); i++) {
            AbstractGuiButton<?> guiButton = shape.get(layout.get(i));
            if (guiButton != null) {
                gui.setUnsafeButton(i, guiButton);
            }
        }
        return (T) gui;
    }
}