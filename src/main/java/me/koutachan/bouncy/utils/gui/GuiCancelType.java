package me.koutachan.bouncy.utils.gui;

public enum GuiCancelType {
    /**
     * 全てのクリックをキャンセルします
     * <li>GUIを操作しているプレイヤーのインベントリーも操作できなくなります</li>
     */
    ALL,
    /**
     * GUIの操作に関する場合のみクリックをキャンセルします
     * <li>GUIのアイテム・イベントリーが操作された場合にのみキャンセルします</li>
     */
    GUI,
    /**
     * 未定義
     * <li>全てのクリックをキャンセルしません</li>
     */
    UNDEFINED
}