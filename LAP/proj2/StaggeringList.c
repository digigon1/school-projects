/*
 * StaggeringList.c
 *
 *  Created on: Apr 14, 2016
 *      Author 1: Goncalo Almeida (45353)
 *      Author 2: Jose Martins (45678)
 */


#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <stdarg.h>
#include "StaggeringList.h"
 
static void error(char *msg)
{
    fprintf(stderr, "An error occurred: %s\n", msg);
    fprintf(stderr, "Exiting.");
    exit(1);
}

static bool isBigEndian(void) {
    int32_t num = 1;
    return *(int8_t *)&num != 1;
}

static void invertStorage(uint8_t *buf, int len)
{
    int i, j, m=len/2;
    
    for(i=0, j = len-1; i<=m; i++, j--) {
        uint8_t temp = buf[i]; buf[i] = buf[j]; buf[j] = temp;
    }
}

static void writeFloat(float v, FILE *fp)
{
    if( !isBigEndian() )
        invertStorage((uint8_t *) &v, sizeof(float));
    fwrite(&v, sizeof(float), 1, fp);
}

static void writeInt(int v, FILE *fp)
{
    if( !isBigEndian() )
        invertStorage((uint8_t *) &v, sizeof(int));
    fwrite(&v, sizeof(int), 1, fp);
}

static void writeSizeT(size_t v, FILE *fp)
{
    if( !isBigEndian() )
        invertStorage((uint8_t *) &v, sizeof(size_t));
    fwrite(&v, sizeof(size_t), 1, fp);
}

static float readFloat(FILE *fp)
{
    float v;
    
    fread(&v, sizeof(float), 1, fp);
    if(!isBigEndian())
        invertStorage((uint8_t *) &v, sizeof(float));
    
    return v;
}

static int readInt(FILE *fp)
{
    int v;
    
    fread(&v, sizeof(int), 1, fp);
    if(!isBigEndian())
        invertStorage((uint8_t *) &v, sizeof(int));
    
    return v;
}

static size_t readSizeT(FILE *fp)
{
    size_t v;
    
    fread(&v, sizeof(size_t), 1, fp);
    if(!isBigEndian())
        invertStorage((uint8_t *) &v, sizeof(size_t));
    
    return v;
}

RecordList loadRecordList(char *filename, RecordType *t)
{
	FILE *f = fopen(filename, "r");
	
	size_t size = readSizeT(f);
	int i;

	RecordList l = NULL;
	RecordList r;

	size_t read;


	t->nFields = size;

	t->format = (char *) malloc(sizeof(char) * (size+1));
	
	fread(t->format, sizeof(char), size, f);
	t->format[size] = 0;

	t->keyPos = readSizeT(f);

	
	t->offsets = (size_t *)malloc(sizeof(size_t) * size);
	
	for (i = 0; i < size; ++i) 
		t->offsets[i] = readSizeT(f);

	t->recordSize = readSizeT(f);

	while(!feof(f)){
		r = newRecord(*t);
		read = fread(r->data, t->recordSize, 1, f);
		if (read == 1) {
			l = insertRecord(*t, l, r);
		}
	}

	destroyRecordList(r);
	
	fclose(f);

	return l;
}


void writeNext(RecordType t, RecordList l, char type, FILE *f, int pos){
	switch(type){
		case 'c': fwrite(t.offsets[pos] + (char *)l->data, sizeof(char), 1, f); break;
		case 'i': writeInt(*(t.offsets[pos] + (int *)l->data), f); break;
		case 'f': writeFloat(*(t.offsets[pos] + (float *)l->data), f); break;
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9': fwrite(t.offsets[pos] + (char *)l->data, sizeof(char), 10 * (type - '0'), f); break;
		default: error("Invalid char used in format.");
	}
}

void saveRecordList(char *filename, RecordType t, RecordList l)
{
	FILE *f = fopen(filename, "w");
	size_t size = t.nFields;

	int i;


	writeSizeT(size, f);

	fwrite(t.format, sizeof(char), size, f);

	writeSizeT(t.keyPos, f);
	
	
	for (i = 0; i < size; ++i) {
		writeSizeT(t.offsets[i], f);
	}

	writeSizeT(t.recordSize, f);
	
	for(; l != NULL; l = l->next){
		fwrite(l->data, t.recordSize, 1, f);
	}
	

	fclose(f);
}

size_t charsize(char c){
	switch(c){
		case 'c': return sizeof(c);
		case 'i': return sizeof(int);
		case 'f': return sizeof(float);
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9': return 10*(sizeof(char) * (c - '0'));
		default: error("Invalid char used in format.");
	}
}

RecordType newRecordType(char *format, size_t keyPos)
{
	size_t nFields;

	RecordType t;

	int i;

	size_t size = 0;

	if (format == NULL)
		error("Format can't be null.");
    
    nFields = strlen(format);

	if (nFields <= keyPos)
		error("Key Position is larger than number of fields.");

    t.format = (char *) malloc(1 + sizeof(char) * nFields);
    strcpy(t.format, format);

	for (i = 0; format != NULL && format[i] != '\0'; ++i) {
		size += charsize(format[i]);
	}

    t.keyPos = keyPos;
    
    if(format != NULL && format[0] != 0){
    	size_t *offsets = (size_t *) malloc(sizeof(size_t)*nFields);
		t.nFields = nFields;
    	offsets[0] = 0;
    	t.recordSize = 0;
    	for (i = 1; format[i] != '\0'; ++i){
    		size_t size = charsize(format[i-1]);
    		offsets[i] = offsets[i-1]+size;
    		t.recordSize += size;
    	}
		t.recordSize += charsize(format[i-1]);
    	t.offsets = offsets;
	}

    return t;
}


void destroyRecordType(RecordType t)
{
	free(t.offsets);
	free(t.format);
}

RecordList newRecord(RecordType t)
{
	RecordList r;
	void *dataPtr;
	
	r = ((RecordList)malloc(sizeof(/*struct RecordList*/RecordNode)));

	dataPtr = calloc(1, t.recordSize);
	r->data = dataPtr;

	r->next = NULL;

	return r;
}


void destroyRecordList(RecordList l)
{
	RecordList nL;
	for (; l != NULL; l = nL) {
		nL = l->next;
		free(l->data);
		free(l);
	}
}


size_t recordListLength(RecordList l)
{
	size_t size = 0;
	
	for (; l != NULL; l = l->next, ++size);

	return size;
}

int cmpChar(RecordList r1, RecordList r2, size_t offset) {
	char v1 = *(((char *)r1->data) + offset);
	char v2 = *(((char *)r2->data) + offset);

	if (v1 > v2)
		return 1;
	else if (v1 == v2)
		return 0;
	else
		return -1;
}

int cmpInt(RecordList r1, RecordList r2, size_t offset) {
	int v1 = *(int *)(((char *)r1->data) + offset);
	int v2 = *(int *)(((char *)r2->data) + offset);

	if (v1 > v2)
		return 1;
	else if (v1 == v2)
		return 0;
	else
		return -1;
}

int cmpFloat(RecordList r1, RecordList r2, size_t offset) {
	float v1 = *(float *)(((char *)r1->data) + offset);
	float v2 = *(float *)(((char *)r2->data) + offset);

	if (v1 > v2)
		return 1;
	else if (v1 == v2)
		return 0;
	else
		return -1;
}

int cmpStr(RecordList r1, RecordList r2, size_t offset) {
	char *v1 = (((char *)r1->data) + offset);
	char *v2 = (((char *)r2->data) + offset);

	return strcmp(v1, v2);
}

int cmpRec(RecordList r1, RecordList r2, size_t offset, char type) {
	switch (type) {
	case 'c': return cmpChar(r1, r2, offset);
	case 'i': return cmpInt(r1, r2, offset);
	case 'f': return cmpFloat(r1, r2, offset);
	case '1':
	case '2':
	case '3':
	case '4':
	case '5':
	case '6':
	case '7':
	case '8':
	case '9': return cmpStr(r1, r2, offset);
	default: error("Error in format.");
	}
}

RecordList insertRecord(RecordType t, RecordList l, RecordList r)
{
	size_t offset = t.offsets[t.keyPos];
	char type = t.format[t.keyPos];
	RecordList prev = NULL, start = l;


	if (l == NULL) {
		return r;
	}

	while (l != NULL && cmpRec(l, r, offset, type) < 0) {
		prev = l;
		l = l->next;
	}

	r->next = l;
	if (prev != NULL)
		prev->next = r;
	else
		return r;

    return start; 
}

RecordList getRecord(RecordList l, int idx) 
{
	int i;
	for (i = 0; i < idx && l != NULL; l = l->next, ++i);

	if (i > idx)
		error("Index was larger than list length.");

	return l;
}

RecordList removeRecord(RecordList l, int idx)
{
	int i;
	RecordList bef = l, start = l;
	l = l->next;
	if (idx == 0)
		return l;

	for (i = 0; l != NULL && i < idx - 1; ++i, bef = l, l = l->next);

	if (l == NULL)
		error("Index was larger than list length.");

	bef->next = l->next;

	free(l->data);
	free(l);

    return start; 
}


char getChar(RecordType t, RecordList r, size_t fieldPos)
{
	void *data, *pos;
	char *charP;

	if (fieldPos > t.nFields)
		error("Field position was larger than number of fields.");

	data = r->data;

	pos = (char *)data + t.offsets[fieldPos];
	
	charP = pos;

	return *charP;
} 


int getInt(RecordType t, RecordList r, size_t fieldPos)
{
	void *data, *pos;
	int *intP;

	if (fieldPos > t.nFields)
		error("Field position was larger than number of fields.");

	data = r->data;

	pos = (char *)data + t.offsets[fieldPos];

	intP = pos;

	return *intP;
} 


float getFloat(RecordType t, RecordList r, size_t fieldPos)
{
	void *data, *pos;
	float *floatP;

	if (fieldPos > t.nFields)
		error("Field position was larger than number of fields.");

	data = r->data;

	pos = (char *)data + t.offsets[fieldPos];

	floatP = pos;

	return *floatP;
} 


void getString(RecordType t, RecordList r, size_t fieldPos, char *str)
{
	void *data, *pos;
	char *strP;

	if (fieldPos > t.nFields)
		error("Field position was larger than number of fields.");

	data = r->data;

	pos = (char *)data + t.offsets[fieldPos];

	strP = pos;

	strcpy(str, strP);
} 


void setChar(RecordType t, RecordList r, size_t fieldPos, char val)
{
	void *data, *pos;
	char *charP;

	if (fieldPos > t.nFields)
		error("Field position was larger than number of fields.");

	data = r->data;

	pos = (char *)data + t.offsets[fieldPos];

	charP = pos;

	*charP = val;
} 


void setInt(RecordType t, RecordList r, size_t fieldPos, int val)
{
	void *data, *pos;
	int *intP;

	if (fieldPos > t.nFields)
		error("Field position was larger than number of fields.");

	data = r->data;

	pos = (char *)data + t.offsets[fieldPos];

	intP = pos;

	*intP = val;

} 


void setFloat(RecordType t, RecordList r, size_t fieldPos, float val)
{
	void *data, *pos;
	float *floatP;

	if (fieldPos > t.nFields)
		error("Field position was larger than number of fields.");

	data = r->data;

	pos = (char *)data + t.offsets[fieldPos];

	floatP = pos;

	*floatP = val;
} 


void setString(RecordType t, RecordList r, size_t fieldPos, char* val)
{
	void *data, *pos;
	char *strP;

	if (fieldPos > t.nFields)
		error("Field position was larger than number of fields.");
	
	data = r->data;

	pos = (char *)data + t.offsets[fieldPos];

	strP = pos;

	strcpy(strP, val);
} 


size_t countIf(RecordType type, RecordList l, BoolFun p)
{
	size_t total;
	for(total = 0; l != NULL; l = l->next)
		if(p(type, l))
			total++;
	
    return total;
} 


/*
 -------------------------------------------------------------------------------
 The following functions are considered low priority. Leave their
 implementation to the end of the project, when all else has been
 implemented and tested.
 -------------------------------------------------------------------------------
 */

void getAndSet(RecordType tin, RecordList rin, char type, int pos, RecordType tout, RecordList rout){
	int i;
	float f;
	char c;
	char *str;
	switch (type) {
	case 'c': c = getChar(tin, rin, pos); setChar(tout, rout, 1, c); break;
	case 'i': i = getInt(tin, rin, pos); setInt(tout, rout, 1, i); break;
	case 'f': f = getFloat(tin, rin, pos); setFloat(tout, rout, 1, f); break;
	case '1':
	case '2':
	case '3':
	case '4':
	case '5':
	case '6':
	case '7':
	case '8':
	case '9': 	str = malloc(sizeof(char) * 10 * (type - '0')); 
				getString(tin, rin, pos, str); 
				setString(tout, rout, 0, str); 
				free(str);
				break;

	default: error("Error in format.");
	}
}

RecordList project(RecordType tin, RecordList l, size_t fieldPos, RecordType *tout) {
	char nFormat[2];
	char type = tin.format[fieldPos];
	RecordList nList = NULL, tempRec;
	RecordType nt;

	nFormat[0] = type;
	nFormat[1] = 0;

	nt = newRecordType(nFormat, 0);


	for(; l != NULL; l = l->next){
		tempRec = newRecord(nt);
		getAndSet(tin, l, type, fieldPos, nt, tempRec);
		nList = insertRecord(nt, nList, tempRec);
	}

	*tout = nt;
	return nList;
}

void getArg(RecordType type, RecordList r, int index, va_list* ap){
	int *i;
	float *f;
	char *c;
	char *str;
	switch (type.format[index]) {
	case 'c': c = va_arg(*ap, char *); *c = getChar(type, r, index); break;
	case 'i': i = va_arg(*ap, int *); *i = getInt(type, r, index); break;
	case 'f': f = va_arg(*ap, float *); *f = getFloat(type, r, index); break;
	case '1':
	case '2':
	case '3':
	case '4':
	case '5':
	case '6':
	case '7':
	case '8':
	case '9': str = va_arg(*ap, char *); getString(type, r, index, str); break;
	default: error("Error in format.");
	}
}

void getAll(RecordType type, RecordList r, ...){
	va_list ap;
	int i;
	
	va_start(ap, r);
	
	for (i = 0; i < type.nFields; ++i) 
		getArg(type, r, i, &ap);
	

	va_end(ap);
}

void setArg(RecordType type, RecordList r, int index, va_list *ap) {
	int i;
	float f;
	char c;
	char *str;
	switch (type.format[index]) {
	case 'c': c = (char) va_arg(*ap, int); setChar(type, r, index, c); break;
	case 'i': i = va_arg(*ap, int); setInt(type, r, index, i); break;
	case 'f': f = (float) va_arg(*ap, double); setFloat(type, r, index, f); break;
	case '1':
	case '2':
	case '3':
	case '4':
	case '5':
	case '6':
	case '7':
	case '8':
	case '9': str = va_arg(ap, char *); setString(type, r, index, str); break;
	default: error("Error in format.");
	}
}

void setAll(RecordType type, RecordList r, ...) {
	va_list ap;
	int i;
	
	va_start(ap, r);
	
	for (i = 0; i < type.nFields; ++i) 
		setArg(type, r, i, &ap);
	

	va_end(ap);
}





