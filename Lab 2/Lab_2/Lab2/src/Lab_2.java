import java.io.FileWriter;
import java.io.IOException;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VCARD;

public class Lab_2 {
	
	public static void main(String[] args) throws IOException {
		
		org.apache.log4j.Logger.getRootLogger().
		setLevel(org.apache.log4j.Level.OFF);
		
		String rootUri  = "http://utdallas/semclass";
		String defaultUri = "http://utdallas/semclass#";
		String movieUri = rootUri + "/movie#";
		String bookUri = rootUri + "/book#";
		String personUri = rootUri + "/person#";
		
		//People
		
		String stanleyUri = personUri + "StanleyKubrick";
		String stanleyGiven = "Stanley";
		String stanleyFamily = "Kubrick";
		
		String peterUri = personUri + "PeterGeorge";
		String peterGiven = "Peter";
		String peterFamily = "George";
		
		String anthonyUri = personUri +"AnthonyBurgess";
		String anthonyGiven = "Anthony";
		String anthonyFamily = "Burgess";
				
		//Books
		String redAlertUri = bookUri + "redAlert";
		String redAlertLanguage = "English";
		String redAlertYear = "1958";
		String redAlertTitle = "Red Alert";
	
		String aClockworkOrangeUri = bookUri + "aClockworkOrange";
		String aClockworkOrangeLanguage = "English";
		String aClockworkOrangeYear = "1962";
		String aClockworkOrangeTitle = "A Clockwork Orange";
		
		//Movies
		
		String drStrangeLoveUri = movieUri + "drStrangeLove";
		String drStrangeLoveTitle = "Dr. Strange Love";
		String drStrangeLoveYear = "1964";
		
		String aClockworkOrangeMovieUri = movieUri + "aClockworkOrange";
		String aClockworkOrangeMovieTitle = "A Clockwork Orange";
		String aClockworkOrangeMovieYear = "1971";
		
	
		 	String directory = "MyDatabases/Dataset1" ;
	        Dataset dataset = TDBFactory.createDataset(directory) ; 
	        Model model = dataset.getDefaultModel();
	      
	        
	     //Create properties which are not avaialble
	        
	    Property director = model.createProperty(movieUri,"directedby");
	    Property movieBasedOn = model.createProperty(movieUri, "movieBasedOn");
	    
	    //Create Resources as Subjects
	    
	    Resource movie = model.createResource(movieUri);
	    Resource book = model.createResource(bookUri);
	    
	    //Create People resource or instantiate object of respective classes
	    
	    Resource stanley = model.createResource(stanleyUri);
	    stanley.addProperty(VCARD.Family, stanleyFamily)
	    		.addProperty(VCARD.Given, stanleyGiven)
	    		.addProperty(VCARD.FN, stanleyFamily+stanleyGiven);

	    Resource peter = model.createResource(peterUri);
	    peter.addProperty(VCARD.Family, peterFamily)
	    		.addProperty(VCARD.Given, peterGiven)
	    		.addProperty(VCARD.FN, peterFamily+peterGiven);
	    

	    Resource anthony = model.createResource(anthonyUri);
	    anthony.addProperty(VCARD.Family, anthonyFamily)
	    		.addProperty(VCARD.Given, anthonyGiven)
	    		.addProperty(VCARD.FN, anthonyFamily+anthonyGiven);
	    
	    //Book resources
	    Resource redAlert = model.createResource(redAlertUri);
	    redAlert.addProperty(RDF.type, book)
	    		.addProperty(DC.creator, peter)
	    		.addProperty(DC.title, redAlertTitle)
	    		.addProperty(DC.date, redAlertYear)
	    		.addProperty(DC.language, redAlertLanguage);
	    
	    Resource aClockworkOrange = model.createResource(aClockworkOrangeUri);
	    aClockworkOrange.addProperty(RDF.type, book)
			    		.addProperty(DC.creator, anthony)
			    		.addProperty(DC.title, aClockworkOrangeTitle)
			    		.addProperty(DC.date, aClockworkOrangeYear)
			    		.addProperty(DC.language, aClockworkOrangeLanguage);
			    
	    //Movie Resource
	    
	    Resource aClockworkOrangeMovie = model.createResource(aClockworkOrangeMovieUri);
	    aClockworkOrangeMovie.addProperty(RDF.type, movie)
	    				.addProperty(DC.title,aClockworkOrangeMovieTitle)
	    				.addProperty(DC.date, aClockworkOrangeMovieYear)
	    				.addProperty(movieBasedOn, aClockworkOrange)
	    				.addProperty(director, stanley);
	    
	    Resource drStrangeLove = model.createResource(drStrangeLoveUri);
	    drStrangeLove.addProperty(RDF.type, movie)
	    			.addProperty(DC.title,drStrangeLoveTitle)
    				.addProperty(DC.date, drStrangeLoveYear)
    				.addProperty(movieBasedOn, redAlert)
    				.addProperty(director, stanley);
	    
	    
	    
   	 	FileWriter xmlOutput = new FileWriter("Lab2_3_ABhandari.xml");		
   	 	FileWriter n3Outout = new FileWriter("Lab2_3_ABhandari.n3");
	    model.write(xmlOutput,"RDF/XML"); 
	    model.write(n3Outout,"N3");
	    
   	 	model.close();
		dataset.close();
	}

}
