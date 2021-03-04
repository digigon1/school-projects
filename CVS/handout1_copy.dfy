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

method existsInArray(b:array<int>, size:int, x:int) returns (r:bool)
    requires 0 <= size <= b.Length;

    ensures r <==> existsInArrayCheck(b, size, x)
    ensures !r <==> !existsInArrayCheck(b, size, x)
{
    return x in b[..size];
}

predicate sorted(a:array<int>, n:int)
    requires 0 <= n <= a.Length
    reads a
{
    forall i, j:: (0 <= i < j < n) ==> a[i] <= a[j]
}

method Deduplicate(a:array<int>, n:int) returns (b:array<int>, m:int)
    requires 0 <= n <= a.Length;
    requires sorted(a,n);

    ensures 0 <= m <= b.Length;
    ensures sorted(b,m);
    ensures unique(b,m);


    ensures n == b.Length;
    ensures fresh(b);

    // ensures forall i :: 0 <= i < n ==> existsInArrayCheck(b, m, a[i])
    
    // ensures forall i :: 0 <= i < n ==> existsInArrayCheck(a, n, b[i])

    ensures n == 0 <==> m == 0;
    ensures n == 1 ==> m == 1;
    ensures n > 1 ==> 0 < m <= n;
    // ensures unique(a, n) <==> m == n;
{
    b := new int[n];

    if (n == 0) {
        m := 0;
        return;
    }

    var bSize := 0;
    var pos := 0;

    while(pos < n)
        decreases n - pos
        invariant 0 <= bSize <= pos <= n

        // invariant sorted(a, pos)

        invariant pos > 0 ==> bSize > 0

        invariant bSize > 0 ==> b[bSize - 1] <= a[pos - 1]

        invariant forall i, j:: (0 <= i < j < bSize) ==> b[i] < b[j] // unique + sorted

        // invariant forall i :: (0 <= i < pos) ==> existsInArrayCheck(b, bSize, a[i])
        

        // invariant forall i :: (0 <= i < bSize) ==> existsInArrayCheck(a, pos, b[i])


        // invariant unique(a, pos) <==> bSize == pos;

    {
        var existance := false;
        if (pos != 0) {
            existance := existsInArray(b, bSize, a[pos]);
        }
        if (/*!existance &&*/ (bSize == 0 || a[pos] > b[bSize - 1])) {
            // assert forall i :: (0 <= i < pos) ==> existsInArrayCheck(b, bSize, a[i]);

            // assert forall i :: (0 <= i < bSize) ==> existsInArrayCheck(a, pos, b[i]);
            
            b[bSize] := a[pos];

            // temp(b, bSize, a, pos);
            
            bSize, pos := bSize + 1, pos + 1;

            // assert forall i :: (0 <= i < pos) ==> existsInArrayCheck(b, bSize, a[i]);

            // assert forall i :: (0 <= i < bSize) ==> existsInArrayCheck(a, pos, b[i]);
        } else {
            // assert forall i :: (0 <= i < pos) ==> existsInArrayCheck(b, bSize, a[i]);

            // assert forall i :: (0 <= i < bSize) ==> existsInArrayCheck(a, pos, b[i]);

            // temp(a, pos, b, bSize);

            pos := pos + 1;

            // assert forall i :: (0 <= i < pos) ==> existsInArrayCheck(b, bSize, a[i]);

            // assert forall i :: (0 <= i < bSize) ==> existsInArrayCheck(a, pos, b[i]);
        }
    }
    
    // assert unique(a, n) ==> bSize == n;

    // assert forall i :: 0 <= i < n ==> existsInArrayCheck(b, m, a[i]);
    
    // assert forall i :: 0 <= i < n ==> existsInArrayCheck(a, n, b[i]);

    m := bSize;
}

lemma temp(b:array<int>, bSize:int, a:array<int>, aSize:int) 
    requires 0 <= bSize <= b.Length
    requires 0 <= aSize <= a.Length

    ensures forall i :: (0 <= i < aSize) ==> bSize < b.Length && existsInArrayCheck(b, bSize, a[i]) ==> existsInArrayCheck(b, bSize + 1, a[i])
{

}

method Main() {
    var a:array<int> := new int[8];
    a[0] := 1;
    a[1] := 1;
    a[2] := 2;
    a[3] := 3;
    a[4] := 4;
    a[5] := 5;
    a[6] := 5;

    var d, f;
    d, f := Deduplicate(a, 7);
    print(d);
    print(f);
}