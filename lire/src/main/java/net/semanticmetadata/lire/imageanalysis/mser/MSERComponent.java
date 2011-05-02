package net.semanticmetadata.lire.imageanalysis.mser;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Shotty
 * Date: 28.06.2010
 * Time: 22:51:05
 */
public class MSERComponent
{
    LinkedImagePoint head;
    LinkedImagePoint tail;
    MSERGrowthHistory history;

    // histories to link to new history (when the next is added)
    ArrayList<MSERGrowthHistory> historiesToLinkToParent = new ArrayList<MSERGrowthHistory>();

    int greyLevel;
    int size;

    public MSERComponent(int level)
    {
        head = null;
        tail = null;
        history = null;
        greyLevel = level;
        size = 0;
    }

    public void addPixel(BoundaryPixel pixel)
    {
        if (size == 0)
        {
            // first pixel in component
            head = new LinkedImagePoint(pixel.getPoint());
            tail = head;
        }
        else
        {
            /*
            System.out.println("Size:" + size);
            System.out.println("Head:" + head);
            System.out.println("Tail:" + tail);
            System.out.println("GreyLevel:" + greyLevel);
            System.out.println("History:" + history);
            */
            // update the circle
            LinkedImagePoint newLast = new LinkedImagePoint(pixel.getPoint());
            newLast.setPrev(tail);
            tail.setNext(newLast);
            tail = newLast;
        }
        size++;
    }

    public void mergeComponents(MSERComponent comp, int newGreyLevel)
    {
        // merge points
        if (comp.getSize() != 0)
        {
            if (this.size == 0)
            {
                head = comp.getHead();
                tail = head;
            }
            else
            {
                comp.getHead().setPrev(tail);
                tail.setNext(comp.getHead());
                tail = comp.getTail();
            }
            this.size+= comp.getSize();
        }

        // winner is always THIS component, not the given one
        // take the historiesToLinkToParents and the current history of the given component
        if (comp.getHistory() != null)
        {
            historiesToLinkToParent.add(comp.getHistory());
        }
        if (comp.getHistoriesToLinkToParent().size() > 0)
        {
            historiesToLinkToParent.addAll(comp.getHistoriesToLinkToParent());
        }

        setGreyLevel(newGreyLevel);
    }

    /**
     * My History implementation
     */
    public void addHistory()
    {
        // add history of greyLevel
        MSERGrowthHistory newHist = new MSERGrowthHistory(size, greyLevel, head);
        if (history != null)
        {
            history.parent = newHist;
        }
        for (MSERGrowthHistory toAdd : historiesToLinkToParent)
        {
            toAdd.parent = newHist;
        }
        // clear after setting the parent
        historiesToLinkToParent.clear();

        history = newHist;
    }

    public MSERGrowthHistory getHistory()
    {
        return history;
    }

    public int getPastSize()
    {
        if (history != null)
        {
            return history.getSize();
        }
        else
        {
            return 0;
        }
    }

    public void setGreyLevel(int currentGreyLevel)
    {
        greyLevel = currentGreyLevel;
    }

    public int getGreyLevel()
    {
        return greyLevel;
    }

    public int getSize()
    {
        return size;
    }

    public LinkedImagePoint getHead()
    {
        return head;
    }

    public LinkedImagePoint getTail()
    {
        return tail;
    }

    public ArrayList<MSERGrowthHistory> getHistoriesToLinkToParent()
    {
        return historiesToLinkToParent;
    }
}
