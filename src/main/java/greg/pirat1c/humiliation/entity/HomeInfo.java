package greg.pirat1c.humiliation.entity;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Objects;

public class HomeInfo extends Object{

    private String playerId;
    private Location playerHome;
    private boolean isPlayerGay;

    public HomeInfo() {

    }

    public HomeInfo(String playerId, Location playerHome, boolean isPlayerGay) {
        this.playerId = playerId;
        this.playerHome = playerHome;
        this.isPlayerGay = isPlayerGay;
    }

    public HomeInfo(Location location) {
        this.playerHome = location;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Location getPlayerHome() {
        return playerHome;
    }

    public void setPlayerHome(Location playerHome) {
        this.playerHome = playerHome;
    }

    public boolean isPlayerGay() {
        return isPlayerGay;
    }

    public void setPlayerGay(boolean playerGay) {
        isPlayerGay = playerGay;
    }

    @Override
    public String toString() {
        return "HomeInfo{" +
                "playerId='" + playerId + '\'' +
                ", playerHome=" + playerHome +
                ", isPlayerGay=" + isPlayerGay +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HomeInfo homeInfo = (HomeInfo) o;
        return isPlayerGay == homeInfo.isPlayerGay && Objects.equals(playerId, homeInfo.playerId) && Objects.equals(playerHome, homeInfo.playerHome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, playerHome, isPlayerGay);
    }
}
