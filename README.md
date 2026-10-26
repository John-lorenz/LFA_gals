# Interpretador de binários com GALS

Trabalho M2 de Linguagens Formais e Autômatos (UNIVALI).
Aluno: João Arthur dos Santos Lorenzoni.

## Requisitos

- Java 17 ou mais recente (`java -version` para conferir).

## Como rodar

Na pasta do projeto:

```bash
javac -encoding UTF-8 -d bin src/gals/*.java
java -cp bin gals.Main exemplos/enunciado.txt
```

Saída esperada:

```
1101
```

No Windows, os caminhos também podem ser escritos com `\` (`src\gals\*.java`).
Use sempre `-encoding UTF-8` no `javac`; sem ele, os acentos das mensagens saem quebrados.

Para digitar o programa direto no terminal, rode sem arquivo e encerre a entrada
(Ctrl+Z e Enter no Windows, Ctrl+D no Linux/Mac):

```bash
java -cp bin gals.Main
```

## Exemplos

| Arquivo | O que mostra |
|---|---|
| `exemplos/enunciado.txt` | O programa do enunciado |
| `exemplos/todas_operacoes.txt` | Soma, subtração, multiplicação, divisão, potência e log |
| `exemplos/erro_lexico.txt` | Erro léxico (dígito que não é binário) |
| `exemplos/erro_sintatico.txt` | Erro sintático (falta operando) |
| `exemplos/erro_negativo.txt` | Erro semântico (subtração com resultado negativo) |

## A linguagem

```
A = 10;
B = 11;
B = 111 + A * B;
Show ( B );
```

- Números só com 0 e 1, inteiros e sem sinal.
- `X = expressão;` atribui; `Show(expressão);` imprime o valor em binário.
- Operações: `+`, `-`, `*`, `/` (divisão inteira), `^` (potência) e `log(x)` (log na base 2, parte inteira).

## Arquivos

- `trabalho_m2.gals`: especificação léxica e sintática (SLR) para o GALS.
- `src/gals/Semantico.java`: o interpretador (ações semânticas).
- `src/gals/Main.java`: lê o programa e mostra os erros com linha e coluna.
- Os demais arquivos de `src/gals` foram gerados pelo GALS. O `Sintatico.java` foi
  ajustado para mostrar o que era esperado nos erros sintáticos; se o código for
  gerado de novo no GALS, guarde antes o `Sintatico.java` e o `Semantico.java`.
- `Trabalho_LFA_M2.pptx`: slides da apresentação.
