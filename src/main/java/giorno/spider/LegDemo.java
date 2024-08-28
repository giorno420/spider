package giorno.spider;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class LegDemo implements CommandExecutor, Listener {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player player)){
            return true;
        }

        if (strings.length == 1){
            int segmentCount = Integer.parseInt(strings[0]);

            new Spider(player.getLocation(), segmentCount);

            player.sendRawMessage("spider spawned at your location");
            return true;
        }
        player.sendRawMessage("Usage: /spider <segments per leg>");
        return true;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){

    }

}
