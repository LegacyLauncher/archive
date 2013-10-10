package joptsimple;

interface OptionSpecVisitor {
   void visit(NoArgumentOptionSpec var1);

   void visit(RequiredArgumentOptionSpec var1);

   void visit(OptionalArgumentOptionSpec var1);

   void visit(AlternativeLongOptionSpec var1);
}
