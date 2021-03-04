#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>
#include <time.h>

#define N_ELEMS     30

void fill(int vect[], int n) {
    srand(time(NULL));
    for(int i = 0; i < n; i++)
        vect[i] = rand(); 
}

int compare(const void *p1, const void *p2){
    int a = *((int *) p1);
    int b = *((int *) p2);
    if(a == b)
        return 0;
    else if(a > b)
        return 1;
    else
        return -1;
}

void sort(int vect[], int n) {
    qsort(vect, n, sizeof(int), compare);
}

void show(int vect[], int n) {
    int i;
    printf("----------------\n");
    for( i = 0 ; i < n ; i++ )
        printf("%12d\n", vect[i]);
    printf("----------------\n");
}

int main() {
    int vect[N_ELEMS];
    fill(vect, N_ELEMS);
    show(vect, N_ELEMS);
    sort(vect, N_ELEMS);
    show(vect, N_ELEMS);
    return 0 ;
}