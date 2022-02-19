package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.util.ElementScanner6;


class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<Token>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",             TokenType.AND);
        keywords.put("class",           TokenType.CLASS);
        keywords.put("else",            TokenType.ELSE);
        keywords.put("false",           TokenType.FALSE);
        keywords.put("for",             TokenType.FOR);
        keywords.put("fun",             TokenType.FUN);
        keywords.put("if",              TokenType.IF);
        keywords.put("nil",             TokenType.NIL);
        keywords.put("or",              TokenType.OR);
        keywords.put("print",           TokenType.PRINT);
        keywords.put("return",          TokenType.RETURN);
        keywords.put("super",           TokenType.SUPER);
        keywords.put("this",            TokenType.THIS);
        keywords.put("true",            TokenType.TRUE);
        keywords.put("var",             TokenType.VAR);
        keywords.put("while",           TokenType.WHILE);
    }

    
    Scanner(String source)
    {
        this.source = source;
    }

    List<Token> scanTokens() 
    {
        while(!isAtEnd())
        {
            this.start = this.current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, this.line));
        return tokens;
    }

    private void scanToken()
    {
        char c = advance();
        switch(c)
        {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if (match('/'))
                {
                    while (peek() != '\n' && !isAtEnd())
                    {
                        advance();
                    }
                }
                else
                {
                    addToken(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // ignore whitespace.
                break;
            case '\n':
                this.line++;

            case '"': string(); break;

            default:
                if (isDigit(c))
                {
                    number();
                }
                else if (isAlpha(c))
                {
                    identifier();
                }
                else
                {
                    Lox.error(this.line, "Unexpected character: '" + c + "'");
                }
                
        }
    }

    private void identifier()
    {
        while (isAlphaNumeric(peek()))
        {
            advance();
        }

        String identifier = source.substring(this.start, this.current);
        TokenType type = keywords.get(identifier);
        if (type == null)
        {
            type = TokenType.IDENTIFIER;
        }

        addToken(type);
    }

    private void number()
    {
        while (isDigit(peek()))
        {
            advance();
        }
        if (peek() == '.' && isDigit(peekNext()))
        {
            advance();
            while(isDigit(peek()))
            {
                advance();
            }
        }

        addToken(TokenType.NUMBER, 
            Double.parseDouble(source.substring(this.start, this.current)));
    }

    private void string() 
    {
        while (peek() != '"' && !isAtEnd())
        {
            if (peek() == '\n')
            {
                this.line++;
            }
            advance();
        }

        if (isAtEnd())
        {
            Lox.error(this.line, "Unterminated string");
        }

        advance();      // Closing '"'
        String value = source.substring(this.start + 1, this.current - 1);
        // If we wanted escape characters, we would resolve them here.
        addToken(TokenType.STRING, value);
    }

    private boolean match(char expected)
    {
        if (isAtEnd()) 
        {
            return false;
        }
        if (source.charAt(this.current) != expected)
        {
            return false;
        }
        ++this.current;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(this.current);
    }

    private char peekNext()
    {
        if (this.current + 1 >= source.length()) 
        {
            return '\0';
        }
        return source.charAt(this.current + 1);
    }

    private boolean isAlphaNumeric(char c)
    {
        return isDigit(c) || isAlpha(c);
    }

    private boolean isAlpha(char c)
    {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c == '_');
    }

    private boolean isDigit(char c)
    {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd()
    {
        return this.current >= this.source.length();
    }

    private char advance()
    {
        char c = this.source.charAt(this.current);
        ++this.current;
        return c;
    }

    private void addToken(TokenType type)
    {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal)
    {
        String text = source.substring(this.start, this.current);
        tokens.add(new Token(type, text, literal, line));
    }
}
