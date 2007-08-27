/**
 * @(#)AbstractReal.java 1.0 1999/08/16 Andre Platzer
 * 
 * Copyright (c) 1999 Andre Platzer. All Rights Reserved.
 */

package orbital.moon.math;
import orbital.math.*;

import orbital.math.Integer;


import java.math.BigDecimal;
import orbital.math.functional.Operations;

abstract class AbstractReal extends AbstractComplex implements Real {
    private static final long serialVersionUID = -4117614439306224843L;

    protected AbstractReal() {}

    
    public boolean equals(Object o) {
	if (o instanceof Real) {
	    return subtract((Arithmetic)o).isZero();
	} else
	    return super.equals(o);
    }
    public int hashCode() {
	//@internal identical to @see Double#hashCode()
	long bits = java.lang.Double.doubleToLongBits(doubleValue());
	return (int)(bits ^ (bits >>> 32));
    }
    public int compareTo(Object o) {
	if (o instanceof Real) {
	    return (int)Math.signum(subtract((Real)o).doubleValue());
	} else
	    return super.compareTo(o);
    }
    public Real norm() {
	if (compareTo(zero()) < 0)
	    return (Real)minus();
	else 
	    return this;
    } 

    // order

    //TODO: optimize using direct + for all Scalars except Complex
    public Arithmetic add(Arithmetic b) {
        if (b instanceof Real)
            return add((Real) b);
        return (Arithmetic) Operations.plus.apply(this, b);
    } 
    public Arithmetic subtract(Arithmetic b) {
        if (b instanceof Real)
            return subtract((Real) b);
        return (Arithmetic) Operations.subtract.apply(this, b);
    } 
    public Arithmetic multiply(Arithmetic b) {
        if (b instanceof Real)
            return multiply((Real) b);
        return (Arithmetic) Operations.times.apply(this, b);
    } 
    public Arithmetic divide(Arithmetic b) {
        if (b instanceof Real)
            return divide((Real) b);
        return (Arithmetic) Operations.divide.apply(this, b);
    } 
    public Arithmetic power(Arithmetic b) {
        if (b instanceof Real)
            return power((Real) b);
        return (Arithmetic) Operations.power.apply(this, b);
    } 

    // overwrite complex
    public final Real re() {
        return this;
    } 
    final double realValue() {
        return doubleValue();
    } 

    public final Real im() {
        return (Real)zero();
    } 

    final double imaginaryValue() {
        return 0;
    } 
    public final Complex conjugate() {
        return this;
    }
    // end of overwrite complex

    // delegate super class operations
    public Complex add(Complex b) {
        return (Complex) Operations.plus.apply(this, b);
    } 

    public Complex subtract(Complex b) {
        return (Complex) Operations.subtract.apply(this, b);
    } 

    public Complex multiply(Complex b) {
        return (Complex) Operations.times.apply(this, b);
    } 

    public Complex divide(Complex b) {
        return (Complex) Operations.divide.apply(this, b);
    } 


    /**
     * Turn numbers a and b into reals of appropriate (compatible) precision.
     * @return an array of the converted versions of a and b respectively.
     */
    static Real[] makeReal(Number a, Number b) {
	if (a instanceof orbital.moon.math.Big || b instanceof orbital.moon.math.Big) {
	    return new Real[] {
		new Big(a), new Big(b)
	    };
	} else {
	    //@todo could also check whether Float would be sufficient
	    return new Real[] {
		new Double(a), new Double(b)
	    };
	}
    }

    /**
     * Represents a real number in <b>R</b> as a float value.
     * 
     * @version $Id$
     * @author  Andr&eacute; Platzer
     */
    static class Float extends AbstractReal {
        private static final long serialVersionUID = -206414766833581552L;
    
        /**
         * the real value (with machine-sized float-precision, only, of course).
         * @serial
         */
        private float                    value;
        public Float(float v) {
            value = v;
        }
        public Float(Number v) {
            value = v.floatValue();
        }
    
        public Object clone() {
            return new Float(floatValue());
        } 

	public boolean equals(Object o) {
	    if (o instanceof Double || o instanceof Float) {
		//@internal identical to @see Double#equals(Object)
		return java.lang.Double.doubleToLongBits(doubleValue()) == java.lang.Double.doubleToLongBits(((Real) o).doubleValue());
	    } else
		return Operations.equal.apply(this, o);
	}

	public int hashCode() {
	    //@internal identical to @see Double#hashCode()
	    long bits = java.lang.Double.doubleToLongBits(doubleValue());
	    return (int)(bits ^ (bits >>> 32));
	}
	public int compareTo(Object o) {
	    if (o instanceof Double || o instanceof Float) {
		return Double.compareDouble(value, ((Number)o).doubleValue());
	    } else
		return ((Integer) Operations.compare.apply(this, o)).intValue();
	} 

	public Real norm() {
	    return Values.getDefaultInstance().valueOf(Math.abs(floatValue()));
	}

	public boolean isZero() {
	    return value == 0;
	} 
	public boolean isOne() {
	    return value == 1;
	} 

        public float floatValue() {
            return value;
        } 

        public double doubleValue() {
            return value;
        } 
    
        public long longValue() {
            return (long)value;
        } 

        public Real add(Real b) {
            //@xxx what's up with b being an Integer.Int or Integer.Long?
            if (b instanceof Float)
                return new Float(floatValue() + b.floatValue());
            else if (b instanceof Double)
                //optimized widening
                return new Double(floatValue() + b.doubleValue());
            else if (b instanceof Big)
                return new Big(floatValue()).add(b);
            return (Real) Operations.plus.apply(this, b);
        }
        public Real subtract(Real b) {
            if (b instanceof Float)
                return new Float(floatValue() - b.floatValue());
            else if (b instanceof Double)
                //optimized widening
                return new Double(floatValue() - b.doubleValue());
            else if (b instanceof Big)
                return new Big(floatValue()).subtract(b);
            return (Real) Operations.subtract.apply(this, b);
        }
        public Arithmetic minus() {
            return new Float(-floatValue());
        } 
        public Real multiply(Real b) {
            if (b instanceof Float)
                return new Float(floatValue() * b.floatValue());
            else if (b instanceof Double)
                //optimized widening
                return new Double(floatValue() * b.doubleValue());
            else if (b instanceof Big)
                return new Big(floatValue()).multiply(b);
            return (Real) Operations.times.apply(this, b);
        }
        public Real divide(Real b) {
            if (b instanceof Float)
                return new Float(floatValue() / b.floatValue());
            else if (b instanceof Double)
                //optimized widening
                return new Double(floatValue() / b.doubleValue());
            else if (b instanceof Big)
                return new Big(floatValue()).divide(b);
            return (Real) Operations.divide.apply(this, b);
        }
        public Real power(Real b) {
            if (b instanceof Float)
                return new Float((float) Math.pow(floatValue(), b.floatValue()));
            else if (b instanceof Double)
                //optimized widening
                return new Double(Math.pow(floatValue(), b.doubleValue()));
            else if (b instanceof Big)
                return new Big(floatValue()).power(b);
            return (Real) Operations.power.apply(this, b);
        }
        public Arithmetic inverse() {
            return new Float(1 / floatValue());
        } 
    }

    /**
     * Represents a real number in <b>R</b> as a double value.
     * 
     * @version $Id$
     * @author  Andr&eacute; Platzer
     */
    static class Double extends AbstractReal {
        private static final long serialVersionUID = 2011638443547790678L;
    
        /**
         * the real value (with machine-sized double-precision, only, of course).
         * @serial
         */
        private double                   value;
        public Double(double v) {
            value = v;
        }
        public Double(Number v) {
            value = v.doubleValue();
        }
    
        public Object clone() {
            return new Double(doubleValue());
        } 

	public boolean equals(Object o) {
	    if (o instanceof Double || o instanceof Float) {
		//@internal identical to @see Double#equals(Object)
		return java.lang.Double.doubleToLongBits(doubleValue()) == java.lang.Double.doubleToLongBits(((Real) o).doubleValue());
	    } else
		return Operations.equal.apply(this, o);
	}

	public int hashCode() {
	    //@internal identical to @see Double#hashCode()
	    long bits = java.lang.Double.doubleToLongBits(doubleValue());
	    return (int)(bits ^ (bits >>> 32));
	}
	public int compareTo(Object o) {
	    if (o instanceof Double || o instanceof Float) {
		return compareDouble(value, ((Number)o).doubleValue());
	    } else
		return ((Integer) Operations.compare.apply(this, o)).intValue();
	} 

	//@internal identical to @see Double#compare(double,double)
	static int compareDouble(double d1, double d2) {
	    if (d1 < d2)
		return -1;           // Neither val is NaN, thisVal is smaller
	    if (d1 > d2)
		return 1;            // Neither val is NaN, thisVal is larger
	    long thisBits = java.lang.Double.doubleToLongBits(d1);
	    long anotherBits = java.lang.Double.doubleToLongBits(d2);

	    return (thisBits == anotherBits ?  0 : // Values are equal
		    (thisBits < anotherBits ? -1 : // (-0.0, 0.0) or (!NaN, NaN)
		     1));                          // (0.0, -0.0) or (NaN, !NaN)
	}

	public Real norm() {
	    return Values.getDefaultInstance().valueOf(Math.abs(doubleValue()));
	}

	public boolean isZero() {
	    return value == 0;
	} 
	public boolean isOne() {
	    return value == 1;
	} 

        public double doubleValue() {
            return value;
        } 
        public long longValue() {
            return (long)value;
        } 
    
        public Real add(Real b) {
            //@xxx what's up with b being an Integer.Int or Integer.Long?
	    if (b instanceof Double || b instanceof Float)
                return new Double(doubleValue() + b.doubleValue());
            else if (b instanceof Big)
                return new Big(doubleValue()).add(b);
            return (Real) Operations.plus.apply(this, b);
        }
        public Real subtract(Real b) {
	    if (b instanceof Double || b instanceof Float)
                return new Double(doubleValue() - b.doubleValue());
            else if (b instanceof Big)
                return new Big(doubleValue()).subtract(b);
            return (Real) Operations.subtract.apply(this, b);
        }
        public Arithmetic minus() {
            return new Double(-doubleValue());
        } 
        public Real multiply(Real b) {
	    if (b instanceof Double || b instanceof Float)
                return new Double(doubleValue() * b.doubleValue());
            else if (b instanceof Big)
                return new Big(doubleValue()).multiply(b);
            return (Real) Operations.times.apply(this, b);
        }
        public Real divide(Real b) {
	    if (b instanceof Double || b instanceof Float)
                return new Double(doubleValue() / b.doubleValue());
            else if (b instanceof Big)
                return new Big(doubleValue()).divide(b);
            return (Real) Operations.divide.apply(this, b);
        }
        public Real power(Real b) {
	    if (b instanceof Double || b instanceof Float)
                return new Double(Math.pow(doubleValue(), b.doubleValue()));
            else if (b instanceof Big)
                return new Big(doubleValue()).power(b);
            return (Real) Operations.power.apply(this, b);
        }
        public Arithmetic inverse() {
            return new Double(1 / doubleValue());
        } 

    }

    /**
     * Represents a real number in <b>R</b> as an arbitrary-precision value.
     * 
     * @version $Id$
     * @author  Andr&eacute; Platzer
     */
    static class Big extends AbstractReal implements orbital.moon.math.Big {
        private static final long serialVersionUID = -5801439569926611104L;
        private static final int ROUNDING_MODE = BigDecimal.ROUND_HALF_EVEN;
    
        /**
         * the real value (with machine-sized arbitrary-precision, only, of course).
         * @serial
         */
        private BigDecimal value;
        public Big(double v) {
            this(BigDecimal.valueOf(v));
        }
        public Big(BigDecimal v) {
            value = v;
        }
        public Big(Number v) {
	    if (v instanceof BigDecimal)
		value = (BigDecimal)v;
	    else if (v instanceof orbital.moon.math.Big) {
		if (v instanceof Big)
		    value = ((Big)v).value;
		else if (v instanceof AbstractInteger.Big)
		    value = new BigDecimal(((AbstractInteger.Big)v).getValue());
		else
		    throw new IllegalArgumentException("unknown arbitrary precision type " + v.getClass() + " " + v);
	    } else {
		assert !java.lang.Double.isNaN(v.doubleValue()) && !java.lang.Double.isInfinite(v.doubleValue()) : v + " should neither be NaN nor infinite";
		value = BigDecimal.valueOf(ArithmeticValuesImpl.doubleValueExact(v));
	    }
        }
    
        public Object clone() {
            return new Big(value);
        } 

	public boolean equals(Object v) {
	    if (v instanceof orbital.moon.math.Big) {
		if (v instanceof Big)
		    //@internal BigDecimal.equals is mincing with scales. Prefer comparaTo
		    return value.compareTo(((Big)v).value) == 0;
		else if (v instanceof AbstractInteger.Big)
		    //@internal BigDecimal.equals is mincing with scales. Prefer comparaTo
		    return value.compareTo(new BigDecimal(((AbstractInteger.Big)v).getValue())) == 0;
		else
		    throw new IllegalArgumentException("unknown arbitrary precision type " + v.getClass() + " " + v);
	    }
            return Operations.equal.apply(this, v);
	}
	public int compareTo(Object v) {
	    if (v instanceof orbital.moon.math.Big) {
		if (v instanceof Big)
		    return value.compareTo(((Big)v).value);
		else if (v instanceof AbstractInteger.Big)
		    return value.compareTo(new BigDecimal(((AbstractInteger.Big)v).getValue()));
		else
		    throw new IllegalArgumentException("unknown arbitrary precision type " + v.getClass() + " " + v);
	    }
            return ((Integer) Operations.compare.apply(this, v)).intValue();
	}

        BigDecimal getValue() {
	    return value;
	}
	
        public int intValue() {
            return value.intValueExact();
        } 
        public long longValue() {
            return value.longValueExact();
        } 
        public double doubleValue() {
            return value.doubleValue();
        } 
    
	public boolean isZero() {
	    return value.compareTo(BigDecimal.ZERO) == 0;
	} 
	public boolean isOne() {
	    return value.compareTo(BigDecimal.ONE) == 0;
	} 

	public Real norm() {
	    return new Big(value.abs());
	} 

        public Real add(Real b) {
            if (b instanceof Big)
                return new Big(value.add(((Big)b).value));
            else if (b instanceof Float || b instanceof Double)
                return new Big(value.add(BigDecimal.valueOf(b.doubleValue())));
            return (Real) Operations.plus.apply(this, b);
        }
        public Real subtract(Real b) {
            if (b instanceof Big)
                return new Big(value.subtract(((Big)b).value));
            else if (b instanceof Float || b instanceof Double)
                return new Big(value.subtract(BigDecimal.valueOf(b.doubleValue())));
            return (Real) Operations.subtract.apply(this, b);
        }
        public Arithmetic minus() {
            return new Big(value.negate());
        } 
        public Real multiply(Real b) {
            if (b instanceof Big)
                return new Big(value.multiply(((Big)b).value));
            else if (b instanceof Float || b instanceof Double)
                return new Big(value.multiply(BigDecimal.valueOf(b.doubleValue())));
            return (Real) Operations.times.apply(this, b);
        }
        public Real divide(Real b) {
            if (b instanceof Big)
                return new Big(value.divide(((Big)b).value, ROUNDING_MODE));
            else if (b instanceof Float || b instanceof Double)
                return new Big(value.divide(BigDecimal.valueOf(b.doubleValue()), ROUNDING_MODE));
            return (Real) Operations.divide.apply(this, b);
        }
        public Real power(Real b) {
	    if (b instanceof Integer) {
		return power((Integer)b);
	    }
	    Real bc = (Real) Values.getDefault().narrow(b);
	    if (bc instanceof Integer) {
		return power((Integer)bc);
	    } else {
		return Values.getDefault().valueOf(Math.pow(ArithmeticValuesImpl.doubleValueExact((Number)this), ArithmeticValuesImpl.doubleValueExact(b)));
	    }
        }
	private Real power(Integer b) {
	    try {
		return new Big(ArithmeticValuesImpl.intValueExact(b));
	    } catch(ArithmeticException ex) {
		throw new ArithmeticException("exponentation is possibly too big: " + this + " ^ " + b);
	    }
	}
        public Arithmetic inverse() {
            return one().divide(this);
        } 
    }
}
