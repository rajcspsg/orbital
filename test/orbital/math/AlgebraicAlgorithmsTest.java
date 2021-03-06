/**
 * @(#)AlgebraicAlgorithmsTest.java 1.1 2002-10-26 Andre Platzer
 * 
 * Copyright (c) 2002 Andre Platzer. All Rights Reserved.
 */

package orbital.math;

import java.util.*;

import orbital.logic.functor.Predicate;
import orbital.math.functional.*;
import orbital.util.KeyValuePair;
import orbital.util.Setops;
import junit.framework.*;
import junit.extensions.*;
import java.math.BigInteger;

/**
 * A test case, testing .
 * @version $Id$
 */
public class AlgebraicAlgorithmsTest extends check.TestCase {
    private static final int TEST_REPETITIONS = 1000;
    private static final int MAX = 1000;
    private static final int PRIMES_BIT_LENGTH = 5;
    private static final Comparator order = AlgebraicAlgorithms.DEGREE_REVERSE_LEXICOGRAPHIC;

    private static final Values vf = Values.getDefaultInstance();
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        TestSuite suite = new TestSuite(AlgebraicAlgorithmsTest.class);
        suite.addTest(new RepeatedTest(new TestCase("chinese remainder theorem") {
                public void runTest() {
                    casetestChineseRemainder();
                }
            }, TEST_REPETITIONS));
        return suite;
    }
    private ArithmeticTestPatternGenerator random;
    protected void setUp() {
         this.random = new  ArithmeticTestPatternGenerator(-10000,10000);
    }
    /**
     * @internal similar to the algorithm computing exact solution to integer LGS.
     */
    private static void casetestChineseRemainder() {
        final Random random = new Random();
        final Integer result = vf.valueOf(random.nextInt(MAX));
        // choose enough coprime numbers (we simply use primes) such that the result is unique
        Integer M = vf.valueOf(1);
        Set coprimes = new HashSet();   // nonempty set, in fact contains even primes
        do {
            Integer prime = vf.valueOf(MathUtilities.generatePrime(PRIMES_BIT_LENGTH, random));
            if (coprimes.contains(prime))
                continue;
            coprimes.add(prime);
            M = M.multiply(prime);
        } while (M.compareTo(result) <= 0);
        final Integer m[] = (Integer[]) coprimes.toArray(new Integer[0]);
        final Integer x[] = new Integer[m.length];
        for (int i = 0; i < x.length; i++) {
            x[i] = (Integer) vf.quotient(result, m[i]).representative();
        }
        final Integer umod = (Integer) Operations.product.apply(vf.valueOf(m));
        assert umod.equals(M) : umod + " = " + M;
        
        System.out.println("expect solution:  " + result);
        System.out.println("#congruences:     " + m.length);
        System.out.println("congruent values: " + MathUtilities.format(x));
        System.out.println("modulo values:    " + MathUtilities.format(m));
        System.out.println("solution:         " + AlgebraicAlgorithms.chineseRemainder(x,m));
        System.out.println("is unique modulo: " + umod);
        System.out.println();
        Arithmetic r = AlgebraicAlgorithms.chineseRemainder(x,m).representative();
        assertTrue(result.equals(r) || result.subtract(r).equals(umod) || result.subtract(r).equals(umod.minus()), "chineseRemainderTheorem: expected " + result + " was " + r + " (mod " + umod + ")");
    }
    
    public void testGroebnerBasisSimple() {
        // some polynomials in <b>Q</b>[X,Y]
        Polynomial/*<Rational>*/ g =
            vf.polynomial(new int[][] {
                {0, 1},
                {1, 0}
            });
        checkPolynomial(vf, g);
        Set/*_<Polynomial<Rational>>_*/ G = new HashSet();
        G.add(g);

        assertEquals(AlgebraicAlgorithms.groebnerBasis(G, order) , G);
        Setops.all(AlgebraicAlgorithms.groebnerBasis(G, order), checkPolynomial(vf));

        Set/*_<Polynomial<Rational>>_*/ H = new HashSet(G);
        H.add(g.multiply(g));
        H.add(g.multiply(g.add(g)).subtract(g));
        Setops.all(H, checkPolynomial(vf));
        assertEquals(AlgebraicAlgorithms.groebnerBasis(H, order) , G);
    }

    public void testPolynomialQuotientCalculation() {
        System.out.println("calculate with quotients of polynomials");
        // create elements in <b>R</b>[X]/(Y^2-X^3-X^2)
        final Collection m = Arrays.asList(new Polynomial[] {
            // alternative form of construction: explicit concatenation
            // of monomials. This is more to type, but also more
            // simple to construct
            vf.MONOMIAL(new int[] {0,2}).subtract(vf.MONOMIAL(new int[] {3,0}))
            .subtract(vf.MONOMIAL(new int[] {2,0}))
        });
        Setops.all(m, checkPolynomial(vf));
        // the Groebner basis of m
        final Set gm = AlgebraicAlgorithms.groebnerBasis(new HashSet(m), order);
        Setops.all(gm, checkPolynomial(vf));
        Quotient/*<Polynomial<Real>>*/ f =
            vf.quotient(vf.polynomial(new double[][] {
                {2,1},
                {3,0}
            }), gm, order);
        Quotient/*<Polynomial<Real>>*/ g =
            vf.quotient(vf.polynomial(new double[][] {
                {-1,1},
                {1,1}
            }), gm, order);

        // perform calculations in both fields
        System.out.println("perform calculations in a quotient ring modulo " + m);

        f = vf.quotient(vf.polynomial(new double[][] {
            {2,-1},
            {3,0},
            {-1,0}
        }), gm, order);

        assertEquals(f.multiply(g).representative(), vf.polynomial(new int[][] {
            {-2,3,-2,-1},
            {-1,4,-1,0},
            {5,3,0,0}
        }));
    }

    public void testgcd() {
        for (int i = 0; i < TEST_REPETITIONS; i++) {
            int x = random.randomInt();
            if (x == 0) x++;
            Integer xargs[] = {
                vf.valueOf(BigInteger.valueOf(x)),
                vf.valueOf((long)x),
                vf.valueOf(x)
            };
            int y = random.randomInt();
            if (y == 0) y++;
            Integer yargs[] = {
                vf.valueOf(BigInteger.valueOf(y)),
                vf.valueOf((long)y),
                vf.valueOf(y)
            };
            for (int k = 0; k < xargs.length; k++) {
                Integer xs = xargs[k];
                Integer ys = yargs[k];
                Integer d = (Integer)AlgebraicAlgorithms.gcd(xs, ys);
                assertTrue(xs.modulo(d).isZero(),
                           "gcd(" + xs + "," + ys + ") = " + d + " divides " + x  + "\n" + xs + "@" + xs.getClass() + " " + ys + "@" + ys.getClass() + " " + d + "@" + d.getClass());
                assertTrue(ys.modulo(d).isZero(),
                           "gcd(" + xs + "," + ys + ") = " + d + " divides " + y  + "\n" + xs + "@" + xs.getClass() + " " + ys + "@" + ys.getClass() + " " + d + "@" + d.getClass());
            }
        }
    }

    public void testlcm() {
        for (int i = 0; i < TEST_REPETITIONS; i++) {
            int x = random.randomInt();
            if (x == 0) x++;
            Integer xargs[] = {
                vf.valueOf(BigInteger.valueOf(x)),
                vf.valueOf((long)x),
                vf.valueOf(x)
            };
            int y = random.randomInt();
            if (y == 0) y++;
            Integer yargs[] = {
                vf.valueOf(BigInteger.valueOf(y)),
                vf.valueOf((long)y),
                vf.valueOf(y)
            };
            for (int k = 0; k < xargs.length; k++) {
                Integer xs = xargs[k];
                Integer ys = yargs[k];
                Integer m = (Integer)AlgebraicAlgorithms.lcm(xs, ys);
                assertTrue(m.modulo(xs).isZero(),
                           "lcm(" + xs + "," + ys + ") = " + m + " multiple of " + xs + "\n" + xs + "@" + xs.getClass() + " " + ys + "@" + ys.getClass() + " " + m + "@" + m.getClass());
                assertTrue(m.modulo(ys).isZero(),
                           "lcm(" + xs + "," + ys + ") = " + m + " multiple of " + ys + "\n" + xs + "@" + xs.getClass() + " " + ys + "@" + ys.getClass() + " " + m + "@" + m.getClass());
            }
        }
    }
    
    public void testdSolve() {
        System.out.println("solving differential equations");
        final Symbol t = vf.symbol("t");
        final Real tau = vf.ZERO();
        Matrix A = vf.valueOf(new double[][] {
            {0}
        });
        Vector b = vf.valueOf(new Arithmetic[]{vf.valueOf(2)});
        Vector eta = vf.valueOf(new Symbol[]{vf.symbol("x0")});
        Function f = AlgebraicAlgorithms.dSolve(A, b, tau, eta);
        System.out.println("Solving ODE x'(t) ==\n" + A + "*x(t) + " + b + "\nwith initial value  " + eta + " at " + tau + "\nyields " + f);
        System.out.println("  solution at " + 0 + " is " + f.apply(vf.valueOf(0)));
        System.out.println("  solution at " + 1 + " is " + f.apply(vf.valueOf(1)));
        System.out.println("  solution at " + t + " is " + f.apply(t));

        A = vf.valueOf(new double[][] {
            {0,1},
            {0,0}
        });
        b = vf.valueOf(new double[]{0,0});
        eta = vf.valueOf(new double[]{0,0});
        f = AlgebraicAlgorithms.dSolve(A, b, tau, eta);
        System.out.println("Solving ODE x'(t) ==\n" + A + "*x(t) + " + b + "\nwith initial value  " + eta + " at " + tau + "\nyields " + f);
        System.out.println("  solution at " + 0 + " is " + f.apply(vf.valueOf(0)));
        System.out.println("  solution at " + 1 + " is " + f.apply(vf.valueOf(1)));
        System.out.println("  solution at " + t + " is " + f.apply(t));


        eta = vf.valueOf(new double[]{1,2});
        f = AlgebraicAlgorithms.dSolve(A, b, tau, eta);
        System.out.println("Solving ODE x'(t) ==\n" + A + "*x(t) + " + b + "\nwith initial value  " + eta + " at " + tau + "\nyields " + f);
        System.out.println("  solution at " + 0 + " is " + f.apply(vf.valueOf(0)));
        System.out.println("  solution at " + 1 + " is " + f.apply(vf.valueOf(1)));
        System.out.println("  solution at " + t + " is " + f.apply(t));
        
        eta = vf.valueOf(new Symbol[]{vf.symbol("z0"),vf.symbol("v0")});
        f = AlgebraicAlgorithms.dSolve(A, b, tau, eta);
        System.out.println("Solving ODE x'(t) ==\n" + A + "*x(t) + " + b + "\nwith initial value  " + eta + " at " + tau + "\nyields " + f);
        System.out.println("  solution at " + 0 + " is " + f.apply(vf.valueOf(0)));
        System.out.println("  solution at " + 1 + " is " + f.apply(vf.valueOf(1)));
        System.out.println("  solution at " + t + " is " + f.apply(t));

        A = vf.valueOf(new double[][] {
            {0,1,0},
            {0,0,1},
            {0,0,0},
        });
        b = vf.valueOf(new double[]{0,0,0});
        eta = vf.valueOf(new Symbol[]{vf.symbol("z0"),vf.symbol("v0"),vf.symbol("a")});
        System.out.println("train dynamics with constant acceleration a as x3 and initial values of position, velocity and acceleration " + eta);
        f = AlgebraicAlgorithms.dSolve(A, b, tau, eta);
        System.out.println("Solving ODE x'(t) ==\n" + A + "*x(t) + " + b + "\nwith initial value  " + eta + " at " + tau + "\nyields " + f);
        System.out.println("  solution at " + 0 + " is " + f.apply(vf.valueOf(0)));
        System.out.println("  solution at " + 1 + " is " + f.apply(vf.valueOf(1)));
        System.out.println("  solution at " + t + " is " + f.apply(t));


        A = vf.valueOf(new double[][] {
            {0,1,2},
            {0,0,1},
            {0,0,0},
        });
        b = vf.valueOf(new double[]{0,0,0});
        eta = vf.valueOf(new double[]{1,2,3});
        f = AlgebraicAlgorithms.dSolve(A, b, tau, eta);
        System.out.println("Solving ODE x'(t) ==\n" + A + "*x(t) + " + b + "\nwith initial value  " + eta + " at " + tau + "\nyields " + f);
        System.out.println("  solution at " + 0 + " is " + f.apply(vf.valueOf(0)));
        System.out.println("  solution at " + 1 + " is " + f.apply(vf.valueOf(1)));
        System.out.println("  solution at " + t + " is " + f.apply(t));


        A = vf.valueOf(new double[][] {
            {0,1},
            {0,0}
        });
        b = vf.valueOf(new Arithmetic[]{vf.ZERO(),vf.symbol("a")});
        eta = vf.valueOf(new Symbol[]{vf.symbol("z0"),vf.symbol("v0")});
        System.out.println("train dynamics with constant acceleration a as inhomogeneous part and initial value " + eta);
        f = AlgebraicAlgorithms.dSolve(A, b, tau, eta);
        System.out.println("Solving ODE x'(t) ==\n" + A + "*x(t) + " + b + "\nwith initial value  " + eta + " at " + tau + "\nyields " + f);
        System.out.println("  solution at " + 0 + " is " + f.apply(vf.valueOf(0)));
        System.out.println("  solution at " + 1 + " is " + f.apply(vf.valueOf(1)));
        System.out.println("  solution at " + t + " is " + f.apply(t));

        A = vf.valueOf(new double[][] {
            {0,1,0,0},
            {0,0,1,0},
            {0,0,0,1},
            {0,0,0,0},
        });
        b = vf.valueOf(new Arithmetic[]{vf.ZERO(),vf.ZERO(),vf.ZERO(),vf.symbol("b")});
        eta = vf.valueOf(new Symbol[]{vf.symbol("a1"),vf.symbol("a2"),vf.symbol("a3"),vf.symbol("a4")});
        f = AlgebraicAlgorithms.dSolve(A, b, tau, eta);
        System.out.println("Solving ODE x'(t) ==\n" + A + "*x(t) + " + b + "\nwith initial value  " + eta + " at " + tau + "\nyields " + f);
        System.out.println("  solution at " + 0 + " is " + f.apply(vf.valueOf(0)));
        System.out.println("  solution at " + 1 + " is " + f.apply(vf.valueOf(1)));
        System.out.println("  solution at " + t + " is " + f.apply(t));

    }
    
    /**
     * Check contract properties of polynomials
     */
    public static boolean checkPolynomial(ValueFactory vf, Polynomial p) {
        ArithmeticTest.checkArithmetic(vf, p, false);
        Integer deg = p.degree();
        int[] degs = p.degrees();
        for (Iterator k = p.indices(); k.hasNext(); ) {
                Vector i = getExponentVector(k.next());
                for (int j = 0; j < i.dimension(); j++) {
                        assertTrue(p.get(i).isZero() || ((Integer)i.get(j)).compareTo(vf.valueOf(degs[j])) <= 0, "exponents in indices() <= degrees(): exponent " + i + " <= " + MathUtilities.format(degs) + " of " + p.get(i) + " in " + p + "@" + p.getClass());
                }
                assertTrue(p.get(i).isZero() || ((Integer)Operations.sum.apply(i)).compareTo(deg) <= 0, "sum of exponents in indices() <= degree(): exponent " + i + " <= " + deg + " of " + p.get(i) + " in " + p + "@" + p.getClass());
        }
        for (Iterator k = p.monomials(); k.hasNext(); ) {
                KeyValuePair e = (KeyValuePair) k.next();
                assertTrue(e.getValue().equals(p.get((Arithmetic) e.getKey())), "monomials() of polynomial correspond to get(): " + e.getValue() + " =  " + p.get((Arithmetic) e.getKey()));
                assertTrue(p.get((Arithmetic) e.getKey()).equals(e.getValue()), "get and monomials() fit " + e.getValue() + " = " + p.get((Arithmetic) e.getKey())+ " at " + e.getKey() + " in " + p + "@" + p.getClass());
        }
        Tensor t = vf.asTensor(p);
        assertTrue(t.rank() == p.rank(), "asTensor preserves rank() " + p.rank() + " = " + t.rank() + " for " + p + "@" + p.getClass());
        for (Iterator k = t.entries(); k.hasNext(); ) {
                KeyValuePair e = (KeyValuePair) k.next();
                Vector i = (Vector)e.getKey();
                assertTrue(e.getValue().equals(t.get(getIndex(i))), "entries() of tensor correspond to get(): " + e.getValue() + " =  " + t.get(getIndex(i)));
                assertTrue(t.get(getIndex(i)).equals(p.get(i)), "tensor.get corresponds to polynomial.get(): " + t.get(getIndex(i)) + " = " + p.get(i));
        }
        for (Iterator k = p.monomials(); k.hasNext(); ) {
                KeyValuePair e = (KeyValuePair) k.next();
                Vector i = getExponentVector(e.getKey());
                assertTrue(e.getValue().equals(p.get((Arithmetic) e.getKey())), "monomials() of polynomial correspond to get(): " + e.getValue() + " =  " + p.get((Arithmetic) e.getKey()));
                assertTrue(p.get(i).equals(t.get(getIndex(i))), "polynomial.get corresponds to tensor.get(): " + p.get(i) + " = " + t.get(getIndex(i)));
        }
        return true;
    }
    protected final Predicate checkPolynomial(final ValueFactory vf) {
        return new Predicate() {
                public boolean apply(Object arg) {
                        return checkPolynomial(vf, (Polynomial)arg);
                }

        };
    }
    private static final int[] getIndex(Vector/*<Integer>*/ v) {
        int[] d = new int[v.dimension()];
        for (int i = 0; i < d.length; i++) {
                d[i] = ((Integer)v.get(i)).intValue();
        }
        return d;
    }
        protected static Vector/*<Integer>*/ getExponentVector(Object m) {
            if (m instanceof Vector) {
                    return (Vector)m;
            } else if (m instanceof Integer) {
                    // univariate case
                    return ((Arithmetic)m).valueFactory().valueOf(new Integer[] {(Integer)m});
            } else {
                    throw new ClassCastException("Cannot convert exponent representation into Vector<Integer> from " + m);
            }
        }
}
