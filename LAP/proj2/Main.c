/*
 ============================================================================
 Name        : Main.c
 Author      : Fernando Birra
 Version     :
 Copyright   : DI - FCT/UNL
 Description : LAP 2nd project 2015/2016, Ansi-style
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

#include "StaggeringList.h"

bool less45(RecordType t, RecordList l){
	if(getFloat(t, l, 0) < 45)
		return true;
	return false;
}

void test1()
{
	char *str;
	float f1, f2;

    /* Define a record type containing two fields, each with char type */
    RecordType t = newRecordType("f3f", 1);
    RecordType nT;
    
    /* Start with an empty record list */
    RecordList l = NULL, print;
    

	/* RECORD 1 */
    /* Create a single-record (unfilled) list */
    RecordList r = newRecord(t);
    
    /* Fill both character fields */
	setFloat(t, r, 0, 10.f);
	setString(t, r, 1, "0 string tamanho = 20");
	setFloat(t, r, 2, 30.f);
    
    str = malloc(sizeof(char) * 20);
    getString(t, r, 1, str);
    printf("record 1: \n");
	printf("%10f %30s %10f\n", getFloat(t, r, 0), str, getFloat(t, r, 2));

	l = insertRecord(t, l, r);


	/* RECORD 2 */
	/* Create a single-record (unfilled) list */
	r = newRecord(t);

	/* Fill both character fields */
	setFloat(t, r, 0, 40.f);
	setString(t, r, 1, "1 1234567890abcdefghi");
	setFloat(t, r, 2, 20.f);

	getString(t, r, 1, str);
	printf("record 2: \n");
	printf("%10f %30s %10f\n", getFloat(t, r, 0), str, getFloat(t, r, 2));

	l = insertRecord(t, l, r);


	/* RECORD 3 */
	/* Create a single-record (unfilled) list */
	r = newRecord(t);

	/* Fill both character fields */
	setFloat(t, r, 0, 50.f);
	setString(t, r, 1, "2 third field");
	setFloat(t, r, 2, 60.f);

	getString(t, r, 1, str);
	printf("record 2: \n");
	printf("%10f %30s %10f\n", getFloat(t, r, 0), str, getFloat(t, r, 2));

	l = insertRecord(t, l, r);


	print = l;
	
	printf("old records: \n");
	for(; print != NULL; print = print->next){
		printf("%p: ", (void *) print);
		getString(t, print, 1, str);
		printf("%10f %30s %10f\n", getFloat(t, print, 0), str, getFloat(t, print, 2));
	}
	
    /* Save the list into a file */
    saveRecordList("data.dat", t, l);
    
    destroyRecordList(l);

    destroyRecordType(t);

	l = loadRecordList("data.dat", &t);
	print = l;

	printf("new records: \n");
	for(; print != NULL; print = print->next){
		printf("%p : ", (void *)print);
		getString(t, print, 1, str);
		printf("%10f %30s %10f\n", getFloat(t, print, 0), str, getFloat(t, print, 2));
	}

	printf("count of 1st < 45: %ld\n", (long int)countIf(t, l, less45));


	/* test getRecord */
	r = getRecord(l, 2);
	getString(t, r, 1, str);
	printf("Third record: %10f %30s %10f\n", getFloat(t, r, 0), str, getFloat(t, r, 2));

	r = getRecord(l, 0);
	getString(t, r, 1, str);
	printf("First record: %10f %30s %10f\n", getFloat(t, r, 0), str, getFloat(t, r, 2));

	r = getRecord(l, 1);
	getString(t, r, 1, str);
	printf("Second record: %10f %30s %10f\n", getFloat(t, r, 0), str, getFloat(t, r, 2));


	/* Test getAll */
	str[0] = 0;
	getAll(t, r, &f1, str, &f2);
	printf("Second record before setAll (with get all): %10f %30s %10f\n", f1, str, f2);

	/* Test setAll */
	setAll(t, r, 4.2, "four twenty", 420.);
	getString(t, r, 1, str);
	printf("Second record after setAll: %10f %30s %10f\n", getFloat(t, r, 0), str, getFloat(t, r, 2));


	/* Test project */
	print = l;
	l = project(t, l, 1, &nT);
	destroyRecordList(print);
	print = l;

	printf("projected records: \n");
	for(; print != NULL; print = print->next){
		printf("%p : ", (void *)print);
		getString(nT, print, 0, str);
		printf("%30s\n", str);
	}

	printf("list length: %ld\n", recordListLength(l));

	/* Test removeRecord */
	l = removeRecord(l, 1);
	print = l;

	printf("removed 1: \n");
	for(; print != NULL; print = print->next){
		printf("%p : ", (void *)print);
		getString(nT, print, 0, str);
		printf("%30s\n", str);
	}

    /* Destroy the list */
    destroyRecordList(l);
    
    /* Destroy the record type */
    destroyRecordType(t);
    destroyRecordType(nT);
	
	free(str);
}

void test2(){
	RecordType t = newRecordType("ci9f", 2);
	RecordList l = NULL;
	RecordList r = newRecord(t);

	char c;
	int i;
	char *str = malloc(sizeof(char) * 90);
	float f;

	setAll(t, r, 'a', 1, "0 first", 1.1);
	l = insertRecord(t, l, r);

	r = newRecord(t);

	setAll(t, r, 'b', 2, "1 second", 2.1);
	l = insertRecord(t, l, r);

	r = newRecord(t);

	setAll(t, r, 'c', 3, "2 third", 3.1);
	l = insertRecord(t, l, r);

	r = newRecord(t);

	setAll(t, r, 'd', 4, "3 fourth", 4.1);
	l = insertRecord(t, l, r);

	r = getRecord(l, 0);
	c = getChar(t, r, 0);

	r = getRecord(l, 1);
	i = getInt(t, r, 1);

	r = getRecord(l, 2);
	getString(t, r, 2, str);

	r = getRecord(l, 3);
	f = getFloat(t, r, 3);

	printf("first char: %c\n", c);
	printf("second int: %d\n", i);
	printf("third string: %s\n", str);
	printf("fourth float: %f\n", f);

	destroyRecordType(t);

	destroyRecordList(l);
}

int main(void)
{
	printf("TEST 1\n");
    test1();
    printf("-----------------------------\n");
    printf("TEST 2\n");
    test2();
}

