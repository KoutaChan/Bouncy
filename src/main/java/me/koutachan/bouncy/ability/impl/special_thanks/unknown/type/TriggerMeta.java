package me.koutachan.bouncy.ability.impl.special_thanks.unknown.type;

public interface TriggerMeta {
    TriggerMeta EMPTY = new TriggerMeta() {
        @Override
        public String toString() {
            return "EmptyTrigger";
        }
    };
}