package com.space.model;

public enum ShipType {
    TRANSPORT,
    MILITARY,
    MERCHANT;

    public static boolean isMember(String name) {
        ShipType[] ships = ShipType.values();
        for (ShipType ship : ships)
            if (ship.name().equals(name))
                return true;
        return false;
    }
}
