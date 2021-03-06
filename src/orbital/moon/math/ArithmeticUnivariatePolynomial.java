/**
 * @(#)ArithmeticUnivariatePolynomial.java 1.0 2001/12/09 Andre Platzer
 *
 * Copyright (c) 2001 Andre Platzer. All Rights Reserved.
 */

package orbital.moon.math;
import orbital.math.*;


import java.util.ListIterator;

import java.util.Collections;
import java.util.Arrays;
import orbital.util.Setops;

import orbital.logic.functor.Functionals;
import orbital.logic.functor.Predicates;

class ArithmeticUnivariatePolynomial/*<R extends Arithmetic>*/ extends AbstractUnivariatePolynomial {
    private static final long serialVersionUID = -7008637791438268097L;
    /**
     * The coefficients in R.
     * @serial
     */
    private Vector/*<R>*/ coefficients;
    /**
     * Caches the degree value.
     * @see #degree()
     */
    private transient int degree;
    public ArithmeticUnivariatePolynomial(int degree, ValueFactory valueFactory) {
        super(valueFactory);
        this.coefficients =
                valueFactory.newInstance(degree < 0
            ? 0
            : degree + 1);
        this.degree = java.lang.Integer.MIN_VALUE;
        this.COEFFICIENT_ZERO = valueFactory.ZERO();
    }
    public ArithmeticUnivariatePolynomial(Arithmetic/*>R<*/ coefficients[], ValueFactory valueFactory) {
        super(valueFactory);
        set(valueFactory.valueOf(coefficients));
    }
  
    /**  
     * Sustain transient variable initialization when deserializing.
     */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Recalculate redundant transient cache fields.
        set(coefficients);
    }

    protected UnivariatePolynomial/*<R>*/ newInstance(int degree) {
        return new ArithmeticUnivariatePolynomial(degree, valueFactory());
    }

    public final int degreeValue() {
        return degree;
    }
    /**
     * Implementation calculating the degree of a polynomial,
     * given its coefficients.
     */
    private int degreeImpl(Vector coefficients) {
        for (int i = coefficients.dimension() - 1; i >= 0; i--) {
            //@internal we can allow skipping null here, since set(R[]) and set(int,R)
            // check for null. However after new ArithmeticPolynomial(int) there may still be
            // some null values, until all have been set
            if (coefficients.get(i) != null && !coefficients.get(i).isZero()) {
                return i;
            }
         }
        return java.lang.Integer.MIN_VALUE;
    }
        
    private void set(Vector/*<R>*/ coefficients) {
        if (coefficients == null)
            throw new IllegalArgumentException("illegal coefficients array: " + coefficients);
        if (Setops.some(coefficients.iterator(), Functionals.bindSecond(Predicates.equal, null)))
            throw new IllegalArgumentException("illegal coefficients: containing null");
        this.coefficients = coefficients;
        this.COEFFICIENT_ZERO = coefficients.dimension() > 0 ? coefficients.get(0).zero() : valueFactory().ZERO();
        this.degree = degreeImpl(coefficients);
    }

    public Arithmetic/*>R<*/ get(int i) {
        if (i <= degreeValue() && i >= coefficients.dimension())
            throw new ArrayIndexOutOfBoundsException(coefficients.dimension() + "=<" + i + "=<" + degreeValue() + "=" + degreeImpl(coefficients));
        return i <= degreeValue() ? coefficients.get(i) : COEFFICIENT_ZERO;
    }
        
    public void set(int i, Arithmetic/*>R<*/ vi) {
        if (vi == null)
            throw new IllegalArgumentException("illegal coefficient value: " + vi);
        final int oldDegree = degreeValue();
        if (i >= coefficients.dimension())
            throw new UnsupportedOperationException("setting coefficients beyond the degree not (always) supported");
        coefficients.set(i, vi);
        this.COEFFICIENT_ZERO = coefficients.dimension() > 0 ? coefficients.get(0).zero() : valueFactory().ZERO();
        if (i >= oldDegree) {
            this.degree = degreeImpl(coefficients);
        }
    }

    public Arithmetic/*>R<*/[] getCoefficients() {
        if (degreeValue() < 0)
            return new Arithmetic/*>R<*/[0];
        return coefficients.toArray();
    } 

    Tensor tensorViewOfCoefficients() {
        return getCoefficientVector();
    }
        public Vector getCoefficientVector() {
                return coefficients;
        }
}
