package deliverable;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RetrieveTicketsID {

	private RetrieveTicketsID() {}

	private static String readAll(Reader rd) throws IOException {
		var sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
	
	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			var rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			String jsonText = readAll(rd);
			return new JSONArray(jsonText);
		} finally {
			is.close();
		}
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			var rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			String jsonText = readAll(rd);
			return new JSONObject(jsonText);
		} finally {
			is.close();
		}
	}

	public static void writeCSV (Map<String, LocalDateTime> resolutions, List<String> tickets) throws IOException {
		FileWriter fileWriter = null;
		Integer t;	
		Integer i;
	    try {
	    	fileWriter = new FileWriter("Process_Control_Chart.csv");
	    } catch (Exception e) {
	    	LogController.getSingletonInstance().saveMess("errore creazione fileWriter");
	    }
	    
	    try {
	    	fileWriter.append("Ticket; Date\n");
	    	t = resolutions.size();

	    	for (i = 0; i < t; i++) {
	    		fileWriter.append(tickets.get(i));
	    		fileWriter.append(";");
	    		fileWriter.append(resolutions.get(tickets.get(i)).toString().subSequence(0, 10));
	    		fileWriter.append("\n");
	    	}
	    } catch (Exception e) {
	    	LogController.getSingletonInstance().saveMess("errore scrittura csv");
	    	e.printStackTrace();
	    } finally {
	    	try {
	    		fileWriter.flush();
	    		fileWriter.close();
	    	} catch (IOException e) {
	    		LogController.getSingletonInstance().saveMess("errore flush o close");
	    	}
	    }
   }
   
   
	public static void main(String[] args) throws IOException, JSONException, GitAPIException {
		Map<String, LocalDateTime> resolutions = new HashMap<>();
		List<String> tickets = new ArrayList<>();
		List<RevCommit> commits = new ArrayList<>();
		LogController log;
		
		var projName ="TAJO";
		var path = "./";
		Integer j = 0;
		Integer i = 0;
		Integer total = 1;
		
		var git = GitHandler.clone(path + projName, projName);
		log = LogController.getSingletonInstance();
		log.saveMess("Retrieve data for project: " + projName);
		
		commits = GitHandler.getCommits(git);
		
		do {
			//Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
			j = i + 1000;
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
					+ projName + "%22AND%22issueType%22=%22new+feature%22AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
					+ i.toString() + "&maxResults=" + j.toString();
			JSONObject json = readJsonFromUrl(url);
			var issues = json.getJSONArray("issues");
			total = json.getInt("total");
			
			for (; i < total && i < j; i++) {
				//Iterate through each bug
				var key = issues.getJSONObject(i%1000).get("key").toString();
				
				LocalDateTime dateTime = GitHandler.getFixDate(commits, key);
				if (dateTime != null) {
					resolutions.put(key, dateTime);
					tickets.add(key);
				} else {
					
					log.saveMess(key);
				}
				                       
			}  
                  
		} while (i < total);
		
		writeCSV(resolutions, tickets);
		log.saveMess("Exiting for project: " + projName);
      
	}
 
}
