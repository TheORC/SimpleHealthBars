package me.gamesareme.orc;

/*
 * SimpleHealthBars - Controls entity health bars bukkit 1.13.2.
 * Copyright (C) 2019 Oliver Clark (https://github.com/TheORC/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 */
public class Bar {
    
    private BarType type;

    public Bar(BarType type) {
        this.type = type;
    }

    public BarType getType() {
        return type;
    }
}
