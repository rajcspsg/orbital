/**
 * @(#)DynamicProgramming.java 1.0 2000/07/31 Andre Platzer
 * 
 * Copyright (c) 2000 Andre Platzer. All Rights Reserved.
 */

package orbital.algorithm.template;

import orbital.math.functional.Function;

import orbital.math.functional.Functions;
import orbital.math.Values;

/**
 * Framework (template class) for Dynamic Programming Algorithms.
 * <p>
 * bottom-up technique.
 * <ul>
 * <li> A table of all sub-instance results is constructed. </li>
 * <li> The entries corresponding to the smallest sub-instances are initiated at the
 * start of the algorithm. </li>
 * <li> The remaining entries are filled in following a precise order (that corresponds
 * to increasing sub-instance size) using only those entries that have already
 * been computed. </li>
 * <li> Each entry is calculated exactly once. </li>
 * <li> The final value computed is the solution to the initial problem instance. </li>
 * <li> Implementation is by iteration (never by recursion, even though the analysis of
 * a problem may naturally suggest a recursive solution). </li>
 * </ul>
 * </p>
 * 
 * @version 1.0, 2000/07/31
 * @author  Andr&eacute; Platzer
 * @see DynamicProgrammingProblem
 * @see Greedy
 * @see "R. E. Bellman. Dynamic Programming. Princeton Universit Press, Princeton, NJ, 1957."
 * @todo could introduce an implicit form of DP that uses recursion to a base case with value reuse. However, explicit DP has a dramatic advantage in stack space consumption
 */
public
class DynamicProgramming implements AlgorithmicTemplate {
	public Object solve(AlgorithmicProblem p) {
		return solve((DynamicProgrammingProblem) p);
	} 

	/**
	 * solves by dynamic programming.
	 * @invariant partialSolutions is a (possibly multidimensional) array of partial solutions.
	 * @post solution is a merge of partial solutions.
	 * @return the solution object as merged from the partial problems.
	 */
	public Object solve(DynamicProgrammingProblem p) {
		partialSolutions = p.getInitialPartialSolutions();
		return solveByDynamicProgramming(p);
	} 

	/**
	 * O(n<sup>2</sup>)
	 */
	public Function complexity() {
		return Functions.pow(Values.valueOf(2));
	} 

	public Function spaceComplexity() {
		//TODO: assure
		return complexity();
	} 

	/**
	 * a (possibly multidimensional) array containing the partial solutions already solved, or null.
	 */
	private Object[] partialSolutions;

	private final Object solveByDynamicProgramming(DynamicProgrammingProblem p) {
		while (!p.isSolution(partialSolutions)) {

			// the next part we divided the problem into
			int[]  part = p.nextPart();

			// solve part
			Object psol = p.solve(part, partialSolutions);

			// memorize this partial solution
			setSolutionPart(part, partialSolutions, psol);
		} 

		// merge all partial solutions to the solution
		return p.merge(partialSolutions);
	} 
    
    // Convenience utilities methods
	
	/**
	 * Get the element in the (possibly multi-dimensional) array partialSolutions specified by the part specification.
	 * @pre partSpecification.length is not lower than the number of dimensions for partialSolutions.
	 * @return partialSolutions[partSpecification[0]][partSpecification[1]]...[partSpecification[partSpecification.length-1]] .
	 */
	public static Object getSolutionPart(int[] partSpecification, Object[] partialSolutions) {
		Object o = partialSolutions;
		for (int i = 0; i < partSpecification.length; i++)
			o = ((Object[]) o)[partSpecification[i]];
		return o;
	} 

	public static void setSolutionPart(int[] partSpecification, Object[] partialSolutions, Object value) {
		Object[] a = partialSolutions;
		for (int i = 0; i < partSpecification.length - 1; i++)
			a = (Object[]) a[partSpecification[i]];
		a[partSpecification[partSpecification.length - 1]] = value;
	} 
}
