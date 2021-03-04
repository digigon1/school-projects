/**
* Goncalo Almeida - 45353
*/

predicate unique(b:array<int>, h:int)
    reads b;
    requires 0 <= h <= b.Length;
{ 
    forall k, j::(0 <= k < j < h) ==> b[k] != b[j]
}

predicate existsInArrayCheck(a:array<int>, size:int, x:int)
    requires 0 <= size <= a.Length
    reads a
{
    x in a[..size]
}

predicate sorted(a:array<int>, n:int)
    requires 0 <= n <= a.Length
    reads a
{
    forall i, j:: (0 <= i < j < n) ==> a[i] <= a[j]
}

method Deduplicate(a:array<int>, n:int) returns (b:array<int>, m:int)
    requires 0 <= n <= a.Length;
    requires sorted(a, n);

    ensures 0 <= m <= b.Length;
    ensures sorted(b, m);
    ensures unique(b, m);


    ensures fresh(b); // b is a new array
    ensures n == b.Length; // of size n

    ensures forall i :: 0 <= i < n ==> existsInArrayCheck(b, m, a[i]) // all in a are in b
    
    ensures forall i :: 0 <= i < m ==> existsInArrayCheck(a, n, b[i]) // all in b are in a

    ensures n == 0 <==> m == 0; // no items in a means no items in b
    ensures n == 1 ==> m == 1; // one item in a means one item in b
    ensures n > 1 ==> 0 < m <= n;
    ensures unique(a, n) <==> n == m; // if a has no duplicates, both arrays are the same size
    
    ensures n == m ==> forall i :: (0 <= i < n) ==> (a[i] == b[i]); // if both arrays have the same size, all the items in them are the same
{
    b := new int[n];

    var bSize := 0;
    var pos := 0;

    while(pos < n)
        decreases n - pos
        invariant 0 <= bSize <= pos <= n

        invariant forall i, j:: (0 <= i < j < bSize) ==> b[i] < b[j] // unique + sorted

        invariant forall i :: (0 <= i < pos) ==> existsInArrayCheck(b, bSize, a[i])
        
        invariant forall i :: (0 <= i < bSize) ==> existsInArrayCheck(a, pos, b[i])

        invariant unique(a, pos) <==> bSize == pos;

        invariant pos == bSize ==> forall i :: (0 <= i < pos) ==> (a[i] == b[i]);
    {
        if (bSize == 0 || a[pos] > b[bSize - 1]) {
            b[bSize] := a[pos];

            assert forall i :: (0 <= i < pos) ==> existsInArrayCheck(b, bSize, a[i]);
            
            bSize := bSize + 1;
        }
        pos := pos + 1;
    }

    m := bSize;
}
