/* Generated By:JJTree: Do not edit this line. ASTUnion.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=BaseNode,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package logic.parse.SOP;

public
class ASTUnion extends SimpleNode {
  public ASTUnion(int id) {
    super(id);
  }

  public ASTUnion(parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(parserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=71bea7ad0619dabdadf958b8c512c32c (do not edit this line) */