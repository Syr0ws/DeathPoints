package com.github.syr0ws.model;

/**
 * Represents a player's death point.
 *
 * @param markerId the marker id on the player's HUD
 * @param day      the day of the player death
 * @param x        the x coordinate of the player death
 * @param y        the y coordinate of the player death
 * @param z        the z coordinate of the player death
 */
public record DeathPoint(String markerId, int day, double x, double y, double z) {

}
