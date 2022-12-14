/*
 (C) 2015 SAP SE or an SAP affiliate company. All rights reserved.
*/
package com.sap.ve;

public class SDVLStep extends ListSelectionItem
{
    public long id;
    public String name;
    public String description;

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        return (o instanceof SDVLStep) && (this.id == ((SDVLStep)o).id);
    }
}
