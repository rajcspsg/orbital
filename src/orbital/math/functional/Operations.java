/**
 * @(#)Operations.java 1.0 2000/08/03 Andre Platzer
 * 
 * Copyright (c) 2000 Andre Platzer. All Rights Reserved.
 */

package orbital.math.functional;

import orbital.math.Arithmetic;

import java.util.Iterator;
import java.util.Collection;

import java.lang.reflect.Method;

import orbital.math.Real;

import orbital.math.MathUtilities;
import java.lang.reflect.InvocationTargetException;
import orbital.math.Values;
import orbital.math.Vector;
import orbital.math.Matrix;

import java.util.Arrays;
import orbital.util.Utility;

// due to JDK1.3 bug  Bug-ID: 4306909
// this class can only be compiled with JDK1.4, JDK1.3.1 or JDK 1.2.2, but not with JDK 1.3.0 or JDK 1.3.0_02
// due to JDK1.3 bug  Bug-ID: 4401422
// this class can only be javadoced with JDK1.4, but not with JDK 1.3.0 or JDK 1.3.0_02 or JDK1.3.1

/**
 * Provides central arithmetic operations for algebraic types.
 * <p>
 * Operations contains BinaryFunction abstractions of
 * mathematical operations like <code>+ - &sdot; / ^</code> etc.
 * For Arithmetic objects, the corresponding elemental function in orbital.math.Arithmetic is called,
 * for functions the operations are defined pointwise.
 * So these Operations can be applied to arithmetic objects as well as functions in the same manner!
 * All function objects in this class provide canonical equality:
 * <center>a.equals(b) if and only if a <span class="operator">==</span> b</center>
 * </p>
 * <p>
 * Operation functions are very useful to implement sly arithmetic operations with full dynamic dispatch.
 * They are then performed with the correct type concerning all argument types, automatically.
 * Simply use an idiom like
 * <pre>
 * <span class="keyword">public</span> <span class="Orbital">Arithmetic</span> add(<span class="Orbital">Arithmetic</span> b) {
 *     <span class="comment">// base case: "add" for two instances of <var>ThisClass</var></span>
 *     <span class="keyword">if</span> (b <span class="keyword">instanceof</span> <var>ThisClass</var>)
 *         <span class="keyword">return</span> <span class="keyword">new</span> <var>ThisClass</var>(value() <span class="operator">+</span> ((<var>ThisClass</var>) b).value());
 *     <span class="comment">// dynamic dispatch with regard to dynamic types of all arguments</span>
 *     <span class="comment">// for sly defer to "add" of most restrictive type</span>
 *     <span class="keyword">return</span> (<span class="Orbital">Arithmetic</span>) <span class="Orbital">Operations</span>.plus.apply(<span class="keyword">this</span>, b);
 * }
 * </pre>
 * Which implicitly uses the tranformation function {@link orbital.math.MathUtilities#getEqualizer()}.
 * The static functions provided in <tt>Operations</tt> delegate type handling like in
 * <pre>
 *     <span class="Orbital">Arithmetic</span> operands[] <span class="operator">=</span> (<span class="Orbital">Arithmetic</span>[]) <span class="Orbital">MathUtilities</span>.getEqualizer().apply(<span class="keyword">new</span> <span class="Orbital">Arithmetic</span>[] {x, y});
 *     <span class="keyword">return</span> operands[<span class="Number">0</span>].add(operands[<span class="Number">1</span>]);
 * </pre>
 * </p>
 * 
 * @structure depends {@link orbital.math.MathUtilities#getEqualizer()}
 * @version 1.0, 2000/08/03
 * @author  Andr&eacute; Platzer
 * @see orbital.math.Arithmetic
 * @see java.lang.Comparable
 * @see orbital.logic.functor.Functor
 * @see orbital.math.functional.Functionals#compose(Function, Function)
 * @see orbital.math.functional.Functionals#genericCompose(BinaryFunction, Object, Object)
 * @see orbital.logic.functor.Functionals.Catamorphism
 */
public interface Operations /* implements ArithmeticOperations */ {
    /**
     * Class alias object.
     * <p>
     * To alias the methods in this class for longer terms, use an idiom like<pre>
     * <span class="comment">// alias object</span>
     * <span class="Orbital">Functions</span> F = <span class="Orbital">Functions</span>.functions;
     * <span class="Orbital">Operations</span> op = <span class="Orbital">Operations</span>.operations;
     * <span class="comment">// use alias</span>
     * <span class="Orbital">Function</span> f = (<span class="Orbital">Function</span>) op.times.apply(F.sin, op.plus.apply(F.square, F.cos));
     * <span class="comment">// instead of the long form</span>
     * <span class="Orbital">Function</span> f = (<span class="Orbital">Function</span>) <span class="Orbital">Operations</span>.times.apply(<span class="Orbital">Functions</span>.sin, <span class="Orbital">Operations</span>.plus.apply(<span class="Orbital">Functions</span>.square, <span class="Orbital">Functions</span>.cos));
     * </pre>
     * </p>
     */
    public static final Operations operations = new Operations() {};

    // the functions below all follow the same recursion scheme but its too reflectious to stamp into a generic delegate

    // junctors of a general group (A,+)

    /**
     * plus +: A&times;A&rarr;A; (x,y) &#8614; x + y.
     * <p>
     * derive plus' = (1, 1)<br />
     * integrate: &int;x<sub>0</sub>+x<sub>1</sub> <i>d</i>x<sub>i</sub> = x<span class="doubleIndex"><sub>i</sub><sup>2</sup></span>/2 + x<sub>0</sub>*x<sub>1</sub></p>
     * @attribute associative
     * @attribute neutral
     * @attribute inverses
     * @attribute commutative
     * @see Arithmetic#add(Arithmetic)
     */
    public static final BinaryFunction plus = new AbstractBinaryFunction/*<Arithmetic,Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ x, Object/*>Arithmetic<*/ y) {
		Arithmetic operands[] = (Arithmetic[]) MathUtilities.getEqualizer().apply(new Arithmetic[] {
		    (Arithmetic) x, (Arithmetic) y
		});
		return operands[0].add(operands[1]);
	    } 
	    public BinaryFunction derive() {
		return (BinaryFunction) Functionals.genericCompose(new BinaryFunction[][] {
		    {Functions.binaryone, Functions.binaryone}
		});
	    } 
	    public BinaryFunction integrate(int i) {
		Utility.pre(0 <= i && i <= 1, "binary integral");
		return (BinaryFunction) plus.apply( times.apply(Functions.projectFirst, Functions.projectSecond), divide.apply(Functionals.on(i, Functions.square), Values.valueOf(2)));
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "+";
	    } 
	};

    /**
     * sum &sum;: A<sup>n</sup>&rarr;A; (x<sub>i</sub>) &#8614; &sum;<sub>i</sub> x<sub>i</sub> = <span class="bananaBracket">(|</span>0,+<span class="bananaBracket">|)</span> (x<sub>i</sub>).
     * <p>
     * derive sum' = (1)<sub>n&isin;<b>N</b></sub><br />
     * integrate: ?</p>
     * @see orbital.logic.functor.Functionals.Catamorphism
     * @see Values#ZERO
     * @see #plus
     * @todo
     * @todo AbstractFunction<Vector<Arithmetic>,Arithmetic>?
     * @todo AbstractFunction<Matrix<Arithmetic>,Arithmetic>?
     */
    public static final Function sum = new AbstractFunction/*<Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ a) {
		return Functionals.foldLeft(plus, Values.ZERO, Utility.asIterator(a));
	    }
	    public Function derive() {
		throw new ArithmeticException(this + " is only partially derivable");
	    } 
	    public Function integrate() {
		throw new ArithmeticException(this + " is only integrable with respect to a single variable");
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "\u2211";
	    } 
	};

    /**
     * minus &minus;: A&rarr;A; x &#8614; &minus;x.
     * <p>
     * derive minus' = &minus;1<br />
     * integrate: &int;&minus;x <i>d</i>x = &minus;x<sup>2</sup>/2</p>
     * @see Arithmetic#minus()
     */
    public static final Function minus = new AbstractFunction/*<Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ x) {
		return ((Arithmetic) x).minus();
	    } 
	    public Function derive() {
		return Functions.constant(Values.valueOf(-1));
	    } 
	    public Function integrate() {
		// return (Function) minus.apply(Functions.id.integrate());
		return (Function) minus.apply( Operations.divide.apply(Functions.square, Values.valueOf(2)) );
	    }
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "-";
	    } 
	};

    /**
     * subtract -: A&times;A&rarr;A; (x,y) &#8614; x - y.
     * <p>
     * derive subtract' = (1, -1)<br />
     * integrate: &int;x<sub>0</sub>-x<sub>1</sub> <i>d</i>x<sub>0</sub> = x<span class="doubleIndex"><sub>0</sub><sup>2</sup></span>/2 - x<sub>0</sub>*x<sub>1</sub><br />
     * integrate: &int;x<sub>0</sub>-x<sub>1</sub> <i>d</i>x<sub>1</sub> = x<sub>0</sub>*x<sub>1</sub> - x<span class="doubleIndex"><sub>1</sub><sup>2</sup></span>/2</p>
     * @see Arithmetic#subtract(Arithmetic)
     */
    public static final BinaryFunction subtract = new AbstractBinaryFunction/*<Arithmetic,Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ x, Object/*>Arithmetic<*/ y) {
		Arithmetic operands[] = (Arithmetic[]) MathUtilities.getEqualizer().apply(new Arithmetic[] {
		    (Arithmetic) x, (Arithmetic) y
		});
		return operands[0].subtract(operands[1]);
	    } 
	    public BinaryFunction derive() {
		return (BinaryFunction) Functionals.genericCompose(new BinaryFunction[][] {
		    {Functions.binaryConstant(Values.valueOf(1)), Functions.binaryConstant(Values.valueOf(-1))}
		});
	    } 
	    public BinaryFunction integrate(int i) {
		Utility.pre(0 <= i && i <= 1, "binary integral");
		return i == 0
		    ? (BinaryFunction) subtract.apply( divide.apply(Functionals.onFirst(Functions.square), Values.valueOf(2)), times.apply(Functions.projectFirst, Functions.projectSecond))
		    : (BinaryFunction) subtract.apply( times.apply(Functions.projectFirst, Functions.projectSecond), divide.apply(Functionals.onSecond(Functions.square), Values.valueOf(2)));
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "-";
	    } 
	};

    // junctors of a general group (A,&sdot;)

    /**
     * times &middot;: A&times;A&rarr;A; (x,y) &#8614; x &middot; y = x &sdot; y.
     * <p>
     * derive times' = (y, x)<br />
     * integrate: &int;x<sub>0</sub>&middot;x<sub>1</sub> <i>d</i>x<sub>0</sub> = x<span class="doubleIndex"><sub>0</sub><sup>2</sup></span>&middot;x<sub>1</sub> / 2<br />
     * integrate: &int;x<sub>0</sub>&middot;x<sub>1</sub> <i>d</i>x<sub>1</sub> = x<sub>0</sub>&middot;x<span class="doubleIndex"><sub>1</sub><sup>2</sup></span> / 2</p>
     * @attribute associative
     * @attribute neutral
     * @attribute commutative
     * @attribute distributive #plus
     * @see Arithmetic#multiply(Arithmetic)
     * @xxx but what about scale?
     * @see Arithmetic#scale(Arithmetic)
     */
    public static final BinaryFunction times = new AbstractBinaryFunction/*<Arithmetic,Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ x, Object/*>Arithmetic<*/ y) {
		Arithmetic operands[] = (Arithmetic[]) MathUtilities.getEqualizer().apply(new Arithmetic[] {
		    (Arithmetic) x, (Arithmetic) y
		});
		return operands[0].multiply(operands[1]);
	    } 
	    public BinaryFunction derive() {
		return (BinaryFunction) Functionals.genericCompose(new BinaryFunction[][] {
		    {Functions.projectSecond, Functions.projectFirst}
		});
	    } 
	    public BinaryFunction integrate(int i) {
		Utility.pre(0 <= i && i <= 1, "binary integral");
		return i == 0
		    ? (BinaryFunction) divide.apply( times.apply(Functionals.onFirst(Functions.square), Functions.projectSecond), Values.valueOf(2))
		    : (BinaryFunction) divide.apply( times.apply(Functions.projectFirst, Functionals.onSecond(Functions.square)), Values.valueOf(2));
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "*";
	    } 
	};

    /**
     * product &prod;: A<sup>n</sup>&rarr;A; (x<sub>i</sub>) &#8614; &prod;<sub>i</sub> x<sub>i</sub> = <span class="bananaBracket">(|</span>1,&sdot;<span class="bananaBracket">|)</span> (x<sub>i</sub>).
     * <p>
     * derive product' = ?<br />
     * integrate: ?</p>
     * @see orbital.logic.functor.Functionals.Catamorphism
     * @see Values#ONE
     * @see #times
     * @todo
     */
    public static final Function product = new AbstractFunction/*<Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ a) {
		return Functionals.foldLeft(times, Values.ONE, Utility.asIterator(a));
	    }
	    public Function derive() {
		throw new ArithmeticException(this + " is only partially derivable");
	    } 
	    public Function integrate() {
		throw new ArithmeticException(this + " is only integrable with respect to a single variable");
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "\u220f";
	    } 
	};

    /**
     * inverse <sup>-1</sup>: A&rarr;A; x &#8614; x<sup>-1</sup>.
     * <p>
     * derive inverse' = -1/x<sup>2</sup><br />
     * integrate: &int;x<sup>-1</sup> <i>d</i>x<sub>1</sub> = &#13266; x</p>
     * @see Arithmetic#inverse()
     */
    public static final Function inverse = new AbstractFunction/*<Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ x) {
		return ((Arithmetic) x).inverse();
	    } 
	    public Function derive() {
		return Functionals.compose(minus, Functions.pow(Values.valueOf(-2)));
	    } 
	    public Function integrate() {
		return Functions.log;
	    }
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "^-1";
	    } 
	};

    /**
     * divide &#8725;: A&times;A&rarr;A; (x,y) &#8614; x &#8725; y.
     * <p>
     * derive divide' = (1&#8725;y, -x&#8725;y<sup>2</sup>)<br />
     * integrate: &int;x<sub>0</sub>&#8725;x<sub>1</sub> <i>d</i>x<sub>0</sub> = x<span class="doubleIndex"><sub>0</sub><sup>2</sup></span>&#8725;(2*x<sub>1</sub>)<br />
     * integrate: &int;x<sub>0</sub>&#8725;x<sub>1</sub> <i>d</i>x<sub>1</sub> = x<sub>0</sub>&middot;&#13266;(x<sub>1</sub>)</p>
     * @see Arithmetic#divide(Arithmetic)
     */
    public static final BinaryFunction divide = new AbstractBinaryFunction/*<Arithmetic,Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ x, Object/*>Arithmetic<*/ y) {
		Arithmetic operands[] = (Arithmetic[]) MathUtilities.getEqualizer().apply(new Arithmetic[] {
		    (Arithmetic) x, (Arithmetic) y
		});
		return operands[0].divide(operands[1]);
	    } 
	    public BinaryFunction derive() {
		return (BinaryFunction) Functionals.genericCompose(new BinaryFunction[][] {
		    {Functionals.onSecond(Functions.reciprocal),
		     Functionals.compose(divide, Functionals.onFirst(minus), Functionals.onSecond(Functions.square))}
		});
	    } 
	    public BinaryFunction integrate(int i) {
		Utility.pre(0 <= i && i <= 1, "binary integral");
		return i == 0
		    ? (BinaryFunction) divide.apply(divide.apply(Functionals.onFirst(Functions.square), Functions.projectSecond), Values.valueOf(2))
		    : (BinaryFunction) times.apply(Functionals.onSecond(Functions.log), Functions.projectFirst);
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "/";
	    } 
	};

    // extended junctors

    /**
     * power ^: A&times;A&rarr;A; (x,y) &#8614; x<sup>y</sup>.
     * <p>
     * power' = (y&sdot;x<sup>y-1</sup>, &#13266;(x)&sdot;x<sup>y</sup>)<br />
     * integrate: &int;x<sub>0</sub><sup>x<sub>1</sub></sup> <i>d</i>x<sub>0</sub> = x<sub>0</sub><sup>x<sub>1</sub>+1</sup> / (x<sub>1</sub>+1)<br />
     * integrate: &int;x<sub>0</sub><sup>x<sub>1</sub></sup> <i>d</i>x<sub>1</sub> = x<sub>0</sub><sup>x<sub>1</sub></sup> / &#13266;(x<sub>0</sub>)</p>
     * @see Arithmetic#power(Arithmetic)
     */
    public static final BinaryFunction power = new AbstractBinaryFunction/*<Arithmetic,Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ x, Object/*>Arithmetic<*/ y) {
		Arithmetic operands[] = (Arithmetic[]) MathUtilities.getEqualizer().apply(new Arithmetic[] {
		    (Arithmetic) x, (Arithmetic) y
		});
		return operands[0].power(operands[1]);
	    } 
	    public BinaryFunction derive() {
		return (BinaryFunction) Functionals.genericCompose(new BinaryFunction[][] {
		    {Functionals.compose(times, Functions.projectSecond, Functionals.compose(power, Functions.projectFirst, Functionals.compose(subtract, Functions.projectSecond, Functions.binaryone))), Functionals.compose(times, Functionals.onFirst(Functions.log), power)}
		});
	    } 
	    public BinaryFunction integrate(int i) {
		Utility.pre(0 <= i && i <= 1, "binary integral");
		return i == 0
		    ? (BinaryFunction) divide.apply(power.apply(Functions.projectFirst, plus.apply(Functions.projectSecond, Values.valueOf(1))), plus.apply(Functions.projectSecond, Values.valueOf(1)))
		    : (BinaryFunction) divide.apply(power, Functionals.onFirst(Functions.log));
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "^";
	    } 
	};

    // order operations

    /**
     * min: A&times;A&rarr;A; (x,y) &#8614; min {x,y} = &#8851;{x,y}.
     * @attribute associative
     * @attribute commutative
     * @attribute idempotent
     * @attribute distributive #max
     * @see java.lang.Comparable#compareTo(Object)
     * @todo AbstractBinaryFunction<Comparable,Comparable,Comparable> would be enough
     */
    public static final BinaryFunction min = new AbstractBinaryFunction/*<Arithmetic,Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ x, Object/*>Arithmetic<*/ y) {
		if (x instanceof Comparable && y instanceof Comparable)
		    return ((Comparable) x).compareTo(y) <= 0 ? x : y;
		return Functionals.genericCompose(min, x, y);
	    } 
	    public BinaryFunction derive() {
		throw new UnsupportedOperationException(this + "'");
	    } 
	    public BinaryFunction integrate(int i) {
		Utility.pre(0 <= i && i <= 1, "binary integral");
		throw new UnsupportedOperationException("integrate " + this);
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "min";
	    } 
	};

    /**
     * inf &#8851;: A<sup>n</sup>&rarr;A; (x<sub>i</sub>) &#8614; &#8851;<sub>i</sub> {x<sub>i</sub>} = <span class="bananaBracket">(|</span>&infin;,min<span class="bananaBracket">|)</span> (x<sub>i</sub>).
     * <p>
     * <dl class="def">
     *   Let (A,&le;) be an ordered set.
     *   <dt>lower bound</dt>
     *   <dd>b&isin;A is a lower bound of M&sube;A :&hArr; &forall;x&isin;M b&le;x</dd>
     *   <dt>infimum</dt>
     *   <dd>inf M = s&isin;A is the infimum of M&sube;A :&hArr; s is a lower bound of M &and; &forall;b&isin;A (b lower bound of M &rArr; b&le;s)</dd>
     * </dl>
     * </p>
     * @see orbital.logic.functor.Functionals.Catamorphism
     * @see #min
     * @todo
     */
    public static final Function inf = new AbstractFunction/*<Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ a) {
		return Functionals.foldLeft(min, Values.POSITIVE_INFINITY, Utility.asIterator(a));
	    }
	    public Function derive() {
		throw new UnsupportedOperationException(this + "'");
	    } 
	    public Function integrate() {
		throw new UnsupportedOperationException("integrate " + this);
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "\u2293";
	    } 
	};

    /**
     * max: A&times;A&rarr;A; (x,y) &#8614; max {x,y} = &#8852;{x,y}.
     * @attribute associative
     * @attribute commutative
     * @attribute idempotent
     * @attribute distributive #min
     * @see java.lang.Comparable#compareTo(Object)
     */
    public static final BinaryFunction max = new AbstractBinaryFunction/*<Arithmetic,Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ x, Object/*>Arithmetic<*/ y) {
		if (x instanceof Comparable && y instanceof Comparable)
		    return ((Comparable) x).compareTo(y) >= 0 ? x : y;
		return Functionals.genericCompose(max, x, y);
	    } 
	    public BinaryFunction derive() {
		throw new UnsupportedOperationException(this + "'");
	    } 
	    public BinaryFunction integrate(int i) {
		Utility.pre(0 <= i && i <= 1, "binary integral");
		throw new UnsupportedOperationException("integrate " + this);
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "max";
	    } 
	};

    /**
     * sup &#8852;: A<sup>n</sup>&rarr;A; (x<sub>i</sub>) &#8614; &#8852;<sub>i</sub> {x<sub>i</sub>} = <span class="bananaBracket">(|</span>-&infin;,max<span class="bananaBracket">|)</span> (x<sub>i</sub>).
     * <p>
     * <dl class="def">
     *   Let (A,&le;) be an ordered set.
     *   <dt>upper bound</dt>
     *   <dd>b&isin;A is an upper bound of M&sube;A :&hArr; &forall;x&isin;M x&le;b</dd>
     *   <dt>supremum</dt>
     *   <dd>sup M = s&isin;A is the supremum of M&sube;A :&hArr; s is an upper bound of M &and; &forall;b&isin;A (b upper bound of M &rArr; s&le;b)</dd>
     * </dl>
     * </p>
     * @see orbital.logic.functor.Functionals.Catamorphism
     * @see #max
     * @todo
     */
    public static final Function sup = new AbstractFunction/*<Arithmetic,Arithmetic>*/() {
	    public Object/*>Arithmetic<*/ apply(Object/*>Arithmetic<*/ a) {
		return Functionals.foldLeft(max, Values.NEGATIVE_INFINITY, Utility.asIterator(a));
	    }
	    public Function derive() {
		throw new UnsupportedOperationException(this + "'");
	    } 
	    public Function integrate() {
		throw new UnsupportedOperationException("integrate " + this);
	    } 
	    public Real norm() {
		return Values.POSITIVE_INFINITY;
	    }
	    public String toString() {
		return "\u2294";
	    } 
	};
}


/**
 * A BinaryFunction that performs an operation elementwise.
 * <p>
 * For Arithmetic objects this will be the elemental method for x applied on y,
 * so to say <code>x.<i>elemental</i>(y)</code>.
 * For functor objects this will be a composition of this elementwise operation
 * with x and y.</p>
 * @see BinaryFunction.PointwiseFunction
 */

/* private static inner class is not allowed for interfaces */
abstract class PointwiseMethodFunction implements BinaryFunction {
    private final Method elemental;

    /**
     * Constructs a new elementwise function from an elemental method.
     * @param elemental the elemental method to perform on Arithmetic x (as "this") and y (as argument).
     * So an Arithmetic x must support <code>x.<i>elemental</i>(y)</code> for this
     * elementwise BinaryFunction to work properly.
     */
    protected PointwiseMethodFunction(Method elemental) {
	this.elemental = elemental;
    }

    /**
     * Performs this operation elementwise on x and y.
     * For Arithmetic objects this will be the elemental method for x applied on y,
     * so to say <code>x.<i>elemental</i>(y)</code>.
     * For functor objects this will be a composition of this elementwise operation
     * with x and y.
     * @param x first argument to this elementwise operation which must be Arithmetic or a Functor.
     * Additionally, it must support the elemental method call.
     * @param y second argument to this elementwise operation which must be Arithmetic or a Functor.
     * @see #elemental
     * @see Functionals#genericCompose(BinaryFunction, Object, Object)
     */
    public Object apply(Object x, Object y) {
	if ((x instanceof Arithmetic && !(x instanceof MathFunctor)) && (y instanceof Arithmetic && !(y instanceof MathFunctor)))
	    try {
		return elemental.invoke(x, new Object[] {y});
	    } catch (IllegalAccessException err) {
		throw new IllegalArgumentException("argument does not support invocation because of " + err);
	    } catch (InvocationTargetException err) {
		throw new IllegalArgumentException("argument does not support invocation because of " + err + ": " + err.getTargetException());
	    } 
	return Functionals.genericCompose(this, x, y);
    } 
}