/**
 * @(#)ModernFormula.java 0.7 1999/01/16 Andre Platzer
 * 
 * Copyright (c) 1999-2002 Andre Platzer. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information
 * of Andre Platzer. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into.
 */

package orbital.moon.logic;

import orbital.logic.imp.*;

import orbital.logic.functor.Functor;
import orbital.logic.functor.Functor.Composite;
import orbital.logic.functor.*;
import orbital.logic.functor.Predicates;

import java.text.ParseException;

import java.util.Set;

import java.util.Map;
import java.util.Collection;
import java.util.Iterator;

import java.util.HashSet;
import java.util.IdentityHashMap;

import java.util.Collections;
import orbital.util.Setops;
import orbital.util.Utility;

/**
 * The formula implementation of (usually truth-functional) modern logic.
 * @version 0.8, 1999/01/16
 * @version 0.7, 1999/01/16
 * @author  Andr&eacute; Platzer
 */
abstract class ModernFormula extends LogicBasis implements Formula {
    /**
     * The underlying logic of this formula (used for composition).
     * @note an alternative implementation would make this class an inner instance class of a Logic implementation basis, saving this instance variable.
     * @xxx then, however, default constructor newInstance() can no longer set the right underlying logic. And that logic cannot even be set directly by setComponent, setCompositor
     */
    private Logic logic = new ClassicalLogic();
    protected ModernFormula(Logic underlyingLogic) {
	this.logic = underlyingLogic;
    }
    /**
     * The set of all binding expressions (i.e. instances of BindingExpression).
     * Represented as an IdentityHashSet = IdentityHashMap.keySet().
     * @xxx improve concept
     */
    private static final Set bindingExpressions;
    static {
	Map helper = new IdentityHashMap();
	helper.put(ClassicalLogic.LogicFunctions.forall, null);
	helper.put(ClassicalLogic.LogicFunctions.exists, null);
	bindingExpressions = helper.keySet();
    }
    /**
     * The symbols of the logical junctors.
     */
    private static final Symbol NOT, AND, OR, XOR, IMPL, EQUIV, FORALL, EXISTS;
    static {
	//@note assuming the symbols and notation of ClassicalLogic, here
	final Logic logic = new ClassicalLogic();
	final Signature core = logic.coreSignature();
	// we also avoid creating true formulas, it's (more or less) futile
	Formula[] arguments = {null};
	NOT = core.get("~", arguments);

	arguments = new Formula[] {null, null};
	AND = core.get("&", arguments);
	OR = core.get("|", arguments);
	XOR = core.get("^", arguments);
	IMPL = core.get("->", arguments);
	EQUIV = core.get("<->", arguments);

	arguments = new Formula[] {null, null};
	FORALL = core.get("�", arguments);
	EXISTS = core.get("?", arguments);
    }

    /**
     * Get the underlying logic of this formula.
     */
    Logic getUnderlyingLogic() {
	return logic;
    }

    /**
     * Set the underlying logic of this formula.
     */
    private void setUnderlyingLogic(Logic underlyingLogic) {
	this.logic = underlyingLogic;
    }

    /**
     * Checks whether the underlying logics of this formula and the given one are compatible.
     * An undefined underlying logic of <code>null</code> is compatible with any logic.
     */
    boolean isCompatibleUnderlyingLogic(Formula formula) {
	Logic myUnderlying = getUnderlyingLogic();
	Logic itsUnderlying = ((ModernFormula)formula).getUnderlyingLogic();
	return myUnderlying == null || itsUnderlying == null
	    || myUnderlying.getClass().equals(itsUnderlying.getClass());
    }

    /**
     * Set our underlying logic like the underlying logic of the given formula.
     * @pre getUnderlyingLogic()&ne;null &and; formula.getUnderlyingLogic()&ne;null &rArr;
     *  isCompatibleUnderlyingLogic(formula)
     */
    void setUnderlyingLogicLikeIn(Formula formula) {
	Logic myUnderlying = getUnderlyingLogic();
	Logic itsUnderlying = ((ModernFormula)formula).getUnderlyingLogic();
	assert isCompatibleUnderlyingLogic(formula) : "expected compatible underlying logics";
	setUnderlyingLogic(itsUnderlying);
    }

    
    public Object apply(Object/*>Interpretation<*/ I) {
	return interpret((Interpretation) I);
    }
    //@internal convenient helper method
    abstract Object interpret(Interpretation I);

    // Formula implementation

    public Set getVariables() {
    	//@todo optimize away that odd formatting just to parse again
	try {
	    return new HashSet(Setops.select(null, logic.scanSignature(toString()), new Predicate() {
		    public boolean apply(Object o) {
			return ((Symbol) o).isVariable();
		    }
		}));
	}
	catch (ParseException ex) {throw (InternalError) new InternalError("formatting and parsing are incompatible").initCause(ex);}
    }

    public Set getFreeVariables() {
    	throw new UnsupportedOperationException("not yet implemented for " + getClass());
    }

    public Set getBoundVariables() {
    	throw new UnsupportedOperationException("not yet implemented for " + getClass());
    }

    
    public Formula not() {
	return compose(NOT, new Formula[] {this});
    } 

    public Formula and(Formula B) {
	return compose(AND, new Formula[] {this, B});
    }

    public Formula or(Formula B) {
	return compose(OR, new Formula[] {this, B});
    } 

    //@todo introduce (currently derived from LogicBasis) xor, impl, equiv

    public Formula exists(Symbol x) {
	return compose(EXISTS, new Formula[] {createSymbol(getUnderlyingLogic(), x), this});
    } 
	
    public Formula forall(Symbol x) {
	return compose(FORALL, new Formula[] {createSymbol(getUnderlyingLogic(), x), this});
    } 

    private Formula compose(Symbol op, Formula[] arguments) {
	try {
	    return (Formula) logic.compose(op, arguments);
	}
	catch (ParseException ex) {throw (InternalError) new InternalError("errorneous internal composition").initCause(ex);}
    }


    // base case atomic symbols

    /**
     * Construct (a formula view of) an atomic symbol.
     * @param symbol the symbol for which to create a formula representation
     * @see Logic#createAtomic(Symbol)
     */
    public static Formula createSymbol(Logic underlyingLogic, Symbol symbol) {
	return new AtomicSymbol(underlyingLogic, symbol);
    }
    /**
     * Construct (a formula view of) an atomic symbol with a fixed interpretation.
     * @param symbol the symbol for which to create a formula representation
     * @param referent the fixed interpretation of this symbol
     * @param core whether symbol is in the core such that it does not belong to the proper signature.
     * @see Logic#createAtomic(Symbol)
     */
    public static Formula createFixedSymbol(Logic underlyingLogic, Symbol symbol, Object referent, boolean core) {
	return new FixedAtomicSymbol(underlyingLogic, symbol, referent, core);
    }
    /**
     * This atomic expression formula is variable iff its symbols is.
     * The interpretation of this formula is the interpretation of the symbol.
     * @structure delegate symbol:Variable
     */
    static class AtomicSymbol extends ModernFormula implements orbital.logic.trs.Variable {
	private Symbol symbol;
	/**
	 * Construct (a formula view of) an atomic symbol.
	 * @param symbol the symbol for which to create a formula representation
	 */
	public AtomicSymbol(Logic underlyingLogic, Symbol symbol) {
	    super(underlyingLogic);
	    this.symbol = symbol;
	}
		
	public boolean equals(Object o) {
	    return getClass() == o.getClass() && Utility.equals(symbol, ((AtomicSymbol) o).symbol);
	}
	public int hashCode() {
	    return Utility.hashCode(symbol);
	}
		
	public boolean isVariable() {return symbol.isVariable();}

        public Signature getSignature() {
	    return new SignatureBase(Collections.singleton(symbol));
        }
        public Set getVariables() {
	    return getFreeVariables();
        }
        public Set getFreeVariables() {
	    return isVariable()
		? Collections.singleton(symbol)
		: Collections.EMPTY_SET;
        }
        public Set getBoundVariables() {
	    return Collections.EMPTY_SET;
        }

	Object interpret(Interpretation I) {
	    if (I == null)
		throw new IllegalStateException("cannot get the truth-value of a symbol without an interpretation");
            
	    // symbols
	    try {
		return interpretationOf(I.get(symbol));
	    }
	    catch (NullPointerException ex) {
		throw (IllegalStateException) new IllegalStateException("truth-value of '" + symbol + "' has an invalid interpretation. " + ex + " in " + I).initCause(ex);
	    }
	    catch (IllegalArgumentException ex) {
		throw (IllegalStateException) new IllegalStateException("truth-value of '" + symbol + "' has an invalid interpretation. " + ex + " in " + I).initCause(ex);
	    }
	}
		
        /**
         * Get a boolean interpretation for classical logic.
         * allow more than Boolean, but assert that the types of symbol and desc fit.
	 * @todo document update this old documentation
         */ 
    	protected final Object interpretationOf(Object desc) {
	    if (desc == null)
		throw new NullPointerException("null is not a valid association for boolean interpretation");
	    else if (desc instanceof Boolean)
		return desc;
	    else if (desc instanceof Number)
		return desc;
	    else if (desc instanceof Functor) {
		if (symbol.getSpecification().isConform((Functor) desc))
		    return desc;
		else
		    throw new IllegalArgumentException("incompatible interpretation " + desc);
	    } else
		throw new IllegalArgumentException("truth-value of '" + desc + "' has no valid interpretation.");
    	}

	public String toString() { return symbol + ""; }
    } 
    private static class FixedAtomicSymbol extends AtomicSymbol {
	private Object referent;
	private boolean core;
	/**
	 * Construct (a formula view of) an atomic symbol with a fixed interpretation.
	 * @param symbol the symbol for which to create a formula representation
	 * @param referent the fixed interpretation of this symbol
	 * @param core whether symbol is in the core such that it does not belong to the proper signature.
	 */
	public FixedAtomicSymbol(Logic underlyingLogic, Symbol symbol, Object referent, boolean core) {
	    super(underlyingLogic, symbol);
	    if (symbol.isVariable())
		throw new IllegalArgumentException("do not use fixed referents for variable symbols");
	    this.referent= referent;
	    this.core = core;
	}
	public boolean equals(Object o) {
	    return getClass() == o.getClass() && Utility.equals(referent, ((FixedAtomicSymbol) o).referent);
	}
	public int hashCode() {
	    return Utility.hashCode(referent);
	}
        public Signature getSignature() {
	    return core ? SignatureBase.EMPTY : super.getSignature();
	}
	Object interpret(Interpretation I) {
	    return referent;
	}
    }

    // composition
    
    /**
     * Delayed composition of a symbol with some arguments.
     * Usually for user-defined predicates etc. or predicates subject to interpretation.
     * @param f the formula really performing the (outer part of the) composition by op.
     * @param op the (outer part of the) composing symbol.
     * @param arguments the arguments to the composition by op.
     */
    public static Formula composeDelayed(Logic underlyingLogic, Formula f, Symbol op, Expression arguments[]) {
        //@fixme was notation = op.getNotation().getNotation(); but either we disable DEFAULT=BESTFIX formatting, or we ignore the signature's notation choice
        Notation notation = Notation.DEFAULT;
	switch(arguments.length) {
	case 0:
	    return new ModernFormula.VoidCompositeVariableFormula(underlyingLogic, f, op.getNotation().getNotation());
	case 1:
	    return new ModernFormula.CompositeVariableFormula(underlyingLogic, f, (Formula) arguments[0], notation);
	case 2:
	    return new ModernFormula.BinaryCompositeVariableFormula(underlyingLogic, f, (Formula) arguments[0], (Formula) arguments[1], notation);
	default:
	    // could simply compose f(arguments), here, if f understands arrays
	    //@todo which Locator to provide, here?
	    throw new IllegalArgumentException("illegal number of arguments, " + f + "/" + arguments.length + " is undefined");
	}
    }

    /**
     * Instant composition of functors with a fixed core interperation
     * Usually for predicates etc. subject to fixed core interpretation.
     * @param f the functor really performing the (outer part of the) composition by op.
     * @param op the (outer part of the) composing symbol.
     * @param arguments the arguments to the composition by op.
     */
    public static Formula composeFixed(Logic underlyingLogic, Functor f, Symbol op, Expression arguments[]) {
        //@fixme was notation = op.getNotation().getNotation(); but either we disable DEFAULT=BESTFIX formatting, or we ignore the signature's notation choice
        Notation notation = Notation.DEFAULT;
	switch(arguments.length) {
	case 0:
	    if (f instanceof VoidPredicate && !(f instanceof VoidFunction))
		f = Functionals.asFunction((VoidPredicate) f);
	    return new ModernFormula.CompositeFormula(underlyingLogic, Functionals.onVoid((VoidFunction) f), (Formula) arguments[0], op.getNotation().getNotation());
	case 1:
	    if (f instanceof Predicate && !(f instanceof Function))
		f = Functionals.asFunction((Predicate) f);
	    return new ModernFormula.CompositeFormula(underlyingLogic, (Function) f, (Formula) arguments[0], notation);
	case 2:
	    if (f instanceof BinaryPredicate && !(f instanceof BinaryFunction))
		f = Functionals.asFunction((BinaryPredicate) f);
	    return new ModernFormula.BinaryCompositeFormula(underlyingLogic, (BinaryFunction) f, (Formula) arguments[0], (Formula) arguments[1], notation);
	default:
	    // could simply compose f(arguments), here, if f understands arrays
	    //@todo which Locator to provide, here?
	    throw new IllegalArgumentException("illegal number of arguments, " + f + "/" + arguments.length + " is undefined");
	}
    }


    // alternative implementation 1 (instant: fixed outer functions)
	
    /**
     * <p>
     * This class is in fact a workaround for multiple inheritance of
     * {@link ModernFormula} and {@link orbital.logic.functor.Compositions.CompositeFunction}.</p>
     * 
     * @structure inherits ModernFormula
     * @structure inherits Compositions.CompositeFunction
     * @todo change type of outer to Formula, and use ConstantFormulas for coreInterpretation instead
     */
    static class CompositeFormula extends ModernFormula implements Function.Composite {
	protected Function outer;
	protected Formula inner;
	public CompositeFormula(Logic underlyingLogic, Function f, Formula g, Notation notation) {
	    super(underlyingLogic);
	    setNotation(notation);
	    this.outer = f;
	    this.inner = g;
	}
	public CompositeFormula(Logic underlyingLogic, Function f, Formula g) {
	    this(underlyingLogic, f, g, null);
	}
		
	private CompositeFormula() {super(null);setNotation(null);}
		
        public Signature getSignature() {
	    //@todo shouldn't we unify with getCompositor().getSignature() in case of formulas representing predicate or function?
	    return ((Formula) getComponent()).getSignature();
        }

	public Set getFreeVariables() {
	    return inner.getFreeVariables();
	}

	public Set getBoundVariables() {
	    return inner.getBoundVariables();
	}

	Object interpret(Interpretation I) {
	    return apply(I);
	}

	// identical to @see orbital.logic.functor.Compositions.CompositeFunction
	public Functor getCompositor() {
	    return outer;
	} 
	public Object getComponent() {
	    return inner;
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    this.outer = (Function) f;
	}
	public void setComponent(Object g) throws ClassCastException {
	    setUnderlyingLogicLikeIn((Formula) g);
	    this.inner = (Formula) g;
	}

	public Object apply(Object/*>Interpretation<*/ arg) {
	    return outer/*.apply(arg)*/.apply(inner.apply(arg));
	} 
		
	// identical to @see orbital.logic.functor.Functor.Composite.Abstract
	/**
	 * the current notation used for displaying this composite functor.
	 * @serial
	 */
	private Notation notation;
	public Notation getNotation() {
	    return notation;
	}
	public void setNotation(Notation notation) {
	    this.notation = notation == null ? Notation.DEFAULT : notation;
	}
		
	/**
	 * Checks for equality.
	 * Two CompositeFunctors are equal iff their classes,
	 * their compositors and their components are equal.
	 */
	public boolean equals(Object o) {
	    if (o == null || getClass() != o.getClass())
		return false;
	    // note that it does not matter to which .Composite we cast since we have already checked for class equality
	    Composite b = (Composite) o;
	    return Utility.equals(getCompositor(), b.getCompositor())
		&& Utility.equalsAll(getComponent(), b.getComponent());
	}

	public int hashCode() {
	    return Utility.hashCode(getCompositor()) ^ Utility.hashCodeAll(getComponent());
	}

	/**
	 * Get a string representation of the composite functor.
	 * @return <code>{@link Notation#format(Object, Object) notation.format}(getCompositor(), getComponent())</code>.
	 */
	public String toString() {
	    return getNotation().format(getCompositor(), getComponent());
	}
    }

    /**
     * <p>
     * This class is in fact a workaround for multiple inheritance of
     * {@link ModernFormula} and {@link orbital.logic.functor.Functionals.BinaryCompositeFunction}.</p>
     * 
     * @structure inherits ModernFormula
     * @structure inherits Functionals.BinaryCompositeFunction
     */
    static class BinaryCompositeFormula extends ModernFormula implements Function.Composite {
	protected BinaryFunction outer;
	protected Formula left;
	protected Formula right;
	public BinaryCompositeFormula(Logic underlyingLogic, BinaryFunction f, Formula g, Formula h, Notation notation) {
	    super(underlyingLogic);
	    setNotation(notation);
	    this.outer = f;
	    this.left = g;
	    this.right = h;
	}
	public BinaryCompositeFormula(Logic underlyingLogic, BinaryFunction f, Formula g, Formula h) {
	    this(underlyingLogic, f, g, h, null);
	}
		
	private BinaryCompositeFormula() {super(null);setNotation(null);}

        public Signature getSignature() {
	    //@todo could cache signature as well, provided left and right don't change
	    return left.getSignature().union(right.getSignature());
        }

	public Set getFreeVariables() {
	    if (bindingExpressions.contains(outer)) {
		Set M = right.getFreeVariables();
		assert left instanceof AtomicSymbol && left.getFreeVariables().size() == 1 : "quantifiers bind an atomic symbol formula";
		M.removeAll(left.getFreeVariables());
		return M;
	    }
	    return Setops.union(left.getFreeVariables(),
				right.getFreeVariables());
	}

	public Set getBoundVariables() {
	    if (bindingExpressions.contains(outer)) {
		Set M = right.getBoundVariables();
		assert left instanceof AtomicSymbol && left.getFreeVariables().size() == 1 : "quantifiers bind an atomic symbol formula";
		M.addAll(left.getFreeVariables());
		return M;
	    }
	    return Setops.union(left.getBoundVariables(),
				right.getBoundVariables());
	}

	Object interpret(Interpretation I) {
	    return apply(I);
	}

	// identical to @see orbital.logic.functor.Functionals.BinaryCompositeFunction
	public Functor getCompositor() {
	    return outer;
	} 
	public Object getComponent() {
	    return new Formula[] {
		left, right
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    this.outer = (BinaryFunction) f;
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    Formula[] a = (Formula[]) g;
	    if (a.length != 2)
		throw new IllegalArgumentException(Formula.class + "[2] expected");
	    assert ((ModernFormula)a[0]).isCompatibleUnderlyingLogic(a[1]) : "only compose formulas of compatible logics";
	    setUnderlyingLogicLikeIn(a[0]);
	    this.left = a[0];
	    this.right = a[1];
	}

	public Object apply(Object/*>Interpretation<*/ arg) {
	    return outer.apply(left.apply(arg), right.apply(arg));
	} 
		
	// identical to @see orbital.logic.functor.Functor.Composite.Abstract
	/**
	 * the current notation used for displaying this composite functor.
	 * @serial
	 */
	private Notation notation;
	public Notation getNotation() {
	    return notation;
	}
	public void setNotation(Notation notation) {
	    this.notation = notation == null ? Notation.DEFAULT : notation;
	}
		
	/**
	 * Checks for equality.
	 * Two CompositeFunctors are equal iff their classes,
	 * their compositors and their components are equal.
	 */
	public boolean equals(Object o) {
	    if (o == null || getClass() != o.getClass())
		return false;
	    // note that it does not matter to which .Composite we cast since we have already checked for class equality
	    Composite b = (Composite) o;
	    return Utility.equals(getCompositor(), b.getCompositor())
		&& Utility.equalsAll(getComponent(), b.getComponent());
	}

	public int hashCode() {
	    return Utility.hashCode(getCompositor()) ^ Utility.hashCodeAll(getComponent());
	}

	/**
	 * Get a string representation of the composite functor.
	 * @return <code>{@link Notation#format(Object, Object) notation.format}(getCompositor(), getComponent())</code>.
	 */
	public String toString() {
	    return getNotation().format(getCompositor(), getComponent());
	}
    }

    
    // alternative implementation 2 (delayed: variable outer functions defined by formulas)
	
    /**
     * <p>
     * This class is in fact a workaround for multiple inheritance of
     * {@link ModernFormula} and {@link orbital.logic.functor.Compositions.CompositeFunction}.</p>
     * 
     * @structure inherits ModernFormula
     * @structure inherits Compositions.CompositeFunction
     * @todo change type of outer to Formula, and use ConstantFormulas for coreInterpretation instead
     * @see CompositeFormula
     */
    static class CompositeVariableFormula extends ModernFormula implements Function.Composite {
	protected Formula outer;
	protected Formula inner;
	public CompositeVariableFormula(Logic underlyingLogic, Formula f, Formula g, Notation notation) {
	    super(underlyingLogic);
	    setNotation(notation);
	    this.outer = f;
	    this.inner = g;
	}
	public CompositeVariableFormula(Logic underlyingLogic, Formula f, Formula g) {
	    this(underlyingLogic, f, g, null);
	}
		
	private CompositeVariableFormula() {super(null);setNotation(null);}
		
        public Signature getSignature() {
	    return inner.getSignature().union(outer.getSignature());
        }

	public Set getFreeVariables() {
	    return Setops.union(inner.getFreeVariables(),
				outer.getFreeVariables());
	}

	public Set getBoundVariables() {
	    return Setops.union(inner.getBoundVariables(),
				outer.getBoundVariables());
	}

	Object interpret(Interpretation I) {
	    return apply(I);
	}

	/**
	 * The functions applied are subject to interpretation.
	 */
	public Object apply(Object/*>Interpretation<*/ arg) {
	    Object f = outer.apply(arg);
	    if (f instanceof Predicate && !(f instanceof Function))
		f = Functionals.asFunction((Predicate) f);
	    return ((Function) f).apply(inner.apply(arg));
	} 
		

	// identical to @see orbital.logic.functor.Compositions.CompositeFunction (apart from Formula instead of Function)
	public Functor getCompositor() {
	    return outer;
	} 
	public Object getComponent() {
	    return inner;
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    setUnderlyingLogicLikeIn((Formula) f);
	    this.outer = (Formula) f;
	}
	public void setComponent(Object g) throws ClassCastException {
	    assert isCompatibleUnderlyingLogic((Formula)g) : "only compose formulas of compatible logics";
	    this.inner = (Formula) g;
	}

	// identical to @see orbital.logic.functor.Functor.Composite.Abstract
	/**
	 * the current notation used for displaying this composite functor.
	 * @serial
	 */
	private Notation notation;
	public Notation getNotation() {
	    return notation;
	}
	public void setNotation(Notation notation) {
	    this.notation = notation == null ? Notation.DEFAULT : notation;
	}
		
	/**
	 * Checks for equality.
	 * Two CompositeFunctors are equal iff their classes,
	 * their compositors and their components are equal.
	 */
	public boolean equals(Object o) {
	    if (o == null || getClass() != o.getClass())
		return false;
	    // note that it does not matter to which .Composite we cast since we have already checked for class equality
	    Composite b = (Composite) o;
	    return Utility.equals(getCompositor(), b.getCompositor())
		&& Utility.equalsAll(getComponent(), b.getComponent());
	}

	public int hashCode() {
	    return Utility.hashCode(getCompositor()) ^ Utility.hashCodeAll(getComponent());
	}

	/**
	 * Get a string representation of the composite functor.
	 * @return <code>{@link Notation#format(Object, Object) notation.format}(getCompositor(), getComponent())</code>.
	 */
	public String toString() {
	    return getNotation().format(getCompositor(), getComponent());
	}
    }

    /**
     * <p>
     * This class is in fact a workaround for multiple inheritance of
     * {@link ModernFormula} and {@link orbital.logic.functor.Compositions.VoidCompositeFunction}.</p>
     * 
     * @structure inherits ModernFormula
     * @structure inherits Compositions.CompositeFunction
     * @todo change type of outer to Formula, and use ConstantFormulas for coreInterpretation instead
     * @see CompositeFormula
     */
    static class VoidCompositeVariableFormula extends ModernFormula implements Function.Composite {
	protected Formula outer;
	public VoidCompositeVariableFormula(Logic underlyingLogic, Formula f, Notation notation) {
	    super(underlyingLogic);
	    setNotation(notation);
	    this.outer = f;
	}
	public VoidCompositeVariableFormula(Logic underlyingLogic, Formula f) {
	    this(underlyingLogic, f, null);
	}
		
	private VoidCompositeVariableFormula() {super(null);setNotation(null);}
		
        public Signature getSignature() {
	    return outer.getSignature();
        }

	public Set getFreeVariables() {
	    return outer.getFreeVariables();
	}

	public Set getBoundVariables() {
	    return outer.getBoundVariables();
	}

	Object interpret(Interpretation I) {
	    return apply(I);
	}

	/**
	 * The functions applied are subject to interpretation.
	 */
	public Object apply(Object/*>Interpretation<*/ arg) {
	    Object f = outer.apply(arg);
	    if (f instanceof VoidPredicate && !(f instanceof VoidFunction))
		f = Functionals.asFunction((VoidPredicate) f);
	    return ((VoidFunction) f).apply();
	} 
		

	// identical? to @see orbital.logic.functor.Compositions.VoidCompositeFunction (apart from Formula instead of Function)
	public Functor getCompositor() {
	    return outer;
	} 
	public Object getComponent() {
	    return new Formula[0];
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    setUnderlyingLogicLikeIn((Formula) f);
	    this.outer = (Formula) f;
	}
	public void setComponent(Object g) throws ClassCastException {
	    // arity 0 case trickily embedded here
	    if ((g instanceof Formula[]) && ((Formula[])g).length == 0)
		return;
	    else
		throw new IllegalArgumentException("illegal component for arity 0: " + g + " of " + g.getClass());
	}

	// identical to @see orbital.logic.functor.Functor.Composite.Abstract
	/**
	 * the current notation used for displaying this composite functor.
	 * @serial
	 */
	private Notation notation;
	public Notation getNotation() {
	    return notation;
	}
	public void setNotation(Notation notation) {
	    this.notation = notation == null ? Notation.DEFAULT : notation;
	}
		
	/**
	 * Checks for equality.
	 * Two CompositeFunctors are equal iff their classes,
	 * their compositors and their components are equal.
	 */
	public boolean equals(Object o) {
	    if (o == null || getClass() != o.getClass())
		return false;
	    // note that it does not matter to which .Composite we cast since we have already checked for class equality
	    Composite b = (Composite) o;
	    return Utility.equals(getCompositor(), b.getCompositor())
		&& Utility.equalsAll(getComponent(), b.getComponent());
	}

	public int hashCode() {
	    return Utility.hashCode(getCompositor()) ^ Utility.hashCodeAll(getComponent());
	}

	/**
	 * Get a string representation of the composite functor.
	 * @return <code>{@link Notation#format(Object, Object) notation.format}(getCompositor(), getComponent())</code>.
	 */
	public String toString() {
	    return getNotation().format(getCompositor(), getComponent());
	}
    }

    /**
     * <p>
     * This class is in fact a workaround for multiple inheritance of
     * {@link ModernFormula} and {@link orbital.logic.functor.Functionals.BinaryCompositeFunction}.</p>
     * 
     * @structure inherits ModernFormula
     * @structure inherits Functionals.BinaryCompositeFunction
     * @see BinaryCompositeFormula
     */
    static class BinaryCompositeVariableFormula extends ModernFormula implements Function.Composite {
	protected Formula outer;
	protected Formula left;
	protected Formula right;
	public BinaryCompositeVariableFormula(Logic underlyingLogic, Formula f, Formula g, Formula h, Notation notation) {
	    super(underlyingLogic);
	    setNotation(notation);
	    this.outer = f;
	    this.left = g;
	    this.right = h;
	}
	public BinaryCompositeVariableFormula(Logic underlyingLogic, Formula f, Formula g, Formula h) {
	    this(underlyingLogic, f, g, h, null);
	}
		
	private BinaryCompositeVariableFormula() {super(null);setNotation(null);}

        public Signature getSignature() {
	    //@todo could cache signature as well, provided left and right don't change
	    return left.getSignature().union(right.getSignature()).union(outer.getSignature());
        }

	public Set getFreeVariables() {
	    return Setops.union(
				Setops.union(left.getFreeVariables(),
					     right.getFreeVariables()),
				outer.getFreeVariables());
	}

	public Set getBoundVariables() {
	    return Setops.union(
				Setops.union(left.getBoundVariables(),
					     right.getBoundVariables()),
				outer.getBoundVariables());
	}

	Object interpret(Interpretation I) {
	    return apply(I);
	}

	/**
	 * The functions applied are subject to interpretation.
	 */
	public Object apply(Object/*>Interpretation<*/ arg) {
	    Object f = outer.apply(arg);
	    if (f instanceof BinaryPredicate && !(f instanceof BinaryFunction))
		f = Functionals.asFunction((BinaryPredicate) f);
	    return ((BinaryFunction) f).apply(left.apply(arg), right.apply(arg));
	} 
		

	// identical to @see orbital.logic.functor.Functionals.BinaryCompositeFunction
	public Functor getCompositor() {
	    return outer;
	} 
	public Object getComponent() {
	    return new Formula[] {
		left, right
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    setUnderlyingLogicLikeIn((Formula) f);
	    this.outer = (Formula) f;
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    Formula[] a = (Formula[]) g;
	    if (a.length != 2)
		throw new IllegalArgumentException(Formula.class + "[2] expected");
	    assert isCompatibleUnderlyingLogic(a[0]) && isCompatibleUnderlyingLogic(a[1]) : "only compose formulas of compatible logics";
	    this.left = a[0];
	    this.right = a[1];
	}

	// identical to @see orbital.logic.functor.Functor.Composite.Abstract
	/**
	 * the current notation used for displaying this composite functor.
	 * @serial
	 */
	private Notation notation;
	public Notation getNotation() {
	    return notation;
	}
	public void setNotation(Notation notation) {
	    this.notation = notation == null ? Notation.DEFAULT : notation;
	}
		
	/**
	 * Checks for equality.
	 * Two CompositeFunctors are equal iff their classes,
	 * their compositors and their components are equal.
	 */
	public boolean equals(Object o) {
	    if (o == null || getClass() != o.getClass())
		return false;
	    // note that it does not matter to which .Composite we cast since we have already checked for class equality
	    Composite b = (Composite) o;
	    return Utility.equals(getCompositor(), b.getCompositor())
		&& Utility.equalsAll(getComponent(), b.getComponent());
	}

	public int hashCode() {
	    return Utility.hashCode(getCompositor()) ^ Utility.hashCodeAll(getComponent());
	}

	/**
	 * Get a string representation of the composite functor.
	 * @return <code>{@link Notation#format(Object, Object) notation.format}(getCompositor(), getComponent())</code>.
	 */
	public String toString() {
	    return getNotation().format(getCompositor(), getComponent());
	}
    }
}



// /**
//  * Tags expressions (and functions) that introduces bindings.
//  */
// interface BindingExpression {
//     /**
//      * Get the symbol bound by this binding expression.
//      * @todo generalize to a set of symbols bound alltogether?
//      * @note however for BindingExpression functions cannot guess their left expression symbol with which they are composed in a CompositeFormula.
//      */
//     Symbol binding();
// }