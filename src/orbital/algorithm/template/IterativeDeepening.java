/**
 * @(#)IterativeDeepening.java 1.0 2000/09/20 Andre Platzer
 *
 * Copyright (c) 2000 Andre Platzer. All Rights Reserved.
 */

package orbital.algorithm.template;
import orbital.algorithm.template.GeneralSearchProblem.Option;

import orbital.logic.functor.Function;
import java.util.Iterator;

import orbital.math.functional.Functions;
import orbital.math.functional.Operations;
import orbital.math.Values;

/**
 * Iterative Deepening (ID). A blind search algorithm.
 * <p>
 * ID is complete, optimal, and
 * has a time complexity of O(b<sup>d</sup>) and a space complexity of O(b*d).</p>
 *
 * @version 1.0, 2000/09/20
 * @author  Andr&eacute; Platzer
 */
public class IterativeDeepening extends DepthFirstBoundingSearch {
    /**
     * Whether we have pruned a node during the last call to super.search.
     * @serial
     * @see IterativeDeepeningAStar#nextBound
     */
    private boolean havePruned;
    public IterativeDeepening() {
	// better solutions would already have appeared with earlier bounds
	setContinuedWhenFound(false);
    }
	
    /**
     * f(n) = g(n).
     */
    public Function getEvaluation() {
	return evaluation;
    }
    private transient Function evaluation = createEvaluation();
    private final Function createEvaluation() {
    	return new Function() {
    		public Object apply(Object a) {
		    GeneralSearchProblem.Option o = (GeneralSearchProblem.Option)a;
		    return Values.valueOf(o.getCost());
    		}
	    };
    }
    /**
     * Sustain transient variable initialization when deserializing.
     */
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	evaluation = createEvaluation();
    }

    /**
     * O(b<sup>d</sup>) where b is the branching factor and d the solution depth.
     * On average &asym;<span class="Formula">(b+1)<sup>d+1</sup>/(2*(b-1)<sup>2</sup>)</span>.
     */
    public orbital.math.functional.Function complexity() {
	return (orbital.math.functional.Function) Operations.power.apply(Values.symbol("b"),Functions.id);
    }
    public boolean isOptimal() {
    	return true;
    }

    protected boolean isOutOfBounds(Option node) {
	if (super.isOutOfBounds(node)) {
	    havePruned = true;
	    return true;
	} else
	    return false;
    }

    /**
     * Solve with bounds 0, 1, 2, ... until a solution is found.
     */
    protected GeneralSearchProblem.Option solveImpl(GeneralSearchProblem problem) {
	int i = 0;
	while (true) {
	    setBound(i++);
	    havePruned = false;
	    GeneralSearchProblem.Option solution = super.search(createTraversal(problem));
	    if (solution != null)
		return solution;
	    else if (!havePruned)
		// the search options have been fully exhausted and an increase of bound would not make sense anymore because the search has completely failed
		return null;
	}
    }

    protected Iterator createTraversal(GeneralSearchProblem problem) {
	return new DepthFirstSearch.OptionIterator(problem);
    }

    /**
     * Solve with bounds 0, 1, 2, ... until a solution is found.
     */
    //	protected GeneralSearchProblem.Option search(Collection nodes) {
    //		int i = 0;
    //		while (true) {
    //			setBound(i++);
    //			havePruned = false;
    //			Collection n = createCollection();
    //			n.addAll(nodes);
    //			GeneralSearchProblem.Option solution = super.search(n);
    //			if (solution != null)
    //				return solution;
    //			else if (!havePruned)
    //				// the search options have been fully exhausted and an increase of bound would not make sense anymore because the search has completely failed
    //				return null;
    //		}
    //	}
}
