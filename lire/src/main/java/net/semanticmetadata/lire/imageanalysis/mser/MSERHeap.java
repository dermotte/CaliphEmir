package net.semanticmetadata.lire.imageanalysis.mser;

import java.util.Stack;

/**
 * Implements a HEAP for the Image pixels.
 *
 * User: Shotty
 * Date: 28.06.2010
 * Time: 23:43:50
 */
public class MSERHeap
{
    // bitmask
    boolean[] bitmask;
    // stack of available pixels for each level
    Stack<BoundaryPixel>[] stacks;

    // normally MSER has 256 levels (0-255)
    public MSERHeap(int levels)
    {
        bitmask = new boolean[levels];
        stacks = new Stack[levels];
        for (int i = 0; i < levels; i++)
        {
            // no pixels available at the beginning
            bitmask[i] = false;
            // every level has its own stack of pixels
            stacks[i] = new Stack<BoundaryPixel>();
        }
    }

    /**
     * Pop the next pixel of the smallest level.
     * Returns null if the heap is completely empty...
     *
     * @return the next BoundaryPixel
     */
    public BoundaryPixel pop()
    {
        for (int i = 0; i < bitmask.length; i++)
        {
            // if there are pixels in the level
            if (bitmask[i])
            {
                // pop the BoundaryPixel
                BoundaryPixel pix = stacks[i].pop();
                // update if stack for this level is now empty
                bitmask[i] = !stacks[i].empty();
                return pix;
            }
        }
        // no pixel in the heap
        return null;
    }

    /**#
     * Pushes a pixel in the heap of the given grey value
     *
     * @param e the pixel
     * @param greyValue the maxGreyValue (level) of the pixel
     */
    public void push(BoundaryPixel e, int greyValue)
    {
        // update if stack on this level is not empty anymore
        if (!bitmask[greyValue])
        {
            bitmask[greyValue] = true;
        }
        // put the pixel on the right stack
        stacks[greyValue].push(e);
    }
}
