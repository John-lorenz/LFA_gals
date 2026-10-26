package gals;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Interpretador da linguagem de números binários inteiros sem sinal.
 *
 * Ações semânticas:
 *  #1  guarda o nome da variável que vai receber a atribuição
 *  #2  desempilha o resultado da expressão e grava na variável
 *  #3  desempilha o resultado da expressão e exibe na tela (Show)
 *  #4  soma            (a + b)
 *  #5  subtração       (a - b)   erro se o resultado for negativo
 *  #6  multiplicação   (a * b)
 *  #7  divisão inteira (a / b)   erro se b = 0
 *  #8  exponenciação   (a ^ b)
 *  #9  empilha a constante binária lida
 *  #10 empilha o valor da variável lida
 *  #11 logaritmo na base 2, parte inteira: log(x)   erro se x = 0
 *  #12 resto da divisão (a % b)   erro se b = 0
 */
public class Semantico implements Constants
{
    private static final int MAIOR_EXPOENTE = 100_000;

    private final Map<String, BigInteger> variaveis = new HashMap<>();
    private final Deque<BigInteger> pilha = new ArrayDeque<>();
    private String variavelDestino;

    /** Descarta valores que sobraram na pilha quando um comando para no meio por causa de um erro. */
    public void limparPilha()
    {
        pilha.clear();
    }

    public void executeAction(int action, Token token) throws SemanticError
    {
        BigInteger a, b;

        switch (action)
        {
            case 1:
                variavelDestino = token.getLexeme();
                break;

            case 2:
                variaveis.put(variavelDestino, pilha.pop());
                break;

            case 3:
                System.out.println(pilha.pop().toString(2));
                break;

            case 4:
                b = pilha.pop();
                a = pilha.pop();
                pilha.push(a.add(b));
                break;

            case 5:
                b = pilha.pop();
                a = pilha.pop();
                if (a.compareTo(b) < 0)
                    throw new SemanticError("Subtração com resultado negativo ("
                            + a.toString(2) + " - " + b.toString(2)
                            + "): a linguagem só trabalha com binários sem sinal", token.getPosition());
                pilha.push(a.subtract(b));
                break;

            case 6:
                b = pilha.pop();
                a = pilha.pop();
                pilha.push(a.multiply(b));
                break;

            case 7:
                b = pilha.pop();
                a = pilha.pop();
                if (b.signum() == 0)
                    throw new SemanticError("Divisão por zero", token.getPosition());
                pilha.push(a.divide(b));
                break;

            case 8:
                b = pilha.pop();
                a = pilha.pop();
                if (b.compareTo(BigInteger.valueOf(MAIOR_EXPOENTE)) > 0)
                    throw new SemanticError("Expoente grande demais: " + b.toString(2), token.getPosition());
                pilha.push(a.pow(b.intValue()));
                break;

            case 9:
                pilha.push(new BigInteger(token.getLexeme(), 2));
                break;

            case 10:
                BigInteger valor = variaveis.get(token.getLexeme());
                if (valor == null)
                    throw new SemanticError("Variável não definida: " + token.getLexeme(), token.getPosition());
                pilha.push(valor);
                break;

            case 11:
                a = pilha.pop();
                if (a.signum() == 0)
                    throw new SemanticError("log(0) é indefinido", token.getPosition());
                pilha.push(BigInteger.valueOf(a.bitLength() - 1));
                break;

            case 12:
                b = pilha.pop();
                a = pilha.pop();
                if (b.signum() == 0)
                    throw new SemanticError("Resto de divisão por zero", token.getPosition());
                pilha.push(a.mod(b));
                break;
        }
    }
}
