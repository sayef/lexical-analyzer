# Lexical Analyzer
##Simple Lexical Analyzer
###How it works:
1. Convert infix regular expression to postfix regular expression
2. Find Nullablity, Firstpos, Lastpos, Followpos
3. Make DFA table from these.
4. Now Find whether any string gets accepted by these DFAs.
5. Now check the longest sequence accepted by the DFAs
That's all about algorithm.

###How to run my code: 
1. In the Assignment_1.pdf, there is given what to do at a glance.
2. Add my 2 sources to your project.
3. There is a file input.l where actually the regular expression are written. 
    (there is a absolute path for the file in the code, change it for your own use)
4. Our program will analyze them and will take input from console to test whether your 
    given string matches any regular expression
5. Initiall we handled only two characters 'a' and 'b', but you can convert it easily for any characters 
    (see comments to understand easily)
###Example strings as input (not regular expressions; regular expressions are given in input.l; offcourse you can also change/add/remove for further checking):

1. aab
2. abab
3. aabbb
