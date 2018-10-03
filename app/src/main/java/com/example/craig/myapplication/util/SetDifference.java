package com.example.craig.myapplication.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SetDifference
{
    private SetDifference() {}

    public static final <T> List<T> getAdditions(Collection<T> a, Collection<T> b)
    {
        List<T> additions = new ArrayList<T>();
        List ax = new ArrayList<T>(a);

        for(T item: b)
        {
            boolean isAddition = true;

            if(!ax.contains(item))
            {
                additions.add(item);
            }
            else
            {
                ax.remove(item);
            }
        }

        return additions;
    }

    public static final <T> List<T> getRemovals(Collection<T> a, Collection<T> b)
    {
        return getAdditions(b,a);
    }
}
