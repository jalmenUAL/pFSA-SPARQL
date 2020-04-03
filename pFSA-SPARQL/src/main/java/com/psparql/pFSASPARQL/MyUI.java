package com.psparql.pFSASPARQL;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.annotation.WebServlet;

import org.apache.log4j.varia.NullAppender;
import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;
import org.vaadin.aceeditor.AceEditor;
import org.vaadin.aceeditor.AceMode;
import org.vaadin.aceeditor.AceTheme;

import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.themes.ValoTheme;

import fsaSPARQL.AND_GOD;
import fsaSPARQL.AND_LUK;
import fsaSPARQL.AND_PROD;
import fsaSPARQL.AT_LEAST;
import fsaSPARQL.AT_MOST;
import fsaSPARQL.CLOSE_TO;
import fsaSPARQL.FsaSPARQL;
import fsaSPARQL.MAX;
import fsaSPARQL.MEAN;
import fsaSPARQL.MIN;
import fsaSPARQL.MORE_OR_LESS;
import fsaSPARQL.OR_GOD;
import fsaSPARQL.OR_LUK;
import fsaSPARQL.OR_PROD;
import fsaSPARQL.VERY;
import fsaSPARQL.WMAX;
import fsaSPARQL.WMIN;
import fsaSPARQL.WSUM;
import pSPARQL.pSPARQL;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of an HTML page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

	Integer step = 0;
	List<List<String>> rules = null;
	Grid<HashMap<String, Term>> answers = new Grid<>();

	public static String readStringFromURL(String requestURL) throws IOException {
		try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
			scanner.useDelimiter("\\A");
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	@Override
	protected void init(VaadinRequest vaadinRequest) {

		final VerticalLayout layout = new VerticalLayout();

		Label lab = new Label("pFSA-SPARQL: FSA-SPARQL to Prolog");
		lab.addStyleName(ValoTheme.LABEL_H1);

		TextField file = new TextField();
		file.setSizeFull();
		file.setValue("file:///C:/movies.rdf");

		Button run = new Button("Execute");

		Panel edS = new Panel();
		Panel edP = new Panel();
		edS.setSizeFull();
		edP.setSizeFull();

		AceEditor editor = new AceEditor();
		editor.setHeight("300px");
		editor.setWidth("2000px");
		editor.setFontSize("12pt");
		editor.setMode(AceMode.sql);
		editor.setTheme(AceTheme.eclipse);
		editor.setUseWorker(true);
		editor.setReadOnly(false);
		editor.setShowInvisibles(false);
		editor.setShowGutter(false);
		editor.setShowPrintMargin(false);
		editor.setUseSoftTabs(false);

		String prog1 = "PREFIX movie: <http://www.movies.org#> \r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX f: <http://www.fuzzy.org#>\r\n" + "PREFIX l: <http://www.lattice.org#>\r\n"
				+ "SELECT ?Name ?Rank \r\n" + "WHERE { ?Movie movie:name ?Name . \r\n"
				+ " ?Movie f:type (movie:quality movie:Good ?r) . \r\n"
				+ " ?Movie f:type (movie:genre movie:Thriller ?t)  \r\n" + " BIND (l:AND_GOD(?r,?t) as ?Rank)  \r\n"
				+ " FILTER (?Rank > 0.5) }";

		String prog2 = "PREFIX movie: <http://www.movies.org#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX f: <http://www.fuzzy.org#>\r\n" + "PREFIX l: <http://www.lattice.org#>\r\n"
				+ " SELECT ?Name ?Rank \r\n" + " WHERE { ?Movie movie:name ?Name . \r\n"
				+ " ?Movie movie:leading_role (?Actor ?l) . \r\n" + " ?Actor movie:name \"George Clooney\". \r\n"
				+ " ?Movie f:type (movie:quality movie:Good ?r) . \r\n" + " BIND(l:AND_PROD(?r,?l) as ?Rank) . \r\n"
				+ " FILTER (?Rank > 0.3) }";

		String prog3 = "PREFIX hotel: <http://www.hotels.org#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX f: <http://www.fuzzy.org#>\r\n" + "PREFIX l: <http://www.lattice.org#>\r\n"
				+ " SELECT ?Name ?d  \r\n" + " WHERE { ?Hotel hotel:name ?Name . \r\n"
				+ " ?Hotel rdf:type hotel:Hotel . \r\n"
				+ " {SELECT ?Hotel ?g ?e WHERE {?Hotel f:type (hotel:quality hotel:Good ?g) . \r\n"
				+ " ?Hotel f:type (hotel:style hotel:Elegant ?e)}} . \r\n" + " BIND(l:AND_PROD(?g,?e) as ?d) .\r\n"
				+ "FILTER (?d > 0.2) }";

		/*
		 * String prog3 = "PREFIX movie: <http://www.movies.org#>\r\n" +
		 * "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" +
		 * "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" +
		 * "PREFIX f: <http://www.fuzzy.org#>\r\n" +
		 * "PREFIX l: <http://www.lattice.org#>\r\n" + "SELECT ?Name ?l ?t \r\n" +
		 * " WHERE { ?Movie movie:name ?Name . \r\n" +
		 * " ?Movie f:type (movie:quality movie:Excellent ?l) .\r\n" +
		 * " ?Movie f:type (movie:genre movie:Thriller ?t)\r\n" +
		 * " BIND(l:OR_PROD(?t,?l) as ?Rank) .\r\n" + " FILTER (?Rank > 0.4) } ";
		 */

		String prog4 = "PREFIX hotel: <http://www.hotels.org#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX f: <http://www.fuzzy.org#>\r\n" + "PREFIX l: <http://www.lattice.org#>\r\n"
				+ "SELECT ?Name ?l \r\n" + " WHERE { ?Hotel hotel:name ?Name . \r\n"
				+ " ?Hotel rdf:type hotel:Hotel . \r\n" + " ?Hotel hotel:close (?pi ?l) . \r\n"
				+ " ?pi hotel:name \"Empire State Building\" \r\n" + " FILTER (?l > 0.75) }";

		String prog5 = "PREFIX hotel: <http://www.hotels.org#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX f: <http://www.fuzzy.org#>\r\n" + "PREFIX l: <http://www.lattice.org#>\r\n"
				+ " SELECT ?Name ?d \r\n" + " WHERE { ?Hotel hotel:name ?Name .\r\n"
				+ " ?Hotel rdf:type hotel:Hotel .\r\n" + " ?Hotel hotel:price ?p \r\n"
				+ " BIND(l:AT_MOST(?p,200,300) as ?d)} ";

		String prog6 = "PREFIX hotel: <http://www.hotels.org#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX f: <http://www.fuzzy.org#>\r\n" + "PREFIX l: <http://www.lattice.org#>\r\n"
				+ " SELECT ?Name ?d \r\n" + " WHERE { ?Hotel hotel:name ?Name . \r\n"
				+ " ?Hotel rdf:type hotel:Hotel . \r\n" + " ?Hotel hotel:price ?p . \r\n"
				+ " ?Hotel f:type (hotel:quality hotel:Good ?g) . \r\n"
				+ " ?Hotel f:type (hotel:style hotel:Elegant ?e) \r\n"
				+ " BIND(l:WSUM(0.1,l:AND_PROD(l:MORE_OR_LESS(?e), \r\n"
				+ " l:VERY(?g)),0.9,l:CLOSE_TO(?p,100,50)) as ?d) .\r\n" + "FILTER(?d > 0.4)}";
		
		String prog7 = "PREFIX hotel: <http://www.hotels.org#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX f: <http://www.fuzzy.org#>\r\n" + "PREFIX l: <http://www.lattice.org#>\r\n"
				+ "SELECT ?Name ?l ?pi ?pi2 \r\n" + " WHERE { ?Hotel hotel:name ?Name . \r\n"
				+ " ?Hotel rdf:type hotel:Hotel . \r\n" + " {?Hotel hotel:close (?pi ?l) . \r\n"
				+ "  ?pi hotel:name \"Empire State Building\"  } "
				+ " UNION { ?Hotel hotel:close (?pi2 ?l) . ?pi2 hotel:name \"Central Park\" }  \r\n" 
				+ " FILTER(?l >0.7) }";
		
		String prog8 = "PREFIX hotel: <http://www.hotels.org#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX f: <http://www.fuzzy.org#>\r\n" + "PREFIX l: <http://www.lattice.org#>\r\n"
				+ "SELECT ?Name ?l1 ?l2 ?pi ?pi2 \r\n" + " WHERE { ?Hotel hotel:name ?Name . \r\n"
				+ " ?Hotel rdf:type hotel:Hotel . \r\n" + " {?Hotel hotel:close (?pi ?l1) . \r\n"
				+ "  ?pi hotel:name \"Empire State Building\" .FILTER(?l1 >0.3) } "
				+ " OPTIONAL { ?Hotel hotel:close (?pi2 ?l2) . ?pi2 hotel:name \"Central Park\"  \r\n" 
				+ " FILTER(?l2 >0.8) }}";
		
		String prog9 = "PREFIX hotel: <http://www.hotels.org#>\r\n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "PREFIX f: <http://www.fuzzy.org#>\r\n" + "PREFIX l: <http://www.lattice.org#>\r\n"
				+ "SELECT ?Name ?l1  \r\n" + " WHERE { ?Hotel hotel:name ?Name . \r\n"
				+ " ?Hotel rdf:type hotel:Hotel . \r\n" + " {?Hotel hotel:close (?pi ?l1) . \r\n"
				+ "  ?pi hotel:name \"Empire State Building\" .FILTER(?l1 >0.3) } "
				+ " MINUS { ?Hotel hotel:close (?pi2 ?l2) . ?pi2 hotel:name \"Central Park\"  \r\n" 
				+ " FILTER(?l2 >0.8) }}";
		
		String prog10 = "PREFIX movie: <http://www.movies.org#>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
				"PREFIX f: <http://www.fuzzy.org#>\r\n" + 
				"PREFIX l: <http://www.lattice.org#>\r\n" + 
				" SELECT ?Name ?r\r\n" + 
				" WHERE { ?Movie movie:name ?Name . \r\n" + 
				" ?Movie f:type (movie:quality movie:Good ?r) .\r\n" + 
				" FILTER (?r > 0.3) . FILTER EXISTS { ?Movie movie:actor ?Actor . ?Actor movie:name \"George Clooney\" } }";

		ComboBox<String> examples = new ComboBox<>("Select an Example");
		examples.setItems("Example 1", "Example 2", "Example 3", "Example 4", "Example 5", "Example 6","Example 7",
				"Example 8", "Example 9", "Example 10");

		examples.addValueChangeListener(event -> {
			if (event.getSource().isEmpty()) {
				Notification.show("No example selected");
			} else {
				if (event.getValue() == "Example 1") {
					file.setValue("file:///C:/movies.rdf");
					editor.setValue(prog1);
				} else if (event.getValue() == "Example 2") {
					file.setValue("file:///C:/movies.rdf");
					editor.setValue(prog2);
				} else if (event.getValue() == "Example 3") {
					file.setValue("file:///C:/hotels.rdf");
					editor.setValue(prog3);
				} else if (event.getValue() == "Example 4") {
					file.setValue("file:///C:/hotels.rdf");
					editor.setValue(prog4);
				} else if (event.getValue() == "Example 5") {
					file.setValue("file:///C:/hotels.rdf");
					editor.setValue(prog5);
				} else if (event.getValue() == "Example 6") {
					file.setValue("file:///C:/hotels.rdf");
					editor.setValue(prog6);
				}
				else if (event.getValue() == "Example 7") {
					file.setValue("file:///C:/hotels.rdf");
					editor.setValue(prog7);
					
				}
				else if (event.getValue() == "Example 8") {
					file.setValue("file:///C:/hotels.rdf");
					editor.setValue(prog8);
					
				}
				else if (event.getValue() == "Example 9") {
					file.setValue("file:///C:/hotels.rdf");
					editor.setValue(prog9);
					
				}
				else if (event.getValue() == "Example 10") {
					file.setValue("file:///C:/movies.rdf");
					editor.setValue(prog10);
					
				}
				 
			}
		});

		editor.setValue(prog1);
		editor.setDescription("SPARQL Query");

		AceEditor editorP = new AceEditor();
		editorP.setHeight("300px");
		editorP.setWidth("2000px");
		editorP.setFontSize("12pt");
		editorP.setMode(AceMode.prolog);
		editorP.setTheme(AceTheme.eclipse);
		editorP.setUseWorker(true);
		editorP.setReadOnly(false);
		editorP.setShowInvisibles(false);
		editorP.setShowGutter(false);
		editorP.setShowPrintMargin(false);
		editorP.setUseSoftTabs(false);
		editorP.setDescription("Prolog Program");

		AceEditor editorOntology = new AceEditor();
		Panel edO = new Panel();
		edO.setSizeFull();

		org.apache.log4j.BasicConfigurator.configure(new NullAppender());

		FunctionRegistry.get().put("http://www.fuzzy.org#AT_LEAST", AT_LEAST.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#AT_MOST", AT_MOST.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#CLOSE_TO", CLOSE_TO.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#MORE_OR_LESS", MORE_OR_LESS.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#VERY", VERY.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#AND_PROD", AND_PROD.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#OR_PROD", OR_PROD.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#AND_LUK", AND_LUK.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#OR_LUK", OR_LUK.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#AND_GOD", AND_GOD.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#OR_GOD", OR_GOD.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#MIN", MIN.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#MAX", MAX.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#MEAN", MEAN.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#WSUM", WSUM.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#WMAX", WMAX.class);

		FunctionRegistry.get().put("http://www.fuzzy.org#WMIN", WMIN.class);

		pSPARQL ps = new pSPARQL();

		String s = FsaSPARQL.FSAtoSPARQL(editor.getValue());

		rules = ps.SPARQLtoProlog(s, step);

		String pp = "";
		for (List<String> r : rules) {
			String prule = r.get(0) + ":-";
			for (int i = 1; i < r.size(); i++) {
				prule = prule + "\n       " + r.get(i) + ",";
			}
			prule = prule.substring(0, prule.length() - 1);
			pp = pp + "\n" + prule;

		}

		String t1 = "use_module(library(semweb/rdf11))";
		Query q1 = new Query(t1);
		System.out.print((q1.hasSolution() ? "" : ""));
		q1.close();

		String t11 = "use_module(library(semweb/rdf_http_plugin))";
		Query q11 = new Query(t11);
		System.out.print((q11.hasSolution() ? "" : ""));
		q11.close();

		// OPERATORS

		String ops = "\n'http://www.lattice.org#AND_LUK'(X^^TX,Y^^TY,Z^^TX):- Z is max(X+Y-1,0).\n"
				+ "'http://www.lattice.org#AND_GOD'(X^^TX,Y^^TY,Z^^TX):-Z is min(X,Y).\n"
				+ "'http://www.lattice.org#AND_PROD'(X^^TX,Y^^TY,Z^^TX):-Z is X*Y.\n"
				+ "'http://www.lattice.org#OR_LUK'(X^^TX,Y^^TY,Z^^TX):-Z is min(X+Y,1).\n"
				+ "'http://www.lattice.org#OR_GOD'(X^^TX,Y^^TY,Z^^TX):-Z is max(X,Y).\n"
				+ "'http://www.lattice.org#OR_PROD'(X^^TX,Y^^TY,Z^^TX):-Z is X+Y-X*Y.\n"
				+ "'http://www.lattice.org#MEAN'(X^^TX,Y^^TY,Z^^TX):-Z is X+Y/2.\n"
				+ "'http://www.lattice.org#WSUM'(U^^TU,X^^TX,V^^TV,Y^^TY,Z^^TX):-Z is U*X+V*Y.\n"
				+ "'http://www.lattice.org#WMAX'(U^^TU,X^^TX,V^^TV,Y^^TY,Z^^TX):-Z is max(min(U,X),min(V,Y)).\n"
				+ "'http://www.lattice.org#WMIN'(U^^TU,X^^TX,V^^TV,Y^^TY,Z^^TX):-Z is min(max(1-U,X),max(1-V,Y)).\n"
				+ "'http://www.lattice.org#VERY'(X^^TX,Z^^TX):-Z is X*X.\n"
				+ "'http://www.lattice.org#MORE_OR_LESS'(X^^TX,Z^^TX):-Z is sqrt(X).\n"
				+ "'http://www.lattice.org#CLOSE_TO'(X^^TX,L^^TL,A^^TA,Z^^TX):-Z is 1/(1+ ((X-L)/A)^2).\n"
				+ "'http://www.lattice.org#AT_LEAST'(X^^TX,L^^TL,A^^TA,Z^^TX):-X =< A,!, Z is 0.\n"
				+ "'http://www.lattice.org#AT_LEAST'(X^^TX,L^^TL,A^^TA,Z^^TX):-A < X, X < L,!, Z is (X-A)/(L-A).\n"
				+ "'http://www.lattice.org#AT_LEAST'(X^^TX,L^^TL,A^^TA,Z^^TX):-L =< X,!, Z is 1.\n"
				+ "'http://www.lattice.org#AT_MOST'(X^^TX,L^^TL,A^^TA,Z^^TX):-X >= A,!, Z is 0.\n"
				+ "'http://www.lattice.org#AT_MOST'(X^^TX,L^^TL,A^^TA,Z^^TX):-A > X , X > L,!, Z is (A-X)/(A-L).\n"
				+ "'http://www.lattice.org#AT_MOST'(X^^TX,L^^TL,A^^TA,Z^^TX):-X =< L,!, Z is 1.\n";

		editorP.setValue(pp + ops);

		String ruleop1 = "retractall('http://www.lattice.org#AND_LUK'(A,B,C)),asserta(('http://www.lattice.org#AND_LUK'(X^^TX,Y^^TY,Z^^TX):- Z is max(X+Y-1,0)))";
		Query q23 = new Query(ruleop1);
		System.out.println((q23.hasSolution() ? ruleop1 : ""));
		q23.close();

		String ruleop2 = "retractall('http://www.lattice.org#AND_GOD'(A,B,C)),asserta(('http://www.lattice.org#AND_GOD'(X^^TX,Y^^TY,Z^^TX):-Z is min(X,Y)))";
		Query q24 = new Query(ruleop2);
		System.out.println((q24.hasSolution() ? ruleop2 : ""));
		q24.close();

		String ruleop3 = "retractall('http://www.lattice.org#AND_PROD'(A,B,C)),asserta(('http://www.lattice.org#AND_PROD'(X^^TX,Y^^TY,Z^^TX):-Z is X*Y))";
		Query q25 = new Query(ruleop3);
		System.out.println((q25.hasSolution() ? ruleop3 : ""));
		q25.close();

		String ruleop4 = "retractall('http://www.lattice.org#OR_LUK'(A,B,C)),asserta(('http://www.lattice.org#OR_LUK'(X^^TX,Y^^TY,Z^^TX):-Z is min(X+Y,1)))";
		Query q26 = new Query(ruleop4);
		System.out.println((q26.hasSolution() ? ruleop4 : ""));
		q26.close();

		String ruleop5 = "retractall('http://www.lattice.org#OR_GOD'(A,B,C)),asserta(('http://www.lattice.org#OR_GOD'(X^^TX,Y^^TY,Z^^TX):-Z is max(X,Y)))";
		Query q27 = new Query(ruleop5);
		System.out.println((q27.hasSolution() ? ruleop5 : ""));
		q27.close();

		String ruleop6 = "retractall('http://www.lattice.org#OR_PROD'(A,B,C)),asserta(('http://www.lattice.org#OR_PROD'(X^^TX,Y^^TY,Z^^TX):-Z is X+Y-X*Y))";
		Query q28 = new Query(ruleop6);
		System.out.println((q28.hasSolution() ? ruleop6 : ""));
		q28.close();

		String ruleop7 = "retractall('http://www.lattice.org#MEAN'(A,B,C)),asserta(('http://www.lattice.org#MEAN'(X^^TX,Y^^TY,Z^^TX):-Z is X+Y/2))";
		Query q29 = new Query(ruleop7);
		System.out.println((q29.hasSolution() ? ruleop7 : ""));
		q29.close();

		String ruleop8 = "retractall('http://www.lattice.org#WSUM'(A,B,C,D,E)),asserta(('http://www.lattice.org#WSUM'(U^^TU,X^^TX,V^^TV,Y^^TY,Z^^TX):-Z is U*X+V*Y))";
		Query q30 = new Query(ruleop8);
		System.out.println((q30.hasSolution() ? ruleop8 : ""));
		q30.close();

		String ruleop9 = "retractall('http://www.lattice.org#WMAX'(A,B,C,D,E)),asserta(('http://www.lattice.org#WMAX'(U^^TU,X^^TX,V^^TV,Y^^TY,Z^^TX):-Z is max(min(U,X)+min(V,Y))))";
		Query q31 = new Query(ruleop9);
		System.out.println((q31.hasSolution() ? ruleop9 : ""));
		q31.close();

		String ruleop10 = "retractall('http://www.lattice.org#WMIN'(A,B,C,D,E)),asserta(('http://www.lattice.org#WMIN'(U^^TU,X^^TX,V^^TV,Y^^TY,Z^^TX):-Z is min(max(1-U,X)+max(1-V,Y))))";
		Query q32 = new Query(ruleop10);
		System.out.println((q32.hasSolution() ? ruleop10 : ""));
		q32.close();

		String ruleop11 = "retractall('http://www.lattice.org#VERY'(A,B)),asserta(('http://www.lattice.org#VERY'(X^^TX,Z^^TX):-Z is X*X ))";
		Query q33 = new Query(ruleop11);
		System.out.println((q33.hasSolution() ? ruleop11 : ""));
		q33.close();

		String ruleop12 = "retractall('http://www.lattice.org#MORE_OR_LESS'(A,B)),asserta(('http://www.lattice.org#MORE_OR_LESS'(X^^TX,Z^^TX):-Z is sqrt(X) ))";
		Query q34 = new Query(ruleop12);
		System.out.println((q34.hasSolution() ? ruleop12 : ""));
		q34.close();

		String ruleop13 = "retractall('http://www.lattice.org#CLOSE_TO'(A,B,C,D)),asserta(('http://www.lattice.org#CLOSE_TO'(X^^TX,L^^TL,A^^TA,Z^^TX):-Z is 1/(1+ ((X-L)/A)^2) ))";
		Query q35 = new Query(ruleop13);
		System.out.println((q35.hasSolution() ? ruleop13 : ""));
		q35.close();

		String ruleop14 = "retractall('http://www.lattice.org#AT_LEAST'(A,B,C,D)),asserta(('http://www.lattice.org#AT_LEAST'(X^^TX,L^^TL,A^^TA,Z^^TX):-X =< A,!, Z is 0 )),"
				+ "asserta(('http://www.lattice.org#AT_LEAST'(X^^TX,L^^TL,A^^TA,Z^^TX):-A < X, X < L,!, Z is (X-A)/(L-A) )),"
				+ "asserta(('http://www.lattice.org#AT_LEAST'(X^^TX,L^^TL,A^^TA,Z^^TX):-L =< X,!, Z is 1 ))";
		Query q36 = new Query(ruleop14);
		System.out.println((q36.hasSolution() ? ruleop14 : ""));
		q36.close();

		String ruleop15 = "retractall('http://www.lattice.org#AT_MOST'(A,B,C,D)),asserta(('http://www.lattice.org#AT_MOST'(X^^TX,L^^TL,A^^TA,Z^^TX):-X >= A,!, Z is 0 )),"
				+ "asserta(('http://www.lattice.org#AT_MOST'(X^^TX,L^^TL,A^^TA,Z^^TX):-A > X , X > L,!, Z is (A-X)/(A-L) )),"
				+ "asserta(('http://www.lattice.org#AT_MOST'(X^^TX,L^^TL,A^^TA,Z^^TX):-X =< L,!, Z is 1 ))";
		Query q37 = new Query(ruleop15);
		System.out.println((q37.hasSolution() ? ruleop15 : ""));
		q37.close();

		String t21 = "rdf_reset_db";
		Query q21 = new Query(t21);
		System.out.print((q21.hasSolution() ? "" : ""));
		q21.close();

		run.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				step++;
				pSPARQL ps = new pSPARQL();

				String s = FsaSPARQL.FSAtoSPARQL(editor.getValue());

				rules = ps.SPARQLtoProlog(s, step);

				String pp = "";
				String prule = "";
				for (List<String> r : rules) {
					prule = r.get(0) + ":-";
					for (int i = 1; i < r.size(); i++) {
						prule = prule + "\n       " + r.get(i) + ",";
					}
					prule = prule.substring(0, prule.length() - 1) + ".";
					pp = pp + "\n" + prule;

				}

				/*
				 * PARA PODER ACTUALIZAR EL EDITOR String content = editorOntology.getValue();
				 * String path = file.getValue(); try { Files.write( Paths.get(path),
				 * content.getBytes(), StandardOpenOption.CREATE); } catch (IOException e) { //
				 * TODO Auto-generated catch block e.printStackTrace(); }
				 */

				String t21b = "rdf_reset_db";
				Query q21b = new Query(t21b);
				System.out.print((q21b.hasSolution() ? "" : ""));
				q21b.close();

				String t21c = " working_directory(_,\"C:/\")";
				Query q21c = new Query(t21c);
				System.out.print((q21c.hasSolution() ? "" : ""));
				q21c.close();

				String t2 = "rdf_load('" + file.getValue() + "')";
				Query q2 = new Query(t2);
				System.out.print((q2.hasSolution() ? "" : ""));
				q2.close();

				String t22 = "rdf(X,Y,Z)";
				Query q22 = new Query(t22);
				String rdfs = "";
				Map<String, Term>[] srdfs = q22.allSolutions();
				q22.close();
				for (Map<String, Term> solution : srdfs) {
					rdfs = rdfs + "rdf(" + solution.get("X") + ',' + solution.get("Y") + ',' + solution.get("Z")
							+ ").\n";
				}

				editorP.setValue(pp + '\n' + ops + rdfs);

				String prule2 = "";
				System.out.println("Number of rules: " + rules.size());
				for (List<String> r : rules) {

					String dr = r.get(0);
					Query drq = new Query("retractall(" + dr + ")");
					System.out.print((drq.hasSolution() ? "" : ""));
					drq.close();

					prule2 = r.get(0) + ":-";
					for (int i = 1; i < r.size(); i++) {
						prule2 = prule2 + r.get(i) + ',';
					}
					prule2 = prule2.substring(0, prule2.length() - 1);
					String aprule = "asserta((" + prule2 + "))";
					Query q3 = new Query(aprule);
					System.out.println((q3.hasSolution() ? aprule : ""));
					q3.close();

				}
				List<HashMap<String, Term>> rows = new ArrayList<>();

				answers.removeAllColumns();
				
				 
				
			   
			    
			    

				answers.setItems(rows);
				
				 
			    
			    Atom t = new Atom("Null");
			    

				Query q3 = new Query(rules.get(0).get(0));
				Map<String, Term>[] sols = q3.allSolutions();
				q3.close();
				
				 
					
					for (Map<String,Term> solution: sols)
					{ 
					  	Set<String> sol = solution.keySet();
					  	for (String var: sol)
					  	{  
					    if (solution.get(var).isCompound()) {solution.put(var, solution.get(var).arg(1));}
					  	if (solution.get(var).isVariable()) {solution.put(var, t); }
					  	}
					}
				 
				
				
				for (Map<String, Term> solution : sols) {
					rows.add((HashMap<String, Term>) solution);
					
				}
				System.out.println("Yes: answers " + sols.length);
				answers.setItems(rows);

				if (rows.size() > 0) {
					HashMap<String, Term> sr = rows.get(0);
					 
					for (Map.Entry<String, Term> entry : sr.entrySet()) {
						answers.addColumn(h -> h.get(entry.getKey())).setCaption(entry.getKey());
					}
				}

			}

		});

		edS.setContent(editor);
		edP.setContent(editorP);

		layout.addComponent(lab);
		layout.addComponent(examples);
		layout.addComponent(file);
		layout.addComponent(edS);
		layout.addComponent(edP);
		layout.addComponent(run);

		/*
		 * PARA LEER FICHERO EN EL EDITOR pSPARQL ont = new pSPARQL(); try { String
		 * ontology = ont.readFile(file.getValue()); editorOntology.setValue(ontology);
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		String ontology;
		try {
			ontology = readStringFromURL(file.getValue());
			editorOntology.setValue(ontology);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Notification.show(e.getMessage());
		}

		edO.setContent(editorOntology);
		editorOntology.setHeight("300px");
		editorOntology.setWidth("2000px");
		editorOntology.setFontSize("12pt");
		editorOntology.setMode(AceMode.sql);
		editorOntology.setTheme(AceTheme.eclipse);
		editorOntology.setUseWorker(true);
		editorOntology.setReadOnly(false);
		editorOntology.setShowInvisibles(false);
		editorOntology.setShowGutter(false);
		editorOntology.setShowPrintMargin(false);
		editorOntology.setUseSoftTabs(false);

		answers.setSizeFull();
		layout.addComponents(answers);
		layout.addComponent(edO);

		setContent(layout);
		this.setSizeFull();

	}

	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
	}
}
