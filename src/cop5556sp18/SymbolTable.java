package cop5556sp18;

import cop5556sp18.AST.Declaration;

import java.util.HashMap;
import java.util.Stack;

public class SymbolTable {

    int currentScope;
    int nextScope;
    Stack<Integer> scopeStack;
    HashMap<String, TableEntry> symbolTable;

    public SymbolTable() {
        scopeStack = new Stack<>();
        symbolTable = new HashMap<>();
        nextScope = 0;
        enterScope();
    }

    public void enterScope() {
        currentScope = nextScope++;
        scopeStack.push(currentScope);
        //System.out.println("push : currentScope -> " + currentScope + " & nextScope-> " + nextScope);
    }

    public void leaveScope() {
        int i = scopeStack.pop();
        currentScope = scopeStack.peek();
        //System.out.println("leave : Leftscope -> " + i + " & currentScope -> " + currentScope + " & nextScope-> " + nextScope);
    }

    public boolean insert(String ident, Declaration dec) {
        if (symbolTable.containsKey(ident)) {
            TableEntry oldEntry = symbolTable.get(ident);
            while (oldEntry != null && oldEntry.scope > scopeStack.peek())
                oldEntry = oldEntry.next;
            if (oldEntry == null || currentScope != oldEntry.scope) {
                TableEntry newEntry = new TableEntry(currentScope, dec, oldEntry);
                symbolTable.put(ident, newEntry);
            } else
                return false;
        } else {
            symbolTable.put(ident, new TableEntry(currentScope, dec, null));
        }
        return true;
    }

    public Declaration lookup(String ident) {
        if (symbolTable.containsKey(ident)) {
            TableEntry symEntry = symbolTable.get(ident);
            while (symEntry != null) {
                if (symEntry.scope <= scopeStack.peek() && scopeStack.search(symEntry.scope) != -1) {
                    return symEntry.dec;
                } else
                    symEntry = symEntry.next;
            }
        }
        return null;
    }

    class TableEntry {
        int scope;
        Declaration dec;
        TableEntry next;
        Scanner.Kind type;

        TableEntry(int scope, Declaration dec, TableEntry next) {
            this.scope = scope;
            this.dec = dec;
            this.next = next;
            this.type = dec.type;
        }
    }

}
