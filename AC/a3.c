#include <stdio.h>
#include <stdlib.h>

extern int calc (int a, int b);

int main(int argc, char *argv[]) {
	int x, y, z;
	if ( argc!=3 ) {
		fprintf(stderr,"use: %s <int num>\n", argv[0] );
		return 1;
	}
	x = atoi(argv[1]);
	z = atoi(argv[2]);
	y = calc( x , z );
	printf("y=%d\n", y );
	return 0;
}

