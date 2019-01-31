package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class provides useful functions to handle all rpc parsing codes.
 * 
 * @author yanxiaoxing
 *
 */

public class RpcHelper {
	// Writes a JSONArray to http response.
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		//规定只有特定的user ag 访问 加了一个*表示都可以 任何浏览器发过来都允许
		out.print(array);
		out.close();
	}

              // Writes a JSONObject to http response.
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException {		
			//Prints formatted representations of objects to a text-output stream. 
			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			response.addHeader("Access-Control-Allow-Origin", "*");
			out.print(obj);
			out.close();
	}
	// Parses a JSONObject from http request.
	// post 往数据库写资源 我们得到用户写进来的东西
	public static JSONObject readJSONObject(HttpServletRequest request) {
		StringBuilder sBuilder = new StringBuilder();
		//try with resources -- java8
		//继承了closable 不用手动close 自动调用 新的特性
		try (BufferedReader reader = request.getReader()) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sBuilder.append(line);
			}
			return new JSONObject(sBuilder.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new JSONObject();
             }


}
