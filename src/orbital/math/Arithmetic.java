/**
 * @(#)Arithmetic.java 1.0 1999/08/16 Andre Platzer
 * 
 * Copyright (c) 1999 Andre Platzer. All Rights Reserved.
 */

package orbital.math;

/**
 * Arithmetic is implemented by all <dfn>arithmetic objects</dfn> that behave like algebraic
 * numbers in terms of their compositional laws.
 * <p>
 * Such an arithmetic object should return objects of appropriate type as resulting values.
 * </p>
 * <hr />
 * <table style="border: none">
 *   <caption>Operations on arithmetic objects for groups, R-vector spaces, rings, fields, R-algebras, etc.</caption>
 *   <tr>
 *     <td colspan="4">law of composition + (addition)</td>
 *   </tr>
 *   <tr>
 *     <td width="5%"></td>
 *     <td>add</td>
 *     <td>+:M�M&rarr;M; (a,b)&#8614;a+b</td>
 *     <td>for magmas</td>
 *   </tr>
 *   <tr>
 *     <td width="5%"></td>
 *     <td>minus</td>
 *     <td>&minus;:M&rarr;M; a&#8614; &minus;a</td>
 *     <td>for groups</td>
 *   </tr>
 *   <tr>
 *     <td width="5%"></td>
 *     <td>subtract</td>
 *     <td>-:M�M&rarr;M; (a,b)&#8614;a-b = a+(&minus;b)</td>
 *     <td>for groups</td>
 *   </tr>
 *   <tr>
 *     <td colspan="4">law &sdot; (multiplication)</td>
 *   </tr>
 *   <tr>
 *     <td width="5%"></td>
 *     <td>multiply</td>
 *     <td>&sdot;:M�M&rarr;M; (a,b)&#8614;a&sdot;b</td>
 *     <td>law of composition for rings</td>
 *   </tr>
 *   <tr>
 *     <td width="5%"></td>
 *     <td>inverse</td>
 *     <td><sup>-1</sup>:M&rarr;M; (a,b)&#8614;a<sup>-1</sup></td>
 *     <td>for fields</td>
 *   </tr>
 *   <tr>
 *     <td width="5%"></td>
 *     <td>divide</td>
 *     <td>&#8725;:M�M&rarr;M; (a,b)&#8614;a&#8725;b = a&sdot;b<sup>-1</sup></td>
 *     <td>for fields</td>
 *   </tr>
 *   <tr>
 *     <td width="5%"></td>
 *     <td>multiply</td>
 *     <td>&lowast;:R�M&rarr;M; (a,b)&#8614;a&lowast;b</td>
 *     <td>law of action for R-modules</td>
 *   </tr>
 *   <tr>
 *     <td width="5%"></td>
 *     <td>multiply</td>
 *     <td>�:M�M&rarr;F; (a,b)&#8614;a�b</td>
 *     <td>scalar-product for vector spaces</td>
 *   </tr>
 *   <tr>
 *     <td colspan="4">extended law ^ (power) is derived from
 *     multiplication</td>
 *   </tr>
 *   <tr>
 *     <td width="5%"></td>
 *     <td>power</td>
 *     <td>^:M�M&rarr;M; (a,b)&#8614;a^b = a<sup>b</sup></td>
 *     <td>for rings,<br>requires &#13266; and exp in general case</br></td>
 *   </tr>
 * </table>
 * <p>
 * <b>Note:</b> Arithmetic objects may support composition with arithmetic
 * objects of various types other than those in M as well. For simplicity these
 * more general laws with a signature of M<sub>1</sub>�M<sub>2</sub>&rarr;M<sub>0</sub>
 * are omitted in the table above. Since they are indeed very useful they can nevertheless
 * be implemented. Vectors for example can be multiplied with a scalar as well as a
 * matrix or a vector resulting in different objects.
 * </p>
 * <p>
 * Also see the complete description of <a href="doc-files/AlgebraicStructures.html">Algebraic Structures</a>
 * related to the interface Arithmetic.
 * </p>
 * <hr />
 * <p>
 * An arithmetic object that supports an order should implement {@link java.lang.Comparable} as well.
 * Of course, a norm imposes a primitive order &le; &sube; M&times;M by x&le;y :&hArr; d(x,a)&le;d(y,a), for any center a&isin;M.
 * But this will most likely differ from the natural ordering on M which should be implemented via {@link java.lang.Comparable}.
 * By the way, {@link orbital.math.functional.Operations} can be very useful to implement Arithmetic objects.
 * </p>
 * <p>
 * Arithmetic objects provides a <em>strong</em> type system which can be either dynamic or static.
 * Operations generally depend polymorphically on the types of all arguments and thus may require
 * dynamic dispatch with regard of the types of all arguments.
 * </p>
 * 
 * @version 1.0, 1999/08/16
 * @author  Andr&eacute; Platzer
 * @see orbital.math.functional.Operations
 * @see java.lang.Comparable
 * @see <a href="doc-files/AlgebraicStructures.html">Algebraic Structures</a>
 * @todo Unfortunately, a Generic interface cannot be extended with different template parameters.
 *  Couldn't we introduce generics with a template type T for add, subtract, times, power ... and write "Arithmetic<T> extends Arithmetic<Arithmetic>" or something?
 *  a ring R could then be a "class R implements Arithmetic<R>" closed or stable under + and *, but not necessarily under /. Hmmm.
 *  This would need covariant return types and(!) dispatched covariant parameter types which are usually illegal since only contravariant parameter types have a general meaning.
 * @todo should we split multiplication into inner ring multiplication and outer module multiplication?
 * @todo link to a document doc-files containing definitions of magmas, monoids, groups, abelian groups, pseudo-rings, rings, commutative rings, fields, algebras, modules,...
 * @TODO: enhance documentation and improve consistency with K-Algebras
 * @todo think about turning into a template class for compile-time type safety.
 */
public interface Arithmetic extends Normed {

    /**
     * Compares two arithmetic objects for tolerant equality.
     * @param tolerance specifies how much the arithmetic objects may differ to be treated as equal.
     * @pre true
     * @post RES &hArr; Metric.INDUCED.distance(this, o).compareTo(tolerance) < 0
     * @return Whether this &asymp; o. More precisely whether d(this,o) := |this-o| &lt; tolerance.
     * @attribute derived
     * @todo @xxx sure that we should already move this to Scalar, or even to Arithmetic?
     */
    /*final*/ boolean equals(Object o, Real tolerance);

    // constants for laws + and &sdot;

    /**
     * 0.
     * @attribute neutral element for Operations.plus
     * @return the neutral element 0 for addition in this algebraic structure (if it is a unital magma or monoid).
     * @throws ArithmeticException if this algebraic structure does not have a 0.
     * @todo should we demand instead, that there are static fields "T ZERO" and "T ONE" in a "class T implements Arithmetic"?
     *  or "Arithmetic ZERO", "Arithmetic ONE", or functions "T zero()" and "T one()" with postcondition RES==OLD(RES)? What about matrices with different ZEROs?
     * @internal perhaps the best model would demand from a parametric type to have a field ZERO and a field ONE. This would even hold for the type Matrix<Rational,4,5>.ZERO etc. However this is neither possible with Java Generics, nor (as far as I know) with compile-time templates of C++.
     * @todo if this is a good thing, then use x.zero() etc throughout
     * @post RES == OLD(RES) &and; this.getClass().isInstance(RES)
     */
    Arithmetic zero() throws ArithmeticException;
    /**
     * 1.
     * @attribute neutral element for Operations.times
     * @return the neutral element 1 for multiplication in this algebraic structure (if it is a true ring, or ...).
     * @throws UnsupportedOperationException if this algebraic structure does not have a 1.
     * @post RES == OLD(RES) &and; this.getClass().isInstance(RES)
     */
    Arithmetic one() throws UnsupportedOperationException;

    // law of composition of the group (M,+)

    /**
     * Adds an arithmetic object to this returning the result.
     * @return this+b.
     * @throws ArithmeticException if an exceptional arithmetic condition has occurred while
     * performing the operation.
     * @throws IllegalArgumentException if the argument type is illegal for this operation.
     *  Note: for single type handling it is also allowed to throw a ClassCastException, instead.
     */
    Arithmetic add(Arithmetic b) throws ArithmeticException;

    /**
     * Returns the additive inverse of this arithmetic object.
     * @return &minus;this.
     * @throws ArithmeticException if an exceptional arithmetic condition has occurred while
     *  performing the operation.
     */
    Arithmetic minus() throws ArithmeticException;

    /**
     * Subtracts an arithmetic object from this returning the result.
     * @return this-b.
     * @throws ArithmeticException if an exceptional arithmetic condition has occurred while
     *  performing the operation.
     * @throws IllegalArgumentException if the argument type is illegal for this operation.
     *  Note: for single type handling it is also allowed to throw a ClassCastException, instead.
     */
    Arithmetic subtract(Arithmetic b) throws ArithmeticException;

    // law of the group (M,&sdot;)

    /**
     * Multiplies an arithmetic object to this returning the result.
     * @return this&sdot;b, or this&lowast;b depending upon context.
     * @throws ArithmeticException if an exceptional arithmetic condition has occurred while
     * performing the operation.
     * @throws IllegalArgumentException if the argument type is illegal for this operation.
     *  Note: for single type handling it is also allowed to throw a ClassCastException, instead.
     * @throws UnsupportedOperationException if this class does not support this operation, principially,
     *  regardless of the argument.
     */
    Arithmetic multiply(Arithmetic b) throws ArithmeticException, UnsupportedOperationException;

    /**
     * Returns the multiplicative inverse of this arithmetic object.
     * @return this<sup>-1</sup>.
     * @throws ArithmeticException if an exceptional arithmetic condition has occurred while
     *  performing the operation.
     * @throws UnsupportedOperationException if this class does not support this operation, principially,
     *  regardless of the argument.
     */
    Arithmetic inverse() throws ArithmeticException, UnsupportedOperationException;

    /**
     * Divides this by an arithmetic object returning the result.
     * @return this&#8725;b.
     * @throws ArithmeticException if an exceptional arithmetic condition has occurred while
     * performing the operation.
     * @throws IllegalArgumentException if the argument type is illegal for this operation.
     *  Note: for single type handling it is also allowed to throw a ClassCastException, instead.
     * @throws UnsupportedOperationException if this class does not support this operation, principially,
     *  regardless of the argument.
     */
    Arithmetic divide(Arithmetic b) throws ArithmeticException, UnsupportedOperationException;

    // extended laws

    /**
     * Returns the power of an arithmetic object to this base.
     * @return this<sup>b</sup>.
     * @throws ArithmeticException if an exceptional arithmetic condition has occurred while
     *  performing the operation.
     * @throws IllegalArgumentException if the argument type is illegal for this operation.
     *  Note: for single type handling it is also allowed to throw a ClassCastException, instead.
     * @throws UnsupportedOperationException if this class does not support this operation, principially,
     *  regardless of the argument.
     */
    Arithmetic power(Arithmetic b) throws ArithmeticException, UnsupportedOperationException;

    // representation methods

    /**
     * Returns a string representation of the Arithmetic object.
     * This method is already provided in {@link java.lang.Object}.
     * If it is overwritten it should return a sound representation of the Arithmetic object.
     * @return a sound representation of this Arithmetic object.
     */
    String toString();
}