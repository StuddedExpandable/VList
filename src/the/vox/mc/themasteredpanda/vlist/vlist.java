package the.vox.mc.themasteredpanda.vlist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

//NOTE: By convention class names should start with an upper case letter
public class vlist extends JavaPlugin implements Listener {

	/*
	  tip: Since version 7 java has limited type inference for generic types
	  Also always try to program against an interface rather than the implementation
	  in this case if you don't functionality specific to the ArrayList use List
	  this make it easy to change the underlying type if you ever need/want to.
	  i.e. from ArrayList to LinkedList. Google for more info.
	 */
	//private List<String> staff = new ArrayList<>();

	//public ArrayList<String> staff = new ArrayList<String>();

	/*
	  BUT don't use a List for this xD use a Map
	  I've also opted to define this in the onEnable() method.
	 */
	private Map<UUID, String> staff = null;

	private Chat chat = null;
	//private User user = null; //not needed
	private IEssentials ess = null;

	//I always consider it good practice to initialize all my instance variables
	//to a default value when I declare them.
	private File configf = null;
	private YamlConfiguration config = null;
	//private File configf;
	//private YamlConfiguration config;

	/*
	  Also note I changed the access to private for all of the above instance variables.
	  Since you only need access to these variables from within this class it doesn't make
	  a huge difference, but its good practice to always follow anyway.
	  Use getter and setter methods to provide access to any variables you need access to outside of the
	  class they are declared in, this allows you to control exactly what users of your code, can and can
	  not do with the variable, and also allows you to do additional conversions, etc. if need be. Its also
	  just following the principals of OOP. Google Encapsulation. Keep your implementation details private.
	 */

	/*
	  Added @Override annotation to onEnable() and onDisable() this makes it clear
	  we are overriding these methods from anoter class.
	 */

	@Override
	public void onEnable() {
		createConfig();
		setupChat();
		ess = (IEssentials)getServer().getPluginManager().getPlugin("Essentials");
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Bukkit.getLogger().info("[VList] is now enabled!");

		staff = new HashMap<>();

		//NOTE: You are never registering your list command. Make sure its in your plugin.yml and then:
		getCommand("list").setExecutor(this);
	}

	@Override
	public void onDisable() {
		Bukkit.getLogger().info("[Vlist] is now disabled.....cya!");
	}

	/*
	  I have changed the access to all your below methods to private. If you were to need
	  access to one of the below methods from another class in your project, declare it as protected.
	  Only declare the methods as public if you intend user to access that method from outside your code.
	 */

	private boolean hasNickName(Player p) {
		String nickname = getNickname(p);
		return (nickname != null) && (!nickname.isEmpty());
	}

	//the below two methods are for convenience
	private String getNickname(Player player) {
		return ess.getUser(player).getNickname();
	}

	private String getPrefix(Player player) {
		return chat.getPlayerPrefix(player);
	}
	//

	private void createConfig() {
		configf = new File(getDataFolder() + File.separator + "config.yml");
		config = YamlConfiguration.loadConfiguration(configf);

		boolean save = false;
		if(!configf.exists()) {
			List<String> defaultlist = new ArrayList<String>();
			defaultlist.add("&8&m---------------&8[&aOnline Players&8]&8&m---------------");
			defaultlist.add(" ");
			defaultlist.add("&7There is currently {onlineplayers} player(s) online");
			defaultlist.add("&6Staff Members &8{>>} &e{staff}");
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
	private void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (p.hasPermission("vox.list.staff") && !staff.containsKey(p.getUniqueId())) {
			String title = getPrefix(p) + (hasNickName(p) ? "~" + getNickname(p) : p.getName());
			staff.put(p.getUniqueId(), title);
		}
	}

	/*EventHandler for Player's leaving the server*/	
	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		if (staff.containsKey(player.getUniqueId())) {
			staff.remove(player.getUniqueId());
		}
	}
	/*/list command*/
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("list")) {
			if (s instanceof Player) {
				for (String listMessage : getConfig().getStringList("ListMessage")) {
					s.sendMessage(ChatColor.translateAlternateColorCodes('&', listMessage
							.replace("{onlineplayers}", Integer.toString(Bukkit.getOnlinePlayers().size()))
							.replace("{staff}", getStaffMessage())
							.replace("{>>}", "»")));
				}
			}
		}
		return true;
	}
	/*Adding in the <name>, <name> feature (i.e. TheMasteredPanda, xExpandable, HoneyHax*/
	private String getStaffMessage() {
		if (staff.size() >= 1) {
			return Joiner.on(ChatColor.YELLOW + ", ").join(staff.values());
		}
		return getConfig().getString("None");
	}

	/*Implementation for getting the prefix (I think)*/
	private boolean setupChat() {
		RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
		if (chatProvider != null) {
			this.chat = ((Chat)chatProvider.getProvider());
		}
		return this.chat != null;
	}

	@EventHandler
	private boolean onPreCommand(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		User user = ess.getUser(p);
		if (e.getMessage().equalsIgnoreCase("/vanish") || e.getMessage().equalsIgnoreCase("/v")) {
			if (!user.isHidden(p)) {
				if (staff.containsKey(p.getUniqueId())) {
					staff.remove(p.getUniqueId());
				}
			} else {
				String title = getPrefix(p) + (hasNickName(p) ? "~" + getNickname(p) : p.getName());
				staff.put(p.getUniqueId(), title);
			}
		}
		return true;
	}

	//I see you are not using this code currently, but it has some invalid logic that needs to be fixed.
	private void checkList() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (staff.containsKey(p.getUniqueId())) {
				if (!p.hasPermission("vox.list.staff")) {
					staff.remove(p.getUniqueId());
				}
			} else {
				if (p.hasPermission("vox.list.staff")) {
					String title = getPrefix(p) + (hasNickName(p) ? "~" + getNickname(p) : p.getName());
					staff.put(p.getUniqueId(), title);
				}
			}
		}
	}
}
