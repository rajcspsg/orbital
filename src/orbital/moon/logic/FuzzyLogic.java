/*
 * @(#)FuzzyLogic.java 0.9 1997/05/01 Andre Platzer
 * 
 * Copyright (c) 1997 Andre Platzer. All Rights Reserved.
 */

package orbital.moon.logic;

import orbital.logic.imp.Logic;
import orbital.logic.imp.Inference;
import orbital.logic.functor.Functor;
import orbital.logic.functor.Functor.Composite;
import orbital.logic.functor.Function;
import orbital.logic.functor.BinaryFunction;

import orbital.logic.imp.Signature;
import orbital.logic.imp.Symbol;
import orbital.logic.imp.Formula;
import orbital.logic.imp.Interpretation;
import orbital.logic.imp.Expression;

import orbital.logic.imp.SignatureBase;
import orbital.logic.imp.InterpretationBase;
import orbital.logic.imp.SymbolBase;
import orbital.logic.imp.LogicBasis;

import java.util.Collection;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.StringReader;
import java.io.Serializable;
import java.io.ObjectStreamException;

import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.TreeMap;

import orbital.math.MathUtilities;
import orbital.io.IOUtilities;
import orbital.util.InnerCheckedException;
import java.beans.IntrospectionException;

import orbital.logic.functor.Notation;
import orbital.logic.functor.Notation.NotationSpecification;

import orbital.util.Utility;

/**
 * A FuzzyLogic class that represents logic values in quantitative fuzzy logic.
 * <p>
 * Fuzzy logic is a numeric approach in which truth-values represent a degree of truth
 * specified as a real number in the range [0,1].
 * (Unlike probabilities in the range [0,1] which specify a degree of belief.)</p>
 * <p>
 * <table>
 *   <tr>
 *     <td colspan="3">&#8911;:[0,1]<sup>2</sup>&rarr;[0,1] is a <dfn>fuzzy AND operator</dfn>, or triangular norm, iff</td>
 *   </tr>
 *   <tr>
 *     <td>(n)</td>
 *     <td>a &#8911; 1 = a</td>
 *     <td>&quot;neutral&quot;</td>
 *   </tr>
 *   <tr>
 *     <td>(c)</td>
 *     <td>a &#8911; b = b &#8911; a</td>
 *     <td>&quot;commutative&quot;</td>
 *   </tr>
 *   <tr>
 *     <td>(mon)</td>
 *     <td>a<sub>1</sub>&le;a<sub>2</sub> and b<sub>1</sub>&le;b<sub>2</sub> implies a<sub>1</sub> &#8911; b<sub>1</sub> &le; a<sub>2</sub> &#8911; b<sub>2</sub></td>
 *     <td>&quot;monotonic&quot;</td>
 *   </tr>
 *   <tr>
 *     <td>(a)</td>
 *     <td>a &#8911; (b &#8911; c) = (a &#8911; b) &#8911; c</td>
 *     <td>&quot;associative&quot;</td>
 *   </tr>
 *   <tr>
 *     <td>(idem)</td>
 *     <td>a &#8911; a = a</td>
 *     <td>&quot;idempotent&quot; (optional)</td>
 *   </tr>
 *   <tr>
 *     <td>(C)</td>
 *     <td>&#8911; is continuous</td>
 *     <td>&quot;continuous&quot; (optional)</td>
 *   </tr>
 *   <tr>
 *     <td>()</td>
 *     <td>a &#8911; 0 = 0</td>
 *     <td>(&lArr; (n),(c),(mon)</td>
 *   </tr>
 *   <tr>
 *     <td>()</td>
 *     <td>0 &#8911; 0 = 0<br>
 *       1 &#8911; 0 = 0<br>
 *       0 &#8911; 1 = 0<br>
 *       1 &#8911; 1 = 1</td>
 *     <td>&quot;&and; boundary conditions&quot; (&lArr; (n),(c),(mon))</td>
 *   </tr>
 * </table>
 * The only function that fulfills all axioms is the classical a&#8911;b = max(a,b) [Klir, Folger 1988].
 * </p>
 * <p>
 * <table>
 *   <tr>
 *     <td colspan="3">&#8910;:[0,1]<sup>2</sup>&rarr;[0,1] is a <dfn>fuzzy OR operator</dfn> or co-t-norm, iff</td>
 *   </tr>
 *   <tr>
 *     <td>(n)</td>
 *     <td>a &#8910; 0 = a</td>
 *     <td>&quot;neutral&quot;</td>
 *   </tr>
 *   <tr>
 *     <td>(c)</td>
 *     <td>a &#8910; b = b &#8910; a</td>
 *     <td>&quot;commutative&quot;</td>
 *   </tr>
 *   <tr>
 *     <td>(mon)</td>
 *     <td>a<sub>1</sub>&le;a<sub>2</sub> and b<sub>1</sub>&le;b<sub>2</sub> implies a<sub>1</sub> &#8910; b<sub>1</sub> &le; a<sub>2</sub> &#8910; b<sub>2</sub></td>
 *     <td>&quot;monotonic&quot;</td>
 *   </tr>
 *   <tr>
 *     <td>(a)</td>
 *     <td>a &#8910; (b &#8910; c) = (a &#8910; b) &#8910; c</td>
 *     <td>&quot;associative&quot;</td>
 *   </tr>
 *   <tr>
 *     <td>(idem)</td>
 *     <td>a &#8910; a = a</td>
 *     <td>&quot;idempotent&quot; (optional)</td>
 *   </tr>
 *   <tr>
 *     <td>(C)</td>
 *     <td>&#8910; is continuous</td>
 *     <td>&quot;continuous&quot; (optional)</td>
 *   </tr>
 *   <tr>
 *     <td>()</td>
 *     <td>a &#8911; 1 = 1</td>
 *     <td>(&lArr; (n),(c),(mon)</td>
 *   </tr>
 *   <tr>
 *     <td>()</td>
 *     <td>0 &#8910; 0 = 0<br>
 *       1 &#8910; 0 = 1<br>
 *       0 &#8910; 1 = 1<br>
 *       1 &#8910; 1 = 1</td>
 *     <td>&quot;&or; boundary conditions&quot; (&lArr; (n),(c),(mon))</td>
 *   </tr>
 * </table>
 * The only function that fulfills all axioms is the classical a&#8910;b = min(a,b) [Klir, Folger 1988].
 * <table>
 *   <tr>
 *     <td colspan="3">~ is a <dfn>fuzzy NOT operator</dfn>, iff</td>
 *   </tr>
 *   <tr>
 *     <td>(1)</td>
 *     <td>~1 = 0<br>
 *       ~0 =1</td>
 *     <td>&quot;&not; boundary conditions&quot;</td>
 *   </tr>
 *   <tr>
 *     <td>(mon)</td>
 *     <td>a&lt;b implies ~a &ge; ~b</td>
 *     <td>&quot;monotonic&quot;</td>
 *   </tr>
 *   <tr>
 *     <td>(inv)</td>
 *     <td>~ ~ a = a</td>
 *     <td>&quot;involutorical&quot; (optional)</td>
 *   </tr>
 *   <tr>
 *     <td>(C)</td>
 *     <td>~ is continuous</td>
 *     <td>&quot;continuous&quot; (optional)</td>
 *   </tr>
 * </table>
 * </p>
 * <p>
 * <dl class="def">
 * Let X be a universal set.
 *   <dt>fuzzy set</dt>
 *   <dd>A = {(x,&mu;<sub>A</sub>(x)) &brvbar; x&isin;X} is defined with a membership function &mu;<sub>A</sub>:X&rarr;[0,1].
 *    A finite fuzzy set A of domain {a<sub>1</sub>,&#8230;,a<sub>n</sub>} is sometimes denoted formally as
 *    <div>"A=&mu;<sub>A</sub>(a<sub>1</sub>)/a<sub>1</sub> + &mu;<sub>A</sub>(a<sub>2</sub>)/a<sub>2</sub> + ... + &mu;<sub>A</sub>(a<sub>n</sub>)/a<sub>n</sub>"</div>
 *    where + and / are purely syntactic symbols and do not denote arithmetic operations.
 *    Then fuzzy sets and fuzzy logic are related by
 *      <blockquote>
 *      &mu;<sub>A&cup;B</sub> = &mu;<sub>A</sub> &#8910; &mu;<sub>B</sub><br />
 *      &mu;<sub>A&cap;B</sub> = &mu;<sub>A</sub> &#8911; &mu;<sub>B</sub><br />
 *      &mu;<sub>A<sup>&#8705;</sup></sub> = ~ &mu;<sub>A</sub>
 *      </blockquote>
 *    </dd>
 *   <dt>cardinality</dt>
 *   <dd>|A| = &sum;<sub>x&isin;X</sub> &mu;<sub>A</sub>(x)</dd>
 *   <dt>entropy</dt>
 *   <dd class="Formula">E(A) = |A&cap;A<sup>&#8705;</sup>| / |A&cup;A<sup>&#8705;</sup>|</dd>
 * </dl>
 * </p>
 * <p>
 * Also note that for fuzzy sets, idem potence and distributive on the one hand,
 * and <span xml:lang="la">tertium non datur</span> on the other hand, are mutually exclusive
 * properties for all operators.
 * </p>
 * 
 * @version 0.7, 1999/01/11
 * @author  Andr&eacute; Platzer
 * @see "Klir, G. and Folger, T. (1988), Fuzzy Sets, Uncertainty and Information, Prentice-Hall, Englewood Cliffs."
 */
public class FuzzyLogic extends ModernLogic implements Logic {
    /**
     * Maximum number of OperatorSet objects (for typesafe enum).
     */
    private static final int MAX_OPERATORS = 20;
    /**
     * tool-main
     * @todo parse arguments in order to obtain OperatorSet used
     */
    public static void main(String arg[]) throws Exception {
	if (orbital.signe.isHelpRequest(arg)) {
	    System.out.println(usage);
	    System.out.println("Core operators:\n\t" + new FuzzyLogic().coreSignature());
	    return;
	} 
	FuzzyLogic logic = new FuzzyLogic();
	System.out.println("Enter sequence 'A|~C' to verify. Simply leave blank to type 'false' or {} or null.");
	System.out.print("Type base expression (A): ");
	System.out.flush();
	String expr = IOUtilities.readLine(System.in);
	System.out.print("Type sequence expression (C): ");
	System.out.flush();
	String  expr2 = IOUtilities.readLine(System.in);
	System.out.println(logic.satisfy(new InterpretationBase(SignatureBase.EMPTY, Collections.EMPTY_MAP), logic.createFormula(expr2)));
	boolean sat = logic.infer(expr, expr2);
	System.out.println(expr + (sat ? " satisfies " : " does not satisfy ") + expr2);
    } 
    public static final String usage = "interpret fuzzy logic";


    /**
     * Remembers the fuzzy logic operators used in coreInterpretation().
     */
    private final OperatorSet fuzzyLogicOperators;
    //@todo remove this bugfix that replaces "xfy" by "yfy" associativity only for *.jj parsers to work without inefficient right-associative lookahead.
    private static final String xfy = "yfy";

    private final Interpretation _coreInterpretation;
    private final Signature _coreSignature;
    public FuzzyLogic() {
	this(GOEDEL);
    }
    public FuzzyLogic(OperatorSet fuzzyLogicOperators) {
	this.fuzzyLogicOperators = fuzzyLogicOperators;
	final OperatorSet op = fuzzyLogicOperators;
	this._coreInterpretation =
	LogicSupport.arrayToInterpretation(new Object[][] {
	    {op.not(),          // "~"
	     new NotationSpecification(900, "fy", Notation.PREFIX)},
	    {op.and(),          // "&"
	     new NotationSpecification(910, xfy, Notation.INFIX)},
	    {LogicFunctions.xor,          // "^"
	     new NotationSpecification(914, xfy, Notation.INFIX)},
	    {op.or(),           // "|"
	     new NotationSpecification(916, xfy, Notation.INFIX)},
	    {LogicFunctions.impl,         // "->"
	     new NotationSpecification(920, xfy, Notation.INFIX)},
	    {LogicFunctions.leftwardImpl, // "<-"
	     new NotationSpecification(920, xfy, Notation.INFIX)},
	    {LogicFunctions.equiv,        // "<->"
	     new NotationSpecification(920, xfy, Notation.INFIX)},
	    {LogicFunctions.forall,       // "�"
	     new NotationSpecification(950, "fxx", Notation.PREFIX)},
	    {LogicFunctions.exists,       // "?"
	     new NotationSpecification(950, "fxx", Notation.PREFIX)}
	}, true);
	this._coreSignature = _coreInterpretation.getSignature();
    }

    /**
     * facade for convenience.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade</a>
     */
    public boolean infer(String expression, String exprDerived) throws java.text.ParseException {
	Signature sigma = scanSignature(expression).union(scanSignature(exprDerived));
	Formula B[] = {
	    createFormula(expression)
	};
	Formula D = createFormula(exprDerived);
	System.err.println(B[0] + " is interpreted to " + B[0].apply(null));
	System.err.println(D + " is interpreted to " + D.apply(null));
	return inference().infer(B, D);
    } 

    public boolean satisfy(Interpretation I, Formula F) {
	if (F == null)
	    throw new NullPointerException("null is not a formula");
	assert F instanceof FuzzyLogicFormula : "F is a formula in this logic";
        // assure core interpretation unless overwritten
        I = new QuickUnitedInterpretation(_coreInterpretation, I);
	return MathUtilities.equals(((Number) F.apply(I)).doubleValue(), 1, 0.001);
    } 

    public Inference inference() {
	throw new InternalError("no calculus implemented");
    } 

    /**
     * static elements of signature
     */
    protected static String   operators = "~! |&^-><=(),";

    public Signature coreSignature() {
	return _coreSignature;
    } 
    public Interpretation coreInterpretation() {
	return _coreInterpretation;
    }

    public Signature scanSignature(String expression) {
	return new SignatureBase(scanSignatureImpl(expression));
    } 
    static Set scanSignatureImpl(String expression) {
	if (expression == null)
	    return SignatureBase.EMPTY;
	Collection		names = new LinkedList();
	StringTokenizer st = new StringTokenizer(expression, operators, false);
	while (st.hasMoreElements())
	    //XXX: undo pair unless comparable
	    names.add(LogicParser.defaultSymbolFor((String) st.nextElement()));
	names = new TreeSet(names);
	// the signature really should not include core signature symbols
	names.remove(LogicParser.defaultSymbolFor("true"));
	names.remove(LogicParser.defaultSymbolFor("false"));
	return (Set) names;
    } 

    
    private Formula createFormula(String expression) throws java.text.ParseException {
	return (Formula) createExpression(expression);
    }

    // Helpers
    
    /**
     * interpretation for a truth-value
     */
    static final Object getInt(double v) {
	return (Number) new Double(v);
    } 
    
    /**
     * truth-value of a Formulas Interpretation
     */
    static final double getTruth(Object f) {
	return ((Number) f).doubleValue();
    } 
    
    
    /**
     * Specifies the type of fuzzy logic to use.
     * Instances will define the fuzzy logic operators applied.
     * @version 1.0, 2002/05/29
     * @author  Andr&eacute; Platzer
     * @see <a href="{@docRoot}/DesignPatterns/enum.html">typesafe enum pattern</a>
     * @internal typesafe enumeration pattern class to specify fuzzy logic operators
     * @invariant a.equals(b) &hArr; a==b
     * @todo improve name
     */
    public static abstract class OperatorSet implements Serializable, Comparable {
	/**
	 * the name to display for this enum value
	 * @serial
	 */
	private final String	  name;

	/**
	 * Ordinal of next enum value to be created
	 */
	private static int	  nextOrdinal = 0;

	/**
	 * Table of all canonical references to enum value classes.
	 */
	private static OperatorSet[] values = new OperatorSet[MAX_OPERATORS];

	/**
	 * Assign an ordinal to this enum value
	 * @serial
	 */
	private final int		  ordinal = nextOrdinal++;

	OperatorSet(String name) {
	    this.name = name;
	    values[nextOrdinal - 1] = this;
	}

	/**
	 * Order imposed by ordinals according to the order of creation.
	 * @post consistent with equals
	 */
	public int compareTo(Object o) {
	    return ordinal - ((OperatorSet) o).ordinal;
	} 

	/**
	 * Maintains the guarantee that all equal objects of the enumerated type are also identical.
	 * @post a.equals(b) &hArr; if a==b.
	 */
	public final boolean equals(Object that) {
	    return super.equals(that);
	} 
	public final int hashCode() {
	    return super.hashCode();
	} 

	public String toString() {
	    return this.name;
	} 

	/**
	 * Maintains the guarantee that there is only a single object representing each enum constant.
	 * @serialData canonicalized deserialization
	 */
	private Object readResolve() throws ObjectStreamException {
	    // canonicalize
	    return values[ordinal];
	} 

	/**
	 * Defines the not operator to use in the fuzzy logic.
	 * @post RES==OLD(RES)
	 */
	abstract Function not();

    	abstract BinaryFunction and();
    
    	abstract BinaryFunction or();
    }

    // enumeration of fuzzy logic operators

    /**
     * G&ouml;del and Zadeh operators in fuzzy logic (default).
     * <div>a &#8911; b = min{a,b}</div>
     * <div>a &#8910; b = max{a,b}</div>
     */
    public static OperatorSet GOEDEL = new OperatorSet("G�del") {
	    Function not() {
		return LogicFunctions.not;
	    }

	    BinaryFunction and() {
		return new BinaryFunction() {
			public Object apply(Object a, Object b) {
			    return getInt(Math.min(getTruth(a), getTruth(b)));
			}
			public String toString() { return "&"; }
		    };
	    }
    
	    BinaryFunction or() {
		return new BinaryFunction() {
			public Object apply(Object a, Object b) {
			    return getInt(Math.max(getTruth(a), getTruth(b)));
			}
			public String toString() { return "|"; }
		    };
	    }
	};

    /**
     * Product operators in fuzzy logic.
     * <div>a &#8911; b = a&sdot;b</div>
     * <div>a &#8910; b = a+b - a&sdot;b</div>
     */
    public static OperatorSet PRODUCT = new OperatorSet("Product") {
	    Function not() {
		return LogicFunctions.not;
	    }

	    BinaryFunction and() {
		return new BinaryFunction() {
			public Object apply(Object a, Object b) {
			    return getInt(getTruth(a) * getTruth(b));
			}
			public String toString() { return "&"; }
		    };
	    }
    
	    BinaryFunction or() {
		return new BinaryFunction() {
			public Object apply(Object wa, Object wb) {
			    final double a = getTruth(wa);
			    final double b = getTruth(wb);
			    return getInt(a + b - a * b);
			}
			public String toString() { return "|"; }
		    };
	    }
	};

    /**
     * Bounded or &#407;ukasiewicz operators in fuzzy logic.
     * <div>a &#8911; b = max{0,a+b-1}</div>
     * <div>a &#8910; b = min{1,a+b}</div>
     */
    public static OperatorSet BOUNDED = new OperatorSet("Bounded") {
	    Function not() {
		return LogicFunctions.not;
	    }

	    BinaryFunction and() {
		return new BinaryFunction() {
			public Object apply(Object a, Object b) {
			    return getInt(Math.max(0, getTruth(a) + getTruth(b) - 1));
			}
			public String toString() { return "&"; }
		    };
	    }
    
	    BinaryFunction or() {
		return new BinaryFunction() {
			public Object apply(Object a, Object b) {
			    return getInt(Math.min(1, getTruth(a) + getTruth(b)));
			}
			public String toString() { return "|"; }
		    };
	    }
	};

    /**
     * Drastic operators in fuzzy logic.
     * <div>a &#8911; b = min{a,b} if max{a,b}=1, else 0</div>
     * <div>a &#8910; b = max{a,b} if min{a,b}=0, else 1</div>
     * @attribute discontinuous
     */
    public static OperatorSet DRASTIC = new OperatorSet("Drastic") {
	    Function not() {
		return LogicFunctions.not;
	    }

	    BinaryFunction and() {
		return new BinaryFunction() {
			public Object apply(Object wa, Object wb) {
			    final double a = getTruth(wa);
			    final double b = getTruth(wb);
			    return getInt(a == 1.0 || b == 1.0 ? Math.min(a, b) : 0);
			}
			public String toString() { return "&"; }
		    };
	    }
    
	    BinaryFunction or() {
		return new BinaryFunction() {
			public Object apply(Object wa, Object wb) {
			    final double a = getTruth(wa);
			    final double b = getTruth(wb);
			    return getInt(a == 0.0 || b == 0.0 ? Math.max(a, b) : 1);
			}
			public String toString() { return "|"; }
		    };
	    }
	};

    /**
     * Hamacher operators in fuzzy logic.
     * <div class="Formula">a &#8911; b = a&sdot;b / <big>(</big>&gamma;+(1-&gamma;)(a+b-a&sdot;b)<big>)</big></div>
     * <div class="Formula">a &#8910; b = <big>(</big>a+b-(2-&gamm;)a&sdot;b<big>)</big> / <big>(</big>1-(1-&gamma;)a&sdot;b<big>)</big></div>
     * @param gamma the parameter &gamma;.
     * @pre gamma&ge;0
     */
    public static OperatorSet HAMACHER(final double gamma) {
	if (!(gamma >= 0))
	    throw new IllegalArgumentException("illegal value for gamma: " + gamma + " < 0");
	return new OperatorSet("Hamacher(" + gamma + ")") {
		Function not() {
		    return LogicFunctions.not;
		}

		BinaryFunction and() {
		    return new BinaryFunction() {
			    public Object apply(Object wa, Object wb) {
				final double a = getTruth(wa);
				final double b = getTruth(wb);
				final double ab = a*b;
				return getInt(ab / (gamma + (1-gamma)*(a+b-ab)));
			    }
			    public String toString() { return "&"; }
			};
		}
    
		BinaryFunction or() {
		    return new BinaryFunction() {
			    public Object apply(Object wa, Object wb) {
				final double a = getTruth(wa);
				final double b = getTruth(wb);
				final double ab = a*b;
				return getInt((a+b-(2-gamma)*ab) / (1 - (1-gamma)*ab));
			    }
			    public String toString() { return "|"; }
			};
		}
	    };
    }

    /**
     * Yager operators in fuzzy logic.
     * <div class="Formula">a &#8911; b = 1 - min<big>{</big>1,<big>(</big>(1-a)<sup>p</sup>+(1-b)<sup>p</sup>)<big>)</big><sup>1/p</sup><big>}</big></div>
     * <div class="Formula">a &#8910; b = min<big>{</big>1,<big>(</big>a<sup>p</sup>+b<sup>p</sup>)<big>)</big><sup>1/p</sup><big>}</big></div>
     * For p&rarr;&infin; these operators approximate those of {@link GOEDEL}.
     * @pre p&gt;0
     */
    public static OperatorSet YAGER(final double p) {
	if (!(p > 0))
	    throw new IllegalArgumentException("illegal parameter: " + p + " =< 0");
	final double inverse_p = 1/p;
	return new OperatorSet("Yager(" + p + ")") {
		Function not() {
		    return LogicFunctions.not;
		}

		BinaryFunction and() {
		    return new BinaryFunction() {
			    public Object apply(Object wa, Object wb) {
				final double a = getTruth(wa);
				final double b = getTruth(wb);
				return getInt(1 - Math.min(1,Math.pow(Math.pow(1-a,p)+Math.pow(1-b,p),inverse_p)));
			    }
			    public String toString() { return "&"; }
			};
		}
    
		BinaryFunction or() {
		    return new BinaryFunction() {
			    public Object apply(Object wa, Object wb) {
				final double a = getTruth(wa);
				final double b = getTruth(wb);
				return getInt(Math.min(1,Math.pow(Math.pow(a,p)+Math.pow(b,p),inverse_p)));
			    }
			    public String toString() { return "|"; }
			};
		}
	    };
    }


    static class LogicFunctions {
        private LogicFunctions() {}
    
	// Basic logical operations (elemental junctors).
    	public static final Function not = new Function() {
    		public Object apply(Object a) {
		    return getInt(1 - getTruth(a));
        	}
		public String toString() { return "~"; }
	    }; 

    	public static final BinaryFunction and = new BinaryFunction() {
    		public Object apply(Object a, Object b) {
		    // classical fuzzy defintion
		    return getInt(Math.min(getTruth(a), getTruth(b)));
		    // return max(0, a+b-1) limited difference
		    // return ?new FuzzyLogicFormula(a.value * b.value);
    		}
		public String toString() { return "&"; }
	    };
    
    	public static final BinaryFunction or = new BinaryFunction() {
    		public Object apply(Object a, Object b) {
		    return getInt(Math.max(getTruth(a), getTruth(b)));
		    // Yager-Union function min(1, (a^p + b^p)^(1/p)) for p>=1.
		    // return min(1, a+b) limited sum
    		}
		public String toString() { return "|"; }
	    };

	// Derived logical operations.

	//XXX: null will prevent calling .toString() and thus hinder Interpretation.get(...)
	//TODO: The following functions for derived logical operations could be generalized perhaps (see LogicBasis)
    	public static final BinaryFunction xor = null;

    	//@todo how about =< as an implementation of the implication in fuzzy logic?
    	public static final BinaryFunction impl = null;

    	public static final BinaryFunction leftwardImpl = null;

    	public static final BinaryFunction equiv = null;

	// Basic logical operations (elemental quantifiers).

    	public static final BinaryFunction forall = null;

    	public static final BinaryFunction exists = null;
    }
}