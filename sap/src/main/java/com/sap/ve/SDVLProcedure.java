/*
 (C) 2015 SAP SE or an SAP affiliate company. All rights reserved.
*/
package com.sap.ve;

import java.util.ArrayList;

public class SDVLProcedure extends ListSelectionItem
{
    public int id;
    public String name;
    public ArrayList<SDVLStep> steps;

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        return (o instanceof SDVLProcedure) && (this.id == ((SDVLProcedure)o).id);
    }
}
