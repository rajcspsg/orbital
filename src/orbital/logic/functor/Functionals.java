/**
 * @(#)Functionals.java 1.0 1996/03/02 Andre Platzer
 * 
 * Copyright (c) 1996-2001 Andre Platzer. All Rights Reserved.
 */

package orbital.logic.functor;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.io.Serializable;

import java.util.ListIterator;
import java.util.Collections;
import java.util.Arrays;
import java.util.NoSuchElementException;

import orbital.util.Pair;
import orbital.util.Utility;
import orbital.logic.IterationLimitException;
import orbital.util.Setops;
import java.lang.reflect.Array;

import java.util.LinkedList;

/**
 * Provides important compositional functionals for functors.
 * For that, this module class contains several static methods that work like <dfn>Functionals</dfn>,
 * i.e. high-order functions that have a Function-object in their argument or return type signature.
 * <p>
 * The compose methods in this class return objects that are instances of {@link Functor.Composite}.
 * By using this interface, notation manipulation and component extraction can be performed.
 * Since some section types like a binary function composed with two void functions are not
 * exported as an interface, decomposition may only be accessible with a dynamic type cast to {@link Functor.Composite}.
 * </p>
 * <p>
 * <i><b>Note:</b> some static methods in this class may get a true functional counterpart</i>,
 * that is functions that take functions as arguments and transform them into new functions.
 * However, while templates are not yet widely available in Java today, this will only increase
 * the number of type casts required.
 * </p>
 * 
 * @version 1.0, 2000/06/14
 * @author  Andr&eacute; Platzer
 * @see Functor
 * @see Function
 * @see BinaryFunction
 * @see Predicate
 * @see BinaryPredicate
 * @see java.util.Collection
 * @see java.util.Iterator
 * @see orbital.math.functional.Functionals
 * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
 * @see orbital.logic.trs.Substitutions#lambda
 * @todo improve names of conversion methods: onVoid, asFunction, onThisAndThat, asSuchAndSuch ...
 * @todo would we benefit from turning functional methods into true functionals, i.e. BinaryFunction<Function<A,B1>, Function<A,B2>, BinaryFunction<A,B1,B2>>? How do we document them without using Pizzadoc, then?
 * @todo introduce Zylomorphism and "GCatamorphism"?
 * @todo introduce operations union and section of predicates and BinaryPredicates and VoidPredicates?
 * @todo turn this class and its subclasses into Singletons?
 */
public class Functionals {
    /**
     * Class alias object.
     * <p>
     * To alias the methods in this class, use an idiom like
     * <pre>
     * <span class="comment">// alias object</span>
     * <span class="Orbital">Functionals</span> F = <span class="Orbital">Functionals</span>.functionals;
     * <span class="comment">// use alias</span>
     * <span class="Orbital">Function</span> f = (<span class="Orbital">Function</span>) F.compose(f1,f2);
     * <span class="comment">// instead of the long form</span>
     * <span class="Orbital">Function</span> f = (<span class="Orbital">Function</span>) <span class="Orbital">Functionals</span>.compose(f1,f2);
     * </pre>
     * </p>
     */
    public static final Functionals functionals = new Functionals();

    /**
     * prevent instantiation - module class
     */
    protected Functionals() {}

    // product functionals

    /**
     * cross: Map(A,B<sub>1</sub>)&times;Map(A,B<sub>2</sub>)&rarr;Map(A,B<sub>1</sub>&times;B<sub>2</sub>); (f,g) &#8614; f &times; g := (f,g).
     * <p>
     * Another notation for the function cross-product is f &times; g = f &#8855; g = f || g.</p>
     * @return x&#8614;f &times; g (x) = <big>(</big>f(x), g(x)<big>)</big> as a {@link orbital.util.Pair}.
     */
    //XXX: //TODO: introduce Function cross(Function[]); as a cross product f1&times;f2&times;f3...&times;fn perhaps in subclass orbital.math.functional.Functionals?
    //XXX: how should resulting Pairs ever be an argument to a BinaryFunction?
    public static /*<A, B1, B2>*/ Function/*<A, Pair>*/ cross(final Function/*<A, B1>*/ f, final Function/*<A, B2>*/ g) {
	return new Function/*<A, Pair>*/() {
		public Object/*>Pair<*/ apply(Object/*>A<*/ x) {
		    return new Pair(f.apply(x), g.apply(x));
		}
			
		public String toString() {
		    return "(" + f + "�" + g + ")";
		}
	    };
    }
	
    // composition functionals

    /**
     * compose: Map(B,C)&times;Map(A,B)&rarr;Map(A,C); (f,g) &#8614; f &#8728; g := f(g).
     * @return x&#8614;f &#8728; g (x) = f<big>(</big>g(x)<big>)</big>.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     */
    public static /*<A, B, C>*/ Function/*<A, C>*/.Composite compose(Function/*<B, C>*/ f, Function/*<A, B>*/ g) {
	return new Compositions.CompositeFunction/*<A, B, C>*/(f, g);
    } 
	
    /**
     * compose: Map(B<sub>1</sub>&times;B<sub>2</sub>,C)&times;(Map(A<sub>1</sub>&times;A<sub>2</sub>,B<sub>1</sub>)&times;Map(A<sub>1</sub>&times;A<sub>2</sub>,B<sub>2</sub>))&rarr;Map(A<sub>1</sub>&times;A<sub>2</sub>,C); (f,g,h) &#8614; f &#8728; (g &times; h) := f(g,h) .
     * @return (x,y)&#8614;f<big>(</big>g(x,y),h(x,y)<big>)</big>.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     */
    public static /*<A1, A2, B1, B2, C>*/ BinaryFunction/*<A1, A2, C>*/.Composite compose(BinaryFunction/*<B1, B2, C>*/ f, BinaryFunction/*<A1, A2, B1>*/ g, BinaryFunction/*<A1, A2, B2>*/ h) {
	return new Compositions.CompositeBinaryFunction/*<A1, A2, B1, B2, C>*/(f, g, h);
    } 

    /**
     * compose: Map(B<sub>1</sub>&times;B<sub>2</sub>,C)&times;(Map(A,B<sub>1</sub>)&times;Map(A,B<sub>2</sub>))&rarr;Map(A,C); (f,g,h) &#8614; f &#8728; (g &times; h) := f(g,h) .
     * @return x&#8614;f<big>(</big>g(x),h(x)<big>)</big>.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     * @internal see Functionals.BinaryCompositeFunction
     */
    public static /*<A, B1, B2, C>*/ Function/*<A, C>*/.Composite compose(BinaryFunction/*<B1, B2, C>*/ f, Function/*<A, B1>*/ g, Function/*<A, B2>*/ h) {
	//TODO: would we like a BinaryFunction composed with a Function, as well?
	return new BinaryCompositeFunction/*<A, B1, B2, C>*/(f, g, h);
    } 
    /**
     * A Function that is composed of
     * a BinaryFunction and two Functions concatenated with the binary one.
     * <p>
     * compose: <code> (f,g,h) &rarr; f &#8728; (g &times; h) := f(g,h) </code>.<br>
     * In other words results <code>f<big>(</big>g(x),h(x)<big>)</big></code>.
     * 
     * @structure implements Function
     * @structure concretizes Functor.Composite.Abstract
     * @structure aggregate outer:BinaryFunction
     * @structure aggregate left:Function
     * @structure aggregate right:Function
     * @version 1.0, 2000/01/23
     * @author  Andr&eacute; Platzer
     * @see Functionals#compose(BinaryFunction, Function, Function)
     */
    private static class BinaryCompositeFunction/*<A, B1, B2, C>*/ extends AbstractCompositeFunctor implements Function/*<A, C>*/.Composite {
	/**
	 * @serial
	 */
	protected BinaryFunction/*<B1, B2, C>*/ outer;
	/**
	 * @serial
	 */
	protected Function/*<A, B1>*/			  left;
	/**
	 * @serial
	 */
	protected Function/*<A, B2>*/			  right;
	public BinaryCompositeFunction(BinaryFunction/*<B1, B2, C>*/ f, Function/*<A, B1>*/ g, Function/*<A, B2>*/ h) {
	    this(f, g, h, null);
	}

	/**
	 * Create <code>f(g,h)</code>.
	 * @param notation specifies which notation should be used for string representations.
	 */
	public BinaryCompositeFunction(BinaryFunction/*<B1, B2, C>*/ f, Function/*<A, B1>*/ g, Function/*<A, B2>*/ h, Notation notation) {
	    super(notation);
	    this.outer = f;
	    this.left = g;
	    this.right = h;
	}
		
	protected BinaryCompositeFunction() {}

	public Functor getCompositor() {
	    return outer;
	} 
	public Object getComponent() {
	    return new Function[] {
		left, right
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    this.outer = (BinaryFunction/**<B1, B2, C>**/) f;
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    Function[] a = (Function[]) g;
	    if (a.length != 2)
		throw new IllegalArgumentException(Function.class + "[2] expected");
	    this.left = a[0];
	    this.right = a[1];
	}

	/**
	 * @return <code>outer<big>(</big>left(arg),right(arg)<big>)</big></code>.
	 */
	public Object/*>C<*/ apply(Object/*>A<*/ arg) {
	    return outer.apply(left.apply(arg), right.apply(arg));
	} 
    }

    /**
     * compose: Map(B,C)&times;Map({()},B)&rarr;Map({()},C); (f,g) &#8614; f &#8728; g := f(g).
     * @return ()&#8614;f &#8728; g () = f<big>(</big>g()<big>)</big>.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     */
    public static /*<B, C>*/ VoidFunction/*<C>*/.Composite compose(Function/*<B,C>*/ f, VoidFunction/*<B>*/ g) {
	return new Compositions.CompositeVoidFunction/*<B,C>*/(f, g);
    } 

    /**
     * compose: Map(B<sub>1</sub>&times;B<sub>2</sub>,C)&times;(Map({()},B<sub>1</sub>)&times;Map({()},B<sub>2</sub>))&rarr;Map({()},C); (f,g,h) &#8614; f &#8728; (g &times; h) := f(g,h) .
     * @return ()&#8614;f<big>(</big>g(),h()<big>)</big>.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     * @internal see Functionals.BinaryCompositeVoidFunction
     */
    public static /*<B1, B2, C>*/ VoidFunction/*<C>*/.Composite compose(BinaryFunction/*<B1, B2, C>*/ f, VoidFunction/*<B1>*/ g, VoidFunction/*<B2>*/ h) {
	return new BinaryCompositeVoidFunction/*<B1, B2, C>*/(f, g, h);
    } 

    /**
     * A VoidFunction that is composed of
     * a BinaryFunction and two VoidFunctions concatenated with the binary one.
     * <p>
     * compose: <code> (f,g,h) &rarr; f &#8728; (g &times; h) := f(g,h) </code>.<br>
     * In other words results <code>f<big>(</big>g(),h()<big>)</big></code>.
     * 
     * @structure implements VoidFunction
     * @structure concretizes Functor.Composite.Abstract
     * @structure aggregate outer:BinaryFunction
     * @structure aggregate left:Function
     * @structure aggregate right:Function
     * @version 1.0, 2000/01/23
     * @author  Andr&eacute; Platzer
     * @see Functionals#compose(BinaryFunction, VoidFunction, VoidFunction)
     */
    private static class BinaryCompositeVoidFunction/*<B1, B2, C>*/ extends AbstractCompositeFunctor implements VoidFunction/*<C>*/.Composite {
	/**
	 * @serial
	 */
	protected BinaryFunction/*<B1, B2, C>*/ outer;
	/**
	 * @serial
	 */
	protected VoidFunction/*<B1>*/   left;
	/**
	 * @serial
	 */
	protected VoidFunction/*<B2>*/   right;
	public BinaryCompositeVoidFunction(BinaryFunction/*<B1, B2, C>*/ f, VoidFunction/*<B1>*/ g, VoidFunction/*<B2>*/ h) {
	    this(f, g, h, null);
	}

	/**
	 * Create <code>f(g,h)</code>.
	 * @param notation specifies which notation should be used for string representations.
	 */
	public BinaryCompositeVoidFunction(BinaryFunction/*<B1, B2, C>*/ f, VoidFunction/*<B1>*/ g, VoidFunction/*<B2>*/ h, Notation notation) {
	    super(notation);
	    this.outer = f;
	    this.left = g;
	    this.right = h;
	}

	protected BinaryCompositeVoidFunction() {}

	public Functor getCompositor() {
	    return outer;
	} 
	public Object getComponent() {
	    return new VoidFunction[] {
		left, right
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    this.outer = (BinaryFunction/**<B1, B2, C>**/) f;
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    VoidFunction[] a = (VoidFunction[]) g;
	    if (a.length != 2)
		throw new IllegalArgumentException(VoidFunction.class + "[2] expected");
	    this.left = a[0];
	    this.right = a[1];
	}

	/**
	 * @return <code>outer<big>(</big>left(),right()<big>)</big></code>.
	 */
	public Object/*>C<*/ apply() {
	    return outer.apply(left.apply(), right.apply());
	} 
    }

    /**
     * compose: &weierp;(B)&times;Map(A,B)&rarr;&weierp;(A); (P,g) &#8614; P &#8728; g := P(g).
     * @return P &#8728; g = <!-- @todo use this(?) &lambda;-abstraction notation without reference to the interpretation with extensional semantics-->&lambda;x P<big>(</big>g(x)<big>)</big> = {x&isin;A &brvbar; P<big>(</big>g(x)<big>)</big>}.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     */
    public static /*<A, B>*/ Predicate/*<A>*/.Composite compose(Predicate/*<B>*/ P, Function/*<A, B>*/ g) {
	return new Compositions.CompositePredicate/*<A, B>*/(P, g);
    } 

    /**
     * compose: &weierp;(B<sub>1</sub>&times;B<sub>2</sub>)&times;(Map(A<sub>1</sub>&times;A<sub>2</sub>,B<sub>1</sub>)&times;Map(A<sub>1</sub>&times;A<sub>2</sub>,B<sub>2</sub>))&rarr;&weierp;(A<sub>1</sub>&times;A<sub>2</sub>); (P,g,h) &#8614; P &#8728; (g &times; h) := P(g,h) .
     * @return P &#8728; (g &times; h) = {(x,y)&isin;A<sub>1</sub>&times;A<sub>2</sub> &brvbar; P<big>(</big>g(x,y),h(x,y)<big>)</big>}.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     */
    public static /*<A1, A2, B1, B2>*/ BinaryPredicate/*<A1, A2>*/.Composite compose(BinaryPredicate/*<B1, B2>*/ P, BinaryFunction/*<A1, A2, B1>*/ g, BinaryFunction/*<A1, A2, B2>*/ h) {
	return new Compositions.CompositeBinaryPredicate/*<A1, A2, B1, B2>*/(P, g, h);
    } 

    /**
     * compose: &weierp;(B<sub>1</sub>&times;B<sub>2</sub>)&times;(Map(A,B<sub>1</sub>)&times;Map(A,B<sub>2</sub>))&rarr;&weierp;(A); (P,g,h) &#8614; P &#8728; (g &times; h) := P(g,h) .
     * @return P &#8728; (g &times; h) = {x&isin;A &brvbar; P<big>(</big>g(x),h(x)<big>)</big>}.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     * @see Functionals.BinaryCompositePredicate
     */
    public static /*<A, B1, B2>*/ Predicate/*<A>*/.Composite compose(BinaryPredicate/*<B1, B2>*/ P, Function/*<A, B1>*/ g, Function/*<A, B2>*/ h) {
	return new BinaryCompositePredicate/*<A, B1, B2>*/(P, g, h);
    } 
    /**
     * A Predicate that is composed of
     * an outer BinaryPredicate and two inner Functions.
     * <p>
     * compose: <code> (P,g,h) &rarr; P &#8728; (g &times; h) := P(g,h) </code>.<br>
     * In other words results <code>P<big>(</big>g(x),h(x)<big>)</big></code>.
     * 
     * @structure implements Predicate
     * @structure concretizes Functor.Composite.Abstract
     * @structure aggregate outer:BinaryPredicate
     * @structure aggregate left:Function
     * @structure aggregate right:Function
     * @version 1.0, 1999/03/16
     * @author  Andr&eacute; Platzer
     * @see Functionals#compose(BinaryPredicate, Function, Function)
     */
    private static class BinaryCompositePredicate/*<A, B1, B2>*/ extends AbstractCompositeFunctor implements Predicate/*<A>*/.Composite {
	/**
	 * @serial
	 */
	protected BinaryPredicate/*<B1, B2>*/ outer;
	/**
	 * @serial
	 */
	protected Function/*<A, B1>*/			left;
	/**
	 * @serial
	 */
	protected Function/*<A, B2>*/			right;
	public BinaryCompositePredicate(BinaryPredicate/*<B1, B2>*/ f, Function/*<A, B1>*/ g, Function/*<A, B2>*/ h) {
	    this(f, g, h, null);
	}

	/**
	 * Create <code>f(g,h)</code>.
	 * @param notation specifies which notation should be used for string representations.
	 */
	public BinaryCompositePredicate(BinaryPredicate/*<B1, B2>*/ f, Function/*<A, B1>*/ g, Function/*<A, B2>*/ h, Notation notation) {
	    super(notation);
	    this.outer = f;
	    this.left = g;
	    this.right = h;
	}

	protected BinaryCompositePredicate() {}

	public Functor getCompositor() {
	    return outer;
	} 
	public Object getComponent() {
	    return new Function[] {
		left, right
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    this.outer = (BinaryPredicate/**<B1, B2>**/) f;
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    Function[] a = (Function[]) g;
	    if (a.length != 2)
		throw new IllegalArgumentException(Function.class + "[2] expected");
	    this.left = a[0];
	    this.right = a[1];
	}

	/**
	 * @return <code>outer<big>(</big>left(arg),right(arg)<big>)</big></code>.
	 */
	public boolean apply(Object/*>A<*/ arg) {
	    return outer.apply(left.apply(arg), right.apply(arg));
	} 
    }

    /**
     * compose: &weierp;(B)&times;Map({()},B)&rarr;&weierp;({()}); (P,g) &#8614; P &#8728; g := P(g).
     * @return ()&#8614;P &#8728; g () = P<big>(</big>g()<big>)</big>.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     */
    public static /*<B>*/ VoidPredicate.Composite compose(Predicate P, VoidFunction g) {
	return new Compositions.CompositeVoidPredicate/*<B>*/(P, g);
    } 

    /**
     * compose: &weierp;(B<sub>1</sub>&times;B<sub>2</sub>)&times;(Map({()},B<sub>1</sub>)&times;Map({()},B<sub>2</sub>))&rarr;&weierp;({()}); (P,g,h) &#8614; P &#8728; (g &times; h) := P(g,h) .
     * @return ()&#8614;P<big>(</big>g(),h()<big>)</big>.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     * @see Functionals.BinaryCompositeVoidPredicate
     */
    public static /*<B1, B2>*/ VoidPredicate.Composite compose(BinaryPredicate/*<B1, B2>*/ P, VoidFunction/*<B1>*/ g, VoidFunction/*<B2>*/ h) {
	return new BinaryCompositeVoidPredicate/*<B1, B2>*/(P, g, h);
    } 
    /**
     * A VoidPredicate that is composed of
     * an outer BinaryPredicate and two inner VoidFunctions.
     * <p>
     * compose: <code> (P,g,h) &rarr; P &#8728; (g &times; h) := P(g,h) </code>.<br>
     * In other words results <code>P<big>(</big>g(),h()<big>)</big></code>.
     * 
     * @structure implements VoidPredicate
     * @structure concretizes Functor.Composite.Abstract
     * @structure aggregate outer:BinaryPredicate
     * @structure aggregate left:Function
     * @structure aggregate right:Function
     * @version 1.0, 1999/03/16
     * @author  Andr&eacute; Platzer
     * @see Functionals#compose(BinaryPredicate, Function, Function)
     */
    private static class BinaryCompositeVoidPredicate/*<B1, B2>*/ extends AbstractCompositeFunctor implements VoidPredicate.Composite {
	/**
	 * @serial
	 */
	protected BinaryPredicate/*<B1, B2>*/ outer;
	/**
	 * @serial
	 */
	protected VoidFunction/*<B1>*/	left;
	/**
	 * @serial
	 */
	protected VoidFunction/*<B2>*/	right;
	public BinaryCompositeVoidPredicate(BinaryPredicate/*<B1, B2>*/ f, VoidFunction/*<B1>*/ g, VoidFunction/*<B2>*/ h) {
	    this(f, g, h, null);
	}

	/**
	 * Create <code>f(g,h)</code>.
	 * @param notation specifies which notation should be used for string representations.
	 */
	public BinaryCompositeVoidPredicate(BinaryPredicate/*<B1, B2>*/ f, VoidFunction/*<B1>*/ g, VoidFunction/*<B2>*/ h, Notation notation) {
	    super(notation);
	    this.outer = f;
	    this.left = g;
	    this.right = h;
	}

	protected BinaryCompositeVoidPredicate() {}

	public Functor getCompositor() {
	    return outer;
	} 
	public Object getComponent() {
	    return new VoidFunction[] {
		left, right
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    this.outer = (BinaryPredicate/**<B1, B2>**/) f;
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    VoidFunction[] a = (VoidFunction[]) g;
	    if (a.length != 2)
		throw new IllegalArgumentException(VoidFunction.class + "[2] expected");
	    this.left = a[0];
	    this.right = a[1];
	}

	/**
	 * @return <code>outer<big>(</big>left(),right()<big>)</big></code>.
	 */
	public boolean apply() {
	    return outer.apply(left.apply(), right.apply());
	} 
    }

    /**
     * compose &#8728;: Map(B,C)&times;Map(A,B)&rarr;Map(A,C); (f,g) &#8614; f &#8728; g := f(g).
     * <p>
     * compose functional &#8728; calls the compose function appropriate for the type of g.
     * Valid types for g and h are {@link orbital.logic.functor.Function}, {@link orbital.logic.functor.VoidFunction}
     * and non-functor {@link java.util.Object}.
     * In the latter case, composition is done using a {@link Functions#constant(Object) constant function}.
     * </p>
     * <p>
     * For example, the following call creates a <a href="{@docRoot}/DesignPatters/Command.html">macro command function</a>
     * <pre>
     * <span class="Class">List</span> fs <span class="assignement">=</span> <span class="Class">Arrays</span>.asList(<span class="operator">new</span> <span class="Orbital">Function</span><span class="operator">[]</span> <span class="operator">{</span>f1, f2, ..., fn<span class="operator">}</span>);
     * <span class="comment">// f = <span class="bananaBracket">(|</span>id,&#8728;<span class="bananaBracket">|)</span> fs</span>
     * <span class="Orbital">Function</span> f <span class="assignement">=</span> <span class="Orbital">Functionals</span>.banana(<span class="Orbital">Functions</span>.id, <span class="Orbital">Functionals</span>.compose, fs.iterator());
     * </pre>
     * </p>
     * @return x&#8614;f &#8728; g (x) = f<big>(</big>g(x)<big>)</big>.
     * @pre f and g are "composable"
     * @see #genericCompose(Function,Object)
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     */
    public static final BinaryFunction compose = new BinaryFunction() {
	    public Object apply(Object f, Object g) {
		return genericCompose((Function)f, g);
	    }
	};

    /**
     * generic compose calls the compose function appropriate for the type of g.
     * Valid types for g and h are {@link orbital.logic.functor.Function}, {@link orbital.logic.functor.VoidFunction}
     * and non-functor {@link java.util.Object}.
     * In the latter case, composition is done using a {@link Functions#constant(Object) constant function}.
     * @pre g is "composable"
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     */
    public static Functor.Composite genericCompose(Function f, Object g) {
	if (g instanceof Functor) {
	    if (g instanceof Function)
		return Functionals.compose(f, (Function) g);
	    else if (g instanceof VoidFunction)
		return Functionals.compose(f, (VoidFunction) g);
    	} else
	    // compose for other types of arithmetics that are NOT any functions at all
	    return Functionals.compose(f, Functions.constant(g));
	throw new IllegalArgumentException("illegal type to compose " + (g == null ? "null" : g.getClass() + ""));
    } 

    /**
     * generic compose calls the compose function appropriate for the type of g and h.
     * Valid types for g and h are {@link orbital.logic.functor.VoidFunction}, {@link orbital.logic.functor.Function}, {@link orbital.logic.functor.BinaryFunction}
     * and non-functor {@link java.util.Object}.
     * In the latter case, composition is done using a {@link Functions#constant(Object) constant function}.
     * @pre g and h are "compatible"
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     */
    public static Functor.Composite genericCompose(BinaryFunction f, Object g, Object h) {
	if (g instanceof Functor && h instanceof Functor) {
	    // compose for equal types
	    if (g instanceof Function && h instanceof Function)
		return Functionals.compose(f, (Function) g, (Function) h);
	    if (g instanceof BinaryFunction && h instanceof BinaryFunction)
		return Functionals.compose(f, (BinaryFunction) g, (BinaryFunction) h);
	    if (g instanceof VoidFunction && h instanceof VoidFunction)
		return Functionals.compose(f, (VoidFunction)g, (VoidFunction)h);
	} else if (g instanceof Functor || h instanceof Functor) {
	    assert g instanceof Functor ^ h instanceof Functor : "!(a&b)&(a|b) -> (a^b)";
	    // compose for well-defined mixed types as well
	    //XXX: see MathUtilities.makeSymbolAware(Arithmetic) calls to constant(...)
	    if (g instanceof Function)
		return Functionals.compose(f, (Function) g, Functions.constant(h));
	    else if (h instanceof Function)
		return Functionals.compose(f, Functions.constant(g), (Function) h);
	    else if (g instanceof BinaryFunction)
		return Functionals.compose(f, (BinaryFunction) g, Functions.binaryConstant(h));
	    else if (h instanceof BinaryFunction)
		return Functionals.compose(f, Functions.binaryConstant(g), (BinaryFunction) h);
        } else
	    // compose for other types of arithmetics that are NOT any functions at all
	    return Functionals.compose(f, Functions.constant(g), Functions.constant(h));
	throw new ClassCastException("the type of the arguments to compose do not match "+ (g == null ? "null" : g.getClass() + "") + ", " + (h == null ? "null" : h.getClass() + ""));
    } 

    /**
     * generic compose calls the compose function appropriate for the type of g and h.
     * @pre g and h are "compatible"
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade Pattern</a>
     */
    public static Functor.Composite genericCompose(BinaryPredicate P, Object g, Object h) {
	if (g instanceof Function && h instanceof Function)
	    return Functionals.compose(P, (Function) g, (Function) h);
	if (g instanceof BinaryFunction && h instanceof BinaryFunction)
	    return Functionals.compose(P, (BinaryFunction) g, (BinaryFunction) h);
	if (g instanceof VoidFunction && h instanceof VoidFunction)
	    return Functionals.compose(P, (VoidFunction)g, (VoidFunction)h);

	//TODO: copy implementation for mixed types as well?
	throw new ClassCastException("the type of the arguments to compose do not match "+ (g == null ? "null" : g.getClass() + "") + ", " + (h == null ? "null" : h.getClass() + ""));
    } 


    // argument binding

    /**
     * Binds the first argument of a BinaryFunction to a fixed value.
     * <p>
     * bindFirst: Map(A<sub>1</sub>&times;A<sub>2</sub>,B)&rarr;Map(A<sub>2</sub>,B); f&#8614;f(x,&middot;).
     * The unary left-adjoint got from f by "currying".</p>
     * @return f(x,&middot;):A<sub>2</sub>&rarr;B; y &#8614; f(y) := f(x, y)
     */
    public static /*<A1, A2, B>*/ Function/*<A2, B>*/ bindFirst(BinaryFunction/*<A1, A2, B>*/ f, Object/*>A1<*/ x) {
	return new BindFirstFunction/*<A1, A2, B>*/(f, x);
    } 
    //TODO: binding can be reduced to composition along with constant. Would this be simpler?
    //TODO: is it of advantage when we derived orbital.math.functional.Functionals.BindFirstFunction from this class?
    // except for the AbstractFunction problem!
    private static class BindFirstFunction/*<A1, A2, B>*/ extends AbstractCompositeFunctor implements Function/*<A2, B>*/ {
	/**
	 * @serial
	 */
	protected final BinaryFunction/*<A1, A2, B>*/ f;
	/**
	 * @serial
	 */
	protected final Object/*>A1<*/ x;
	public BindFirstFunction(BinaryFunction/*<A1, A2, B>*/ f, Object/*>A1<*/ x, Notation notation) {
	    super(notation);
	    this.f = f;
	    this.x = x;
	}
	public BindFirstFunction(BinaryFunction/*<A1, A2, B>*/ f, Object/*>A1<*/ x) {
	    this(f, x, null);
	}
	public Object/*>B<*/ apply(Object/*>A2<*/ y) {
	    return f.apply(x, y);
	} 

	public Functor getCompositor() {
	    return f;
	}
	public Object getComponent() {
	    return new Object[] {
		x, "#0"
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}

    } 

    /**
     * Binds the first argument of a BinaryPredicate to a fixed value.
     * <p>
     * bindFirst: &weierp;(A<sub>1</sub>&times;A<sub>2</sub>)&rarr;&weierp;(A<sub>2</sub>); P&#8614;P(x,&middot;).
     * The unary left-adjoint got from P by "currying".</p>
     * @return P(x,&middot;) := {y&isin;A<sub>2</sub> &brvbar; P(x, y)}
     */
    public static /*<A1, A2>*/ Predicate/*<A2>*/ bindFirst(BinaryPredicate/*<A1, A2>*/ P, Object/*>A1<*/ x) {
	return new BindFirstPredicate/*<A1, A2>*/(P, x);
    } 

    private static class BindFirstPredicate/*<A1, A2>*/ extends AbstractCompositeFunctor implements Predicate/*<A2>*/ {
	/**
	 * @serial
	 */
	protected final BinaryPredicate/*<A1, A2>*/ P;
	/**
	 * @serial
	 */
	protected final Object/*>A1<*/ x;
	public BindFirstPredicate(BinaryPredicate/*<A1, A2>*/ P, Object/*>A1<*/ x, Notation notation) {
	    super(notation);
	    this.P = P;
	    this.x = x;
	}
	public BindFirstPredicate(BinaryPredicate/*<A1, A2>*/ P, Object/*>A1<*/ x) {
	    this(P, x, null);
	}
	public boolean apply(Object/*>A2<*/ y) {
	    return P.apply(x, y);
	} 
	public Functor getCompositor() {
	    return P;
	}
	public Object getComponent() {
	    return new Object[] {
		x, "#0"
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
    } 

    /**
     * Binds the second argument of a BinaryFunction to a fixed value.
     * <p>
     * bindSecond: Map(A<sub>1</sub>&times;A<sub>2</sub>,B)&rarr;Map(A<sub>1</sub>,B); f&#8614;f(&middot;,y).
     * The unary right-adjoint got from f by "currying".</p>
     * @return f(&middot;,y):A<sub>1</sub>&rarr;B; x &#8614; f(x) := f(x, y)
     */
    public static /*<A1, A2, B>*/ Function/*<A1, B>*/ bindSecond(BinaryFunction/*<A1, A2, B>*/ f, Object/*>A2<*/ y) {
	return new BindSecondFunction/*<A1, A2, B>*/(f, y);
    } 
    private static class BindSecondFunction/*<A1, A2, B>*/ extends AbstractCompositeFunctor implements Function/*<A1, B>*/ {
	/**
	 * @serial
	 */
	protected final BinaryFunction/*<A1, A2, B>*/ f;
	/**
	 * @serial
	 */
	protected final Object/*>A2<*/ y;
	public BindSecondFunction(BinaryFunction/*<A1, A2, B>*/ f, Object/*>A2<*/ y, Notation notation) {
	    super(notation);
	    this.f = f;
	    this.y = y;
	}
	public BindSecondFunction(BinaryFunction/*<A1, A2, B>*/ f, Object/*>A2<*/ y) {
	    this(f, y, null);
	}
	public Object/*>B<*/ apply(Object/*>A1<*/ x) {
	    return f.apply(x, y);
	} 
	public Functor getCompositor() {
	    return f;
	}
	public Object getComponent() {
	    return new Object[] {
		"#0", y
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
    }

    /**
     * Binds the second argument of a BinaryPredicate to a fixed value.
     * <p>
     * bindSecond: &weierp;(A<sub>1</sub>&times;A<sub>2</sub>)&rarr;&weierp;(A<sub>1</sub>); P&#8614;P(&middot;,y).
     * The unary right-adjoint got from P by "currying".</p>
     * @return P(&middot;,y) := {x&isin;A<sub>1</sub> &brvbar; P(x, y)}
     */
    public static /*<A1, A2>*/ Predicate/*<A1>*/ bindSecond(BinaryPredicate/*<A1, A2>*/ P, Object/*>A2<*/ y) {
	return new BindSecondPredicate/*<A1, A2>*/(P, y);
    } 
    private static class BindSecondPredicate/*<A1, A2>*/ extends AbstractCompositeFunctor implements Predicate/*<A1>*/ {
	/**
	 * @serial
	 */
	protected final BinaryPredicate/*<A1, A2>*/ P;
	/**
	 * @serial
	 */
	protected final Object/*>A2<*/ y;
	public BindSecondPredicate(BinaryPredicate/*<A1, A2>*/ P, Object/*>A2<*/ y, Notation notation) {
	    super(notation);
	    this.P = P;
	    this.y = y;
	}
	public BindSecondPredicate(BinaryPredicate/*<A1, A2>*/ P, Object/*>A2<*/ y) {
	    this(P, y, null);
	}
	public boolean apply(Object/*>A1<*/ x) {
	    return P.apply(x, y);
	} 
	public Functor getCompositor() {
	    return P;
	}
	public Object getComponent() {
	    return new Object[] {
		"#0", y
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
    }	
		
    /**
     * Binds the argument of a Function to a fixed value.
     * <p>
     * bind: Map(A,B)&rarr;Map({()},B); f&#8614;f(a).
     * The void adjoint got from f by "currying"
     * Every evaluation of a function can be seen as a sequence of binds ending with a void bind.</p>
     * @return f(a):{()}&rarr;B; () &#8614; f := f(a)
     */
    public static /*<A, B>*/ VoidFunction/*<B>*/ bind(Function/*<A, B>*/ f, Object/*>A<*/ a) {
	return new BindFunction/*<A, B>*/(f, a);
    } 
    private static class BindFunction/*<A, B>*/ extends AbstractCompositeFunctor implements VoidFunction/*<B>*/ {
	/**
	 * @serial
	 */
	protected final Function/*<A, B>*/ f;
	/**
	 * @serial
	 */
	protected final Object/*>A<*/ a;
	public BindFunction(Function/*<A, B>*/ f, Object/*>A<*/ a, Notation notation) {
	    super(notation);
	    this.f = f;
	    this.a = a;
	}
	public BindFunction(Function/*<A, B>*/ f, Object/*>A<*/ a) {
	    this(f, a, null);
	}
	public Object/*>B<*/ apply() {
	    return f.apply(a);
	} 
	public Functor getCompositor() {
	    return f;
	}
	public Object getComponent() {
	    return null;
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
    }

    /**
     * Binds the argument of a Predicate to a fixed value.
     * <p>
     * bind: &weierp;(A)&rarr;&weierp;({()}); P&#8614;P(a).
     * The void adjoint got from P by "currying".
     * </p>
     * @return P(a):&empty;&rarr;<span class="@todo void predicate types">Boole</span>; () &#8614; P := P(a)
     */
    public static /*<A>*/ VoidPredicate bind(Predicate/*<A>*/ P, Object/*>A<*/ a) {
	return new BindPredicate/*<A>*/(P, a);
    } 
    private static class BindPredicate/*<A>*/ extends AbstractCompositeFunctor implements VoidPredicate {
	/**
	 * @serial
	 */
	protected final Predicate/*<A>*/ P;
	/**
	 * @serial
	 */
	protected final Object/*>A<*/ a;
	public BindPredicate(Predicate/*<A>*/ P, Object/*>A<*/ a, Notation notation) {
	    super(notation);
	    this.P = P;
	    this.a = a;
	}
	public BindPredicate(Predicate/*<A>*/ P, Object/*>A<*/ a) {
	    this(P, a, null);
	}
	public boolean apply() {
	    return P.apply(a);
	} 
	public Functor getCompositor() {
	    return P;
	}
	public Object getComponent() {
	    return null;
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
    }	
		

    /**
     * Binds both arguments of a BinaryFunction together.
     * <p>
     * bind: Map(A&times;A,B)&rarr;Map(A,B); f&#8614;g.
     * The unitary adjoint function (not by "currying").</p>
     * @return g:A&rarr;B; x &#8614; g(x) := f(x, x)
     */
    public static /*<A, B>*/ Function/*<A, B>*/ bind(BinaryFunction/*<A, A, B>*/ f) {
	return new BindTogetherFunction/*<A, B>*/(f);
    } 
    private static class BindTogetherFunction/*<A, B>*/ extends AbstractCompositeFunctor implements Function/*<A, B>*/ {
	/**
	 * @serial
	 */
	protected final BinaryFunction/*<A, A, B>*/ f;
	public BindTogetherFunction(BinaryFunction/*<A, A, B>*/ f, Notation notation) {
	    super(notation);
	    this.f = f;
	}
	public BindTogetherFunction(BinaryFunction/*<A, A, B>*/ f) {
	    this(f, null);
	}
	public Object/*>B<*/ apply(Object/*>A<*/ x) {
	    return f.apply(x, x);
	} 
	public Functor getCompositor() {
	    return f;
	}
	public Object getComponent() {
	    return new String[] {
		"#0", "#0"
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
    }

    /**
     * Binds both arguments of a BinaryPredicate together.
     * <p>
     * bind: &weierp;(A&times;A)&rarr;&weierp;(A); P&#8614;Q.
     * The unitary adjoint predicate (not by "currying").
     * </p>
     * @return Q := {x&isin;A &brvbar; P(x, x)}
     */
    public static /*<A>*/ Predicate/*<A>*/ bind(BinaryPredicate/*<A, A>*/ P) {
	return new BindTogetherPredicate/*<A>*/(P);
    } 
    private static class BindTogetherPredicate/*<A>*/ extends AbstractCompositeFunctor implements Predicate/*<A>*/ {
	/**
	 * @serial
	 */
	protected final BinaryPredicate/*<A, A>*/ P;
	public BindTogetherPredicate(BinaryPredicate/*<A, A>*/ P, Notation notation) {
	    super(notation);
	    this.P = P;
	}
	public BindTogetherPredicate(BinaryPredicate/*<A, A>*/ P) {
	    this(P, null);
	}
	public boolean apply(Object/*>A<*/ x) {
	    return P.apply(x, x);
	} 
	public Functor getCompositor() {
	    return P;
	}
	public Object getComponent() {
	    return new String[] {
		"#0", "#0"
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
    }

    // projection binding

    /**
     * Applies a function on the first argument, ignoring the second.
     * <p>
     * onFirst: f&#8614;g.</p>
     * <p><b><i>Evolves</i>:</b> might be renamed or removed.</p>
     * @return (x,y) &#8614; g(x,y) := f(x)
     * @see <a href="{@docRoot}/DesignPatterns/Adapter.html">Adapter Pattern</a>
     * @todo is this "uncurrying"? Also, the documentation arraws etc. are garbage. See bind*
     */
    public static /*<A1, B>*/ BinaryFunction/*<A1, Object, B>*/ onFirst(final Function/*<A1, B>*/ f) {
	return new BinaryFunction/*<A1, Object, B>*/() {
		public Object/*>B<*/ apply(Object/*>A1<*/ first, Object second) {
		    return f.apply(first);
		} 

		//TODO: implement equals(Object)
		// not quite beautyful for all functors
		public String toString() {
		    return Notation.DEFAULT.format(f, new String[] {
			"#0"	//XXX: was "x", "_"
		    });
		} 
	    };
    } 

    /**
     * Applies a predicate on the first argument, ignoring the second.
     * <p>
     * onFirst: f&#8614;g;.</p>
     * <p><b><i>Evolves</i>:</b> might be renamed or removed.</p>
     * @return (x,y) &#8614; g(x,y) := f(x)
     * @see <a href="{@docRoot}/DesignPatterns/Adapter.html">Adapter Pattern</a>
     */
    public static /*<A1>*/ BinaryPredicate/*<A1, Object>*/ onFirst(final Predicate/*<A1>*/ p) {
	return new BinaryPredicate/*<A1, Object>*/() {
		public boolean apply(Object/*>A1<*/ first, Object second) {
		    return p.apply(first);
		} 

		//TODO: implement equals(Object)
		// not quite beautyful for all functors
		public String toString() {
		    return Notation.DEFAULT.format(p, new String[] {
			"#0"	//XXX: was "x", "_"
		    });
		} 
	    };
    } 

    /**
     * Applies a function on the second argument, ignoring the first.
     * <p>
     * onSecond:  f&#8614;g;.</p>
     * <p><b><i>Evolves</i>:</b> might be renamed or removed.</p>
     * @return (x,y) &#8614; g(x,y) := f(y)
     * @see <a href="{@docRoot}/DesignPatterns/Adapter.html">Adapter Pattern</a>
     */
    public static /*<A2, B>*/ BinaryFunction/*<Object, A2, B>*/ onSecond(final Function/*<A2, B>*/ f) {
	return new BinaryFunction/*<Object, A2, B>*/() {
		public Object/*>B<*/ apply(Object first, Object/*>A2<*/ second) {
		    return f.apply(second);
		} 

		//XXX: not quite beautyful for all functors
		public String toString() {
		    return Notation.DEFAULT.format(f, new String[] {
			"#1"
		    });
		} 
	    };
    } 

    /**
     * Applies a predicate on the second argument, ignoring the first.
     * <p>
     * onSecond: f&#8614;g;.</p>
     * <p><b><i>Evolves</i>:</b> might be renamed or removed.</p>
     * @return (x,y) &#8614; g(x,y) := f(y)
     * @see <a href="{@docRoot}/DesignPatterns/Adapter.html">Adapter Pattern</a>
     */
    public static /*<A2>*/ BinaryPredicate/*<Object, A2>*/ onSecond(final Predicate/*<A2>*/ p) {
	return new BinaryPredicate/*<Object, A2>*/() {
		public boolean apply(Object first, Object/*>A2<*/ second) {
		    return p.apply(second);
		} 

		//XXX: not quite beautyful for all functors
		public String toString() {
		    return Notation.DEFAULT.format(p, new String[] {
			"#1"
		    });
		} 
	    };
    } 

    /**
     * Applies a function on the void argument, ignoring all arguments.
     * <p>
     * onVoid:  f&#8614;g;.</p>
     * <p><b><i>Evolves</i>:</b> might be renamed or removed.</p>
     * @return x &#8614; g(x) := f()
     * @see <a href="{@docRoot}/DesignPatterns/Adapter.html">Adapter Pattern</a>
     */
    public static /*<A, B>*/ Function/*<A, B>*/ onVoid(final VoidFunction/*<B>*/ f) {
	return new Function/*<A, B>*/() {
		public Object/*>B<*/ apply(Object/*>A<*/ arg) {
		    return f.apply();
		} 

		//@xxx: implement equals(Object), hashCode
		// not quite beautyful for all functors
		public String toString() {
		    return Notation.DEFAULT.format(f, new String[] {});
		} 
	    };
    } 

    /**
     * Applies a predicate on the void argument, ignoring all arguments.
     * <p>
     * onVoid:  f&#8614;g;.</p>
     * <p><b><i>Evolves</i>:</b> might be renamed or removed.</p>
     * @return x &#8614; g(x) := f()
     * @see <a href="{@docRoot}/DesignPatterns/Adapter.html">Adapter Pattern</a>
     */
    public static /*<A>*/ Predicate/*<A>*/ onVoid(final VoidPredicate p) {
	return new Predicate/*<A>*/() {
		public boolean apply(Object/*>A<*/ arg) {
		    return p.apply();
		} 

		//@xxx: implement equals(Object), hashCode
		// not quite beautyful for all functors
		public String toString() {
		    return Notation.DEFAULT.format(p, new String[] {});
		} 
	    };
    } 

    // argument swapping

    /**
     * Swaps the two arguments of a BinaryFunction.
     * <p>
     * swap: Map(A<sub>1</sub>&times;A<sub>2</sub>,B)&rarr;Map(A<sub>2</sub>&times;A<sub>1</sub>,B); f&#8614;f<sup>&harr;</sup>.
     * </p>
     * @return f<sup>&harr;</sup>:A<sub>2</sub>&times;A<sub>1</sub>&rarr;B; (x,y) &#8614; f<sup>&harr;</sup>(x,y) := f(y,x)
     * @attribute involutive ...
     */
    public static /*<A1, A2, B>*/ BinaryFunction/*<A2, A1, B>*/ swap(BinaryFunction/*<A1, A2, B>*/ f) {
	return f instanceof SwapFunction
	    ? ((SwapFunction) f).f					// involutive
	    : new SwapFunction/*<A1, A2, B>*/(f);
    } 
    private static class SwapFunction/*<A1, A2, B>*/ extends AbstractCompositeFunctor implements BinaryFunction/*<A2, A1, B>*/ {
	/**
	 * @serial
	 */
	protected final BinaryFunction/*<A1, A2, B>*/ f;
	public SwapFunction(BinaryFunction/*<A1, A2, B>*/ f, Notation notation) {
	    super(notation);
	    this.f = f;
	}
	public SwapFunction(BinaryFunction/*<A1, A2, B>*/ f) {
	    this(f, null);
	}
	public Object/*>B<*/ apply(Object/*>A2<*/ x, Object/*>A1<*/ y) {
	    return f.apply(y, x);
	} 
	public Functor getCompositor() {
	    return f;
	}
	public Object getComponent() {
	    return new String[] {
		"#1", "#0"
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
    } 

    /**
     * Swaps the two arguments of a BinaryPredicate.
     * <p>
     * swap: &weierp;(A<sub>1</sub>&times;A<sub>2</sub>)&rarr;&weierp;(A<sub>2</sub>&times;A<sub>1</sub>); P&#8614;P<sup>&harr;</sup>.
     * </p>
     * <p>
     * Returns the converse predicate P<sup>-1</sup> of P.
     * </p>
     * @return P<sup>-1</sup>=P<sup>&harr;</sup> := {(x,y)&isin;A<sub>2</sub>&times;A<sub>1</sub> &brvbar; P(y,x)}
     * @attribute morph &cup;
     * @attribute morph &cap;
     * @attribute involutive ...
     * @todo document the relationship with P<sup>-1</sup> from Logik II better.
     */
    public static /*<A1, A2>*/ BinaryPredicate/*<A2, A1>*/ swap(BinaryPredicate/*<A1, A2>*/ P) {
	return P instanceof SwapPredicate
	    ? ((SwapPredicate) P).P					// involutive
	    : new SwapPredicate/*<A1, A2>*/(P);
    } 
    private static class SwapPredicate/*<A1, A2>*/ extends AbstractCompositeFunctor implements BinaryPredicate/*<A2, A1>*/ {
	/**
	 * @serial
	 */
	protected final BinaryPredicate/*<A1, A2>*/ P;
	public SwapPredicate(BinaryPredicate/*<A1, A2>*/ P, Notation notation) {
	    super(notation);
	    this.P = P;
	}
	public SwapPredicate(BinaryPredicate/*<A1, A2>*/ P) {
	    this(P, null);
	}
	public boolean apply(Object/*>A2<*/ x, Object/*>A1<*/ y) {
	    return P.apply(y, x);
	} 
	public Functor getCompositor() {
	    return P;
	}
	public Object getComponent() {
	    return new String[] {
		"#1", "#0"
	    };
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    throw new UnsupportedOperationException("how to do");
	}
    } 


    // adapter methods
	
    /**
     * converts a predicate to a function.
     * <p>
     * This method acts as a bridge between predicates and functions in case
     * a predicate representation is not acceptable.
     * </p>
     * @see <a href="{@docRoot}/DesignPatterns/Adapter.html">Adapter Pattern</a>
     */
    public static VoidFunction/*<Boolean>*/ asFunction(VoidPredicate p) {
	return new VoidPredicateFunction(p);
    }
    private static class VoidPredicateFunction extends AbstractCompositeFunctor implements VoidFunction/*<Boolean>*/ {
	/**
	 * @serial
	 */
	protected VoidPredicate p;
	public VoidPredicateFunction(VoidPredicate p) {
	    this.p = p;
	}
	private VoidPredicateFunction() {}

	public Object/*>Boolean<*/ apply() {
	    return new Boolean(p.apply());
	} 
	public Functor getCompositor() {
	    return p;
	}
	public Object getComponent() {
	    return new Object[] {};
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    this.p = (VoidPredicate) f;
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    Object[] a = (Object[]) g;
	    if (a.length != 0)
		//@todo better description
		throw new IllegalArgumentException("zero components expected");
	}
    }

    /**
     * converts a predicate to a function.
     * <p>
     * This method acts as a bridge between predicates and functions in case
     * a predicate representation is not acceptable.
     * </p>
     * @see <a href="{@docRoot}/DesignPatterns/Adapter.html">Adapter Pattern</a>
     */
    public static /*<A>*/ Function/*<A, Boolean>*/ asFunction(Predicate/*<A>*/ p) {
	return new PredicateFunction/*<A>*/(p);
    }
    private static class PredicateFunction/*<A>*/ extends AbstractCompositeFunctor implements Function/*<A, Boolean>*/ {
	/**
	 * @serial
	 */
	protected Predicate/*<A>*/ p;
	public PredicateFunction(Predicate/*<A>*/ p) {
	    this.p = p;
	}
	private PredicateFunction() {}

	public Object/*>Boolean<*/ apply(Object/*>A<*/ x) {
	    return new Boolean(p.apply(x));
	} 
	public Functor getCompositor() {
	    return p;
	}
	public Object getComponent() {
	    // provide nice formatting and avoid pure arguments
	    return new String[] {};
	    /*new String[] {
	      "#0"
	      };*/
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    this.p = (Predicate/**<A>**/) f;
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    Object[] a = (Object[]) g;
	    if (a.length != 0)
		//@todo better description
		throw new IllegalArgumentException("zero components expected");
	}
    }

    /**
     * converts a predicate to a function.
     * <p>
     * This method acts as a bridge between predicates and functions in case
     * a predicate representation is not acceptable.
     * </p>
     * @see <a href="{@docRoot}/DesignPatterns/Adapter.html">Adapter Pattern</a>
     */
    public static /*<A1, A2>*/ BinaryFunction/*<A1, A2, Boolean>*/ asFunction(BinaryPredicate/*<A1, A2>*/ p) {
	return new BinaryPredicateFunction/*<A1, A2>*/(p);
    }
    private static class BinaryPredicateFunction/*<A1, A2>*/ extends AbstractCompositeFunctor implements BinaryFunction/*<A1, A2, Boolean>*/ {
	/**
	 * @serial
	 */
	protected BinaryPredicate/*<A1, A2>*/ p;
	public BinaryPredicateFunction(BinaryPredicate/*<A1, A2>*/ p) {
	    this.p = p;
	}
	private BinaryPredicateFunction() {}

	public Object/*>Boolean<*/ apply(Object/*>A1<*/ x, Object/*>A2<*/ y) {
	    return new Boolean(p.apply(x, y));
	} 
	public Functor getCompositor() {
	    return p;
	}
	public Object getComponent() {
	    // provide nice formatting and avoid pure arguments
	    return new String[] {};
	    /*new String[] {
	      "#0", "#1"
	      };*/
	} 

	public void setCompositor(Functor f) throws ClassCastException {
	    this.p = (BinaryPredicate/**<A2, A2>**/) f;
	}
	public void setComponent(Object g) throws IllegalArgumentException, ClassCastException {
	    Object[] a = (Object[]) g;
	    if (a.length != 0)
		//@todo better description
		throw new IllegalArgumentException("zero components expected");
	}
    }


    // functional-style bulk operations

    /**
     * Get a listable function applicable to lists.
     * <p>
     * listable: Map(A,B)&rarr;Map(A&cup;A<sup>*</sup>,C&cup;C<sup>*</sup>); f &#8614; f<sup>*</sup>.
     * </p>
     * <p>
     * Applies the Function to each element of the list a.
     * Also known as collect.
     * </p>
     * @return x&#8614;f<sup>*</sup>(x)
     *  where f<sup>*</sup><big>(</big>{x<sub>0</sub>,...,x<sub>m</sub>}<big>)</big> = <big>{</big>f(x<sub>0</sub>),...,f(x<sub>m</sub>)<big>}</big>
     *  for {@link List}s, {@link Collection}s and {@link Object Object[]},
     *  and f<sup>*</sup>(x) = f(x), otherwise.
     * <div>listable: a &#8614; f(a) := {f(x) &brvbar; x&isin;a}</div>
     * @see #map(Function, Collection)
     */
    public static /*<A, B>*/ Function listable(Function/*<A, B>*/ f) {
	return f instanceof ListableFunction/* A, B */ ? (Function) f : (Function) new ListableFunction/*<A, B>*/(f);
    }
    private static class ListableFunction/*<A, B>*/ implements Function {
	/**
	 * @serial
	 */
	protected final Function/*<A, B>*/ function;
	public ListableFunction(Function/*<A, B>*/ function) {
	    this.function = function;
	}
	public Functor getCompositor() {
	    return function;
	}

	public Object apply(Object x) {
	    // almost identical to @see Utility#asIterator
	    // (almost) return map(function, asIterator(x), ...); but with optimized third argument and optimized adequate return-type
	    if (x instanceof Collection/*_<A>_*/) {
		Collection/*_<A>_*/ c = (Collection/*_<A>_*/) x;
		return map(function, c.iterator(), Setops.newCollectionLike(c));
	    } else if (x instanceof Iterator/*_<A>_*/)
		return map(function, (Iterator/*_<A>_*/) x, new LinkedList/*_<B>_*/());
	    else if (x instanceof Object/*>A<*/[])
		return map(function, (Object/*>A<*/[]) x);
	    else
		return function.apply((Object/*>A<*/) x);
	} 

	public boolean equals(Object o) {
	    if (o instanceof ListableFunction)
		return getCompositor().equals(((ListableFunction) o).getCompositor());
	    else
		return false;
	}

	public int hashCode() {
	    return Utility.hashCode(function);
	}

	public String toString() {
	    return function + "";
	}
    } 

    /**
     * Maps a list of arguments with a Function.
     * <p>
     * map: a &#8614; f(a) := {f(x) &brvbar; x&isin;a}.<br />
     * Applies the Function to each element of the list a.</p>
     * <p>Also known as collect.</p>
     */
    public static /*<A, B>*/ Object/*>B<*/[] map(Function/*<A, B>*/ f, Object/*>A<*/[] a) {
	//Object/*>B<*/[] r = new Object/*>B<*/[a.length]; // is not of correct sub-type
	Object/*>B<*/[] r = (Object/*>B<*/[]) Array.newInstance(a.getClass().getComponentType(), a.length);
	for (int i = 0; i < r.length; i++)
	    r[i] = f.apply(a[i]);
	return r;
    } 
    private static /*<A, B>*/ Collection/*_<B>_*/ map(Function/*<A, B>*/ f, Iterator/*_<A>_*/ a, Collection/*_<B>_*/ r) {
	while (a.hasNext())
	    r.add(f.apply((/*__*/Object/*>A<*/) a.next()));
	return r;
    } 
    public static /*<A, B>*/ Collection/*_<B>_*/ map(Function/*<A, B>*/ f, Iterator/*_<A>_*/ a) {
	return map(f, a, new LinkedList/*_<B>_*/());
    } 
    /**
     * Maps a list of arguments with a Function.
     * <p>
     * map: a &#8614; f(a) := {f(x) &brvbar; x&isin;a}.<br />
     * Applies the Function to each element of the list a.</p>
     * <p>Also known as collect.</p>
     * @post RES.getClass()=a.getClass() or at least compatible
     * @see #listable(Function)
     */
    public static /*<A, B>*/ Collection/*_<B>_*/ map(Function/*<A, B>*/ f, Collection/*_<A>_*/ a) {
	return map(f, a.iterator(), Setops.newCollectionLike(a));
    }
    public static /*<A, B>*/ List/*_<B>_*/ map(Function/*<A, B>*/ f, List/*_<A>_*/ a) {
	return (List/*_<B>_*/) map(f, (Collection/*_<B>_*/) a);
    }

    /**
     * Get a listable function applicable to lists.
     * <p>
     * listable: Map(A,B)&rarr;Map(A&cup;A<sup>*</sup>,C&cup;C<sup>*</sup>); f &#8614; f<sup>*</sup>.</p>
     * <p>
     * listable: a &#8614; f(a) := {f(x) &brvbar; x&isin;a}.<br />
     * Applies the Function to each element of the list a.</p>
     * @return x&#8614;f<sup>*</sup>(x)
     *  where f<sup>*</sup><big>(</big>{x<sub>0</sub>,...,x<sub>m</sub>}<big>)</big> = <big>{</big>f(x<sub>0</sub>),...,f(x<sub>m</sub>)<big>}</big>
     *  for {@link List}s, {@link Collection}s and {@link Object Object[]},
     *  and f<sup>*</sup>(x) = f(x), otherwise.
     * @see #map(BinaryFunction, Collection, Collection)
     */
    public static /*<A1, A2, B>*/ BinaryFunction listable(BinaryFunction/*<A1, A2, B>*/ f) {
	return f instanceof ListableBinaryFunction/* A1, A2, B */ ? (BinaryFunction) f : (BinaryFunction) new ListableBinaryFunction/*<A1, A2, B>*/(f);
    }
    private static class ListableBinaryFunction/*<A1, A2, B>*/ implements BinaryFunction {
	/**
	 * @serial
	 */
	protected final BinaryFunction/*<A1, A2, B>*/ function;
	public ListableBinaryFunction(BinaryFunction/*<A1, A2, B>*/ function) {
	    this.function = function;
	}
	public Functor getCompositor() {
	    return function;
	}

	public Object apply(Object x, Object y) {
	    // almost identical to @see Utility#asIterator, also @see ListableFunction
	    if ((x instanceof Collection/*_<A1>_*/) && (y instanceof Collection/*_<A2>_*/)) {
		Collection/*_<A1>_*/ c = (Collection/*_<A1>_*/) x;
		Collection/*_<A2>_*/ d = (Collection/*_<A2>_*/) y;
		Utility.pre(c.size() == d.size(), "argument collections must have same size");
		return map(function, c.iterator(), d.iterator(), Setops.newCollectionLike(c));
	    } else if ((x instanceof Iterator/*_<A1>_*/) && (y instanceof Iterator/*_<A2>_*/))
		return map(function, (Iterator/*_<A1>_*/) x, (Iterator/*_<A2>_*/) y, new LinkedList/*_<B>_*/());
	    else if ((x instanceof Object/*>A1<*/[]) && (y instanceof Object/*>A2<*/[]))
		return map(function, (Object/*>A1<*/[]) x, (Object/*>A2<*/[]) y);
	    else
		return function.apply((Object/*>A1<*/) x, (Object/*>A2<*/) y);
	} 

	public boolean equals(Object o) {
	    if (o instanceof ListableBinaryFunction)
		return getCompositor().equals(((ListableFunction) o).getCompositor());
	    else
		return false;
	}

	public int hashCode() {
	    return Utility.hashCode(function);
	}

	public String toString() {
	    return function + "";
	}
    } 

    /**
     * Maps two lists of arguments with a BinaryFunction.
     * <p>
     * map:  (X,Y) &#8614; f(X,Y) := {f(x,y) &brvbar; x=X<sub>i</sub>,y=Y<sub>i</sub>} .</p>
     * @pre X.length == Y.length
     */
    public static /*<A1, A2, B>*/ Object/*>B<*/[] map(BinaryFunction/*<A1, A2, B>*/ f, Object/*>A1<*/[] x, Object/*>A2<*/[] y) {
	Utility.pre(x.length == y.length, "argument arrays must have same length");
	//Object/*>B<*/[] a = new Object/*>B<*/[x.length]; // is not of correct sub-type
	Object/*>B<*/[] a = (Object/*>B<*/[]) Array.newInstance(x.getClass().getComponentType(), x.length);
	for (int i = 0; i < a.length; i++)
	    a[i] = f.apply(x[i], y[i]);
	return a;
    } 
    private static /*<A1, A2, B>*/ Collection/*_<B>_*/ map(BinaryFunction/*<A1, A2, B>*/ f, Iterator/*_<A1>_*/ x, Iterator/*_<A2>_*/ y, Collection/*_<B>_*/ r) {
	while (x.hasNext() && y.hasNext())
	    r.add(f.apply((/*__*/Object/*>A1<*/) x.next(), (/*__*/Object/*>A2<*/) y.next()));
	Utility.pre(!x.hasNext() && !y.hasNext(), "argument iterators must have same length");
	return r;
    } 
    public static /*<A1, A2, B>*/ Collection/*_<B>_*/ map(BinaryFunction/*<A1, A2, B>*/ f, Iterator/*_<A1>_*/ x, Iterator/*_<A2>_*/ y) {
	return map(f, x, y, new LinkedList/*_<B>_*/());
    } 
    /**
     * Maps two lists of arguments with a BinaryFunction.
     * <p>
     * map: (X,Y) &#8614; f(X,Y) := {a=f(x,y) &brvbar; x=X<sub>i</sub>,y=Y<sub>i</sub>} .</p>
     * @pre x.size() == y.size()
     * @post RES.getClass()=x.getClass() or at least compatible
     * @see #listable(BinaryFunction)
     */
    public static /*<A1, A2, B>*/ Collection/*_<B>_*/ map(BinaryFunction/*<A1, A2, B>*/ f, Collection/*_<A1>_*/ x, Collection/*_<A2>_*/ y) {
	Utility.pre(x.size() == y.size(), "argument collections must have same size");
	return map(f, x.iterator(), y.iterator(), Setops.newCollectionLike(x));
    } 

    //TODO: introduce Function thread(Function f)
    //TODO: introduce Function outer(Function f)

    // functional-style high-order functions
    // general recursion schemes

    /**
     * Folds a list with a BinaryFunction.
     * <p>
     * foldLeft:  a &#8614; f<big>(</big>f(<small>f(c,a[0])</small>,a[1]),a[2]<big>)</big>, ... .</p>
     * <p id="Theory">
     * Corresponds to a left-recursive function.
     * <center>
     *   <table class="equation">
     *     <tr><td>h &empty;</td> <td>=</td> <td>c</td> </tr>
     *     <tr><td>h <big>(</big>[first|rest]<big>)</big></td> <td>=</td> <td>f<big>(</big>h(rest), first<big>)</big></td></tr>
     *   </table>
     * </center>
     * </p>
     * <p id="Properties">
     * For commutative functions the result will equal that of foldRight.
     * </p>
     * <p>Also known as inject, accumulate.
     * Implemented as an iterative unrolling of a linear left tail-recurrence.</p>
     * @param f the function used to fold the list a with.
     * @param c the left (first) argument to start with.
     * The result of the application of f will progressively build the next left (first) argument.
     * @param a the list of arguments to be iteratively used as the right (second) arguments.
     * @todo document the equational Zusammenhang between foldLeft and foldRight perhaps in terms of ReverseList and Swap(BinaryFunction)...
     */
    public static /*<A, B>*/ Object/*>A<*/ foldLeft(BinaryFunction/*<A, B, A>*/ f, Object/*>A<*/ c, Object/*>B<*/[] a) {
	Object/*>A<*/ accumulated = c;
	for (int i = 0; i < a.length; i++)
	    accumulated = f.apply(accumulated, a[i]);
	return accumulated;
    } 
    public static /*<A, B>*/ Object/*>A<*/ foldLeft(BinaryFunction/*<A, B, A>*/ f, Object/*>A<*/ c, Iterator/*_<B>_*/ a) {
	Object/*>A<*/ accumulated = c;
	while (a.hasNext())
	    accumulated = f.apply(accumulated, (/*__*/Object/*>B<*/) a.next());
	return accumulated;
    } 
    public static /*<A, B>*/ Object/*>A<*/ foldLeft(BinaryFunction/*<A, B, A>*/ f, Object/*>A<*/ c, Collection/*_<B>_*/ a) {
	return foldLeft(f, c, a.iterator());
    } 

    /**
     * Folds a list with a BinaryFunction.
     * <p>
     * foldRight:  a &#8614; f<big>(</big>a[0], f(a[1], <small>f(a[2],c)</small>)<big>)</big>, ... .</p>
     * <p><a id="Theory"></a>
     * foldRight is the same as the catamorphism or banana of a.</p>
     * <p><a id="Properties"></a>
     * For commutative functions f the result will equal that of foldLeft.
     * </p>
     * <p>Also known as fold, reduce.
     * Implemented as an iterative unrolling of a linear right tail-recurrence.</p>
     * @param f the function used to fold the list a with.
     * @param c the right (second) argument to start with.
     * The result of the application of f will progressively build the next right (second) argument.
     * @param a the list of arguments to be iteratively used as the left (first) arguments.
     * @return <span class="bananaBracket">(|</span>c,f<span class="bananaBracket">|)</span> a.
     * @see Functionals.Catamorphism#Functionals.Catamorphism(Object, BinaryFunction)
     * @see "G. Hutton. A Tutorial on the universality and expressiveness of fold. Journal of Functional Programming 1(1), Jan. 1993."
     */
    public static /*<A, B>*/ Object/*>B<*/ foldRight(BinaryFunction/*<A, B, B>*/ f, Object/*>B<*/ c, Object/*>A<*/[] a) {
	Object/*>B<*/ result = c;
	for (int i = a.length - 1; i >= 0; i--)
	    result = f.apply(a[i], result);
	return result;
    } 
    /**
     * efficient foldRight for lists.
     */
    public static /*<A, B>*/ Object/*>B<*/ foldRight(BinaryFunction/*<A, B, B>*/ f, Object/*>B<*/ c, List/*_<A>_*/ a) {
	Object/*>B<*/ result = c;
	for (ListIterator i = a.listIterator(a.size()); i.hasPrevious(); )
	    result = f.apply((/*__*/Object/*>A<*/) i.previous(), result);
	return result;
    } 
    public static /*<A, B>*/ Object/*>B<*/ foldRight(BinaryFunction/*<A, B, B>*/ f, Object/*>B<*/ c, Collection/*_<A>_*/ a) {
	//iterative with copying: return foldRight(f, c, new LinkedList(a));
	//recursive banana
	return banana(c, f, a.iterator());
    } 
    public static /*<A, B>*/ Object/*>B<*/ foldRight(BinaryFunction/*<A, B, B>*/ f, Object/*>B<*/ c, Iterator/*_<A>_*/ a) {
	//iterative with copying: return foldRight(f, c, Setops.asList(a));
	//recursive banana
	return banana(c, f, a);
    }

    /**
     * Catamorphism recursion functional scheme (banana).
     * <p><a id="Theory"></a>
     * A catamorphism is denoted by bananas and is the same as the iterative foldRight.
     * <center>this := <span class="bananaBracket">(|</span>c,f<span class="bananaBracket">|)</span></center>
     * <i>Fusion Law</i>:<center>
     * h<big>(</big><span class="bananaBracket">(|</span>c,f<span class="bananaBracket">|)</span><big>)</big> = <big><span class="bananaBracket">(|</span></big>h(c),g<big><span class="bananaBracket">|)</span></big> &lArr;
     * h<big>(</big>f(first,rest)<big>)</big> = g<big>(</big>first, h(rest)<big>)</big>
     * </center>
     * </p>
     * <p>Also known as fold, reduce.
     * Implemented as a linear right tail-recurrence.
     * </p>
     * @see Functionals#banana(Object, BinaryFunction, Iterator)
     * @see Functionals#foldRight(BinaryFunction, Object, Iterator)
     * @see Functionals#foldRight(BinaryFunction, Object, List)
     * @see <a href="http://wwwhome.cs.utwente.nl/~fokkinga/index.html#mmf91m">Meijer, E. and Fokkinga, M.M. and Paterson, R., Functional Programming with Bananas, Lenses, Envelopes and Barbed Wire, FPCA91: Functional Programming Languages and Computer Architecture, pp. 124--144, volume 523, Lecture Notes in Computer Science, Springer-Verlag, 1991.</a>
     */
    public static /*abstract template*/ class Catamorphism/* abstract <Object c, BinaryFunction f> abstract */ /*<A, B>*/ implements Function, Serializable {

	/**
	 * the right (second) argument basevalue in B to start with.
	 * The result of the application of f will progressively build the next right (second) argument.
	 * @serial
	 */
	protected final Object/*>B<*/		   c;

	/**
	 * the function f:A&times;B=A||B&rarr;B used to fold the list a with.
	 * @serial
	 */
	protected final BinaryFunction/*<A, B, B>*/ f;

	/**
	 * Constructs a new catamorphism <span class="bananaBracket">(|</span>c,f<span class="bananaBracket">|)</span>:A<sup>*</sup>&rarr;B.
	 * @param f the function f:A&times;B=A||B&rarr;B used to fold the list a with.
	 * @param c the right (second) argument basevalue in B to start with.
	 * The result of the application of f will progressively build the next right (second) argument.
	 */
	public Catamorphism(Object/*>B<*/ c, BinaryFunction/*<A, B, B>*/ f) {
	    this.c = c;
	    this.f = f;
	}

	public boolean equals(Object o) {
	    if (!(o instanceof Catamorphism))
		return false;
	    Catamorphism b = (Catamorphism) o;
	    return (c == null ? b.c == null : c.equals(b.c)) && (f == null ? b.f == null : f.equals(b.f));
	} 

	public int hashCode() {
	    return c.hashCode() ^ f.hashCode();
	} 

	/**
	 * <span class="bananaBracket">(|</span>c,f<span class="bananaBracket">|)</span> a.
	 * <center>
	 *   <table class="equation">
	 *     <tr><td>cata &empty;</td> <td>=</td> <td>c</td> </tr>
	 *     <tr><td>cata <big>(</big>[first|rest]<big>)</big></td> <td>=</td> <td>f<big>(</big>first, cata(rest)<big>)</big></td></tr>
	 *     <tr><td colspan="3"><span class="bananaBracket">(|</span>c,f<span class="bananaBracket">|)</span> := cata</td></tr>
	 *   </table>
	 * </center>
	 * @param a a value &isin;A<sup>*</sup> represented as a {@link orbital.util.Utility#asIterator(Object) generalized iteratable},
	 *  like f.ex. {@link java.util.Iterator}.
	 * @return the value <span class="bananaBracket">(|</span>c,f<span class="bananaBracket">|)</span> a &isin; B.
	 */
	public final Object/*>B<*/ apply(Object a) {
	    return apply(Utility.asIterator(a));
	} 
	private final Object/*>B<*/ apply(Iterator/*_<A>_*/ it) {
	    if (!it.hasNext())
		return c;
	    return f.apply((/*__*/Object/*>A<*/) it.next(), apply(it));
	} 
	public String toString() {
	    return "(|" + c + ',' + f + "|)";
	} 
    }
    /**
     * banana <span class="bananaBracket">(|</span>c,f<span class="bananaBracket">|)</span> a.
     * @see Functionals.Catamorphism#Functionals.Catamorphism(Object, BinaryFunction)
     * @see #foldRight(BinaryFunction, Object, Iterator)
     */
    public static /*<A, B>*/ Object/*>B<*/ banana(Object/*>B<*/ c, BinaryFunction/*<A, B, B>*/ f, Iterator/*_<A>_*/ a) {
	return new Catamorphism/*<A, B>*/(c, f).apply(a);
    } 

    /**
     * Anamorphism recursion functional scheme (lense).
     * <p><a id="Theory"></a>
     * An anamorphism is denoted by concave lenses.
     * <center>this = <span class="lenseBracket">|(</span>g,p<span class="lenseBracket">)|</span></center>
     * </p>
     * <p>Also known as unfold.</p>
     * @version 1.0, 2000/06/14
     * @author  Andr&eacute; Platzer
     * @see Functionals#lense(Function, Predicate, Object)
     * @see <a href="http://wwwhome.cs.utwente.nl/~fokkinga/index.html#mmf91m">Meijer, E. and Fokkinga, M.M. and Paterson, R., Functional Programming with Bananas, Lenses, Envelopes and Barbed Wire, FPCA91: Functional Programming Languages and Computer Architecture, pp. 124--144, volume 523, Lecture Notes in Computer Science, Springer-Verlag, 1991.</a>
     * @todo should we introduce property get methods? But what's a good name for "g" and "p"?
     */
    public static class Anamorphism/*<A, B>*/ implements Function/*<B, List>*//*_<A>_*/, Serializable {

	/**
	 * a function g:B&rarr;A&times;B=A||B that returns objects of type {@link orbital.util.Pair}.
	 * @serial
	 */
	private final Function/*<B, Pair<A, B>>*/  g;

	/**
	 * a predicate p&sube;B.
	 * @serial
	 */
	private final Predicate/*<B>*/ p;

	/**
	 * Constructs a new anamorphism <span class="lenseBracket">|(</span>g,p<span class="lenseBracket">)|</span>:B&rarr;A<sup>*</sup>.
	 * @param g is a function g:B&rarr;A&times;B=A||B that returns objects of type {@link orbital.util.Pair}.
	 * @param p is a predicate p&sube;B.
	 * @return <span class="lenseBracket">|(</span>g,p<span class="lenseBracket">)|</span>:B&rarr;A<sup>*</sup> where the return value &isin;A<sup>*</sup> is an instance of {@link java.util.List}.
	 */
	public Anamorphism(Function/*<B, Pair<A, B>>*/ g, Predicate/*<B>*/ p) {
	    this.g = g;
	    this.p = p;
	}

	public boolean equals(Object o) {
	    if (!(o instanceof Anamorphism))
		return false;
	    Anamorphism b = (Anamorphism) o;
	    return (g == null ? b.g == null : g.equals(b.g)) && (p == null ? b.p == null : p.equals(b.p));
	} 

	public int hashCode() {
	    return g.hashCode() ^ p.hashCode();
	} 

	/**
	 * <span class="lenseBracket">|(</span>g,p<span class="lenseBracket">)|</span> b.
         * <center>
         *   <table style="border: none">
         *     <tr><td>ana b</td> <td>=</td> <td>&empty;</td> <td>&lArr; p(b)</td></tr>
         *     <tr><td rowspan="2"></td> <td>=</td> <td>[a|ana(b')]</td> <td>&lArr; � p(b)</td></tr>
         *     <tr><td></td> <td>where (a, b') = g(b)</td> <td></td></tr>
         *     <tr><td colspan="4"><span class="lenseBracket">|(</span>g,p<span class="lenseBracket">)|</span> := ana</td></tr>
         *   </table>
         * </center>
         * @param b a value &isin;B.
	 * @return the value <span class="lenseBracket">|(</span>g,p<span class="lenseBracket">)|</span> b &isin; A<sup>*</sup> represented as a {@link java.util.List List<A>}.
	 */
	public final Object/*>List<*//*_<A>_*/ apply(Object/*>B<*/ b) {
	    if (p.apply(b))
		return new LinkedList/*_<A>_*/();
	    Pair/*<A, B>*/ pair = (Pair/*<A, B>*/) g.apply(b);
	    List r = (List) apply(pair.B);
	    r.add(0, pair.A);
	    return (List) r;
	} 
	public String toString() {
	    return "[(" + g + ',' + p + ")]";
	} 
    }
    /**
     * lense <span class="lenseBracket">|(</span>g,p<span class="lenseBracket">)|</span> b.
     * @see Functionals.Anamorphism#Functionals.Anamorphism(Function, Predicate)
     */
    public static /*<A, B>*/ List/*_<A>_*/ lense(final Function/*<B, Pair<A, B>>*/ g, final Predicate/*<B>*/ p, Object/*>B<*/ b) {
	return (List/*_<A>_*/) new Anamorphism/*<A, B>*/(g, p).apply(b);
    } 

    /**
     * Hylomorphism recursion functional scheme (envelope).
     * <p><a id="Theory"></a>
     * A hylomorphism is denoted by envelopes.
     * <center>this = <span class="envelopeBracket">[[</span>(c,f),(g,p)<span class="envelopeBracket">]]</span> = <span class="envelopeBracket">|[</span>(c,f),(g,p)<span class="envelopeBracket">]|</span></center>
     * </p>
     * <p><a id="Properties"></a>
     * A hylomorphism corresponds to the composition of an anamorphism and a catamorphism.<center>
     * <span class="envelopeBracket">[[</span>(c,f),(g,p)<span class="envelopeBracket">]]</span> = <span class="bananaBracket">(|</span>c,f<span class="bananaBracket">|)</span> &#8728; <span class="lenseBracket">|(</span>g,p<span class="lenseBracket">)|</span>
     * </center>
     * So it is very much like an anamorphism except for basevalue instead of nil, and f instead of cons.</p>
     * @version 1.0, 2000/06/14
     * @author  Andr&eacute; Platzer
     * @see Functionals#envelope(Object, BinaryFunction, Function, Predicate, Object)
     * @see <a href="http://wwwhome.cs.utwente.nl/~fokkinga/index.html#mmf91m">Meijer, E. and Fokkinga, M.M. and Paterson, R., Functional Programming with Bananas, Lenses, Envelopes and Barbed Wire, FPCA91: Functional Programming Languages and Computer Architecture, pp. 124--144, volume 523, Lecture Notes in Computer Science, Springer-Verlag, 1991.</a>
     */
    //TODO: check types for abstract template public static /* abstract template*/ class Hylomorphism/* abstract <C c, T f(C,List), g, boolean p(C)> abstract */ implements Function {
    public static class Hylomorphism/*<A, B, C>*/ implements Function/*<A, C>*/, Serializable {

	/**
	 * an element &isin;C that is the basevalue for p(a) = true.
	 * @serial
	 */
	private final Object/*>C<*/		   c;

	/**
	 * a binary function f:B||C&rarr;C.
	 * @serial
	 */
	private final BinaryFunction/*<B, C, C>*/ f;

	/**
	 * a function g:A&rarr;B||A that returns objects of type {@link orbital.util.Pair Pair<B,A>}.
	 * @serial
	 */
	private final Function/*<A, Pair<B,A>>*/	   g;

	/**
	 * a predicate p&sube;A saying whether to use the basevalue case.
	 * @serial
	 */
	private final Predicate/*<A>*/	   p;

	/**
	 * Constructs a new hylomorphism <span class="envelopeBracket">[[</span>(c,f),(g,p)<span class="envelopeBracket">]]</span>:A&rarr;C.
	 * @param c is an element &isin;C that is the basevalue for p(a) = true.
	 * @param f is a binary function f:B||C&rarr;C.
	 * @param g is a function g:A&rarr;B||A that returns objects of type {@link orbital.util.Pair Pair<B,A>}.
	 * @param p is a predicate p&sube;A saying whether to use the basevalue case.
	 */
	public Hylomorphism(Object/*>C<*/ c, BinaryFunction/*<B, C, C>*/ f, Function/*<A, Pair<B,A>>*/ g, Predicate/*<A>*/ p) {
	    this.c = c;
	    this.f = f;
	    this.g = g;
	    this.p = p;
	}

	public boolean equals(Object o) {
	    if (!(o instanceof Hylomorphism))
		return false;
	    Hylomorphism b = (Hylomorphism) o;
	    return (c == null ? b.c == null : c.equals(b.c)) && (f == null ? b.f == null : f.equals(b.f)) && (g == null ? b.g == null : g.equals(b.g)) && (p == null ? b.p == null : p.equals(b.p));
	} 

	public int hashCode() {
	    return c.hashCode() ^ f.hashCode() ^ g.hashCode() ^ p.hashCode();
	} 

	/**
	 * <span class="envelopeBracket">[[</span>(c,f),(g,p)<span class="envelopeBracket">]]</span> a.
	 * <center><table border="0">
	 * <tr><td>hylo a</td> <td>=</td> <td>c</td> <td>&lArr; p(a)</td></tr>
	 * <tr><td></td> <td>=</td> <td>f<big>(</big>b, hylo(a')<big>)</big></td> <td>&lArr; &not;p(a)</td></tr>
	 * <tr><td></td> <td></td> <td>where (b, a') = g(a)</td> <td></td></tr>
	 * <tr><td colspan="4"><span class="envelopeBracket">[[</span>(c,f),(g,p)<span class="envelopeBracket">]]</span> := hylo</td></tr>
	 * </table></center>
	 * @param a value &isin;A.
	 * @return the value <span class="envelopeBracket">[[</span>(c,f),(g,p)<span class="envelopeBracket">]]</span> a &isin; C.
	 */
	public final Object/*>C<*/ apply(Object/*>A<*/ a) {
	    if (p.apply(a))
		return c;
	    Pair/*<B,A>*/ pair = (Pair/*<B,A>*/) g.apply(a);
	    return f.apply(pair.A, apply(pair.B));
	} 

	public String toString() {
	    return "|[" + '(' + c + ',' + f + "),(" + g + ',' + p + ')' + "]|";
	} 
    }

    /**
     * envelope <span class="envelopeBracket">[[</span>(c,f),(g,p)<span class="envelopeBracket">]]</span> a.
     * @see Functionals.Hylomorphism#Functionals.Hylomorphism(Object, BinaryFunction, Function, Predicate)
     */
    public static /*<A, B, C>*/ Object envelope(final Object/*>C<*/ c, final BinaryFunction/*<B, C, C>*/ f, final Function/*<A, Pair<B,A>>*/ g, final Predicate/*<A>*/ p, Object/*>A<*/ a) {
	return new Hylomorphism/*<A, B, C>*/(c, f, g, p).apply(a);
    } 

    /**
     * Paramorphism recursion functional scheme (barbed wire).
     * <p><a id="Theory"></a>
     * A paramorphism is denoted by barbed wires.
     * <center>this = <span class="barbedwireBracket">{|</span>b,f<span class="barbedwireBracket">|}</span></center>
     * </p>
     * @version 1.0, 2000/06/14
     * @author  Andr&eacute; Platzer
     * @see Functionals#barbedwire(Object, BinaryFunction, Iterator)
     * @see "Lambert Meertens. Paramorphisms. In: Formal Aspects of Computing, 1990."
     * @see <a href="http://wwwhome.cs.utwente.nl/~fokkinga/index.html#mmf91m">Meijer, E. and Fokkinga, M.M. and Paterson, R., Functional Programming with Bananas, Lenses, Envelopes and Barbed Wire, FPCA91: Functional Programming Languages and Computer Architecture, pp. 124--144, volume 523, Lecture Notes in Computer Science, Springer-Verlag, 1991.</a>
     */
    public static class Paramorphism/*<A, B>*/ implements Function, Serializable {

	/**
	 * the basevalue b&isin;B to use.
	 * @serial
	 */
	private final Object/*>B<*/		   b;

	/**
	 * a binary function f:A&times;(A*||B)&rarr;B.
	 * @serial
	 */
	private final BinaryFunction/*<A, Pair<Iterator, B>, B>*//*_<A>_*/ f;

    	/**
	 * Constructs a new paramorphism <span class="barbedwireBracket">{|</span>b,f<span class="barbedwireBracket">|}</span>:A<sup>*</sup>&rarr;B.
    	 * @param b is the basevalue b&isin;B to use.
    	 * @param f is a binary function f:A&times;(A<sup>*</sup>||B)&rarr;B.
    	 * @return <span class="barbedwireBracket">{|</span>b,f<span class="barbedwireBracket">|}</span> a
    	 */
	public Paramorphism(Object/*>B<*/ b, BinaryFunction/*<A, Pair<Iterator, B>, B>*//*_<A>_*/ f) {
	    this.b = b;
	    this.f = f;
	}

	public boolean equals(Object o) {
	    if (!(o instanceof Paramorphism))
		return false;
	    Paramorphism b = (Paramorphism) o;
	    return (b == null ? b.b == null : b.equals(b.b)) && (f == null ? b.f == null : f.equals(b.f));
	} 

	public int hashCode() {
	    return b.hashCode() ^ f.hashCode();
	} 

	/**
	 * <span class="barbedwireBracket">{|</span>b,f<span class="barbedwireBracket">|}</span> a.
    	 * <center>
    	 *   <table style="border:none">
    	 *     <tr><td>para &empty;</td> <td>=</td> <td>b</td</tr>
    	 *     <tr><td>para [a|as]</td> <td>=</td> <td>f<big>(</big>a, &lang;as, para as&rang;<big>)</big></td></tr>
    	 *     <tr><td colspan="3"><span class="barbedwireBracket">{|</span>b,f<span class="barbedwireBracket">|}</span> := barbedwire(b,f) := para</td></tr>
    	 *   </table>
    	 * </center>
    	 * </p>
    	 * @param a is a {@link orbital.util.Utility#asIterator(Object) generalized iteratable} over a list of objects.
	 * @return the value <span class="barbedwireBracket">{|</span>b,f<span class="barbedwireBracket">|}</span> a.
	 */
	public final Object/*>B<*/ apply(Object a) {
	    // almost identical to @see Utility#asIterator
	    return apply(Utility.asIterator(a));
	} 
	private final Object/*>B<*/ apply(Iterator/*_<A>_*/ ai) {
	    if (!ai.hasNext())
		return b;
    
	    Object/*>A<*/ x = (/*__*/Object/*>A<*/) ai.next();
	    ParallelIterator clone = new ParallelIterator(ai);
	    ai = clone.getParallel();
	    return f.apply(x, new Pair/*<Iterator, B>*//*_<A>_*/(clone, apply(ai)));
	} 

	public String toString() {
	    return "{|" + b + ',' + f + "|}";
	} 
    }
    /**
     * barbedwire <span class="barbedwireBracket">{|</span>b,f<span class="barbedwireBracket">|}</span> a.
     * @see Functionals.Paramorphism#Functionals.Paramorphism(Object, BinaryFunction)
     */
    public static /*<A, B>*/ Object/*>B<*/ barbedwire(final Object/*>B<*/ b, final BinaryFunction/*<A, Pair<Iterator, B>, B>*//*_<A>_*/ f, Iterator/*_<A>_*/ a) {
	return new Paramorphism/*<A, B>*/(b, f).apply(a);
    } 
    /**
     * Paramorphism recursion functional operator (barbed wire).
     * <p><a id="Theory"></a>
     * A paramorphism is denoted by barbed wires.
     * For numbers a paramorphism is
     * <center><table border="0">
     * <tr><td>para 0</td> <td>=</td> <td>b</td</tr>
     * <tr><td>para (n+1)</td> <td>=</td> <td>f<big>(</big>n, para n<big>)</big></td></tr>
     * </table></center>
     * If b and f are functionals (or &lambda;-expressions), <span class="barbedwireBracket">{|</span>b,f<span class="barbedwireBracket">|}</span>
     * directly corresponds to the primitive recursion.
     * </p>
     * 
     * @param b is the basevalue to use.
     * @param f is a binary function.
     * @param a is a starting value.
     * @return <span class="barbedwireBracket">{|</span>b,f<span class="barbedwireBracket">|}</span> a
     * @see "Lambert Meertens. Paramorphisms. In: Formal Aspects of Computing, 1990."
     */
    //TODO: think about changing from java.lang.Integer to java.lang.Number to support java.lang.Double and orbital.math.Integer as well
    /*public*/ protected static Function/*<Integer, Integer>*/ paramorphism(final int b, final BinaryFunction/*<Integer, Integer, Integer>*/ f) {
	return new Function/*<Integer, Integer>*/() {
		public Object/*>Integer<*/ apply(Object/*>Integer<*/ ao) {
		    int a = ((Integer) ao).intValue();
		    if (a == 0)
			return new Integer(b);
		    return f.apply(new Integer(a - 1), apply(new Integer(a - 1)));
		} 
		public String toString() {
		    return "{|" + b + "," + f + "|}";
		} 
	    };
    } 
    public static int barbedwire(final int b, final BinaryFunction/*<Integer, Integer, Integer>*/ f, int a) {
	return ((Number) paramorphism(b, f).apply(new Integer(a))).intValue();
    } 


    /**
     * Generalized inner product.
     */

    //TODO: implement inner, outer

    /*
      public Object inner(BinaryFunction inner, Object[] A, Object[] B, BinaryFunction outer) {
      Utility.pre(A.length==B.length, "inner product assumes equal argument array lengths");
      Object result = x;
      for (int i=0; i<A.length; i++)
      result = outer.apply( result, inner.apply(A[i], B[i]) );
      return result;
      }
    */

    /**
     * Nests a function n times within itself.
     * <p>
     * nest:  (f,n) &#8614; f<sup>n</sup> = &#8728;<sub>i=1,...,n</sub> f = f &#8728; f &#8728; ... &#8728; f (n times).<br>
     * nest(f,n).apply(A) gives an expression with f applied n times to A.</p>
     * @param f the function to be nested.
     * @param n the number of times the f should be composed.
     * @return f<sup>n</sup>
     * @pre n>=0
     */
    public static /*<A>*/ Function/*<A, A>*/ nest(Function/*<A, A>*/ f, int n) {
	Utility.pre(n >= 0, "non negative nesting expected");
	if (n == 0)	   // only compose with id if necessary
	    throw new UnsupportedOperationException("0 nesting not implemented. would be orbital.math.functional.Functions.id");
	Function/*<A, A>*/ r = f;
	for (int i = 1; i < n; i++)
	    r = compose(f, r);
	return r;
    } 


    // iterative algorithms

    /**
     * fixedPoint starts with an object, then applies f repeatedly until the result no longer changes.
     * <p>
     * Beginning with x, it will find an x<sup>*</sup> such that f(x<sup>*</sup>) = x<sup>*</sup>, if
     * f is continuous.
     * Then x<sup>*</sup> is a fixed point of f.</p>
     * <p>
     * The fix point iteration will converge if f is a contraction, i.e.<blockquote>
     * &exist; q&isin;[0,1) with ||f(x)-f(y)|| &le; q*||x-y|| &forall;x,y
     * </blockquote>
     * If q = sup ||f'(z)|| &lt; 1, then f is a contraction with q.
     * </p>
     * @para f the (continuous) functon to apply repeatedly for searching a fixed point.
     * @para x where to start searching a fixed point.
     * @para maxIteration the maximum number of iterations waiting for a convergence to a fixed point.
     * Will then throw an IterationLimitException
     * @throws IterationLimitException when the maximum number of iterations maxIteration is overrun.
     */
    public static /*<A>*/ Object/*>A<*/ fixedPoint(final Function/*<A, A>*/ f, Object/*>A<*/ x, int maxIteration) throws IterationLimitException {
	Object/*>A<*/ o_x = x;	   // contains last x value for convergence analysis

	for (int i = 0; i < maxIteration; i++) {
	    // idiom for updating x and remembering last x value
	    x = f.apply(o_x = x);
	    if (x.equals(o_x))
		return x;
	} 
	throw new IterationLimitException("limit of fixed point iterations (" + maxIteration + ") reached");
    } 
    public static /*<A>*/ Object/*>A<*/ fixedPoint(final Function/*<A, A>*/ f, Object/*>A<*/ x) throws IterationLimitException {
	try {
	    return fixedPoint(f, x, IterationLimitException.MaxIterations);
	}
	catch (IterationLimitException ex) {throw new IterationLimitException(ex.getMessage() + " with IterationLimitException.MaxIterations=" + IterationLimitException.MaxIterations);}
    } 
}

/**
 * Allows an iterator to be traversed twice (in parallel), virtually cloning it.
 * Either one of this parallel iterator and the one got from getParallel() will return the
 * elements kept in the backing iterator.
 * Which will be queried by the first parallel iterator using it.
 * Not thread-safe.
 */
class ParallelIterator extends orbital.util.QueuedIterator {
    /**
     * @serial
     */
    protected final Iterator backing;
    /**
     * @serial
     */
    protected final ParallelIterator parallel;
    public ParallelIterator(Iterator backing) {
	this.backing = backing;
	this.parallel = new ParallelIterator(backing, this);
    }
    private ParallelIterator(Iterator backing, ParallelIterator parallel) {
	this.backing = backing;
	this.parallel = parallel;
    }

    /**
     * Get the parallel iterator of this.
     * Both iterators can be used in parallel (but they must be synchronized) and will return
     * the backing iterations elements.
     */
    public Iterator getParallel() {
	return parallel;
    }

    public boolean hasNext() {
	return super.hasNext() || backing.hasNext();
    }

    /**
     * get queued first, then get from backing iterator.
     */
    public Object next() {
	try {
	    return super.next();
	} catch (NoSuchElementException x) {
	    Object o = backing.next();
	    parallel.add(o);
	    return o;
	} 
    }

    public void remove() {
	throw new UnsupportedOperationException();
    }
}
