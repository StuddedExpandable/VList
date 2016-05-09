package the.vox.mc.themasteredpanda.vlist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.google.common.base.Joiner;

import net.milkbowl.vault.chat.Chat;

public class vlist extends JavaPlugin implements Listener{

	ArrayList<String> staff = new ArrayList<String>();
	ArrayList<String> contributors = new ArrayList<String>();
	Chat chat = null;
	User user = null;
	IEssentials ess = null;
	public File configf;
	public YamlConfiguration config;
//	IEssentials ess = (IEssentials)vlist.this.getServer().getPluginManager().getPlugin("Essentials");	

	
	public boolean hasNickName(Player p) {
		return ((ess.getUser(p).getNickname() != null) && (!ess.getUser(p).getNickname().isEmpty()));
	}
	
	public void onEnable() {
		createConfig();
		setupChat();
		ess = (IEssentials)getServer().getPluginManager().getPlugin("Essentials");
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getLogger().info("[VList] is now enabled!");
	}
	public void onDisable() {
		Bukkit.getLogger().info("[Vlist] is now disabled.....cya!");
	}
	
	public void createConfig() {
		configf = new File(getDataFolder() + File.separator + "config.yml");
		config = YamlConfiguration.loadConfiguration(configf);
		
		boolean save = false;
		if(!configf.exists()) {
			List<String> defaultlist = new ArrayList<String>();
			defaultlist.add("&8&m---------------&8[&aOnline Players&8]&8&m---------------");
			defaultlist.add(" ");
			defaultlist.add("&7There is currently {onlineplayers} player(s) online");
			defaultlist.add("&6Staff Members &8{>>} &e{staff}");
			defaultlist.add("&6Contributors &8{>>} &e{contributors}");
			defaultlist.add(" ");
			defaultlist.add("&8&m--------------------------------------------");
			config.set("ListMessage", defaultlist);
			config.set("None", "&cNone");
			save = true;
		} 
		if (save) {
			try {
				config.save(configf);
				System.out.println("[VList] Creating config.yml...");
				System.out.println("[VList] Created config.yml!");
			} catch (IOException e) {
				System.out.println("[VList] Failed to save/create config.yml");
				System.out.println("[VList] Caused by: " + e.getMessage());	
			}
		}
	}
	
	/*EventHandler for Player's joining the server*/
	@EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if ((p.hasPermission("vox.list.staff")) && (!staff.contains(p.getName()))) {
            if (hasNickName(p)) {
                   staff.add(this.chat.getPlayerPrefix(p) + "~" + ess.getUser(p).getNickname());
            } else {
        staff.add(this.chat.getPlayerPrefix(p) + p.getName());
            }
            }
        if ((p.hasPermission("vox.list.architect")) && (!contributors.contains(p.getName()))) {
            if (hasNickName(p)) {
                   contributors.add(this.chat.getPlayerPrefix(p) + "~" + ess.getUser(p).getNickname());
            } else {
                contributors.add(this.chat.getPlayerPrefix(p) + p.getName());
            }
            }
	}
	/*EventHandler for Player's leaving the server*/	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
	    Player p = e.getPlayer();
        if (staff.contains(hasNickName(p))) {
       	 staff.remove(this.chat.getPlayerPrefix(p) + ess.getUser(p).getNickname());
        } else {
       	 staff.remove(this.chat.getPlayerPrefix(p) + p.getName());
        }
        if (contributors.contains(hasNickName(p))) {
       	 contributors.remove(this.chat.getPlayerPrefix(p) + ess.getUser(p).getNickname());
        } else {
       	 contributors.remove(this.chat.getPlayerPrefix(p) + p.getName());
        }
	}
	/*/list command*/
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("list")) {
			if (s instanceof Player) {
			for (String listMessage : getConfig().getStringList("ListMessage")) {
				s.sendMessage(ChatColor.translateAlternateColorCodes('&', listMessage
						.replace("{onlineplayers}", Integer.toString(Bukkit.getOnlinePlayers().size()))
						.replace("{staff}", getStaffMessage())
						.replace("{contributors}", getContributorMessage())
						.replace("{>>}", "»")));
			}
			}
		}
	return true;
	}
	/*Adding in the <name>, <name> feature (i.e. TheMasteredPanda, xExpandable, HoneyHax*/
	public String getStaffMessage() {
		if (staff.size() >= 1) {
			return Joiner.on(ChatColor.YELLOW + ", ").join(staff);
		}
		return getConfig().getString("None");
	}
	public String getContributorMessage() {
		if (contributors.size() >= 1) {
			return Joiner.on(ChatColor.YELLOW + ", ").join(contributors);
		}
		return getConfig().getString("None");
	}
	
	public boolean onVanishPreCommand(PlayerCommandPreprocessEvent e) {
		System.out.println("DB: PlayerCommandPreProcessEvent for onVanishCommand fired");
		Player p = e.getPlayer();
		if (e.getMessage().contains("/v") || (e.getMessage().contains("/vanish"))) {
		System.out.println("DB: Checked /v command - Success");
			if (user.isHidden()) {
				System.out.println("DB: Before removal of players in staff arraylist " + staff);
			     if (staff.contains(hasNickName(p))) {
			       	 staff.remove(this.chat.getPlayerPrefix(p) + ess.getUser(p).getNickname());
			        } else {
			       	 staff.remove(this.chat.getPlayerPrefix(p) + p.getName());
			        }
			        System.out.println("DB: After removal " + staff);
					System.out.println("DB: Before removal of players in contrbutor arraylist " + contributors);
			     if (contributors.contains(hasNickName(p))) {
			         contributors.remove(this.chat.getPlayerPrefix(p) + ess.getUser(p).getNickname());
			        } else {
			         contributors.remove(this.chat.getPlayerPrefix(p) + p.getName());
			        }   
			     }
		     System.out.println("DB: After removal " + contributors);
			} else {
				System.out.println("DB: Before adding of players in staff arraylist " + staff);
		        if ((p.hasPermission("vox.list.staff")) && (!staff.contains(p.getName()))) {
		            if (hasNickName(p)) {
		                   staff.add(this.chat.getPlayerPrefix(p) + "~" + ess.getUser(p).getNickname());
		            } else {
		        staff.add(this.chat.getPlayerPrefix(p) + p.getName());
		            }
		            }
			     System.out.println("DB: After adding " + staff);
				 System.out.println("DB: Before adding of players in contrbutor arraylist " + contributors);
		        if ((p.hasPermission("vox.list.contributor")) && (!contributors.contains(p.getName()))) {
		            if (hasNickName(p)) {
		                   contributors.add(this.chat.getPlayerPrefix(p) + "~" + ess.getUser(p).getNickname());
		            } else {
		                contributors.add(this.chat.getPlayerPrefix(p) + p.getName());
		            }
		            }
			     System.out.println("DB: After removal " + contributors);
			}
		return true;
		}
	
	/*Implementation for getting the prefix (I think)*/
	public boolean setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
	if (chatProvider != null) {
		this.chat = ((Chat)chatProvider.getProvider());
	    }
		return this.chat != null;
	}
}