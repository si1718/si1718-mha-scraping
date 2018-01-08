package data.scraping.utils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import net.minidev.json.JSONObject;

public class MongoUtils {
	
	private static final String URL_BASE = "https://investigacion.us.es/sisius/sis_dep.php?id_dpto=91";
	private static List<org.bson.Document> json_array = new ArrayList<org.bson.Document>();


	public static void updateProjects() throws MalformedURLException, IOException {
		String projectURL ="";
		String projectURL2 ="";
		
		JSONObject jsonProject = new JSONObject();
		final String URL_BASE_PROY = "https://investigacion.us.es";
		Document doc = Jsoup.parse(new URL(URL_BASE), 10000);
		Elements elements = doc.getElementsByClass("content");
		int i = 0;
		Iterator<Element> it = elements.get(i).getElementsByTag("a").iterator();
		while(it.hasNext()){
			Element e = it.next();
			if(e.attr("href").contains("idpers")) {
				projectURL = e.attr("href");

				Document doc2 = Jsoup.parse(new URL(URL_BASE_PROY + projectURL), 10000);
				Elements elements2 = doc2.getElementsByClass("content");
				Iterator<Element> it2 = elements2.get(i).getElementsByTag("a").iterator();
				int j=0;
				while(it2.hasNext()){
					Element e2 = it2.next();
					if(e2.attr("href").contains("idproy")) {
						projectURL2 = e2.attr("href");
						
						Document doc3 = Jsoup.parse(new URL(URL_BASE_PROY + "/sisius/"+projectURL2), 10000);
						Elements elements3 = doc3.getElementsByClass("content");
						Iterator<Element> it3 = elements3.iterator();
						Iterator<Element> it4 = elements3.get(i).getElementsByTag("a").iterator();
						
						while(it3.hasNext()){
							if(j>4) {
								j=0;
							}

							List<String> keywords = getKeywords(j);
							Element e3 = it3.next();
							String name = e3.getElementsByTag("h5").text();
							String others = e3.getElementsByTag("p").get(0).text();
							String researcher = "";
							String type = "";
							String fecIni = "";
							String fecFin = "";
							if(others.indexOf("Responsable actual: ")!=-1){
								researcher = others.substring(others.indexOf("Responsable actual: "), others.indexOf("Tipo de Proyecto/Ayuda: ")-1).replace("Responsable actual: ", "");
								
							}else if(others.indexOf("Responsable: ")!=-1){
								researcher = others.substring(others.indexOf("Responsable: "), others.indexOf("Tipo de Proyecto/Ayuda: ")-1).replace("Responsable: ", "");
								
							}
							
							if(researcher.indexOf("/")!=-1) {
								researcher = researcher.substring(0, researcher.indexOf("/")-1);
							}
							if(others.indexOf("Tipo de Proyecto/Ayuda: ")!=-1){
								type = others.substring(others.indexOf("Tipo de Proyecto/Ayuda: "), others.indexOf("Referencia: ")-1).replace("Tipo de Proyecto/Ayuda: ", "");
							}
							if(others.indexOf("Fecha de Inicio: ")!=-1 && others.indexOf("Fecha de Finalización: ")!=-1){
								fecIni = others.substring(others.indexOf("Fecha de Inicio: "), others.indexOf("Fecha de Finalización: ")-1).replace("Fecha de Inicio: ", "");
							}
							if(others.indexOf("Fecha de Finalización: ")!=-1){
								fecFin = others.substring(others.indexOf("Fecha de Finalización: ")).replace("Fecha de Finalización: ", "");
							}
					
							
							List<String> researchers = new ArrayList<String>();
							while(it4.hasNext()){
								Element e4 = it4.next();
								if(e4.attr("href").contains("idpers")) {
									researchers.add(e4.text());							
								}
							}
							
							
							jsonProject.put("idProject", e2.text().replace("/", "-"));
							jsonProject.put("researcher", researcher);
							jsonProject.put("researcherName", "");
							jsonProject.put("name", name);
							jsonProject.put("type", type);
							jsonProject.put("startDate", fecIni);
							jsonProject.put("endDate", fecFin);
							jsonProject.put("researchers", researchers);
							jsonProject.put("keywords", keywords);
							org.bson.Document bson = org.bson.Document.parse(jsonProject.toString().replace(".", ""));
							json_array.add(bson);
							j++;
							
							
						}
						
						
					}
				
				}
			
			}
		}
		MongoClientURI uri  = new MongoClientURI("mongodb://manuel:manuel@ds255455.mlab.com:55455/si1718-mha-projects");
	    MongoClient client = new MongoClient(uri);
	    MongoDatabase db = client.getDatabase(uri.getDatabase());

	    MongoCollection<org.bson.Document> col = db.getCollection("projects");
	    MongoCursor<org.bson.Document> cursor = col.find().iterator();
	    while (cursor.hasNext()) {
	    	org.bson.Document project = cursor.next();
	    	col.deleteOne(project);
        }
	    col.insertMany(json_array);

	 	client.close();
		
		
	}
	
	public static List<String> getKeywords(int index) throws FileNotFoundException {
		 Scanner sc = new Scanner(new File("words.txt"));
		 List<String> wordArray = new ArrayList<String>(); 
        while (sc.hasNext()){
            String word = sc.next();
            wordArray.add(word);
      
        }
        sc.close();
        List<String> keywords = new ArrayList<String>();
        for(int i = 0; i <= index; i++){
		       	 int idx = new Random().nextInt(wordArray.size());
		       	 String random = wordArray.get(idx);
		       	 keywords.add(random);

		}
        return keywords;
	}
}
