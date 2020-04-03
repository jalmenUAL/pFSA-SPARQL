package pSPARQL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

/*import org.jpl7.Atom;
import org.jpl7.Query;
import org.jpl7.Term;*/

import org.apache.log4j.varia.NullAppender;
import org.jpl7.Term;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;
import com.hp.hpl.jena.sparql.syntax.RecursiveElementVisitor;
import com.hp.hpl.jena.util.FileUtils;
import com.vaadin.ui.Notification;

import fsaSPARQL.FsaSPARQL;

public class pSPARQL {

	Integer next = 1;
	Integer current = 0;

	Integer nvar = 0;

	Boolean firstblock = true;
	Boolean firstoptional = true;
	Boolean firstunion = true;
	Boolean firstminus = true;
	Boolean firstselect = true;

	List<String> vars = new ArrayList();
	List<List<String>> rules = new ArrayList();
	
	 

	public String readFile(String pathname) throws IOException {

		File file = new File(pathname);
		StringBuilder fileContents = new StringBuilder((int) file.length());
		Scanner scanner = new Scanner(file);
		String lineSeparator = System.getProperty("line.separator");

		try {
			while (scanner.hasNextLine()) {
				fileContents.append(scanner.nextLine() + lineSeparator);
			}
			return fileContents.toString();
		} finally {
			scanner.close();
		}
	}

	public List<List<String>> SPARQLtoProlog(String queryString, Integer step) {

		final Query query = QueryFactory.create(queryString);
		
		if (    
				query.hasValues() ||
				
				query.hasValues() ||
				
				query.isConstructType() ||
				
				query.isDescribeType() || 
				
				query.isDistinct() ||
				
				query.hasAggregators() ||
				
				query.hasOrderBy() ||
				
				!query.getGraphURIs().isEmpty() ||
				
				!query.getNamedGraphURIs().isEmpty() ||
				
				query.hasLimit())
				
				{System.out.println("SPARQL expression not supported");
				
				
				}
				
				
				else
				
				{

		rules.add(current, new ArrayList());
		
		for (String v:query.getResultVars() )
		{vars.add(v.toUpperCase());}
		
		 

		String head;
		
		if (vars.isEmpty()) { if (current==0 && step==0) {head = "p";}
		else {head = "p" + current + "_" + step;}
			
		}
		else
		{
			if (current==0 && step==0) {head = "p"+"(";}
			else {head = "p" + current + "_" + step+"(";}
			
		for (String v : vars) {
			head = head + v.toUpperCase() + ",";
		}

		head = head.substring(0, head.length() - 1);
		head = head + ")";
		}
		rules.get(current).add(0, head);
		
		Element e = query.getQueryPattern();
		
		
		elementGroup((ElementGroup)e,step);
		}
		return rules;}

		 
	
	public void elementFilter(ElementFilter el,Integer step) {
		if (el.getExpr().getFunction().getFunctionName(null) == "exists") {
			
			
			List<String> varstemp = new ArrayList<String>();
			for (String v : vars) {
				varstemp.add(v);
			}

			Integer tmp = current;
			current = next;
			next++;
			rules.add(current, new ArrayList());
			
			 
					
			Element ex = ((ExprFunctionOp) el.getExpr().getFunction()).getElement();
			if (ex instanceof ElementPathBlock) {
				elementPathBlock((ElementPathBlock) ex, step);
				
			} else if (ex instanceof ElementOptional) {
				elementOptional((ElementOptional) ex, step);
			}
			 else if (ex instanceof ElementMinus) {
			elementMinus((ElementMinus) ex, step);
			 }
			 else if (ex instanceof ElementSubQuery) {
					elementSubQuery((ElementSubQuery) ex, step);
				}
			 else if (ex instanceof ElementGroup) {
					elementGroup((ElementGroup) ex, step);
				}
			 else if (ex instanceof ElementFilter) {
					elementFilter((ElementFilter) ex, step);
				}
			 else if (ex instanceof ElementBind) {
					elementBind((ElementBind) ex, step);
				}
			 else 
			 {System.out.println("SPARQL expression not supported");
			 rules.clear();
			 }
			
			 
			
			String head;
			
			if (vars.isEmpty()) {if (current==0 && step==0) {head = "p";}
			else {head = "p" + current + "_" + step;}
				
			}
			else
			{
				if (current==0 && step==0) {head = "p"+"(";}
				else {head = "p" + current + "_" + step+"(";}
				
			for (String v : vars) {
				head = head + v.toUpperCase() + ",";
			}
			head = head.substring(0, head.length() - 1);
			head = head + ")";
			}
			 
			rules.get(current).add(0, head);
			rules.get(tmp).add(head);
			rules.get(current).add("!");
 			
 			
 			for (String v : vars) {
 				if (!varstemp.contains(v.toUpperCase())){varstemp.add(v.toUpperCase());}
 			}
 			vars.clear();
 			for (String v : varstemp) {
				vars.add(v);
			}
 			current = tmp;
 			
			 

		} else

		if (el.getExpr().getFunction().getFunctionName(null) == "notexists") {
			
			List<String> varstemp = new ArrayList<String>();
			for (String v : vars) {
				varstemp.add(v);
			}

			Integer tmp = current;
			current = next;
			next++;
			rules.add(current, new ArrayList());
			
			 
					
			Element ex = ((ExprFunctionOp) el.getExpr().getFunction()).getElement();
			if (ex instanceof ElementPathBlock) {
				elementPathBlock((ElementPathBlock) ex, step);
				
			} else if (ex instanceof ElementOptional) {
				elementOptional((ElementOptional) ex, step);
			}
			 else if (ex instanceof ElementMinus) {
			elementMinus((ElementMinus) ex, step);
			 }
			 else if (ex instanceof ElementSubQuery) {
					elementSubQuery((ElementSubQuery) ex, step);
				}
			 else if (ex instanceof ElementGroup) {
					elementGroup((ElementGroup) ex, step);
				}
			 else if (ex instanceof ElementFilter) {
					elementFilter((ElementFilter) ex, step);
				}
			 else if (ex instanceof ElementBind) {
					elementBind((ElementBind) ex, step);
				}
			 else 
			 {System.out.println("SPARQL expression not supported");
			 rules.clear();
			 }
			
			 
			
			String head;
			
			if (vars.isEmpty()) { if (current==0 && step==0) {head = "p";}
			else {head = "p" + current + "_" + step;}
				
			}
			else
			{
				if (current==0 && step==0) {head = "p"+"(";}
				else {head = "p" + current + "_" + step+"(";}
			for (String v : vars) {
				head = head + v.toUpperCase() + ",";
			}
			head = head.substring(0, head.length() - 1);
			head = head + ")";
			}
			 
			rules.get(current).add(0, head);
			rules.get(tmp).add("(\\+("+head+"))");
			rules.get(current).add("!");
 			
 			
 			for (String v : vars) {
 				if (!varstemp.contains(v.toUpperCase())){varstemp.add(v.toUpperCase());}
 			}
 			vars.clear();
 			for (String v : varstemp) {
				vars.add(v);
			}
 			current = tmp;
 			
			
 		} else if ((el.getExpr().getFunction().getOpName().toString() == "<")
				|| (el.getExpr().getFunction().getOpName().toString() == "<=")
				|| (el.getExpr().getFunction().getOpName().toString() == "=")
				|| (el.getExpr().getFunction().getOpName().toString() == ">")
				|| (el.getExpr().getFunction().getOpName().toString() == ">="))

		{
			nvar++;
			List<String> ss = new ArrayList<>(SExprtoPTerm(el.getExpr(), null));
			for (int i = 0; i < ss.size(); i++) {
				rules.get(current).add(ss.get(i));
			}

		}
	}
	
	public void elementBind(ElementBind el,Integer step) {
		nvar++;
		List<String> ss = new ArrayList<>(SExprtoPTerm(el.getExpr(), el.getVar().asNode()));
		for (int i = 0; i < ss.size(); i++) {
			rules.get(current).add(ss.get(i));
		}
	}
	
	
	 
	

	public void elementPathBlock(ElementPathBlock el, Integer step) {

		 
		 
 		List<TriplePath> lp = el.getPattern().getList();
		for (TriplePath p : lp) {
			
			
			 
			if (!p.getSubject().isConcrete() && 
					!vars.contains(STermtoPTerm(p.getSubject()))) {
				vars.add(STermtoPTerm(p.getSubject()));
			}
			if (!p.getPredicate().isConcrete() && 
					!vars.contains(STermtoPTerm(p.getPredicate()))) {
				vars.add(STermtoPTerm(p.getPredicate()));
			}
				
			if (!p.getObject().isConcrete() && 
					!vars.contains(STermtoPTerm(p.getObject()))) {
				vars.add(STermtoPTerm(p.getObject()));
			}
			
			String rule = "rdf(" + STermtoPTerm(p.getSubject()) + "," + STermtoPTerm(p.getPredicate()) + ","
					+ STermtoPTerm(p.getObject()) + ")";
			List<String> l = rules.get(current);
			l.add(rule);
		}
	};
	
	public void elementUnion(ElementUnion el, Integer step) {

		String union ="(";
		
		for (Element e : el.getElements()) {
			
			List<String> varstemp = new ArrayList<String>();
			for (String v : vars) {
				varstemp.add(v);
			}
			
			vars.clear(); 

			Integer tmp = current;
			current = next;
			next++;
			rules.add(current, new ArrayList());
			
			if (e instanceof ElementPathBlock) {
				elementPathBlock((ElementPathBlock) e, step);
				
			} else if (e instanceof ElementOptional) {
				elementOptional((ElementOptional) e, step);
			}
			 else if (e instanceof ElementMinus) {
			elementMinus((ElementMinus) e, step);
		}
			 else if (e instanceof ElementSubQuery) {
					elementSubQuery((ElementSubQuery) e, step);
				}
			 else if (e instanceof ElementGroup) {
					elementGroup((ElementGroup) e, step);
				}
			 else if (e instanceof ElementFilter) {
					elementFilter((ElementFilter) e, step);
				}
			 else if (e instanceof ElementBind) {
					elementBind((ElementBind) e, step);
				}
			 else if (e instanceof ElementUnion) {
					elementUnion((ElementUnion) e, step);
				}
			 else 
			 {System.out.println("SPARQL expression not supported");
			 rules.clear();
			 }
			
			String head;
			
			if (vars.isEmpty()) { if (current==0 && step==0) {head = "p";}
			else {head = "p" + current + "_" + step;}
				
			}
			else
			{
				if (current==0 && step==0) {head = "p"+"(";}
				else {head = "p" + current + "_" + step+"(";}
			for (String v : vars) {
				head = head + v.toUpperCase() + ",";
			}
			head = head.substring(0, head.length() - 1);
			head = head + ")";
			}
			 
			rules.get(current).add(0, head);
			
			union = union + head + ";";
			
			//rules.get(tmp).add(head);
			
 			
 			
 			for (String v : vars) {
 				if (!varstemp.contains(v.toUpperCase())){varstemp.add(v.toUpperCase());}
 			}
 			vars.clear();
 			for (String v : varstemp) {
				vars.add(v);
			}
 			current = tmp;
		}
		union = union.substring(0, union.length() - 1);
		union = union + ")";
		rules.get(current).add(union);
		
		
	}

	public void elementGroup(ElementGroup el, Integer step) {

		for (Element e : el.getElements()) {
			
			 
			
			  

			
			
			if (e instanceof ElementPathBlock) {
				elementPathBlock((ElementPathBlock) e, step);
				
			} else if (e instanceof ElementOptional) {
				elementOptional((ElementOptional) e, step);
			}
			 else if (e instanceof ElementMinus) {
			elementMinus((ElementMinus) e, step);
		}
			 else if (e instanceof ElementSubQuery) {
					elementSubQuery((ElementSubQuery) e, step);
				}
			 else if (e instanceof ElementUnion) {
					elementUnion((ElementUnion) e, step);
				}
			 else if (e instanceof ElementFilter) {
					elementFilter((ElementFilter) e, step);
				}
			 else if (e instanceof ElementBind) {
					elementBind((ElementBind) e, step);
				}
			 else if (e instanceof ElementGroup) {
					elementGroup((ElementGroup) e, step);
				}
			 else 
			 {System.out.println("SPARQL expression not supported");
			 rules.clear();
			 }
			  
			 
			  
		}

	}

	public void elementMinus(ElementMinus el, Integer step) {
		Element e = el.getMinusElement();

		List<String> varstemp = new ArrayList<String>();
		for (String v : vars) {
			varstemp.add(v);
		}

		Integer tmp = current;
		current = next;
		next++;
		rules.add(current, new ArrayList());

		if (e instanceof ElementPathBlock) {
			elementPathBlock((ElementPathBlock) e, step);
			
		} else if (e instanceof ElementOptional) {
			elementOptional((ElementOptional) e, step);
		}
		 else if (e instanceof ElementMinus) {
		elementMinus((ElementMinus) e, step);
		 }
		 else if (e instanceof ElementSubQuery) {
				elementSubQuery((ElementSubQuery) e, step);
			}
		 else if (e instanceof ElementGroup) {
				elementGroup((ElementGroup) e, step);
			}
		 else if (e instanceof ElementFilter) {
				elementFilter((ElementFilter) e, step);
			}
		 else if (e instanceof ElementBind) {
				elementBind((ElementBind) e, step);
			}
		 else 
		 {System.out.println("SPARQL expression not supported");
		 rules.clear();
		 }

		String head;
		
		if (vars.isEmpty()) { 
			/* GOAL */
			if (current==0 && step==0) {head = "p";}
			else {head = "p" + current + "_" + step;}
			
		}
		else
		{
		/* GOAL */
		if (current==0 && step==0) {head = "p"+"(";}
		else {
		head = "p" + current + "_" + step + "(";}
		
		for (String v : vars) {
			head = head + v.toUpperCase() + ",";
		}
		head = head.substring(0, head.length() - 1);
		head = head + ")";
		}
		rules.get(current).add(0, head);
		rules.get(tmp).add("(\\+("+head+"))");
	
		for (String v : vars) {
			if (!varstemp.contains(v.toUpperCase())){varstemp.add(v.toUpperCase());}
		}
		vars.clear();
		for (String v : varstemp) {
			vars.add(v);
		}
		current = tmp;
			
		}
	
	public void elementOptional(ElementOptional el, Integer step) {
		Element e = el.getOptionalElement();

		List<String> varstemp = new ArrayList<String>();
		for (String v : vars) {
			varstemp.add(v);
		}

		Integer tmp = current;
		current = next;
		next++;
		rules.add(current, new ArrayList());

		if (e instanceof ElementPathBlock) {
			elementPathBlock((ElementPathBlock) e, step);
			
		} else if (e instanceof ElementOptional) {
			elementOptional((ElementOptional) e, step);
		}
		 else if (e instanceof ElementMinus) {
		elementMinus((ElementMinus) e, step);
		 }
		 else if (e instanceof ElementSubQuery) {
				elementSubQuery((ElementSubQuery) e, step);
			}
		 else if (e instanceof ElementGroup) {
				elementGroup((ElementGroup) e, step);
			}
		 else if (e instanceof ElementFilter) {
				elementFilter((ElementFilter) e, step);
			}
		 else if (e instanceof ElementBind) {
				elementBind((ElementBind) e, step);
			}
		 else 
		 {System.out.println("SPARQL expression not supported");
		 rules.clear();
		 }

		String head;
		
			if (vars.isEmpty()) { if (current==0 && step==0) {head = "p";}
			else {head = "p" + current + "_" + step;}
			
		}
		else
		{
			if (current==0 && step==0) {head = "p"+"(";}
			else {head = "p" + current + "_" + step+"(";}	
		
		for (String v : vars) {
			head = head + v.toUpperCase() + ",";
		}
		head = head.substring(0, head.length() - 1);
		head = head + ")";
		}

		rules.get(current).add(0, head);
		rules.get(tmp).add("("+head+";"+"\\+("+head+"))");
		
		for (String v : vars) {
			if (!varstemp.contains(v.toUpperCase())){varstemp.add(v.toUpperCase());}
		}
		vars.clear();
		for (String v : varstemp) {
			vars.add(v);
		}
		current = tmp;

	};
	
	 
	public void elementSubQuery(ElementSubQuery el, Integer step)
	{
		
		Element e = el.getQuery().getQueryPattern();
		
		 
		
 		List<String> varstemp = new ArrayList<String>();
		for (String v : vars) {
			varstemp.add(v);
		}
	 	
		Integer tmp = current;
		current = next;
		next++;
		rules.add(current, new ArrayList());

		//vars.clear();
		
		
		
		if (e instanceof ElementPathBlock) {
			elementPathBlock((ElementPathBlock) e, step);
			
		} else if (e instanceof ElementOptional) {
			elementOptional((ElementOptional) e, step);
		}
		 else if (e instanceof ElementMinus) {
		elementMinus((ElementMinus) e, step);
		 }
		 else if (e instanceof ElementSubQuery) {
				elementSubQuery((ElementSubQuery) e, step);
			}
		 else if (e instanceof ElementGroup) {
				elementGroup((ElementGroup) e, step);
			}
		 else if (e instanceof ElementFilter) {
				elementFilter((ElementFilter) e, step);
			}
		 else if (e instanceof ElementBind) {
				elementBind((ElementBind) e, step);
			}
		 else 
		 {System.out.println("SPARQL expression not supported");
		 rules.clear();
		 }
		  
		 
		
		String head;
		
		if (vars.isEmpty()) { if (current==0 && step==0) {head = "p";}
		else {head = "p" + current + "_" + step;}
			
		}
		else
		{
			if (current==0 && step==0) {head = "p"+"(";}
			else {head = "p" + current + "_" + step+"(";}
			
		for (String v : vars) {
			head = head + v.toUpperCase() + ",";
		}
		head = head.substring(0, head.length() - 1);
		head = head + ")";
		}
		
	 
		rules.get(current).add(0, head);
		rules.get(tmp).add(head);
		 
		
		for (String v : vars) {
			if (!varstemp.contains(v.toUpperCase())){varstemp.add(v.toUpperCase());}
		}
		vars.clear();
		for (String v : varstemp) {
			vars.add(v);
		}
		
		 
		
		current = tmp;
		
	 
	}

	public static String STermtoPTerm(Node st) {
		String pt = "";

		if (st.isVariable()) {
			if (st.getName().startsWith("?")) {
				pt = "X" + st.getName().substring(1);
			} else
				pt = st.toString().replace('?', ' ').replaceAll("\\s", "").toUpperCase();

		} else if (st.isURI()) {
			pt = "'" + st.toString() + "'";
		}

		else if (st.isLiteral()) {
			if (st.getLiteralDatatypeURI() == null)

			{
				if (st.toString().startsWith("\"#"))
					{pt = st.toString().replaceAll("\"", "");}
				else {pt = st.toString() + "^^" + "'http://www.w3.org/2001/XMLSchema#string'";}
			}

			else {
				pt = st.getLiteralValue() + "^^'" + st.getLiteralDatatypeURI() + "'";
			}

		}

		return pt;
	}

	public Stack<String> SExprtoPTerm(Expr st, Node var) {
		Stack<String> pt = new Stack<String>();

		if (var == null) {
			if (st.isVariable()) {
				pt.add(st.toString().replace('?', ' ').replaceAll("\\s", "").toUpperCase());

			} else if (st.isConstant()) {
				//System.out.println(st);
				if (st.toString().startsWith("\"#"))
					{pt.add(st.toString().replaceAll("\"",""));}
				else {pt.add(st.toString() + "^^'" + st.getConstant().getDatatypeURI() + "'");}

			}

			else if (st.isFunction()) {

				if (st.getFunction().getFunctionIRI() == null) {

					Integer act = nvar;
					nvar++;

					List<String> ss = new ArrayList<>(
							SExprtoPTerm(st.getFunction().getArg(1), NodeFactory.createVariable("A" + act)));

					for (int i = 0; i < ss.size(); i++) {
						pt.add(ss.get(i));
					}

					nvar++;
					List<String> ss2 = new ArrayList<>(
							SExprtoPTerm(st.getFunction().getArg(2), NodeFactory.createVariable("B" + act)));

					for (int i = 0; i < ss2.size(); i++) {
						pt.add(ss2.get(i));
					}

					pt.add("{ A" + act + st.getFunction().getOpName() + "B" + act + " }");

					nvar++;
				} else {

					List<Expr> args = st.getFunction().getArgs();
					List<String> varsh = new ArrayList();
					String argsvars = "";

					Integer act = nvar;

					for (int i = 0; i < args.size(); i++) {
						varsh.add("A" + i + "_" + act);
					}
					;

					for (int i = 0; i < args.size(); i++) {
						argsvars = argsvars + "A" + i + "_" + act + ",";
					}
					;
					argsvars = argsvars.substring(0, argsvars.length() - 1);

					for (int i = 0; i < args.size(); i++) {
						nvar++;
						List<String> ss = new ArrayList<>(
								SExprtoPTerm(args.get(i), NodeFactory.createVariable(varsh.get(i))));
						for (int j = 0; j < ss.size(); j++) {
							pt.add(ss.get(j));
						}
					}
					;

					pt.add("'" + st.getFunction().getFunctionIRI() + "'(" + argsvars + ",VAR" + act + ")");

					nvar++;

				}

			}
		} else

		if (st.isVariable()) {
			pt.add(st.toString().replace('?', ' ').replaceAll("\\s", "").toUpperCase() + "="
					+ var.toString().replace('?', ' ').replaceAll("\\s", "").toUpperCase());

		} else if (st.isConstant()) {
			//System.out.println(st.toString());
			if (st.toString().startsWith("\"#"))
			{   
				pt.add(st.toString().replaceAll("\"", "") + "="
						+ var.toString().replace('?', ' ').replaceAll("\\s", "").toUpperCase());}
		else
			{pt.add(st.toString() + "^^'" + st.getConstant().getDatatypeURI() + "'" + "="
					+ var.toString().replace('?', ' ').replaceAll("\\s", "").toUpperCase());}

		}

		else if (st.isFunction()) {

			if (st.getFunction().getFunctionIRI() == null) {

				Integer act = nvar;
				nvar++;

				List<String> ss = new ArrayList<>(
						SExprtoPTerm(st.getFunction().getArg(1), NodeFactory.createVariable("A" + act)));

				for (int i = 0; i < ss.size(); i++)
					pt.add(ss.get(i));

				nvar++;
				List<String> ss2 = new ArrayList<>(
						SExprtoPTerm(st.getFunction().getArg(2), NodeFactory.createVariable("B" + act)));

				for (int i = 0; i < ss2.size(); i++)
					pt.add(ss2.get(i));

				pt.add("A" + act + "=" + "U" + act + "^^TU" + act);
				pt.add("B" + act + "=" + "V" + act + "^^TV" + act);

				pt.add("W" + act + " is " + "U" + act + st.getFunction().getOpName() + "V" + act);

				String res = var.toString().replace('?', ' ').replaceAll("\\s", "").toUpperCase();
				pt.add(res + "=" + "W" + act + "^^TV" + act);

				nvar++;
			} else {

				List<Expr> args = st.getFunction().getArgs();
				List<String> varsh = new ArrayList();
				String argsvars = "";

				Integer act = nvar;

				for (int i = 0; i < args.size(); i++) {
					varsh.add("A" + i + "_" + act);
				}
				;

				for (int i = 0; i < args.size(); i++) {
					argsvars = argsvars + "A" + i + "_" + act + ",";
				}
				;
				argsvars = argsvars.substring(0, argsvars.length() - 1);

				for (int i = 0; i < args.size(); i++) {
					nvar++;
					List<String> ss = new ArrayList<>(
							SExprtoPTerm(args.get(i), NodeFactory.createVariable(varsh.get(i))));
					for (int j = 0; j < ss.size(); j++) {
						pt.add(ss.get(j));
					}
				}
				;

				pt.add("VAR" + act + "=" + var.toString().replace('?', ' ').replaceAll("\\s", "").toUpperCase());

				pt.add("'" + st.getFunction().getFunctionIRI() + "'(" + argsvars + ",VAR" + act + ")");

				nvar++;
			}

		}

		return pt;
	}

	public String SPARQL(String filei, String queryStr) {

		OntModel model = ModelFactory.createOntologyModel();
		model.read(filei);
		com.hp.hpl.jena.query.Query query = QueryFactory.create(queryStr);

		if (query.isSelectType()) {
			ResultSet result = (ResultSet) QueryExecutionFactory.create(query, model).execSelect();
			File theDir = new File("tmp-sparql");
			if (!theDir.exists()) {
				theDir.mkdir();
			}
			String fileName = "./tmp-sparql/" + "result.owl";
			File f = new File(fileName);
			FileOutputStream file;
			try {
				file = new FileOutputStream(f);
				ResultSetFormatter.outputAsXML(file, (com.hp.hpl.jena.query.ResultSet) result);
				try {
					file.close();

				} catch (IOException e) {

					Notification.show(e.getMessage());
					// e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {

				Notification.show(e1.getMessage()); // e1.printStackTrace();
			}

			String s = "";
			try {
				s = readFile(fileName);
			} catch (IOException e) {

				Notification.show(e.getMessage());
				// e.printStackTrace();
			}

			final File[] files = theDir.listFiles();
			for (File g : files)
				g.delete();
			theDir.delete();
			return s;
		} else

		if (query.isConstructType()) {
			Model result = QueryExecutionFactory.create(query, model).execConstruct();
			File theDir = new File("tmp-sparql");
			if (!theDir.exists()) {
				theDir.mkdir();
			}
			String fileName = "./tmp-sparql/" + "result.owl";
			File f = new File(fileName);
			FileOutputStream file;
			try {
				file = new FileOutputStream(f);
				result.write(file, FileUtils.langXMLAbbrev);
				try {
					file.close();

				} catch (IOException e) {

					Notification.show(e.getMessage());
					// e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {

				Notification.show(e1.getMessage());
				// e1.printStackTrace();
			}

			String s = "";
			try {
				s = readFile(fileName);
			} catch (IOException e) {

				Notification.show(e.getMessage());
				// e.printStackTrace();
			}
			final File[] files = theDir.listFiles();
			for (File g : files)
				g.delete();
			theDir.delete();
			return s;
		} else

		if (query.isDescribeType()) {
			Model result = QueryExecutionFactory.create(query, model).execDescribe();

			File theDir = new File("tmp-sparql");
			if (!theDir.exists()) {
				theDir.mkdir();
			}
			String fileName = "./tmp-sparql/" + "result.owl";

			File f = new File(fileName);

			FileOutputStream file;

			try {
				file = new FileOutputStream(f);
				result.write(file, FileUtils.langXMLAbbrev);
				try {
					file.close();

				} catch (IOException e) {

					e.printStackTrace();
				}
			} catch (FileNotFoundException e1) {

				Notification.show(e1.getMessage());
				// e1.printStackTrace();
			}

			String s = "";
			try {
				s = readFile(fileName);
			} catch (IOException e) {

				Notification.show(e.getMessage());
				// e.printStackTrace();
			}
			final File[] files = theDir.listFiles();
			for (File g : files)
				g.delete();
			theDir.delete();
			return s;
		} else

		{
			Boolean b = QueryExecutionFactory.create(query, model).execAsk();
			return b.toString();
		}

	};

	public static void main(String[] args) {
		
		String ops = "\n'http://www.lattice.org#AND_LUK'(X^^TX,Y^^TY,Z^^TX):- Z is max(X+Y-1,0).\n"
				+ "'http://www.lattice.org#AND_GOD'(X^^TX,Y^^TY,Z^^TX):-Z is min(X,Y).\n"
				+ "'http://www.lattice.org#AND_PROD'(X^^TX,Y^^TY,Z^^TX):-Z is X*Y.\n"
				+ "'http://www.lattice.org#OR_LUK'(X^^TX,Y^^TY,Z^^TX):-Z is min(X+Y,1).\n"
				+ "'http://www.lattice.org#OR_GOD'(X^^TX,Y^^TY,Z^^TX):-Z is max(X,Y).\n"
				+ "'http://www.lattice.org#OR_PROD'(X^^TX,Y^^TY,Z^^TX):-Z is X+Y-X*Y.\n"
				+ "'http://www.lattice.org#MEAN'(X^^TX,Y^^TY,Z^^TX):-Z is X+Y/2.\n"
				+ "'http://www.lattice.org#WSUM'(U^^TU,X^^TX,V^^TV,Y^^TY,Z^^TX):-Z is U*X+V*Y.\n"
				+ "'http://www.lattice.org#WMAX'(U^^TU,X^^TX,V^^TV,Y^^TY,Z^^TX):-Z is max(min(U,X)+min(V,Y)).\n"
				+ "'http://www.lattice.org#WMIN'(U^^TU,X^^TX,V^^TV,Y^^TY,Z^^TX):-Z is min(max(1-U,X)+max(1-V,Y)).\n"
				+ "'http://www.lattice.org#VERY'(X^^TX,Z^^TX):-Z is X*X.\n"
				+ "'http://www.lattice.org#MORE_OR_LESS'(X^^TX,Z^^TX):-Z is sqrt(X).\n"
				+ "'http://www.lattice.org#CLOSE_TO'(X^^TX,L^^TL,A^^TA,Z^^TX):-Z is 1/(1+ ((X-L)/A)^2).\n"
				+ "'http://www.lattice.org#AT_LEAST'(X^^TX,L^^TL,A^^TA,Z^^TX):-X =< A,!, Z is 0.\n"
				+ "'http://www.lattice.org#AT_LEAST'(X^^TX,L^^TL,A^^TA,Z^^TX):-A < X, X < L,!, Z is (X-A)/(L-A).\n"
				+ "'http://www.lattice.org#AT_LEAST'(X^^TX,L^^TL,A^^TA,Z^^TX):-L =< X,!, Z is 1.\n"
				+ "'http://www.lattice.org#AT_MOST'(X^^TX,L^^TL,A^^TA,Z^^TX):-X >= A,!, Z is 0.\n"
				+ "'http://www.lattice.org#AT_MOST'(X^^TX,L^^TL,A^^TA,Z^^TX):-A > X , X > L,!, Z is (A-X)/(A-L).\n"
				+ "'http://www.lattice.org#AT_MOST'(X^^TX,L^^TL,A^^TA,Z^^TX):-X =< L,!, Z is 1.\n";
		
		String ops2 ="'{}'(X > Y) <- @gt(X, Y).\r\n" + 
				"\r\n" + 
				"'http://www.lattice.org#AND_LUK'(X,Y,Z):- truth_degree(&luk(X,Y), Z).\r\n" + 
				"'http://www.lattice.org#AND_GOD'(X,Y,Z):-truth_degree(&god(X,Y), Z).\r\n" + 
				"'http://www.lattice.org#AND_PROD'(X,Y,Z):-truth_degree(&prod(X,Y), Z).\r\n" + 
				"\r\n" + 
				"'http://www.lattice.org#OR_LUK'(X,Y,Z):-truth_degree(|luk(X,Y), Z).\r\n" + 
				"'http://www.lattice.org#OR_GOD'(X,Y,Z):-truth_degree(|god(X,Y), Z).\r\n" + 
				"'http://www.lattice.org#OR_PROD'(X,Y,Z):-truth_degree(|prod(X,Y), Z).\r\n" + 
				"\r\n" + 
				"'http://www.lattice.org#AGR_MEAN'(X,Y,Z) :- truth_degree(@mean(X,Y), Z).\r\n" + 
				"'http://www.lattice.org#AGR_WMEAN'(W,X,Y,Z) :- truth_degree(@wmean(W,X,Y), Z).\r\n" + 
				"'http://www.lattice.org#WSUM'(U,X,V,Y,Z):-truth_degree(@wsum(U,X,V,Y), Z).\r\n" + 
				"'http://www.lattice.org#WMAX'(U,X,V,Y,Z):-truth_degree(@wmax(U,X,V,Y), Z).\r\n" + 
				"'http://www.lattice.org#WMIN'(U,X,V,Y,Z):-truth_degree(@wmin(U,X,V,Y), Z).\r\n" + 
				"'http://www.lattice.org#VERY'(X,Z):-truth_degree(@very(X), Z).\r\n" + 
				"'http://www.lattice.org#MORE_OR_LESS'(X,Z):-truth_degree(@more_or_less(X), Z).\r\n" + 
				"'http://www.lattice.org#CLOSE_TO'(X,L,A,Z):-truth_degree(@close_to(X,L,A), Z).\r\n" + 
				"'http://www.lattice.org#AT_LEAST'(X,L,A,Z):-truth_degree(@at_least(X,L,A), Z).\r\n" + 
				"'http://www.lattice.org#AT_MOST'(X,L,A,Z):-truth_degree(@at_most(X,L,A), Z).";

		String prog1 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"

				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?Ind WHERE {" + "?Ind rdf:type sn:User "
				+ "} ";

		String prog2 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "SELECT ?Ind ?Event {" + "?Ind rdf:type sn:User ;  sn:age 47 . ?Event rdf:type sn:Event } ";

		String prog3 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "SELECT ?X ?Z  WHERE { ?X rdf:type sn:User . ?X ?Y ?Z }";

		String prog4 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "SELECT ?user WHERE {" + "?user rdf:type sn:User }";

		String prog5 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "SELECT ?Ind WHERE  {" + "?Ind rdf:type sn:User .  ?Ind sn:age ?Age  . FILTER(?Age > 30) } ";

		String prog6 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>\n"
				+ "SELECT ?NUSER1 ?NUSER2 WHERE {\n" + "?USER1 sn:name ?NUSER1 . ?USER2 sn:name ?NUSER2 . \n"
				+ "?USER1 sn:age ?AU1 . ?USER2 sn:age ?AU2 . \n " + "FILTER(?AU1 > 40 ).\n" + "FILTER (?AU2 > 50) }\n";

		String prog7 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>\n"
				+ "SELECT ?USER WHERE {\n" + "?USER sn:age ?AGE .\n" + "FILTER (?AGE > 25) .\n"
				+ "FILTER EXISTS {SELECT ?USER2 WHERE {\n" + "?USER2 sn:age ?AGE2 .\n" + "FILTER (?AGE < ?AGE2 ) }\n"
				+ "}}\n";

		String prog8 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>\n"
				+ "SELECT ?USER ?EVENT WHERE {\n" + "?USER rdf:type sn:User .\n" + "?USER sn:age ?AGE .\n"
				+ "FILTER (?AGE > 40) .\n" + "?USER sn:attends_to ?EVENT" + "}\n";

		String prog9 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"

				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?y WHERE " + "{ ?Ind rdf:type ?x ."
				+ "BIND(?x as ?y)} ";

		String prog10 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"

				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?Ind WHERE "
				+ "{ ?Ind sn:attends_to ?y .  { SELECT ?y1 WHERE {?y1 rdf:type sn:Event  "
				+ ". { SELECT ?y2 WHERE {?y2 rdf:type sn:Event   .  { SELECT ?y3 WHERE {?y3 rdf:type sn:Event }}}} "
				+ "} }}";
		String prog11 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"

				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?Ind ?age ?event WHERE "
				+ "{  ?Ind sn:age ?age ." + "OPTIONAL { SELECT ?Ind ?event WHERE { ?Ind sn:attends_to ?event "
				+ "OPTIONAL {?Ind rdf:type sn:User} } } " + "} ";

		String prog12 = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?name "
				+ "FROM NAMED   <http://www.semanticweb.org/ontologies/2011/7/miscojones.owl#> "
				+ "WHERE   { ?Ind sn:name ?name }"
				+ "ORDER BY ?Ind";

		String prog13 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>\n"
				+ "SELECT ?USER WHERE {\n" + "?USER sn:age ?AGE .\n" + "FILTER (?AGE > 25) .\n"
				+ "FILTER NOT EXISTS {SELECT ?USER2 WHERE {\n" + "?USER2 sn:age ?AGE2 .\n"
				+ "FILTER (?AGE < ?AGE2 ) }\n" + "}}\n";

		String prog14 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"

				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?Ind WHERE "
				+ "{ ?Ind rdf:type sn:User ."
				+ "OPTIONAL { ?Ind2 rdf:type sn:User . ?Ind3 rdf:type sn:User . OPTIONAL {  ?Ind4 rdf:type sn:User ."

				+ "OPTIONAL {?Ind5 rdf:type sn:User }  } } }";

		String prog15 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"

				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?Ind WHERE { "
				+ " { ?y1 rdf:type sn:Event }  " + "UNION"
				+ "{ {?y2 rdf:type sn:Event } UNION { ?y3 rdf:type sn:Event } } }";

		String prog16 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"

				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?x WHERE " + "{ ?x sn:age ?y ."
				+ "VALUES ?y {47} }";

		String prog17 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"

				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?Ind WHERE "
				+ "{ ?Ind rdf:type sn:User ." + "  { ?Ind sn:attends_to ?event2 }  " + "} ";

		String prog18 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"

				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?Ind WHERE "
				+ "{ ?Ind rdf:type sn:User .  MINUS { ?Ind2 rdf:type sn:User . MINUS { ?Ind3 rdf:type sn:User  "
				+ ". MINUS { ?Ind4 rdf:type sn:User } } } }";
		
		String prog19 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"

				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "PREFIX f: <http://www.fuzzy.org/>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "PREFIX apf: <http://jena.hpl.hp.com/ARQ/property#>" + "SELECT ?Ind ?Ind2 WHERE { "
				+ " { ?Ind rdf:type sn:Event }  " + "UNION"
				+ "{SELECT ?Ind2 WHERE {?Ind2 rdf:type sn:Event } } }";
		
		String prog20 = "PREFIX hotel: <http://www.hotels.org#>\r\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
				"PREFIX f: <http://www.fuzzy.org#>\r\n" + 
				"PREFIX l: <http://www.lattice.org#>\r\n" + 
				"SELECT ?Name  \r\n" + 
				" WHERE { ?Hotel hotel:name ?Name . \r\n" + 
				" ?Hotel rdf:type hotel:Hotel . \r\n" + 
				" {?Hotel hotel:close ( ?p1 ?l1) . \r\n" + 
				"  ?pi hotel:name \"Empire State Building\"  } UNION { ?Hotel hotel:close ( ?p2 ?l2) . ?pi2 hotel:name \"Central Park\"  \r\n" + 
				"  }}";
		
		String prog21 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "SELECT ?user WHERE { ?user rdf:type sn:User . {?user2 rdf:type sn:User} }";
		
		String prog22 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "SELECT ?user  WHERE {  {?user rdf:type ?type1} UNION {?user rdf:type ?type2} UNION {?user rdf:type ?type3} }";
		
		String prog23 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "SELECT ?user WHERE {" + "?user rdf:type ?type . {SELECT ?user WHERE {?user rdf:type ?type }} }";
		
		String prog24 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "SELECT ?user ?user2 WHERE {" + "?user sn:age ?age . {SELECT ?user2 WHERE {?user2 sn:age ?age2 . "
						+ "FILTER(?age2 > ?age )} }}";

		String prog25 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>\n"
				+ "SELECT ?USER WHERE {\n" + "?USER sn:age ?AGE .\n" + "FILTER (?AGE > 25) .\n"
				+ "FILTER NOT EXISTS {SELECT ?USER WHERE {\n" + "?USER sn:age ?AGE2 .\n"
				+ "FILTER (?AGE < ?AGE2 ) }\n" + "}}\n";
		
		 
		String prog26 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "SELECT ?user WHERE {" + "?user2 rdf:type ?type . {SELECT ?user WHERE {?user rdf:type ?type }} }";
		
		
		String prog27 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX sn: <http://www.semanticweb.org/ontologies/2011/7/socialnetwork.owl#>"
				+ "SELECT ?user WHERE {" + " FILTER EXISTS {SELECT ?user2 WHERE {?user2 rdf:type ?type }} }";
		
		String q = 
				"PREFIX movie: <http://www.movies.org#>\r\n" + 
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
						"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
						"PREFIX f: <http://www.fuzzy.org#>\r\n" + 
						"PREFIX l: <http://www.lattice.org#>\r\n" +
						"SELECT ?Movie ?Rank\r\n" + 
						"WHERE {\r\n" + 
						"?Movie f:type (movie:genre movie:Thriller '#s1').\r\n" + 
						"?Movie f:type (movie:quality movie:Good ?r) .\r\n" + 
						"BIND(l:WMEAN('#s2',?r,?c) as ?Rank)."
						+ "FILTER ('#s3' > 0.8)}";
		
		org.apache.log4j.BasicConfigurator.configure(new NullAppender());
		
		
		String filename ="file:///C:/movies.rdf";
		pSPARQL ps = new pSPARQL();
		/*List<List<String>> rules = ps.SPARQLtoProlog(q, 0);*/
		
		String fsa = FsaSPARQL.FSAtoSPARQL(args[0]);
		//System.out.println(fsa);
		List<List<String>> rules = ps.SPARQLtoProlog(fsa, 0);
		for (List<String> r : rules) {
			
			System.out.print(r.get(0)+":-\n");
			
			for (int i=1; i< r.size();i++)
			{
			if (i+1==r.size()) {System.out.print(r.get(i)+".\n");}
			else {System.out.print(r.get(i)+",\n");}
			}
			
			
		}
		
		//System.out.println(ops2);
		
		/*
		String t1 = "use_module(library(semweb/rdf11))";
		org.jpl7.Query q1 = new org.jpl7.Query(t1);
		System.out.print((q1.hasSolution() ? "" : ""));
		q1.close();

		String t11 = "use_module(library(semweb/rdf_http_plugin))";
		org.jpl7.Query q11 = new org.jpl7.Query(t11);
		System.out.print((q11.hasSolution() ? "" : ""));
		q11.close();
		
		String t21b = "rdf_reset_db";
		org.jpl7.Query q21b = new org.jpl7.Query(t21b);
		System.out.print((q21b.hasSolution() ? "" : ""));
		q21b.close();

		String t21c = " working_directory(_,\"C:/\")";
		org.jpl7.Query q21c = new org.jpl7.Query(t21c);
		System.out.print((q21c.hasSolution() ? "" : ""));
		q21c.close();

		String t2 = "rdf_load('" + filename + "')";
		org.jpl7.Query q2 = new org.jpl7.Query(t2);
		System.out.print((q2.hasSolution() ? "" : ""));
		q2.close();

		String t22 = "rdf(X,Y,Z)";
		org.jpl7.Query q22 = new org.jpl7.Query(t22);
		String rdfs = "";
		Map<String, Term>[] srdfs = q22.allSolutions();
		q22.close();
		for (Map<String, Term> solution : srdfs) {
			rdfs = rdfs + "rdf(" + solution.get("X") + ',' + solution.get("Y") + ',' + solution.get("Z")
					+ ").\n";
		}
		
		System.out.println(rdfs);
		*/
		
		//  String s = ps.SPARQL(
		//  "file:/c:/Users/Administrator/eclipse-workspace/PSPARQL/example-ontology-web.owl",prog1); System.out.println(s);
		 

		//System.out.println("Prolog Program:");
		/*for (List<String> r : rules) {
			System.out.println(r);
		}
		;*/

		 

		/*
		 * String t1 = "use_module(library(semweb/rdf11))"; org.jpl7.Query q1 = new
		 * org.jpl7.Query(t1); System.out.print((q1.hasSolution() ? "" : "")); String t2
		 * =
		 * "rdf_load('C:/Users/Administrator/eclipse-workspace/PSPARQL/example-ontology-web.owl')";
		 * org.jpl7.Query q2 = new org.jpl7.Query(t2);
		 * System.out.print((q2.hasSolution() ? "" : "")); for (List<String>r: rules) {
		 * String prule = r.get(0)+":-"; for (int i=1;i < r.size();i++) { prule = prule
		 * + r.get(i)+','; } prule = prule.substring(0, prule.length()-1);
		 * System.out.println(prule); System.out.println(""); String aprule =
		 * "asserta(("+prule+"))"; org.jpl7.Query q3 = new org.jpl7.Query(aprule);
		 * System.out.println((q3.hasSolution() ? "" : ""));
		 * 
		 * }
		 * 
		 * 
		 * org.jpl7.Query q3 = new org.jpl7.Query(rules.get(0).get(0));
		 * System.out.println((q3.hasSolution() ? "Yes" : "No")); while
		 * (q3.hasMoreSolutions()) { Map<String, Term> solution = q3.nextSolution(); for
		 * (String s : solution.keySet()) { System.out.println(s + " : " +
		 * solution.get(s)); } }
		 */

	};
};
