package db;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;
import external.YelpAPI;


public class MySQLConnection {
	private Connection conn;

	public MySQLConnection() {
		try {
			
			//java 反射机制  -- reflection in java -- wiki 
			// https://www.oracle.com/technetwork/articles/java/javareflection-1536171.html
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 不会造成memory leak
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}

		try {
			String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES (?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}

		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<String> favoriteItemIds = new HashSet<>();
		
		
		

		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ? ";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			
			ResultSet rs = ps.executeQuery();
			//把rs 可以看为表格 每一行代表一个符合条件的数据
			
			while (rs.next()) {
				favoriteItemIds.add(rs.getString("item_id"));
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
		return favoriteItemIds;
	}

	public Set<Item> getFavoriteItems(String userId) {
		
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();

		Set<String> favouriteItemIds = getFavoriteItemIds(userId);

		try {
			String sql = "SELECT * FROM items WHERE item_id = ? ";
			PreparedStatement ps = conn.prepareStatement(sql);
			
			for (String itemId : favouriteItemIds) {
				ps.setString(1, itemId);
				ResultSet rs = ps.executeQuery();
				
				ItemBuilder builder = new ItemBuilder();
				while (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setUrl(rs.getString("url"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setAddress(rs.getString("address"));
					builder.setRating(rs.getDouble("rating"));
					builder.setDistance(rs.getDouble("distance"));
					builder.setCategories(getCategories(itemId));
			
					favoriteItems.add(builder.build());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		return favoriteItems;
	}

	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<String> categories = new HashSet<>();

		try {
			String sql = "SELECT category FROM categories WHERE item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, itemId);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return categories;
	}

	public List<Item> searchItems(double lat, double lon, String term) {
		YelpAPI api = new YelpAPI();
		List<Item> items = (List<Item>) api.search(lat, lon, term); // ?????

		//不会搜索一次存一次 会提前判断
		for (Item item : items) {
			saveItem(item);
		}

		return items;
	}

	public void saveItem(Item item) {

		//ignore -- 去重
		// ? -- prepare statement -- in case of injection 所以传进去的是值
		if (conn == null) {
			System.err.println("DB connection failed");
			return;
		}

		// "SELECT * FROM users WHERE name = '" + userName + "' and pw = '"+ passWord +"';"
		// userName = "1' OR '1'='1";
		// passWord = "1' OR '1'='1";
		// "SELECT * FROM users WHERE name = '1' OR '1'='1' and pw = '1' OR '1'='1';"
		try {
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			ps.setString(2, item.getName());
			ps.setDouble(3, item.getRating());
			ps.setString(4, item.getAddress());
			ps.setString(5, item.getUrl());
			ps.setString(6, item.getImageUrl());
			ps.setDouble(7, item.getDistance());
			ps.execute();

			sql = "INSERT IGNORE INTO categories VALUES(?, ?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			for (String category : item.getCategories()) {
				ps.setString(2, category);
				ps.execute();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}



}
