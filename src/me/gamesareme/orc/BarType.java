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

public enum BarType {
    HEARTBAR("heartbar"),
    PIPEBAR("pipebar"),
    HEALTHSHORT("healthshort"),
    HEALTH("health"),
    HEALTHPERCENTAGE("healthpercentage");
	
	
	private String name;
	
	private BarType(String name) {
		this.name = name;
	}
	
	public String GetName() {
		return this.name;
	}
	
	
	public static BarType GetType(String name) {
		switch(name) {
			case "heartbar":
				return BarType.HEARTBAR;
			case "pipebar":
				return BarType.PIPEBAR;
			case "healthshort":
				return BarType.HEALTHSHORT;
			case "health":
				return BarType.HEALTH;
			case "healthpercentage":
				return BarType.HEALTHPERCENTAGE;
		}
		
		return BarType.HEARTBAR;
	}
	
}
