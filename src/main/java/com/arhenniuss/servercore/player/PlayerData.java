package com.arhenniuss.servercore.player;

import com.arhenniuss.servercore.element.Element;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private Element element;

    public PlayerData(UUID uuid, Element element) {
        this.uuid = uuid;
        this.element = element;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public boolean hasElement() {
        return element != null;
    }
}
