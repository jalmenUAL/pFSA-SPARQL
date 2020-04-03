package fsaSPARQL;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp;
import com.hp.hpl.jena.sparql.function.FunctionBase1;
import com.hp.hpl.jena.sparql.function.FunctionBase2;
import com.hp.hpl.jena.sparql.function.FunctionBase3;
import com.hp.hpl.jena.sparql.function.FunctionBase4;

public class WMEAN extends FunctionBase3
{
public WMEAN() { super() ; }

@Override
public NodeValue exec(NodeValue weight, NodeValue truth1, NodeValue truth2)
{ return XSDFuncOp.numAdd(XSDFuncOp.numMultiply(truth1, weight),XSDFuncOp.numMultiply(truth2,XSDFuncOp.numSubtract(NodeValue.makeInteger(1), weight))) ; }
}
