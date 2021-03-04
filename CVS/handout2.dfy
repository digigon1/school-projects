class Row {
    var name:string;
    var age:int;

    var id:int;

    predicate RepInv()
        reads this
    {
        age >= 0
    }

    function equals(other:Row?):bool
        reads this, other
    {
        other != null && (name == other.name) && (age == other.age) && id == other.id
    }
}

class Person {
    var name:string;
    var age:int;

    var id:int;
    var store:DB?;

    constructor(n:string, a:int) 
        requires a >= 0
        ensures Transient()
    {
        name := n;
        age := a;
        store := null;
        id := -1;
    }

    predicate RepInv()
        reads this
    {
        age >= 0 && (id == -1 || (0 <= id < 10))
    }

    predicate NoId()
        reads this
    {
        id < 0
    }

    predicate NoStore()
        reads this
    {
        store == null
    }

    predicate Transient() 
        reads this
    {
        RepInv() && NoId() && NoStore()
    }

    predicate Persistent()
        reads this
    {
        RepInv() && !NoId() && !NoStore()
    }

    predicate Detached()
        reads this
    {
        RepInv() && NoId() && !NoStore()
    }

    method save(s:DB)
        requires Transient()
        requires s.RepInv()

        ensures id != -1 ==> Persistent()
        modifies this`store, this`id, s`size, s`filled, s.a
    {
        assert s.RepInv();
        id := s.add(this);
        if id != -1 {
            store := s;
        }
    }

    method delete()
        requires Persistent()
        requires store.RepInv()

        ensures id == -1 ==> Transient()

        modifies this`store, this`id, store.a, store
    {
        if 0 <= id < store.size {
            store.delete(id);
            
            id := -1;
            store := null;
            assert Transient();
        }
    }

    method close()
        requires Persistent()

        ensures Detached()

        modifies this`id
    {
        id := -1;
    }

    method update(s:DB) returns (r:bool)
        requires Detached()
        requires s.RepInv()

        ensures r ==> Persistent()

        modifies s, s`a
        modifies this`store, this`id, s.a
    {
        var updated := s.update(this);
        if updated != -1 {
            store := s;
        }
        id := updated;
        return id != -1;
        return false;
    }
}


class DB {
    var a:array<Row?>;
    ghost var filled:set<Row?>
    var size:int;

    constructor() 
        ensures fresh(a)
        ensures RepInv()
    {
        a := new Row?[10];
        filled := { null };
        size := 0;
    }

    predicate RepInv()
        reads this, a, filled
    {
        null in filled &&
        a.Length == 10 && 
        0 <= size <= a.Length &&
        (forall i :: 0 <= i < size ==> a[i] in filled) &&
        (forall i :: 0 <= i < size ==> a[i] == null || a[i].id == i) &&
        (forall i, j :: 0 <= i < j < size ==> a[i] == null || a[j] == null || a[i].id != a[j].id) &&
        (forall r :: r in filled ==> (r == null || r.RepInv()))
    }


    method add(obj:Person) returns (r:int)
        requires RepInv()
        requires obj.RepInv()

        ensures RepInv()
        ensures 0 <= r < a.Length ==> a[r] != null
        ensures size > 0 && r == size - 1 ==> a[r].name == obj.name && a[r].age == obj.age && a[r].id == r
        ensures -1 <= r < a.Length

        modifies a, this`size, this`filled
    {
        if size < a.Length {
            var nr := new Row;
            nr.name := obj.name;
            nr.age := obj.age;
            nr.id := size;
            a[size] := nr;
            size := size + 1;
            filled := filled + { nr };
            return size - 1;
        } else {
            return -1;
        }
    }

    method update(obj:Person) returns (r:int)
        requires RepInv()
        requires obj.RepInv()

        ensures RepInv()
        ensures -1 <= r < 10

        modifies a, this
    {
        var id := findByNameAge(obj.name, obj.age);
        if id >= 0 {
            var p := a[id];
            assert p.id == id;

            filled := filled - { p };
            a[id] := null;

            var nr := new Row;
            nr.name := obj.name;
            nr.age := obj.age;
            nr.id := id;
            
            a[id] := nr;
            
            filled := filled + { nr };

            return id;
        } else {
            var ret := add(obj);
            return ret;
        }
    }

    method delete(i:int)
        requires RepInv()
        requires 0 <= i < size

        ensures RepInv()

        modifies a, this
    {
        if (a[i] != null) {
            filled := filled - { a[i] };
        }
        a[i] := null;
    }

    method findByNameAge(name:string, age:int) returns (r:int)
        requires RepInv()
        ensures RepInv()
        ensures -1 <= r < size
        ensures r >= 0 ==> a[r] != null && a[r].name == name && a[r].age == age && a[r].id == r
    {
        var i := 0;
        while i < size
            decreases size - i
        {
            var o := a[i];
            if o != null && o.name == name && o.age == age {
                return o.id;
            }
            i := i + 1;
        }

        return -1;
    }

    method find(id:int) returns (r:Person?)
        requires RepInv()
        requires 0 <= id < size

        ensures RepInv()
    {
        var p := a[id];
        if p != null {
            assert p.RepInv();
            var resp := new Person(p.name, p.age);
            resp.store := this;
            resp.id := id;
            return resp;
        } else {
            return null;
        }
    }
}