/**
 * @(#)GeneralSearch.java 1.0 2000/09/17 Andre Platzer
 *
 * Copyright (c) 2000 Andre Platzer. All Rights Reserved.
 */

package orbital.algorithm.template;
import orbital.algorithm.template.GeneralSearchProblem.Option;

import java.util.Collection;
import java.io.Serializable;
import java.util.Iterator;

import java.util.NoSuchElementException;
import java.util.LinkedList;

/**
 * Abstract general search algorithm scheme.
 * <p>
 * The various subclasses implement different search strategies, but follow this coherent framework.</p>
 * <p>
 * Apart from the reference to the problem to solve, GeneralSearch implementations are usually stateless.</p>
 *
 * @version 1.0, 2000/09/17
 * @author  Andr&eacute; Platzer
 * @see GeneralSearchProblem
 * @see <a href="http://www.ldc.usb.ve/~hector">Hector Geffner. Modelling and Problem Solving</a>
 * @see <a href="{@docRoot}/DesignPatterns/Strategy.html">Strategy</a>
 * @see <a href="doc-files/AlgorithmicTable.gif">Table of Algorithms in Comparison</a>
 * @todo introduce BidirectionalSearch and a BidirectionalSearchProblem (either extending GeneralSearchProblem complementing expand and getInitialState with methods working backwards from the goal, or feed BidirectionalSearch with two complementary GeneralSearchProblem objects). They work like Backchaining from the goal instead of "usual" Forward chaining from the initial state.
 * @todo introduce TabuSearch
 * @todo once we have decided once and for all against expand-Collections we can get rid of createCollection(), select(Collection), add(Collection,Collection), and search(Collection) in our subclasses.
 */
public abstract class GeneralSearch implements AlgorithmicTemplate/*<GeneralSearchProblem,Object>*/, Serializable {
    /**
     * The search problem to solve.
     * @serial
     */
    private GeneralSearchProblem problem = null;
	
    // get/set properties
	
    /**
     * Get the current problem.
     * @pre true
     * @return the problem specified in the last call to solve,
     *  or <code>null</code> if there is no current problem (since solve has not yet been called etc.).
     */
    protected final GeneralSearchProblem getProblem() {
	return problem;
    }
    /**
     * Set the current problem.
     */
    private final void setProblem(GeneralSearchProblem newproblem) {
	this.problem = newproblem;
    }
    
    /**
     * Whether this search algorithm is optimal.
     * <p>
     * If a search algorithm is not optimal, i.e. it might find solutions that are
     * sub optimal only, then it can only reliably find solutions, not best solutions.
     * However, those solutions still provide an upper bound to the optimal solution.
     * </p>
     * @return whether this search algorithm is optimal, i.e. whether solutions are guaranteed to be optimal.
     * @pre true
     * @post RES == OLD(RES) && OLD(this) == this
     */
    public abstract boolean isOptimal();
    
    // solution operations for search problems
    
    /**
     * Solves a general search problem.
     * @pre p instanceof GeneralSearchProblem.
     * @throws ClassCastException if p is not an instance of GeneralSearchProblem.
     * @see #solve(GeneralSearchProblem)
     */
    public final Object solve(AlgorithmicProblem p) {
    	return solve((GeneralSearchProblem)p);
    }
    
    /**
     * Solves a general search problem.
     * <p>
     * This method does not need to be overwritten.
     * Overwrite {@link #solveImpl(GeneralSearchProblem)}, instead.</p>
     * @return the solution found (represented as an option with solution state, final action and accumulated cost),
     *  or <code>null</code> if no solution was found at all.
     * @pre true
     * @post solution == null &or; p.isSolution(solution)
     * @see <a href="{@docRoot}/DesignPatterns/TemplateMethod.html">Template Method</a>
     * @see #solveImpl(GeneralSearchProblem)
     */
    public final Option solve(GeneralSearchProblem/*<S,A>*/ p) {
    	setProblem(p);
	Option solution;
	//    	if (false) {
	//    		Collection/*<Option>*/ nodes = createCollection();
	//    		nodes.add(new Option(getProblem().getInitialState()));
	//    		solution = search(nodes);
	//    	} else
	solution = solveImpl(p);
    	assert solution == null || p.isSolution(solution) : "post";
    	return solution;
    }

    // central primitive operations
    
    /**
     * Implements the solution policy.
     * <p>
     * Like the default solution policies, this implementation will
     * <ol>
     *   <li>
     *     Fetch a traversal order policy by calling {@link #createTraversal(GeneralSearchProblem)}.
     *   </li>
     *   <li>
     *     Then call {@link #search(Iterator)} to search through the state space in the traversal order.
     *   </li>
     * </ol>
     * However, sophisticated search algorithms may want to change that policy and iterate the
     * above process, resulting in a sequence of calls to those methods.
     * They may do so by by overwriting this method.
     * </p>
     * @return the solution found by {@link #search(Iterator)}.
     * @pre problem == getProblem()
     * @post solution == null &or; p.isSolution(solution)
     * @see #search(Iterator)
     * @xxx what <em>exactly</em> is the conceptual difference between solveImpl(GeneralSearchProblem) and search(Iterator). Perhaps we could get rid of this method?
     */
    protected Option solveImpl(GeneralSearchProblem/*<S,A>*/ problem) {
	return search(createTraversal(problem));
    }

    /**
     * Define a traversal order by creating an iterator for the problem's state space.
     * @param problem the problem whose state space to create a traversal iterator for.
     * @return an iterator over the options of the problem's state space thereby encapsulating
     *  and hiding the traversal order.
     * @attribute secret traversal order
     * @see <a href="{@docRoot}/DesignPatterns/FactoryMethod.html">Factory Method</a>
     * @see GeneralSearch.OptionIterator
     */
    protected abstract Iterator/*<Option<S,A>>*/ createTraversal(GeneralSearchProblem/*<S,A>*/ problem);

    // central virtual methods

    /**
     * Run the general search algorithm scheme.
     * <p>
     * This method only needs to be overwritten to provide a completely different search scheme.
     * Usually, the default search algorithm scheme is sufficient.
     * </p>
     * @param nodes is the iterator over the nodes to visit (sometimes called open set)
     *  which determines the traversal order.
     * @return the solution found searching the state space via <var>nodes</var>.
     * @post solution == null &or; p.isSolution(solution)
     * @see <a href="{@docRoot}/DesignPatterns/TemplateMethod.html">Template Method</a>
     * @internal
     *  Implemented as an iterative unrolling of a right-linear tail-recursion.
     *  Otherwise it would be important to shallow-clone the nodes list
     *  for byvalue semantics of recursion call.</p>
     */
    protected Option search(Iterator/*<Option<S,A>>*/ nodes) {
	while (nodes.hasNext()) {
	    Option node = (Option) nodes.next();

	    if (getProblem().isSolution(node))
		return node;
    	}

    	// fail
    	return null;
    }

    /**
     * Abstract implementation base class for iterators determining the traversal order.
     * Concrete implementations define the traversal order for the state space by providing
     * an iterator over the options of a search graph (as induced by a {@link GeneralSearchProblem state-model}).
     * <p>
     * Although it is not required that option iterators extend this class, it usually comes as
     * a great relief since sub classes will not have to deal with too much details.
     * Full-blown option iterators only have to implement the abstract methods of this
     * class and determine the data collection implementation maintained.
     * </p>
     * <p>
     * Particularly, this iterator approach has the advantage of promising that an option node
     * will not be required again, when the next element has already been requested.
     * Those search algorithms that have a need to keep intermediate states for later comparison,
     * will need to remember the states themselves.
     * </p>
     *
     * @attribute secret traversal order
     * @invariant sub classes maintain a collection of nodes to select from
     * @version 0.8, 2001/08/01
     * @author  Andr&eacute; Platzer
     * @see <a href="{@docRoot}/DesignPatterns/Strategy.html">Strategy</a>
     */
    public static abstract class OptionIterator implements Iterator/*_<Option>_*/, Serializable {
    	/**
    	 * The search problem to solve.
    	 * @serial
    	 */
    	private final GeneralSearchProblem/*<S,A>*/ problem;
    	
	/**
	 * The last node selected by next().
	 * @serial
	 */
	private Option lastRet = null;

	/**
	 * Whether lastRet has already been expanded by hasNext().
	 * @serial
	 */
	private boolean						hasExpanded = false;
		
	/**
	 * Create a traversal iterator over the problem's state options.
	 * <p>
	 * Sub classes <em>must</em> add the {@link GeneralSearchProblem#getInitialState() initial state}
	 * to their internal collection of nodes, such that it will be the (single) element
	 * expanded first.</p>
	 * @param problem the problem whose options to iterate in an iterator specific order.
	 * @post must still add problem.getInitialState() to nodes, such that !isEmpty()
	 */
	protected OptionIterator(GeneralSearchProblem/*<S,A>*/ problem) {
	    this.problem = problem;
	}
		
    	/**
    	 * Get the current problem.
    	 * @pre true
    	 * @return the problem specified in the last call to solve.
    	 */
    	protected final GeneralSearchProblem/*<S,A>*/ getProblem() {
	    return problem;
    	}

        // central template methods

        /**
         * Returns <code>true</code> if this iterator's collection of nodes currently does not contain any elements.
         * @return <code>true</code> if this collection contains no elements.
         * @post RES == nodes.isEmpty()
         */
        protected abstract boolean isEmpty();

        /**
         * Selects an option to visit from nodes.
         * @return the selected option after <em>removing</em> it from nodes.
         * @pre !isEmpty()
         * @post OLD(nodes).contains(RES) && nodes == OLD(nodes) \ {RES}
         */
        protected abstract Option select();

        /**
         * Concatenate the new nodes and the old nodes.
         * Concatenates by some algorithm-dependant means.
         * @param newNodes the new nodes we apparently became aware of. (Might be modified by this method).
         * @return true if nodes changed as a result of the call.
         * @post nodes &sube; OLD(nodes) &cup; newNodes && RES = nodes&ne;OLD(nodes)
         */
        protected abstract boolean add(Iterator/*<Option>*/ newNodes);

        // interface Iterator implementation
        
    	/**
    	 * {@inheritDoc}
    	 * @pre true
    	 * @see <a href="{@docRoot}/DesignPatterns/TemplateMethod.html">Template Method</a>
    	 */
    	public boolean hasNext() {
	    if (!isEmpty())
		// we still have some nodes in petto
		return true;
	    else if (lastRet == null)
		// we have no more nodes, and none to expand as well
		return false;
	    else
		// we have no more nodes, but we might get some if we expand lastRet
		return expand();
    	}

    	/**
    	 * {@inheritDoc}
    	 * <p>
    	 * Will expand the last element returned, and select an option to visit.</p>
    	 * @pre hasNext()
    	 * @see <a href="{@docRoot}/DesignPatterns/TemplateMethod.html">Template Method</a>
    	 */
    	public Object/*_>Option<_*/ next() {
	    if (lastRet != null)
		expand();
	    if (isEmpty())
		throw new NoSuchElementException();
	    hasExpanded = false;
	    return lastRet = select();
    	}

    	/**
    	 * Expands lastRet.
    	 * <p>
    	 * Will only expand lastRet, if it has not already been expanded.
    	 * Particularly, if lastRet has already been expanded, this method will only return <code>false</code>.</p>
    	 * @pre lastRet != null
    	 * @return whether new nodes were added by this call to expand.
    	 */
    	private boolean expand() {
	    if (lastRet == null)
		throw new IllegalStateException("cannot expand without a node returned last!");
	    if (hasExpanded)
		return false;
	    Iterator/*<Option>*/ children = getProblem().expand(lastRet);
	    hasExpanded = true;
	    return add(children);
    	}

    	/**
    	 * Removes from the list of exandable nodes the last element returned by the iterator.
    	 * <p>
    	 * When calling this method, the last node that was returned by this iterator will be pruned
    	 * and not be expanded any further.
    	 * </p>
    	 * @throws UnsupportedOperationException if the <code>remove</code> operation is not supported by this Iterator.
	 * @throws IllegalStateException if the <code>next</code> method has not yet been called, or the <code>remove</code> method has already been called after the last call to the <code>next</code> method.
    	 */
    	public void remove() {
    	    if (lastRet == null)
		throw new IllegalStateException();
	    lastRet = null;
    	}
    }


    //    /**
    //     * Get a new instance of the implementation data structure.
    //     * <p>
    //     * Implementing methods could return special list implementations like stacks or queues.</p>
    //	 * @see <a href="{@docRoot}/DesignPatterns/FactoryMethod.html">Factory Method</a>
    //	 * @todo remove
    //     */
    //	protected Collection/*<Option>*/ createCollection() {
    //		return new LinkedList/*<Option>*/();
    //	}
	

    //    /**
    //     * Selects an option to visit from nodes.
    //     * @return the selected option after <em>removing</em> it from nodes.
    //     * @pre !nodes.isEmpty()
    //     * @post OLD(nodes).contains(RES) && nodes == OLD(nodes) \ {RES}
    //	 * @todo remove
    //     */
    //    protected abstract Option select(Collection/*<Option>*/ nodes);
    //    
    //    /**
    //     * Concatenate the new nodes and the old nodes.
    //     * Concatenates by some algorithm-dependant means.
    //     * @param newNodes the new nodes we apparently became aware of. (May be modified by this method).
    //     * @param oldNodes the old nodes we already knew of. (May be modified by this method).
    //     * @return a list of nodes to explore that contains oldNodes and newNodes.
    //     * @post RES &sube; oldNodes &cup; newNodes
    //	 * @todo remove
    //     */
    //    protected abstract Collection/*<Option>*/ add(Collection/*<Option>*/ newNodes, Collection/*<Option>*/ oldNodes);
    //	
    //    // central virtual methods
    //
    //	/**
    //	 * run the general search algorithm scheme.
    //	 * <p>
    //	 * Overwrite to provide a whole different search scheme.</p>
    //	 * @param nodes is the list of nodes to visit (sometimes called open set).
    //	 * @return the solution found searching the state space starting with nodes.
    //	 * @see <a href="{@docRoot}/DesignPatterns/TemplateMethod.html">Template Method</a>
    //	 * @todo remove
    //	 */
    //	protected Option search(Collection/*<Option>*/ nodes) {
    //		while (!nodes.isEmpty()) {
    //    		Option node = select(nodes);
    //
    //    		if (getProblem().isSolution(node))
    //    			return node;
    //    		Collection/*<Option>*/ children = orbital.util.Setops.asList(getProblem().expand(node));
    //    		nodes = add(children, nodes);
    //    	}
    //
    //    	// fail
    //    	return null;
    //	}
}


//@internal Obsolete stuff, can be removed
//    /**
//     * Get the accumulated cost of the solution.
//	 * @pre {@link #solve(GeneralSearchProblem)} has finished successfully
//     * @return the accumulated cost that lead to the solution last returned by solve.
//     * @see #solve(GeneralSearchProblem)
//     */
//	public double getCost() {
//		return cost;
//	}
//    
//	/**
//	 * The accumulated cost that lead to the solution.
//	 * @serial
//	 */
//	private double cost;
	
