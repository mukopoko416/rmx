/* Generated By:JJTree: Do not edit this line. ASTDomainArg.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=BaseNode,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package logic.parse.SOP;

public
class ASTDomainArg extends SimpleNode {
  public ASTDomainArg(int id) {
    super(id);
  }

  public ASTDomainArg(parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(parserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=77abc4c4c5e49b195dd72d3d7feb974e (do not edit this line) */
