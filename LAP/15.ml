let rec belongs elem w =
	match w with
	| [] -> false
	| head::tail -> head=elem || belongs elem tail
;;

let rec diff w1 w2 =
	match w1 with
	| [] -> []
	| head::tail -> if belongs head w2 then diff tail w2 else head::diff tail w2
;;

let union list1 list2 =
	(diff list1 list2)@list2
;;

let rec inter w1 w2 =
	match w1 with
	| [] -> []
	| head::tail -> if belongs head w2 then head::inter tail w2 else inter tail w2 
;;

let rec map f w =
	match w with
	| [] -> []
	| h::t -> (f h)::(map f t)
;;

let rec power w =
	match w with
	| [] -> [[]]
	| head::tail -> union (power tail) (map (fun l -> head::l) (power tail))
;;
