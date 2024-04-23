package me.antonino.aacteams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Aacteams extends JavaPlugin implements Listener {
    private List<Team> teams;
    private Map<Player, Team> playerTeams;

    @Override
    public void onEnable() {
        teams = new ArrayList<>();
        playerTeams = new HashMap<>();
        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        String advancementId = event.getAdvancement().getKey().getKey();

        if (!advancementId.startsWith("minecraft:recipes/") && !advancementId.startsWith("minecraft:recipe/")) {
            // Suppose you have a method to get the team of a player
            Team team = playerTeams.get(player);

            if (team != null) {
                // Grant the advancement only to players in the same team
                for (Player teamPlayer : team.getMembers()) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "advancement grant " + teamPlayer.getName() + " only " + advancementId);
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + teamPlayer.getName() + " actionbar {\"text\":\"Succès débloqué\",\"color\":\"green\"}");
                }
            }
        }
    }

    // Command handler for creating, joining, and leaving teams
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("team")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Cette commande ne peut être exécutée que par un joueur.");
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 0) {
                // Display team info for the player
                displayTeamInfo(player);
                return true;
            }

            if (args[0].equalsIgnoreCase("create")) {
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Utilisation incorrecte. Usage: /team create <nom_de_l'équipe>");
                    return true;
                }

                String teamName = args[1];
                createTeam(player, teamName);
                return true;
            }

            if (args[0].equalsIgnoreCase("join")) {
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Utilisation incorrecte. Usage: /team join <nom_de_l'équipe>");
                    return true;
                }

                String teamName = args[1];
                joinTeam(player, teamName);
                return true;
            }

            if (args[0].equalsIgnoreCase("leave")) {
                leaveTeam(player);
                return true;
            }

            player.sendMessage(ChatColor.RED + "Commande inconnue.");
            return true;
        }

        return false;
    }

    // Display team info for the player
    private void displayTeamInfo(Player player) {
        Team team = playerTeams.get(player);
        if (team != null) {
            player.sendMessage(ChatColor.GREEN + "Vous êtes dans l'équipe: " + team.getName());
            player.sendMessage(ChatColor.GREEN + "Membres de l'équipe:");
            for (Player member : team.getMembers()) {
                player.sendMessage(ChatColor.GREEN + "- " + member.getName());
            }
        } else {
            player.sendMessage(ChatColor.GREEN + "Vous n'êtes dans aucune équipe.");
        }
    }

    // Create a new team
    private void createTeam(Player player, String name) {
        if (getTeamByName(name) != null) {
            player.sendMessage(ChatColor.RED + "Une équipe avec ce nom existe déjà.");
            return;
        }
        Team newTeam = new Team(name);
        newTeam.addMember(player);
        teams.add(newTeam);
        playerTeams.put(player, newTeam);
        player.sendMessage(ChatColor.GREEN + "Équipe créée avec succès.");
    }

    // Join a team
    private void joinTeam(Player player, String name) {
        Team team = getTeamByName(name);
        if (team == null) {
            player.sendMessage(ChatColor.RED + "L'équipe spécifiée n'existe pas.");
            return;
        }
        team.addMember(player);
        playerTeams.put(player, team);
        player.sendMessage(ChatColor.GREEN + "Vous avez rejoint l'équipe " + team.getName());
    }

    // Leave the current team
    private void leaveTeam(Player player) {
        Team team = playerTeams.get(player);
        if (team == null) {
            player.sendMessage(ChatColor.RED + "Vous n'êtes dans aucune équipe.");
            return;
        }
        team.removeMember(player);
        playerTeams.remove(player);
        player.sendMessage(ChatColor.GREEN + "Vous avez quitté l'équipe " + team.getName());
    }

    // Get a team by its name
    private Team getTeamByName(String name) {
        for (Team team : teams) {
            if (team.getName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return null;
    }

    private static class Team {
        private String name;
        private List<Player> members;

        public Team(String name) {
            this.name = name;
            this.members = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void addMember(Player player) {
            members.add(player);
        }

        public void removeMember(Player player) {
            members.remove(player);
        }

        public boolean isMember(Player player) {
            return members.contains(player);
        }

        public List<Player> getMembers() {
            return members;
        }
    }
}
