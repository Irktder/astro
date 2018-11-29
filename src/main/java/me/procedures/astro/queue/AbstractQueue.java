package me.procedures.astro.queue;

import lombok.Getter;
import lombok.Setter;
import me.procedures.astro.AstroPlugin;
import me.procedures.astro.inventories.StateInventories;
import me.procedures.astro.ladder.Ladder;
import me.procedures.astro.match.Match;
import me.procedures.astro.player.PlayerProfile;
import me.procedures.astro.player.PlayerState;
import me.procedures.astro.utils.CC;
import me.procedures.astro.utils.GameUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.LinkedList;
import java.util.List;

@Getter @Setter
public abstract class AbstractQueue implements Listener {

    private final AstroPlugin plugin;
    private final Ladder ladder;

    private List<QueueData> queue = new LinkedList<>();

    private int playingAmount;

    public AbstractQueue(AstroPlugin plugin, Ladder ladder) {
        this.plugin = plugin;
        this.ladder = ladder;
    }

    public abstract void createMatch(QueueData playerOne, QueueData playerTwo);

    public abstract void startQueueTask();

    public void addToQueue(Player player) {
        PlayerProfile playerProfile = this.plugin.getProfileManager().getProfile(player);
        QueueData queueData = new QueueData(player);

        playerProfile.setQueue(this);
        playerProfile.setState(PlayerState.QUEUING);

        GameUtil.resetPlayer(player);

        player.getInventory().setContents(StateInventories.QUEUE.getContents());
        player.updateInventory();

        this.queue.add(queueData);

        player.sendMessage(this.plugin.getConfiguration().getMessages().getConfig().getString("queue.added")
                .replace("{ladder-name}", ChatColor.stripColor(this.ladder.getName())));

        /* int min = playerProfile.getRatings().get(this.ladder).getRating() - 250;
        int max = playerProfile.getRatings().get(this.ladder).getRating() + 250;

        queueData.setMin(min);
        queueData.setMax(max); */
    }

    public void removeFromQueue(Player player) {
        if (player.isOnline()) {
            PlayerProfile playerProfile = this.plugin.getProfileManager().getProfile(player);

            playerProfile.setState(PlayerState.LOBBY);
            player.getInventory().setContents(StateInventories.LOBBY.getContents());

            GameUtil.resetPlayer(player);

            player.sendMessage(this.plugin.getConfiguration().getMessages().getConfig().getString("queue.removed")
                    .replace("{ladder-name}", ChatColor.stripColor(this.ladder.getName())));
        }

        this.queue.remove(this.getQueueData(player));
    }

    public void handleMatch(Match match) {
        this.playingAmount -= match.getPlayers().size();
    }

    public QueueData getQueueData(Player player) {
        for (QueueData data : this.queue) {
            if (data.getPlayer() == player) {
                return data;
            }
        }

        return null;
    }
}
