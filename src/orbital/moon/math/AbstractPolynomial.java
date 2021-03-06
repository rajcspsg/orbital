/**
 * @(#)AbstractPolynomial.java 1.1 2002/08/21 Andre Platzer
 *
 * Copyright (c) 2002 Andre Platzer. All Rights Reserved.
 */

package orbital.moon.math;
import orbital.math.*;
import orbital.math.Integer;

import orbital.math.functional.Function;
import java.util.ListIterator;
import java.util.Iterator;

import orbital.math.functional.Functionals;
import orbital.logic.functor.BinaryFunction;
import orbital.logic.functor.Predicate;
import orbital.math.functional.Operations;
import orbital.util.KeyValuePair;
import orbital.util.Setops;
import orbital.util.Pair;

/**
 * @version $Id$
 * @author  Andr&eacute; Platzer
 * @todo (working on it) this implementation could be split in two.
 *  One part that is general for every S,
 *  and one part that specially assumes S=<b>N</b><sup>n</sup>.
 */
abstract class AbstractPolynomial/*<R extends Arithmetic, S extends Arithmetic>*/
    extends AbstractProductArithmetic/*<R,S,Polynomial<R,S>>*/
    implements Polynomial/*<R,S>*/ {
    private static final long serialVersionUID = 4336092442446250306L;
        
    protected AbstractPolynomial(ValueFactory valueFactory) {
        super(valueFactory);
    }
  
    public boolean equals(Object o) {
        if (o instanceof Polynomial) {
            final Polynomial/*>T<*/ b = (Polynomial) o;
            if (rank() != b.rank())
                return false;
            boolean eq =  Setops.all(combinedIndices(this, b),
                       new Predicate() {
                           public boolean apply(Object o) {
                               Arithmetic/*>S<*/ i = (Arithmetic)o;
                               return get(i).equals(b.get(i));
                           }
                       });
            boolean eq2 = false;
            assert (eq2 = validateEquals(this, b)) || true;
            assert eq == eq2 : "fast and full equality yield same result: " +  eq + " and " + eq2 + " for " + this + " and " + b;
            return eq;
        } 
        return false;
    } 

    static final boolean validateEquals(Polynomial a, Polynomial b) {
        boolean eq1 = true;
                for (Iterator i = a.monomials(); i.hasNext(); ) {
                        KeyValuePair e = (KeyValuePair) i.next();
                        if (!e.getValue().equals(b.get((Arithmetic) e.getKey())))
                                eq1 = false;
                }
        boolean eq2 = true;
                for (Iterator i = b.monomials(); i.hasNext(); ) {
                        KeyValuePair e = (KeyValuePair) i.next();
                        if (!e.getValue().equals(a.get((Arithmetic) e.getKey())))
                                eq2 = false;
                }
                assert a != b || eq1 && eq2 : "identity implies equality"; 
                return eq1 && eq2;
        }

        public int hashCode() {
        //@xxx throw new UnsupportedOperationException("would require dimensions() to reduce to non-zero part");
        // the following is ok (though, perhaps, not very surjective), since 0 has hashCode 0 anyway
        int hash = 0;
        //@todo functional?
        for (java.util.Iterator i = iterator(this); i.hasNext(); ) {
            Object e = i.next();
            hash += e == null ? 0 : e.hashCode();
        } 
        return hash;
    }

    public boolean isZero() {
        Integer deg = degree();
        assert (deg.compareTo(deg.zero()) < 0) == equals(zero()) : "polynomial is zero iff its degree is negative: " + this;
        return deg.compareTo(deg.zero()) < 0;
    }

    public Integer degree() {
        return valueFactory().valueOf(degreeValue());
    }

    /**
     * Sets a value for the coefficient specified by index.
     * @preconditions i&isin;S
     * @throws UnsupportedOperationException if this polynomial is constant and does not allow modifications.
     * @todo move to Polynomial?
     */
    protected abstract void set(Arithmetic/*>S<*/ i, Arithmetic/*>R<*/ vi);

    protected Object/*>S<*/ productIndexSet(Arithmetic/*>Polynomial<R,S><*/ productObject) {
        return indexSet();
    }

    protected ListIterator/*<R>*/ iterator(Arithmetic/*>Polynomial<R,S><*/ productObject) {
        return ((Polynomial)productObject).iterator();
    }

    /**
     * Get a tensor view of the coefficients.
     * @xxx somehow get rid of this trick
     */
    abstract Tensor tensorViewOfCoefficients();

    
    /**
     * Evaluate this polynomial at <var>a</var>.
     * Using the "Einsetzungshomomorphismus".
     * @return f(a) = f(X)|<sub>X=a</sub> =
     * @todo we could just as well generalize the argument and return type of R
     */
    public Object/*>R<*/ apply(Object/*>R<*/ a) {
        throw new UnsupportedOperationException("not yet implemented");
    }   

    public Function derive() {
        throw new UnsupportedOperationException("not yet implemented");
    } 

    public Function integrate() {
        throw new ArithmeticException(this + " is only (undefinitely) integrable with respect to a single variable");
    }

    public Real norm() {
        return degreeValue() < 0 ? valueFactory().ZERO() : valueFactory().POSITIVE_INFINITY();
    }

    //@todo we should also support adding other functions (like in AbstractFunctor)?

    protected Arithmetic operatorImpl(final orbital.math.functional.BinaryFunction op, Arithmetic bb) {
        final Polynomial b = (Polynomial)bb;
        if (!indexSet().equals(b.indexSet()))
            throw new IllegalArgumentException("a" + op + "b only defined for equal indexSet() not for " + indexSet() + " and " + b.indexSet() + " of " + this + " and " + b);
        //@internal assuming the dimensions will grow as required
        Polynomial/*>T<*/ ret = (Polynomial) newInstance(indexSet());

        // component-wise
        try {
                ListIterator dst;
                Setops.copy(dst = ret.iterator(), Functionals.map(new orbital.logic.functor.Function() {
                        public Object apply(Object o) {
                                //@todo could rewrite pure functional even more (by using pair copy function etc)
                                Arithmetic/*>S<*/ i = (Arithmetic)o;
                                return op.apply(get(i), b.get(i));
                        }
                }, combinedIndices(this,b)));
                assert !dst.hasNext() : "equal indexSet() for iterator view implies equal structure of iterators";
            return ret;
        } catch (IndexOutOfBoundsException ex) {
                throw (IndexOutOfBoundsException) new IndexOutOfBoundsException(ex + " during a" + op + "b with indexSet()  " + indexSet() + " and " + b.indexSet() + " of " + this + "@" + getClass() + " and " + b + "@" + b.getClass()).initCause(ex);
        }
    }
    
    public Arithmetic add(Arithmetic b) {
        return add((Polynomial)b);
    }
    public Polynomial/*<R,S>*/ add(Polynomial/*<R,S>*/ b) {
        return (Polynomial) operatorImpl(Operations.plus, b);
    }
        
    public Arithmetic subtract(Arithmetic b) throws ArithmeticException {
        return subtract((Polynomial)b);
    } 
    public Polynomial/*<R,S>*/ subtract(Polynomial/*<R,S>*/ b) {
        return (Polynomial) operatorImpl(Operations.subtract, b);
    }

    public Arithmetic multiply(Arithmetic b) {
        if (b instanceof Scalar) {
                return scale((Scalar)b);
        }
        return multiply((Polynomial)b);
    }

    //@internal subclasses can optimize by far when using knowledge of the structure of S
    public Polynomial/*<R,S>*/ multiply(Polynomial/*<R,S>*/ bb) {
        Polynomial b = (Polynomial)bb;
        if (!indexSet().equals(b.indexSet())) {
                throw new IllegalArgumentException("Cannot multiply polynomials of different polynomial rings with " + indexSet() + " and " + b.indexSet() + " variables/indices in (" + this + "@" + getClass() + ") * (" + b + "@" + b.getClass() + ")");
        }
        if (degreeValue() < 0)
            return this;
        else if (b.degreeValue() < 0)
            return b;
        //@internal assuming the dimensions will grow as required
        AbstractPolynomial/*<R,S>*/ ret = (AbstractPolynomial)newInstance(indexSet());
        ret.setZero();
        // ret = &sum;<sub>i&isin;indices(),j&isin;b.indices()</sub> a<sub>i</sub>b<sub>j</sub>X<sup>i * j</sup>
        // perform (very slow) multiplications "jeder mit jedem"
        for (Iterator index = Setops.cross(indices(), b.indices()); index.hasNext(); ) {
            Pair pair = (Pair) index.next();
            Arithmetic/*>S<*/ i = (Arithmetic/*>S<*/)pair.A;
            Arithmetic/*>S<*/ j = (Arithmetic/*>S<*/)pair.B;
            //@internal assuming (S,+) here
            Arithmetic/*>S<*/ Xij = i.add(j);
            // a<sub>i</sub>b<sub>j</sub>
            Arithmetic/*>R<*/ aibj = get(i).multiply(b.get(j));

            // + a<sub>i</sub>b<sub>j</sub>X<sup>i * j</sup>

            Arithmetic cXij = ret.get(Xij);
            ret.set(Xij, cXij != null ? cXij.add(aibj) : aibj);
        }
        return ret;
    }

    /**
     * Sets all our coefficients to 0.
     */
    protected void setZero() {
        Iterator index = indices();
        final Arithmetic/*>R<*/ ZERO;
        if (index.hasNext())
            ZERO = get((Arithmetic/*>S<*/)index.next()).zero();
        else {
                throw new IllegalStateException("Cannot determine 0 coefficient for " + this + "@" + getClass());
        }
        for (ListIterator i = iterator(); i.hasNext(); ) {
            i.next();
            i.set(ZERO);
        }
    }

    public Arithmetic divide(Arithmetic b) throws UnsupportedOperationException {
        if (b instanceof Scalar) {
                return scale((Scalar)b.inverse());
        }
        throw new UnsupportedOperationException("dividing polynomials is not generally defined:( " + this + ") / (" + b + ")");
    } 

    public Arithmetic inverse() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("inverse of polynomials is not generally defined: 1 / (" + this + ")");
    } 

    public String toString() {
        return ArithmeticFormat.getDefaultInstance().format(this);
    }

    /**
     * return an iterator over all indices occurring in either polynomial.
     */
    static final Iterator combinedIndices(Polynomial f, Polynomial g) {
        return Setops.union(Setops.asSet(f.indices()), Setops.asSet(g.indices())).iterator();
    }
}
