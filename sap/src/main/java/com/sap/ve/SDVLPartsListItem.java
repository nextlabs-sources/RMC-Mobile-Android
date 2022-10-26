/*
 (C) 2015 SAP SE or an SAP affiliate company. All rights reserved.
*/
package com.sap.ve;

import java.util.ArrayList;

public class SDVLPartsListItem extends ListSelectionItem
{
    public String partName;
    public ArrayList<Long> nodesList = new ArrayList<Long>();

    @Override
    public String toString()
    {
        return partName;
    }
}
