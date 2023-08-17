package me.kingc.votekick.commands;

import me.kingc.votekick.VoteKick;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class vote implements CommandExecutor {
    int votes = 0;
    Player kick_player;
    boolean is_voting = false;
    TimerTask timerTask;
    ArrayList<Player> voted_players = new ArrayList<>();

    Plugin config = me.kingc.votekick.VoteKick.getProvidingPlugin(me.kingc.votekick.VoteKick.class);
    String prefix = Objects.requireNonNull(config.getConfig().getString("prefix")).replace("&", "§");
    private double percent(int n, int m) {
        double result = (double) n / m * 100;
        DecimalFormat d = new DecimalFormat("#.##");
        return Double.parseDouble(d.format(result));
    }


    private class TimerTask extends BukkitRunnable {
        int seconds = config.getConfig().getInt("seconds");
        List<Integer> list = new ArrayList<>();

        public TimerTask() {
            for (int i = seconds - 10; i >= 10; i -= 10) {
                list.add(i);
            }
        }

        @Override
        public void run() {
            seconds--;
            if (seconds == 0) {
                int onlinePlayers = Bukkit.getServer().getOnlinePlayers().size();
                double p = percent(votes, onlinePlayers);
                if (p > config.getConfig().getDouble("kick_percent")) {
                    kick_player.kickPlayer(prefix + "§c您已被投票踢出");
                    Bukkit.broadcastMessage(prefix + "§a到达踢出百分比，已踢出玩家 " + kick_player.getName());
                } else {
                    Bukkit.broadcastMessage(prefix + "§c未到达踢出百分比，玩家不会被踢出");
                }
                votes = 0;
                is_voting = false;
                kick_player = null;
                voted_players.clear();
                this.cancel();
            } else {
                for (Integer integer : list) {
                    if (seconds == integer) {
                        Bukkit.broadcastMessage(prefix + "§6还有 §c" + seconds + " §6秒对玩家 §c" + kick_player.getName() + " §6的踢出投票结束");
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        String help_msg_op = "§7---------- §6VoteKick §7----------\n" +
                "§6/vote start <玩家名> §7开启踢出玩家的投票\n" +
                "§6/vote stop §7强制关闭当前投票\n" +
                "§6/vote reload §7重新加载配置文件\n" +
                "§6/vote §7投票踢出玩家\n" +
                "§6/vote help §7查看帮助\n" +
                "§7---------- §6VoteKick §7----------";
        String help_msg = "§7---------- §6VoteKick §7----------\n" +
                "§6/vote start <玩家名> §7开启踢出玩家的投票\n" +
                "§6/vote §7投票踢出玩家\n" +
                "§6/vote help §7查看帮助\n" +
                "§7---------- §6VoteKick §7----------";

        if (args.length == 0) {
            if (sender.hasPermission("votekick.vote")) {
                if (is_voting) {
                    if (!voted_players.contains((Player) sender)) {
                        votes = votes + 1;
                        voted_players.add((Player) sender);
                        sender.sendMessage(prefix + "§a投票成功！当前票数: " + votes);
                    } else {
                        sender.sendMessage(prefix + "§c你已经投过票了");
                    }
                } else {
                    sender.sendMessage(prefix + "§c没有正在进行的投票");
                }
            } else {
                sender.sendMessage(prefix + "§c你没有权限");
            }
        } else if (args.length == 1) {
            if (args[0].equals("reload")) {
                if (sender.hasPermission("votekick.reload")) {
                    config.reloadConfig();
                    sender.sendMessage(prefix + "§a已重新加载配置文件");
                } else {
                    sender.sendMessage(prefix + "§c你没有权限");
                }
            } else if (args[0].equals("help")) {
                if (sender.isOp()) {
                    sender.sendMessage(help_msg_op);
                } else {
                    sender.sendMessage(help_msg);
                }
            } else if (args[0].equals("stop")) {
                if (sender.hasPermission("votekick.stop")) {
                    if (is_voting) {
                        kick_player = null;
                        is_voting = false;
                        votes = 0;
                        voted_players.clear();
                        timerTask.cancel();
                        sender.sendMessage(prefix + "§a已关闭投票");
                    } else {
                        sender.sendMessage(prefix + "§c没有正在进行的投票");
                    }
                } else {
                    sender.sendMessage(prefix + "§c你没有权限");
                }
            }
        } else if (args.length == 2) {
            if (args[0].equals("start")) {
                if (sender.hasPermission("votekick.start")) {
                    if (Bukkit.getPlayer(args[1]) != null) {
                        Player player = Bukkit.getPlayer(args[1]);
                        if (!is_voting) {
                            if (player.hasPermission("votekick.bypass")) {
                                sender.sendMessage(prefix + "§c你不能踢出该玩家！");
                            } else {
                                is_voting = true;
                                kick_player = player;
                                // 创建计时器对象
                                timerTask = new TimerTask();

                                // 启动计时器，每秒执行一次
                                timerTask.runTaskTimer(VoteKick.getPlugin(VoteKick.class), 0L, 20L);
                                Bukkit.broadcastMessage(prefix + "§6有玩家发起了对 §c" + kick_player.getName() + " §6的踢出投票，如果您赞成，请输入 /vote");
                                sender.sendMessage(prefix + "§a已开始对 " + args[1] + " 的踢出投票");
                            }
                        } else {
                            sender.sendMessage(prefix + "§c正在进行一个投票");
                        }
                    } else {
                        sender.sendMessage(prefix + "§c未找到玩家");
                    }
                } else {
                    sender.sendMessage(prefix + "§c你没有权限");
                }
            }
        }
        return false;
    }
}
