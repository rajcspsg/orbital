import orbital.logic.imp.*;
import orbital.moon.logic.ClassicalLogic;
import java.util.*;

/**
 * A simple example for logic evaluations using classical logic implementation.
 * 
 * @version 1.0,, 2001/09/18
 * @author  Andr&eacute; Platzer
 */
public class SimpleLogic implements Runnable {

    /**
     * Application-start entry point.
     */
    public static void main(String arg[]) throws Exception {
	new SimpleLogic().run();
    } 

    /**
     * The logic to use.
     */
    protected final Logic logic;

    /**
     * the formula and its signature.
     */
    private Formula		  formula;
    private Signature	  sigma;

    /**
     * Runnable-init entry point.
     */
    public SimpleLogic() {
	logic = new ClassicalLogic();
    }

    public void run() {
	try {
	    String formulaText = "(a&~b) | ~c <=> c";
	    this.sigma = logic.scanSignature(formulaText);
	    formula = (Formula) logic.createExpression(formulaText);

	    Formula formula2 = (Formula) logic.createExpression("~c|~b|~a | ~(c<=>c)");
	    boolean deduce = logic.inference().infer(new Formula[] {formula}, formula2);
	    System.out.println("from " + formula + " we could " + (deduce ? "deduce" : "not deduce") + " that " + formula2);

	    // create our custom interpretation
	    Map intermap = new HashMap();
	    intermap.put(new SymbolBase("a", SymbolBase.BOOLEAN_ATOM), Boolean.TRUE);
	    intermap.put(new SymbolBase("b", SymbolBase.BOOLEAN_ATOM), Boolean.FALSE);
	    intermap.put(new SymbolBase("c", SymbolBase.BOOLEAN_ATOM), Boolean.TRUE);
	    Interpretation interpretation = new InterpretationBase(sigma, intermap);
	    boolean		   satisfied = logic.satisfy(interpretation, formula);
	    System.out.println(formula + " is " + (satisfied ? "satisfied" : "not satisfied") + " by the given interpretation");
	} catch (Exception x) {x.printStackTrace();}
    } 

}

