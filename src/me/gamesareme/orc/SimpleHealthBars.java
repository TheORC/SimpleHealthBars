package me.gamesareme.orc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.java.JavaPlugin;

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

public class SimpleHealthBars extends JavaPlugin implements Listener {

	//All mobs with health bars in the world
    private Map<UUID, Bar> mobs = new HashMap<>();

    //Look up table for each mob type health bar height
    private Map<EntityType, Integer> mobheight = new HashMap<EntityType, Integer>();

    private boolean cnvfix = false;
    private String healthBarType = "{heartbar}";
    
  //Get a reference to the server, whichever way you can
     
    ConsoleCommandSender console = this.getServer().getConsoleSender();
     
    
    
    //On the server start
    public void onEnable() {
        
        //saveDefaultConfig();
        
        getConfig().options().copyDefaults(true);
        saveConfig();
        
        
        ConfigurationSection heightsection = getConfig().getConfigurationSection("mobheight");
        
        if (heightsection != null) {
            for (String type : heightsection.getKeys(false)) {
            	
                try {
                    mobheight.put(EntityType.valueOf(type.toUpperCase()), heightsection.getInt(type));
                } catch (IllegalArgumentException e) {
                	//Send the console something!
                    console.sendMessage(ChatColor.YELLOW + "[Warning]" + ChatColor.DARK_PURPLE + "[SimpleHealthBars] " + ChatColor.DARK_GRAY + type + " is not a valid Bukkit EntityType! Couldn't load height for this mob!");
                }
                
            }
        }
        
        healthBarType = "{" + getConfig().getString("barType") + "}";
        
        //Load all previous entities
        List<String> uuids = getConfig().getStringList("moblist");
        
        if (uuids != null) {
            for (String id : uuids) {
            	loadBar(UUID.fromString(id), healthBarType);	
            }
        }
        
        cnvfix = getConfig().getBoolean("CustomNameVisibleFix");
        
        Bukkit.getPluginManager().registerEvents(this, this);
        
        console.sendMessage(ChatColor.DARK_PURPLE + "[SimpleHealthBars] " + ChatColor.DARK_GRAY + "Enabled and running");

    }

    //On the server close
    public void onDisable() {
    	
    	//Remove the old mob list
        getConfig().set("moblist", null);
        
        //Save new mob list values
        List<String> uuids = new ArrayList<String>();
        for (UUID id : mobs.keySet()) {
        	uuids.add(id.toString());	
        }
        
        getConfig().set("moblist", uuids);
        
        saveConfig();
        
    }
    


    //On mob spawn event
    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
    	
    	//Make sure entity is a living entity
    	if(!(event.getEntity() instanceof LivingEntity))
    		return;
    	
        LivingEntity e = event.getEntity();
        
        //Don't spawn for special cases
        if(e.getType() == EntityType.ENDER_DRAGON || e.getType() == EntityType.WITHER)
        	return;
        
        //Load the bar
        loadBar(e.getUniqueId(), healthBarType);
        
        //getLogger().severe("Spawned: " + e.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        
        //Set the bar value
        setBar(e, (int) (e.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())); // getMaxHealth() + getHealth()));
        
    }

    //On mob take damage
    @EventHandler
    public void onMobDamaged(EntityDamageEvent event) {
        
    	if (!(event.getEntity() instanceof LivingEntity))
        	return;
        	
    	LivingEntity e = (LivingEntity) event.getEntity();
    	
    	 //Don't spawn for special cases
        if(e.getType() == EntityType.ENDER_DRAGON || e.getType() == EntityType.WITHER)
        	return;
    	
        //Load bar to check if it is in uuids list
        loadBar(e.getUniqueId(), healthBarType);
        
        //Update the health bar
        setBar(e, e.getHealth() - event.getDamage()) ; // - event.getDamage()));
        
        
    }

    @EventHandler
    public void onMobHeal(EntityRegainHealthEvent event) {
    	
    	if(!(event.getEntity() instanceof LivingEntity))
    		return;
    	
		LivingEntity e = (LivingEntity) event.getEntity();
		
		//Don't update for special cases
		if(e.getType() == EntityType.ENDER_DRAGON || e.getType() == EntityType.WITHER)
			return;
		
		//Load bar to check if it is in uuids list
		loadBar(e.getUniqueId(), healthBarType);
		
		//Update the health bar
		setBar(e, e.getHealth() + event.getAmount());
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (mobs.containsKey(event.getEntity().getUniqueId())) {
            clearSnowballs(event.getEntity());

            mobs.remove(event.getEntity().getUniqueId());
        }
    }

    //Put a mob in the list of current mobs with a health bar
    private void loadBar(UUID id, String type) {
    	
    	//Make sure the same entity is not being added to the list twice.
    	if(mobs.containsKey(id)) {
    		return;
    	}
    	
    	BarType bType = BarType.GetType(type);
    	addBar(id, bType);
    	
    }

    //Add a bar to the entity with UUID of id.
    private void addBar(UUID id, BarType type) 
    {
    	mobs.put(id, new Bar(type));
    }

    /**
     * Set the bar above the head of an entity to the bars defined in the map
     * @param e      The entity
     * @param health The health to set the bar to
     */
    public void setBar(LivingEntity e, double health) {
    	
    	if (!mobs.containsKey(e.getUniqueId())) 
    	{
            console.sendMessage(ChatColor.YELLOW + "[Warning]" + ChatColor.DARK_PURPLE + "[SimpleHealthBars] " + ChatColor.DARK_GRAY + "Attempting to set the health bar on an living entity without one!");
    		return;
    	}
    	
        Bar b = mobs.get(e.getUniqueId());
        String name = "";
        int i;
        
        
        
        int maxHealth = (int) e.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        
        if (health < 0)
            health = 0;
        
        if(health > maxHealth)
        	health = maxHealth;
        
        switch(b.getType()) {
			case HEALTH:
				name = "" + (health / 2);
				break;
			case HEALTHPERCENTAGE:
				name = Integer.toString((int) (health / maxHealth));
				break;
			case HEALTHSHORT:
				name = ChatColor.RED + "" + (health / 2) + ChatColor.GRAY + "/" + Integer.toString(maxHealth / 2);
				break;
			case HEARTBAR:
				
				//getLogger().severe("Mob damaged: " + (health / 2));
				
				name = ChatColor.DARK_RED + "";
                
            	i = 0;
            	
            	while (i < (health / 2)) {
            		
            		if((i + 1) >= (health / 2)) {
            			if(health  % 2 == 0)
            				name += "â?¤";
            			else
            				name += ChatColor.RED + "â?¤";
            		}else {
            			name += "â?¤";
            		}
            		
            		i++;
                }
                
                if (health < maxHealth) {
                	
                	if(maxHealth <= 3)
                		i--;
                	
                	name += ChatColor.DARK_GRAY + "";
                    while (i < (maxHealth / 2)) {
                    	name += "â?¤";
                        i++;
                    }
                }
				
				break;
			case PIPEBAR:
				
				name = ChatColor.RED + "";
                
            	i = 0;
                while (i < health / 2) {
                    name += "|";
                    i++;
                }
                
                if (health < maxHealth) {
                    name += ChatColor.DARK_GRAY + "";
                    while (i < maxHealth / 2) {
                        name += "|";
                        i++;
                    }
                }
                
				break;
			default:
				name = ""; //No name
				break;
         
        }
        
        setNameTag(e, name);
    }

    public void setNameTag(LivingEntity e, String tag) {
        if (cnvfix && e.isCustomNameVisible()) {
            e.setCustomName(null);

            List<Entity> sb = e.getPassengers(); //Get a list of passengers
            
            //Get the top entity
            Entity top = null;
            
            if(sb.size() > 0) {
            	//There is something riding this entity
            	top = sb.get(sb.size() - 1); //Set the top to the last entity in the passenger list
            	
            	if(top.getType() != EntityType.SNOWBALL)
            	{
            		//Is not snowball
            		top = null;
            	}
            	
            }
            
            //Check to see if there is a snow ball
            if (top == null) {
                
            	Entity ridden = e;

                for (int i = 0; i < getMobHeight(e.getType()); i++) {
                    top = e.getWorld().spawnEntity(e.getLocation(), EntityType.SNOWBALL);
                    ridden.addPassenger(top);
                }

            }

            if (top != null) {
                top.setCustomNameVisible(true);
                top.setCustomName(tag);
            } else {
                console.sendMessage(ChatColor.RED + "[ERROR]" + ChatColor.DARK_PURPLE + "[SimpleHealthBars] " + ChatColor.DARK_GRAY + "Top entity is null! This shouldn't be possible to happen... please report that immediately! You can disable the CustonNameVisibleFix option in your config for now.");
                e.setCustomName(tag);
            }

        } else
            e.setCustomName(tag);
    }

    public int getMobHeight(EntityType et) {
        if (mobheight.containsKey(et) && mobheight.get(et) > 0)
            return mobheight.get(et);
        return 1;
    }

    public void clearSnowballs(Entity e) {
        if (cnvfix) {
            //e = e.getPassenger();
            List<Entity> passengers = e.getPassengers();
            for(int i = 0; i < passengers.size(); i++) {
            	e.removePassenger(passengers.get(i));
            }
            
            /*if (e != null) {
                while (e != null && e.getType() == EntityType.SNOWBALL) {
                    Entity remove = e;
                    e = e.getPassenger();
                    remove.remove();
                }
            }*/
        }
    }

    public int GetEntityMaxHealth(LivingEntity e) {
    	
    	
    	
    	
    	return 0;
    }
    
}
