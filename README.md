# Lexical Analyzer
##Simple Lexical Analyzer
###How it works:
1. Convert infix regular expression to postfix regular expression
2. Find Nullablity, Firstpos, Lastpos, Followpos
3. Make DFA table from these.
4. Now Find whether any string gets accepted by these DFAs.
5. Now check the longest sequence accepted by the DFAs.
    That's all about algorithm.

###How to run the application: 
1. In the Assignment_1.pdf, there is given what to do at a glance.
2. Add the sources to your project.
3. There is a file input.l where actually the regular expressions (regex) are written. 
    (there is a absolute path for the file in the code, change it for your own use)
4. This application will analyze them and will take input from console to test whether your 
    given string matches any regular expressions.
5. Characters A-Z, a-z and 0-9 are supported as string literals.
6. Supported regex symbols are, '|', '?', '+', '*', [a-zPQR] etc. '(', ')' encloure.

###Example of regular expressions:
1. [a-b]?at
2. ab*c?
3. Ab+Cd+
4. a?
5. (ab*)|c
6. abb
7. (a*|b*)*abba
8. (a*|b+)a
9. (a*b+)a

###Example strings as input:
    (not regular expressions; regular expressions are given in input.l; 
    offcourse you can also change/add/remove for further checking)
1. aab
2. abab
3. aabbb
4. bat
5. AbCd
6. AbbCddddd
