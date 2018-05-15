
package SemWeb;
import java.io.IOException;
import java.io.InputStream;
//import java.util.Iterator;
import java.io.PrintWriter;

//import org.mindswap.pellet.jena.PelletReasonerFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
//import com.hp.hpl.jena.reasoner.ValidityReport;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.FileManager;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/* Hello Semantic Web Demonstration */

public class HelloSemWeb {
	static String defaultNameSpace = "http://org.semwebprogramming/chapter2/people#";
	
	Model baseFriends = null;
	Model schema = null;
	InfModel inferredFriends = null;
	
	public static void main(String[] args) throws IOException
	{
		//File test = new File("here.txt");
		//System.out.print(" Running at directory " + test.getCanonicalPath() + " \n");
		
		HelloSemWeb hello = new HelloSemWeb();

		// "Deal" with the Jena logger...i.e., turn it off...
		Logger.getRootLogger().setLevel(Level.OFF);
		
		// Load my FOAF friends...
		System.out.println("Load my FOAF Friends...");
		hello.baseFriends = ModelFactory.createOntologyModel();
		InputStream inFoafInstance = FileManager.get().open("Ontologies/FOAFFriends.rdf");
		hello.baseFriends.read(inFoafInstance, defaultNameSpace);	
		
		// Say Hello to myself...
		System.out.println("\nSay Hello to Myself...");
		// Hello to Me - focused search...
		hello.runQuery(" select DISTINCT ?name where{ people:me foaf:name ?name  }", hello.baseFriends);

		// Say Hello to my FOAF Friends...
		System.out.println("\nSay Hello to my FOAF Friends...");
		//Hello to just my friends - navigation...
		hello.runQuery(" select DISTINCT ?myname ?name where{  people:me foaf:knows ?friend. ?friend foaf:name ?name } ", hello.baseFriends);

		// Add my new friends...
		System.out.println("\nAdd my new friends from another ontology...");
		hello.populateNewFriends();
		
		// Say hello to my friends - hey my new ones are missing?
		System.out.println("\nSay hello to all my friends...");
		hello.myFriends(hello.baseFriends);
		System.out.println("What?!? The new ones are missing!");
		
		// Add the ontologies...
		System.out.println("\nAdd the ontologies (schemas) to help find my new friends...");
		hello.populateFOAFSchema();
		hello.populateNewFriendsSchema();
		
		// See if the ontologies help identify my new friends? Nope!
		System.out.println("\nDid ontologies help? Say hello to all my friends...");
		hello.myFriends(hello.baseFriends);
		System.out.println("Nope, the new ones are still missing!");
		
		// Align the ontologies to bind my friends together...
		System.out.println("\nAdd alignment statements for the two ontologies to help find my new friends...");
		hello.addAlignment();
		
		// Now say hello to my friends - nope still no new friends!
		System.out.println("\nTry again. Say hello to all my friends...");
		hello.myFriends(hello.baseFriends);
		System.out.println("Nope, still not all!");
		
		// Run a reasoner to align the ontologies...
		System.out.println("\nRun a Reasoner to align the ontologies to help find my new friends...");
		hello.bindReasoner();
		
		// Say hello to all my friends...
		System.out.println("\nTry again. Say hello to all my friends...");
		hello.myFriends(hello.inferredFriends);
		System.out.println("Finally, all are found!");
		
		// Say hello to my self again - oh no there are two of us!
		System.out.println("\nSay hello to myself...");
		hello.mySelf(hello.inferredFriends);
		System.out.println("Oh no, there are two names for me! Why?");
		// ...this is due to the previous addAlignment() call
	
		// One more thing - now we can set a restriction...
		System.out.println("\nEstablishing a restriction to just get email friends...");
		hello.setRestriction(hello.inferredFriends);
		hello.myEmailFriends(hello.inferredFriends);
		
		// We can also set a rule...
		System.out.println("\nSay hello to my gmail friends only...");
		hello.runJenaRule(hello.inferredFriends);
		hello.myGmailFriends(hello.inferredFriends);
		
		System.out.println("\nSuccess!");

		PrintWriter fileOut = new PrintWriter("myFile.txt", "ASCII");
		fileOut.println("Finished!");
		fileOut.close();
	}

/*	private void populateFOAFFriends() throws IOException
	{
		_friends = ModelFactory.createOntologyModel();
		InputStream inFoafInstance = FileManager.get().open("Ontologies/FOAFFriends.rdf");
		_friends.read(inFoafInstance,defaultNameSpace);		
		inFoafInstance.close();
	}
*/	
	private void mySelf(Model model)
	{
		// Hello to Me - focused search...
		runQuery(" select DISTINCT ?name where{ people:me foaf:name ?name  }", model);
	}
	
	private void myFriends(Model model)
	{
		// Hello to just my friends - navigation...
		runQuery(" select DISTINCT ?myname ?name where{  people:me foaf:knows ?friend. ?friend foaf:name ?name } ", model);
	}

	private void populateNewFriends() throws IOException
	{		
		InputStream inFoafInstance = FileManager.get().open("Ontologies/additionalFriends.owl");
		baseFriends.read(inFoafInstance, defaultNameSpace);
		inFoafInstance.close();
	} 
	
	private void addAlignment()
	{	
		// State that :individual is equivalentClass of foaf:Person
		Resource resource = schema.createResource(defaultNameSpace + "Individual");
		Property prop = schema.createProperty("http://www.w3.org/2002/07/owl#equivalentClass");
		Resource obj = schema.createResource("http://xmlns.com/foaf/0.1/Person");
		schema.add(resource, prop, obj);
		
		// State that :hasName is an equivalentProperty of foaf:name
		resource = schema.createResource(defaultNameSpace + "hasName");
		//prop = schema.createProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		prop = schema.createProperty("http://www.w3.org/2002/07/owl#equivalentProperty");
		obj = schema.createResource("http://xmlns.com/foaf/0.1/name");
		schema.add(resource, prop, obj);
		
		// State that :hasFriend is a subproperty of foaf:knows
		resource = schema.createResource(defaultNameSpace + "hasFriend");
		prop = schema.createProperty("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		obj = schema.createResource("http://xmlns.com/foaf/0.1/knows");
		schema.add(resource, prop, obj);
		
		// State that "sem web" is the same person as "Semantic Web"
		resource = schema.createResource("http://org.semwebprogramming/chapter2/people#me");
		prop = schema.createProperty("http://www.w3.org/2002/07/owl#sameAs");
		obj = schema.createResource("http://org.semwebprogramming/chapter2/people#Individual_5");
		schema.add(resource, prop, obj);
	}
	
	private void populateFOAFSchema() throws IOException
	{
		InputStream inFoaf = FileManager.get().open("Ontologies/foaf.rdf");
		InputStream inFoaf2 = FileManager.get().open("Ontologies/foaf.rdf");
		schema = ModelFactory.createOntologyModel();
			
		schema.read(inFoaf, defaultNameSpace);
		baseFriends.read(inFoaf2, defaultNameSpace);	
		
		inFoaf.close();
		inFoaf2.close();
	}
	
	private void populateNewFriendsSchema() throws IOException
	{
		InputStream inFoafInstance = FileManager.get().open("Ontologies/additionalFriendsSchema.owl");
		baseFriends.read(inFoafInstance, defaultNameSpace);
		inFoafInstance.close();
	}
	
	private void bindReasoner()
	{
	    Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
	    reasoner = reasoner.bindSchema(schema);
	    inferredFriends = ModelFactory.createInfModel(reasoner, baseFriends);
	}

	private void runQuery(String queryRequest, Model model)
	{	
		StringBuffer queryStr = new StringBuffer();
		// Establish Prefixes...
		// Set default Name space first...
		queryStr.append("PREFIX people" + ": <" + defaultNameSpace + "> ");
		queryStr.append("PREFIX rdfs" + ": <" + "http://www.w3.org/2000/01/rdf-schema#" + "> ");
		queryStr.append("PREFIX rdf" + ": <" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + "> ");
		queryStr.append("PREFIX foaf" + ": <" + "http://xmlns.com/foaf/0.1/" + "> ");
		
		// Now add query..
		queryStr.append(queryRequest);
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		try
		{
			ResultSet response = qexec.execSelect();
		
			while ( response.hasNext() )
			{
				QuerySolution soln = response.nextSolution();
				RDFNode name = soln.get("?name");
				if ( name != null )
				{
					System.out.println( "  Hello to " + name.toString() );
				}
				else
					System.out.println("  No Friends found!");
			}
		}
		finally
		{
			qexec.close();
		}				
	}
		
	private void runJenaRule(Model model)
	{
		String rules = "[emailChange: (?person <http://xmlns.com/foaf/0.1/mbox> ?email), strConcat(?email, ?lit), regex( ?lit, '(.*@gmail.com)') -> (?person <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://org.semwebprogramming/chapter2/people#GmailPerson>)]";

		Reasoner ruleReasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		ruleReasoner = ruleReasoner.bindSchema(schema);
	    inferredFriends = ModelFactory.createInfModel(ruleReasoner, model);		
	}
	
/*	private void runPellet()
	{
		Reasoner reasoner = PelletReasonerFactory.theInstance().create();
	    reasoner = reasoner.bindSchema(schema);
	    inferredFriends = ModelFactory.createInfModel(reasoner, _friends);
	    
	    ValidityReport report = inferredFriends.validate();
	    //printIterator(report.getReports(), "Validation Results");
	}

    public static void printIterator(Iterator i, String header)
    {
        System.out.println(header);

        for (int c = 0; c < header.length(); c++)
            System.out.print("=");

        System.out.println();
       
        if ( i.hasNext() )
        {
	        while ( i.hasNext() ) 
	            System.out.println( i.next() );
        }       
        else
            System.out.println("<EMPTY>");

        System.out.println();
    }
*/
    public void setRestriction(Model model) throws IOException
    {
    		// Load restriction - if entered in model with reasoner,
    		//		reasoner sets entailments...
		InputStream inResInstance = FileManager.get().open("Ontologies/restriction.owl");
		model.read(inResInstance, defaultNameSpace);
		inResInstance.close();
    }
    
    public void myEmailFriends(Model model)
    {
     	// Get all my email friends - the ones with email addresses...
		runQuery(" select DISTINCT ?name " +
					"where{  " +
						"?sub rdf:type <http://org.semwebprogramming/chapter2/people#EmailPerson> . " +
						"?sub foaf:name ?name } ", model);
    }
    
    public void myGmailFriends(Model model)
    {
		runQuery(" select DISTINCT ?name " +
					"where{  " +
						"?sub rdf:type people:GmailPerson ." +
						"?sub foaf:name ?name } ", model);
    }
}

