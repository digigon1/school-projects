/*
 * StaggeringList.h
 *
 *  Created on: Apr 14, 2016
 *      Author: fpb
 */

#ifndef STAGGERINGLIST_H_
#define STAGGERINGLIST_H_

#include <stdbool.h>

/*
 Data type that represents a list of records. 
 
 Note the since the exact format of the record
 is not know at compile time, its storage needs to be managed in a dynamic way. The field "data"
 will hold a pointer to the concrete memory used by each record in the list.
 */
typedef struct RecordList {
    void *data;
    struct RecordList *next;
} *RecordList, RecordNode;

/*
 Data type that stores all the information needed (and useful) to manage
 dynamically specified records.
 
 The "format" field is a dymically allocated character string of arbitrary
 size, where each character represents the type of a field in a record
 (from left to right). The meaning (codification) of the type of each field
 is as follows:

    Record field specification format:
 
    c - a single char
    i - a signed integer
    f - a single precision floating point value
    1 - a 10 char array
    2 - a 20 char array
    3 - a 30 char array
    ...
    9 - a 90 char array
 
 The "keyPos" field is the index of the record field that is used to keep the
 record list sorted in ascending order.
 
 The "offsets" pointer will point to a dynamically allocated array of
 size_t elements storing the offsets to the start of each record field by
 following the "void *data" pointer.
 
 The "recordSize" field will store the effective footprint (number of bytes)
 used by all the fields of a record of this type.
 
 */

typedef struct {
    size_t nFields;
    char *format;
    size_t keyPos;
    size_t *offsets;
    size_t recordSize;
} RecordType ;

/* 
 The type of the predicates that will be used to filter lists of records
 The first argument of the predicate is the type of the record, while the
 second is the record to be evaluated.
 */
typedef bool (BoolFun)(RecordType, RecordList);


/*
 Loads a list of records from the specified file (filename) and returns it.
 The record type (description of the data fields) is returned by the second
 parameter (t).
 */
RecordList loadRecordList(char *filename, RecordType *t);

/*
 Saves a list of records (l) with the given type (t) into the specified
 file (filename).
 */
void saveRecordList(char *filename, RecordType t, RecordList l);

/*
 Builds and returns a record type that describes the fields contained in each
 record given:
  - its specification encoded in a string (format),
  - the index (key) of the field that will be used to keep the record list
    sorted.
 */
RecordType newRecordType(char *format, size_t keyPos);

/*
 Destroys any dynamically allocated memory kept by RecordType structures
 */
void destroyRecordType(RecordType t);

/*
 Allocates the memory for a new record and returns it, given:
  - the type of the record to be created (t).
 */
RecordList newRecord(RecordType t);

/*
 Destroys and frees all the memory allocated by a record list
 */
void destroyRecordList(RecordList l);

/*
 Returns the length of the record list (l).
 */
size_t recordListLength(RecordList l);

/*
 Inserts a record (r) of type (t) into the list (l) and returns the
 resulting list.
 Note: the list is kept sorted by the field whose index was given when the
 record type was created.
 */
RecordList insertRecord(RecordType t, RecordList l, RecordList r);

/*
Returns the record with a given index (idx) from the list (l). The first
element of the list has index 0. The returned record is the original record
and not a new copy.
*/
RecordList getRecord(RecordList l, int idx);

/*
 Removes a record with a given index (idx) from the list (l). The first
 element of the list has index 0.
 */
RecordList removeRecord(RecordList l, int idx);

/*
 Returns the value of the character field with the given index (fieldPos),
 from a given record (r) and record type (t).
 */
char getChar(RecordType t, RecordList r, size_t fieldPos);

/*
 Returns the value of the integer field with the given index (fieldPos),
 from a given record (r) and record type (t).
 */
int getInt(RecordType t, RecordList r, size_t fieldPos);

/*
 Returns the value of the floating point field with the given index (fieldPos),
 from a given record (r) and record type (t).
 */
float getFloat(RecordType t, RecordList r, size_t fieldPos);

/*
 Copies the value of the string field with the given index (fieldPos),
 from a given record (r) and record type (t) into the memory pointed to by str.
 */
void getString(RecordType t, RecordList r, size_t fieldPos, char *str);

/*
 Sets the value (val) of the character field with the given index (fieldPos),
 for a given record (r) and record type (t)
 */
void setChar(RecordType t, RecordList r, size_t fieldPos, char val);

/*
 Sets the value (val) of the integer field with the given index (fieldPos),
 for a given record (r) and record type (t)
 */
void setInt(RecordType t, RecordList r, size_t fieldPos, int val);

/*
 Sets the value (val) of the floating point field with the given index
 (fieldPos), for a given record (r) and record type (t)
 */
void setFloat(RecordType t, RecordList r, size_t fieldPos, float val);

/*
 Sets the value (val) of the string field with the given index (fieldPos),
 for a given record (r) and record type (t). Care must be taken not to
 overflow allocated field space.
 */
void setString(RecordType t, RecordList r, size_t fieldPos, char* val);

/*
 Counts the number of records in the list (l) that satisfy the predicate (p).
 The type (t) of the records is also provided.
 */
size_t countIf(RecordType type, RecordList l, BoolFun p);

/* 
 ----------------------------------------------------------------------------------------------------------
 The following functions are considered low priority. Leave their
 implementation to the end of the project, when all else has been
 implemented and tested.
 ----------------------------------------------------------------------------------------------------------
 */

/*
 Returns a new record list by projecting each record of the original list (l)
 into the single field with the given index (fieldPos). 
 The type of the records in the original list is tin and the type of the
 records in the output list  is returned in tout.
 */
RecordList project(RecordType tin, RecordList l, size_t fieldPos, RecordType *tout);

/*
 Gets the values for all the fields of the given record (r) with type (t).
 The remaining arguments are the pointers to the variables that will hold
 the field's values
 */
void getAll(RecordType t, RecordList r, ...);

/*
 Sets all the fields in the given record (r) with type (t).
 The remaining arguments are the values that will be stored in
 each record field.
 */
void setAll(RecordType type, RecordList r, ...);


#endif /* STAGGERINGLIST_H_ */
