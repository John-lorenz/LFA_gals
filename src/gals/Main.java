package gals;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Uso:
 *   java -cp bin gals.Main programa.txt   executa o arquivo inteiro
 *   java -cp bin gals.Main                modo interativo: cada comando terminado em ';'
 *                                         executa ao apertar Enter; uma linha vazia executa
 *                                         o que foi digitado sem ';'; "sair" encerra
 */
public class Main
{
    public static void main(String[] args) throws IOException
    {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, charsetSaida("stdout")));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, charsetSaida("stderr")));

        if (args.length > 0)
            executarArquivo(args[0]);
        else
            modoInterativo();
    }

    private static void executarArquivo(String caminho) throws IOException
    {
        String fonte;
        try
        {
            fonte = Files.readString(Path.of(caminho), StandardCharsets.UTF_8);
        }
        catch (NoSuchFileException e)
        {
            System.err.println("Arquivo não encontrado: " + caminho);
            System.exit(1);
            return;
        }

        String erro = executar(fonte, new Semantico());
        if (erro != null)
        {
            System.err.println(erro);
            System.exit(1);
        }
    }

    private static void modoInterativo() throws IOException
    {
        boolean terminal = System.console() != null;
        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Semantico semantico = new Semantico();
        StringBuilder comando = new StringBuilder();

        if (terminal)
        {
            System.out.println("Interpretador de binários. Termine cada comando com ';' (ex.: A = 101; Show(A);).");
            System.out.println("Se esquecer o ';', aperte Enter numa linha vazia. Digite sair para encerrar.");
        }

        while (true)
        {
            if (terminal)
            {
                System.out.print(comando.length() == 0 ? "> " : "... ");
                System.out.flush();
            }

            String linha = entrada.readLine();
            if (linha == null)
                break;
            if (comando.length() == 0 && linha.trim().equalsIgnoreCase("sair"))
                return;

            boolean linhaVazia = linha.trim().isEmpty();
            comando.append(linha).append('\n');
            String texto = comando.toString().trim();
            if (texto.isEmpty())
            {
                comando.setLength(0);
                continue;
            }
            if (!texto.endsWith(";") && !linhaVazia)
                continue;

            executarInterativo(comando.toString(), semantico);
            comando.setLength(0);
        }

        if (comando.toString().trim().length() > 0)
            executarInterativo(comando.toString(), semantico);
    }

    private static void executarInterativo(String fonte, Semantico semantico)
    {
        String erro = executar(fonte, semantico);
        if (erro != null)
        {
            System.err.println(erro);
            semantico.limparPilha();
        }
    }

    /** Executa o código e devolve a mensagem de erro, ou null se deu tudo certo. */
    private static String executar(String fonte, Semantico semantico)
    {
        try
        {
            new Sintatico().parse(new Lexico(new StringReader(fonte)), semantico);
            return null;
        }
        catch (LexicalError e)
        {
            String msg = e.getMessage();
            int p = e.getPosition();
            if (p >= 0 && p < fonte.length())
                msg += ": '" + fonte.charAt(p) + "'";
            return formatar("Erro léxico", msg, p, fonte);
        }
        catch (SyntacticError e)
        {
            return formatar("Erro sintático", e.getMessage(), e.getPosition(), fonte);
        }
        catch (SemanticError e)
        {
            return formatar("Erro semântico", e.getMessage(), e.getPosition(), fonte);
        }
    }

    private static String formatar(String tipo, String msg, int posicao, String fonte)
    {
        int pos = Math.max(0, Math.min(posicao, fonte.length()));
        int linha = 1, coluna = 1;
        for (int i = 0; i < pos; i++)
        {
            if (fonte.charAt(i) == '\n') { linha++; coluna = 1; }
            else coluna++;
        }
        return tipo + " na linha " + linha + ", coluna " + coluna + ": " + msg;
    }

    /**
     * Usa a codificação do console (ex.: cp850 no Windows); se a saída for redirecionada, usa UTF-8.
     * Java 17 só define sun.stdout.encoding quando a saída é o console. Java 19+ sempre define
     * stdout.encoding, mas, com a saída redirecionada, ela é igual à native.encoding.
     */
    private static Charset charsetSaida(String fluxo)
    {
        String nome = System.getProperty("sun." + fluxo + ".encoding");
        if (nome == null)
        {
            String atual = System.getProperty(fluxo + ".encoding");
            if (atual != null && !atual.equalsIgnoreCase(System.getProperty("native.encoding")))
                nome = atual;
        }
        if (nome != null && Charset.isSupported(nome))
            return Charset.forName(nome);
        return StandardCharsets.UTF_8;
    }
}
