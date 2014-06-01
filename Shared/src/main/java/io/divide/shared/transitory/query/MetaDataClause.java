package io.divide.shared.transitory.query;

import io.divide.shared.transitory.TransientObject;

/**
 * Created by williamwebb on 11/13/13.
 */
public class MetaDataClause extends Clause
{
    protected MetaDataClause(String before, OPERAND operand, String after) {
        super(TransientObject.META_DATA + "." + before, operand, after);
    }

    protected MetaDataClause(OPERAND.Conditional conditional,String before, OPERAND operand, String after) {
        super(conditional,TransientObject.META_DATA + "." + before, operand, after);
    }
}
