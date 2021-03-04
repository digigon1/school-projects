#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

#define MAX_LINE     1000

char line[MAX_LINE] ;
int curr ;

void error(char *mesg) {
    fprintf(stderr, "ERROR: %s\n", mesg) ;
    exit(1) ;
}

bool isDigit(char c) {
    return '0' <= c && c <= '9' ;
}

int D(void) {
    if( isDigit(line[curr]) ) {
        int val = line[curr] - '0' ;
        curr++ ;
        return val ;
    }
    else error("Expected digit") ;
}

int Z(int val) {
    if( isDigit(line[curr]) )
        return Z(val * 10 + D()) ;
    else
        return val ;
}

int I(void) {
    return Z(D()) ;
}

// Faltam T, Y, F

int E();

int F(){
    if(line[curr] == '('){
        curr++;
        int e = E();
        if(line[curr] != ')')
            error("Expected )");
        curr++;
        return e;
    } else if (isDigit(line[curr])){
        return I();
    } else {
        error("Expected expression or number");
    }
}

int Y(int val){
    if(line[curr] == '*'){
        curr++;
        return Y(val * F());
    } else 
        return val;
}

int T(){
    return Y(F());
}

// all done

int X(int val) {
    if( line[curr] == '+' ) {
        curr++ ;
        return X(val + T()) ;
    }
    else
        return val ;
}

int E(void) {
    return X(T()) ;
}

int S(void) {
    int val = E() ;
    if( line[curr] != '\0' )
        error("Texto a mais depois do final da expressão") ;
    return val ;
}

int main() {
    for(;;) {
        printf("> ") ;
        fflush(stdout) ;
        fgets(line, MAX_LINE, stdin) ;   // faça "man fgets" para aprender...
        line[strlen(line)-1] = '\0' ;
        curr = 0 ;
        printf("%s = %d\n", line, S()) ;
    }
}