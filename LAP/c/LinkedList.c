#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include "LinkedList.h"

static List newNode(Data val, List next)
{
    List n = malloc(sizeof(Node));
    if( n == NULL )
        return NULL;
    n->data = val;
    n->next = next;
    return n;
}

List listMakeRange(Data a, Data b)
{  // TECNICA ESSENCIAL: Ir fazendo crescer a lista no ultimo no'.
    if( a > b )
        return NULL;
    double i;
    List l = newNode(a, NULL), last = l;
    for( i = a + 1 ; i <= b ; i++ )
        last = last->next = newNode(i, NULL);
    return l;
}

/* Outra maneira, mais palavrosa, de escrever a funcao anterior:

List listMakeRange(Data a, Data b)
{
    if( a > b )
        return NULL;
    double i;
    List l = newNode(a, NULL);
    List last = l;
    for( i = a + 1 ; i <= b ; i++ ) {
        List q = newNode(i, NULL);
        last->next = q;
        last = q;
    }
    return l;
}
*/

int listLength(List l) {
    int count;
    for( count = 0 ; l != NULL ; l = l->next, count++ );
    return count;
}

bool listGet(List l, int idx, Data *res)
{
    int i;
    for( i = 0 ; i < idx && l != NULL ; i++, l = l->next );
    if( l == NULL )
        return false;
    else {
        *res = l->data;
        return true;
    }
}

List listPutAtHead(List l, Data val)
{
    return newNode(val, l);
}

List listPutAtEnd(List l, Data val)
{
    if( l == NULL )
        return newNode(val, NULL);
    else {
        List p;
        for( p = l ; p->next != NULL ; p = p->next ); // Stop at the last node
        p->next = newNode(val, NULL);  // Assign to the next of the last node
        return l;
    }
}

List listFilter(List l, BoolFun toKeep)
{  // TECNICA ESSENCIAL: Adicionar um no' auxiliar inicial para permitir tratamento uniforme.
      // Tente fazer sem o no' suplementar e veja como fica muito mais complicado.
    Node dummy;
    dummy.next = l;
    l = &dummy;
    while( l->next != NULL )
        if( toKeep(l->next->data) )
            l = l->next;
        else {
            List del = l->next;
            l->next = l->next->next;
            free(del);
        }
    return dummy.next;
}

void listPrint(List l)
{
    for( ; l != NULL ; l = l->next )
        printf("%lf\n", l->data);
}

static bool isEven(Data data) {
    return (int)data % 2 == 0;
}

static bool isOdd(Data data) {
    return (int)data % 2 != 0;
}

void listTest(void) {
    List l = listMakeRange(1.1, 7.8);
    printf("original rev2 ----------\n");
    l = listRev2(l);
    listPrint(l);
    printf("append ----------\n");
    l = listAppend(l, l);
    listPrint(l);
    printf("unique ----------\n");
    l = listUniq(l);
    listPrint(l);
    printf("filter even ----------\n");
    l = listFilter(l, isEven);
    listPrint(l);
    printf("filter odd ----------\n");
    l = listFilter(l, isOdd);
    listPrint(l);
    printf("----------\n");
}

List listClone(List l){
    List aux = NULL;
    List new = aux;
    for(; l != NULL; l = l->next){
        if(aux == NULL){
            aux = newNode(l->data, NULL);
            new = aux;
        } else {
            aux->next = newNode(l->data, NULL);
            aux = aux->next;
        }
    }
    return new;
}


List listAppend(List l1, List l2){
    List aux = l1;
    if(l1 == NULL)
        return listClone(l2);

    for(; aux->next != NULL; aux = aux->next);
    aux->next = listClone(l2);
    return l1;
}


List listRev(List l){
    List newL = NULL;

    for(; l != NULL; l = l->next)
        newL = listPutAtHead(newL, l->data);

    return newL;
}


List listRev2(List l){ //TODO
    List prev = NULL, next;
    
    for(; l != NULL; prev = l, l = next){
        next = l->next;
        l->next = prev;
    }

    return prev;
}


int belongs(List l, Data val){
    for(; l != NULL; l = l->next){
        if(l->data == val)
            return 1;
    }
    return 0;
}

/*
List listUniq(List l){
    List aux = newNode(l->data, NULL);
    List newL = aux;
    for(; l != NULL; l = l->next){
        if(!belongs(newL, l->data)){
            aux->next = newNode(l->data, NULL);
            aux = aux->next;
        }
    }
    return newL;
}*/

List listUniq(List l){
    List f = l;
    List next = NULL;
    for(; l != NULL; l = next){
        if(belongs(l->next, l->data)){
            if(l == f)
                f = l->next;
            List del = l;
            next = l->next;
            free(del);
        } else {
            next = l->next;
        }
    }
    return f;
}