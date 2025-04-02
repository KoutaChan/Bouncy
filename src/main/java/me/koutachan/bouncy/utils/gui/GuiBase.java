package me.koutachan.bouncy.utils.gui;

import me.koutachan.bouncy.utils.ItemCreator;
import me.koutachan.bouncy.utils.gui.button.AbstractGuiButton;
import me.koutachan.bouncy.utils.gui.button.impl.ItemButton;
import me.koutachan.bouncy.utils.gui.v2.GuiLayout;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.HashMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public class GuiBase<T extends GuiBase<T>> extends GuiHolder {
    private final static ItemButton<?> EMPTY_GLASS_BUTTON = new ItemButton<>(ItemCreator.of(Material.GRAY_STAINED_GLASS_PANE)
            .setDisplayName("&0")
            .setCustomModelData(0)
            .create());

    protected final HashMap<Integer, AbstractGuiButton<?>> buttons = new HashMap<>();

    protected GuiCancelType cancelType = GuiCancelType.GUI;
    protected boolean preventDragClick;
    protected long lastClick;

    public GuiBase(InventoryType type) {
        this.inventory = Bukkit.createInventory(this, type);
    }

    public GuiBase(InventoryType type, String title) {
        this.inventory = Bukkit.createInventory(this, type, title);
    }

    public GuiBase(int size) {
        this.inventory = Bukkit.createInventory(this, size);
    }

    public GuiBase(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    public void onGuiClick(InventoryClickEvent event) {
        if (!event.isCancelled()) {
            event.setCancelled(switch (cancelType) {
                case ALL -> true;
                case GUI -> event.isShiftClick() || (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof GuiHolder);
                default -> false;
            });
        }
        if (event.isShiftClick() && preventDragClick && System.currentTimeMillis() - lastClick < 50) {
            return;
        }
        lastClick = System.currentTimeMillis();

        int rawSlot = event.getRawSlot();
        if (rawSlot > 0) {
            AbstractGuiButton<T> button = getButton(rawSlot);
            if (button != null) {
                button.onClick(event, (T) this);
            }
        }
    }

    @Override
    public void onGuiDrag(InventoryDragEvent event) {
        if (!event.isCancelled()) {
            event.setCancelled(switch (cancelType) {
                case ALL, GUI -> true;
                default -> false;
            });
        }
    }

    @Override
    public void onGuiClose(InventoryCloseEvent event) {
        for (AbstractGuiButton<?> button : buttons.values()) {
            button.onRemove();
        }
    }

    public GuiBase<T> getHolder() {
        return this;
    }

    public boolean isRange(int rawSlot) {
        return rawSlot >= 0 && rawSlot < inventory.getSize();
    }

    public HashMap<Integer, AbstractGuiButton<?>> getButtons() {
        return buttons;
    }

    public AbstractGuiButton<T> getButton(int index) {
        return (AbstractGuiButton<T>) buttons.get(index);
    }

    public GuiLayout<T> setLayout(String layout) {
        return new GuiLayout<>(this, layout);
    }

    public void setButton(int index, AbstractGuiButton<T> button) {
        setUnsafeButton(index, button);
    }

    public void setUnsafeButton(int index, AbstractGuiButton<?> button) {
        if (button == null) {
            AbstractGuiButton<?> old = buttons.remove(index);
            if (old != null) {
                old.onRemove();
            }
            inventory.setItem(index, null);
            return;
        }
        button.onAdd();
        inventory.setItem(index, button.getIcon());
        AbstractGuiButton<?> old = buttons.put(index, button);
        if (old != null) {
            old.onRemove();
        }
    }

    public void updateIcon(AbstractGuiButton<?> button) {
        buttons.entrySet().stream()
                .filter(entry -> entry.getValue() == button)
                .findFirst()
                .ifPresent(entry -> inventory.setItem(entry.getKey(), button.getIcon()));
    }

    public void updateIcons(Class<?> clazz) {
        buttons.entrySet().stream()
                .filter(entry -> clazz.isAssignableFrom(entry.getValue().getClass()))
                .forEach(entry -> inventory.setItem(entry.getKey(), entry.getValue().getIcon()));
    }

    public boolean isPreventDragClick() {
        return preventDragClick;
    }

    public void setPreventDragClick(boolean preventDragClick) {
        this.preventDragClick = preventDragClick;
    }

    public GuiCancelType getCancelType() {
        return cancelType;
    }

    protected static ItemButton<?> createGlassButton() {
        return EMPTY_GLASS_BUTTON;
    }

    public void setCancelType(GuiCancelType cancelType) {
        this.cancelType = cancelType;
    }
}