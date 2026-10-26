package gals;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Uso:
 *   java -cp bin gals.Main programa.txt
 *   java -cp bin gals.Main            (lê o programa da entrada padrão)
 */
public class Main
{
    public static void main(String[] args) throws IOException
    {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, charsetSaida("sun.stdout.encoding")));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, charsetSaida("sun.stderr.encoding")));

        String fonte;
        if (args.length > 0)
        {
            try
            {
                fonte = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);
            }
            catch (NoSuchFileException e)
            {
                System.err.println("Arquivo não encontrado: " + args[0]);
                System.exit(1);
                return;
            }
        }
        else
            fonte = lerTudo(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        Lexico lexico = new Lexico(new StringReader(fonte));
        Sintatico sintatico = new Sintatico();
        Semantico semantico = new Semantico();

        try
        {
            sintatico.parse(lexico, semantico);
        }
        catch (LexicalError e)
        {
            String msg = e.getMessage();
            int p = e.getPosition();
            if (p >= 0 && p < fonte.length())
                msg += ": '" + fonte.charAt(p) + "'";
            reportar("Erro léxico", msg, e.getPosition(), fonte);
        }
        catch (SyntacticError e)
        {
            reportar("Erro sintático", e.getMessage(), e.getPosition(), fonte);
        }
        catch (SemanticError e)
        {
            reportar("Erro semântico", e.getMessage(), e.getPosition(), fonte);
        }
    }

    private static void reportar(String tipo, String msg, int posicao, String fonte)
    {
        int pos = Math.max(0, Math.min(posicao, fonte.length()));
        int linha = 1, coluna = 1;
        for (int i = 0; i < pos; i++)
        {
            if (fonte.charAt(i) == '\n') { linha++; coluna = 1; }
            else coluna++;
        }
        System.err.println(tipo + " na linha " + linha + ", coluna " + coluna + ": " + msg);
        System.exit(1);
    }

    /** Usa a codificação do console (ex.: cp850 no Windows); se a saída for redirecionada, usa UTF-8. */
    private static Charset charsetSaida(String propriedade)
    {
        String nome = System.getProperty(propriedade);
        if (nome != null && Charset.isSupported(nome))
            return Charset.forName(nome);
        return StandardCharsets.UTF_8;
    }

    private static String lerTudo(Reader r) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        int n;
        while ((n = r.read(buf)) != -1)
            sb.append(buf, 0, n);
        return sb.toString();
    }
}