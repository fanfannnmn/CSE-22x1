package components.waitingline;

import components.standard.Standard;

/**
 * First-in-first-out (FIFO) queue kernel component with primary methods. (Note:
 * by package-wide convention, all references are non-null.)
 *
 * @param <T>
 *            type of {@code QueueKernel} entries
 * @mathmodel type QueueKernel is modeled by string of T
 * @initially <pre>
 * default:
 *  ensures
 *   this = <>
 * </pre>
 * @iterator ~this.seen * ~this.unseen = this
 */
public interface WaitingLineKernel<T>
        extends Standard<WaitingLine<T>>, Iterable<T> {

    /**
     * Adds {@code x} to the end of {@code this}.
     *
     * @param x
     *            the entry to be added
     * @aliases reference {@code x}
     * @updates this
     * @requires x is not in entries of {@code this}
     * @ensures this = #this * <x>
     */
    void newCustomer(T x);

    /**
     * Removes and returns the entry at the front of {@code this}.
     *
     * @return the entry removed
     * @updates this
     * @requires this /= <>
     * @ensures #this = <attendCustomer> * this
     */
    T attendCustomer();

    /**
     * Reports length of {@code this}.
     *
     * @return the length of {@code this}
     * @ensures length = |this|
     */
    int length();

}
